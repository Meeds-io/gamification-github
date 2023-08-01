/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.gamification.github.services.impl;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.services.WebhookService;
import org.exoplatform.gamification.github.storage.WebHookStorage;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class WebhookServiceImpl implements WebhookService {

  private static final Log      LOG            = ExoLogger.getLogger(WebhookServiceImpl.class);

  private static final String   GITHUB_API_URL = "https://api.github.com/orgs/";

  private static final String[] GITHUB_EVENTS  = { "push", "pull_request", "pull_request_review", "pull_request_review_comment",
      "pull_request_review_comment" };

  private final ListenerService listenerService;

  private final WebHookStorage  webHookStorage;

  public WebhookServiceImpl(ListenerService listenerService, WebHookStorage webHookStorage) {
    this.listenerService = listenerService;
    this.webHookStorage = webHookStorage;
  }

  public List<WebHook> getWebhooks(String currentUser, int offset, int limit) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access gitHub Hooks");
    }
    List<Long> hooksIds = webHookStorage.getWebhookIds(offset, limit);
    return hooksIds.stream().map(webHookStorage::getWebHookById).toList();
  }

  public int countWebhooks(String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access gitHub Hooks");
    }
    return webHookStorage.countWebhooks();
  }

  public void createWebhook(String organizationName,
                            String secret,
                            String accessToken,
                            String currentUser) throws ObjectAlreadyExistsException, IllegalAccessException {

    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to create GitHub hook");
    }
    RemoteOrganization remoteOrganization;
    try {
      remoteOrganization = retrieveRemoteOrganization(organizationName, accessToken);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization info.", e);
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(remoteOrganization.getId());
    if (webHook != null) {
      throw new ObjectAlreadyExistsException(webHook);
    }
    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + organizationName + "/hooks";
    config.put("url",
               "https://fac5-2a01-cb05-890f-e600-20b2-cbf1-33ab-2540.ngrok-free.app"
                   + "/portal/rest/gamification/connectors/github/webhooks");
    config.put("content_type", "json");
    config.put("insecure_ssl", "0");
    config.put("secret", secret);
    hook.put("name", "web");
    hook.put("active", true);
    hook.put("config", config);
    hook.put("events", GITHUB_EVENTS);
    try {
      URL obj = new URL(url);
      HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
      postConnection.setRequestMethod("POST");
      setAuthorizationHeader(postConnection, accessToken);
      postConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      postConnection.setDoOutput(true);
      createGitHubWebhook(hook, postConnection);
      int responseCode = postConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_CREATED) { // success
        saveWebhook(remoteOrganization, secret, accessToken, currentUser, postConnection);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to open GitHub connection", e);
    }
  }

  private void saveWebhook(RemoteOrganization remoteOrganization,
                           String secret,
                           String token,
                           String currentUser,
                           HttpURLConnection postConnection) throws ObjectAlreadyExistsException, IllegalAccessException {
    try (InputStream in = postConnection.getInputStream()) {
      String response = IOUtil.getStreamContentAsString(in);
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode infoNode = objectMapper.readTree(response);
      long hookId = infoNode.get("id").longValue();
      if (!Utils.isRewardingManager(currentUser)) {
        throw new IllegalAccessException("The user is not authorized to create GitHub hook");
      }
      WebHook webHook = new WebHook();
      webHook.setWebhookId(hookId);
      webHook.setOrganizationId(remoteOrganization.getId());
      webHook.setWatchedBy(currentUser);
      secret = encode(secret);
      token = encode(token);
      webHookStorage.saveWebHook(webHook, secret, token);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create GitHub hook", e);
    }
  }

  private void createGitHubWebhook(JSONObject hook, HttpURLConnection postConnection) {
    try (OutputStream os = postConnection.getOutputStream()) {
      os.write(hook.toString().getBytes(StandardCharsets.UTF_8));
      os.flush();
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create github hook", e);
    }
  }

  public void deleteConnectorHook(long organizationId, String currentUser) throws IllegalAccessException,
                                                                           ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete GitHub hook");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationId);
    if (webHook == null) {
      throw new ObjectNotFoundException("Github hook for organization id : " + organizationId + " wasn't found");
    }
    String url = GITHUB_API_URL + organizationId + "/hooks/" + webHook.getWebhookId();
    URL obj;
    int responseCode;
    try {
      obj = new URL(url);
      HttpURLConnection deleteConnection = (HttpURLConnection) obj.openConnection();
      deleteConnection.setRequestMethod("DELETE");
      setAuthorizationHeader(deleteConnection, getHookAccessToken(organizationId));
      responseCode = deleteConnection.getResponseCode();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) { // success
      deleteWebhook(organizationId);
    } else {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
  }

  @Override
  public String getHookSecret(long organizationRemoteId) {
    return decode(webHookStorage.getWebHookHookSecret(organizationRemoteId));
  }

  @Override
  public String getHookAccessToken(long organizationRemoteId) {
    return decode(webHookStorage.getWebHookAccessToken(organizationRemoteId));
  }

  public void createGamificationHistory(String ruleTitle, String senderId, String receiverId, String object) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", ruleTitle);
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("object", object);
      listenerService.broadcast("exo.gamification.generic.action", gam, "");
      LOG.info("Github action {} gamified for user {} {} {}",
               ruleTitle,
               senderId,
               (ruleTitle.equals("pullRequestValidated")) ? "from" : "to",
               receiverId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast gamification event", e);
    }
  }

  public void broadcastGithubEvent(String ruleTitle, String senderId, String receiverId, String object) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", ruleTitle);
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("object", object);
      listenerService.broadcast("exo.github.event", gam, "");
      LOG.info("Github action {} brodcasted for user {}", ruleTitle, senderId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast github event", e);
    }
  }

  private void deleteWebhook(long organizationId) {
    webHookStorage.deleteWebHook(organizationId);
  }

  private void setAuthorizationHeader(HttpURLConnection connection, String accessToken) {
    connection.setRequestProperty("Authorization", "token " + accessToken);
  }

  private RemoteOrganization retrieveRemoteOrganization(String organizationName, String accessToken) throws IOException {
    URL obj = new URL(GITHUB_API_URL + organizationName);
    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
    connection.setRequestMethod("GET");
    setAuthorizationHeader(connection, accessToken);
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (InputStream in = connection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        RemoteOrganization gitHubOrganization = new RemoteOrganization();
        gitHubOrganization.setId(infoNode.get("id").longValue());
        gitHubOrganization.setName(organizationName);
        gitHubOrganization.setTitle(infoNode.get("name").textValue());
        gitHubOrganization.setDescription(infoNode.get("description").textValue());
        gitHubOrganization.setAvatarUrl(infoNode.get("avatar_url").textValue());
        return gitHubOrganization;
      } catch (IOException e) {
        throw new IllegalStateException("Unable to retrieve GitHub organization: " + organizationName);
      }
    } else {
      throw new IllegalStateException("Unable to retrieve GitHub organization: " + organizationName);
    }
  }

  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) throws IOException {
    URL obj = new URL(GITHUB_API_URL + organizationId);
    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
    connection.setRequestMethod("GET");
    setAuthorizationHeader(connection, accessToken);
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (InputStream in = connection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        RemoteOrganization gitHubOrganization = new RemoteOrganization();
        gitHubOrganization.setId(infoNode.get("id").longValue());
        gitHubOrganization.setName(infoNode.get("login").textValue());
        gitHubOrganization.setTitle(infoNode.get("name").textValue());
        gitHubOrganization.setDescription(infoNode.get("description").textValue());
        gitHubOrganization.setAvatarUrl(infoNode.get("avatar_url").textValue());
        return gitHubOrganization;
      } catch (IOException e) {
        throw new IllegalStateException("Unable to retrieve GitHub organization with id: " + organizationId);
      }
    } else {
      throw new IllegalStateException("Unable to retrieve GitHub organization with id: " + organizationId);
    }
  }

  private static String encode(String token) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().encode(token);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when encoding token", e);
      return null;
    }
  }

  private static String decode(String encryptedToken) {
    try {
      CodecInitializer codecInitializer = CommonsUtils.getService(CodecInitializer.class);
      return codecInitializer.getCodec().decode(encryptedToken);
    } catch (TokenServiceInitializationException e) {
      LOG.warn("Error when decoding token", e);
      return null;
    }
  }
}
