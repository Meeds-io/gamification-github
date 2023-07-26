/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.gamification.github.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import io.meeds.gamification.model.ConnectorHook;
import io.meeds.gamification.service.ConnectorHookService;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GithubHooksManagement {

  private static final Log              LOG            = ExoLogger.getLogger(GithubHooksManagement.class);

  private static final String           GITHUB_API_URL = "https://api.github.com/orgs/";

  private static final String[]         GITHUB_EVENTS  = { "push", "pull_request", "pull_request_review",
      "pull_request_review_comment", "pull_request_review_comment" };

  public static final String            CONNECTOR_NAME = "github";

  private String                        accessToken;

  private final ListenerService         listenerService;

  private final ConnectorHookService    connectorHookService;

  private final ConnectorSettingService connectorSettingService;

  private final IdentityManager         identityManager;

  private final FileService             fileService;

  public GithubHooksManagement(ListenerService listenerService,
                               ConnectorHookService connectorHookService,
                               ConnectorSettingService connectorSettingService,
                               IdentityManager identityManager,
                               FileService fileService) {
    this.listenerService = listenerService;
    this.connectorHookService = connectorHookService;
    this.connectorSettingService = connectorSettingService;
    this.identityManager = identityManager;
    this.fileService = fileService;
  }

  public void addHook(String organizationName, String secret, String currentUser) throws ObjectAlreadyExistsException,
                                                                                  IllegalAccessException {

    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to create GitHub hook");
    }
    if (StringUtils.isBlank(getAccessToken())) {
      throw new IllegalArgumentException("Access token is required to create GitHub hook");
    }
    ConnectorHook connectorHook = connectorHookService.getConnectorHook(CONNECTOR_NAME, organizationName);
    if (connectorHook != null) {
      throw new ObjectAlreadyExistsException(connectorHook);
    }
    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + organizationName + "/hooks";
    config.put("url", CommonsUtils.getCurrentDomain() + "/portal/rest/gamification/connectors/github/webhooks");
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
      setAuthorizationHeader(postConnection);
      postConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
      postConnection.setDoOutput(true);
      createGitHubWebhook(hook, postConnection);
      int responseCode = postConnection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_CREATED) { // success
        save(organizationName, secret, currentUser, postConnection);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to open GitHub connection", e);
    }
  }

  private void save(String organizationName,
                    String secret,
                    String currentUser,
                    HttpURLConnection postConnection) throws ObjectAlreadyExistsException, IllegalAccessException {
    try (InputStream in = postConnection.getInputStream()) {
      String response = IOUtil.getStreamContentAsString(in);
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode infoNode = objectMapper.readTree(response);
      long hookId = infoNode.get("id").longValue();
      saveWebhook(hookId, organizationName, secret, currentUser);
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

  public void deleteHook(String organizationName, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete GitHub hook");
    }
    ConnectorHook connectorHook = connectorHookService.getConnectorHook(CONNECTOR_NAME, organizationName);
    if (connectorHook == null) {
      throw new ObjectNotFoundException("Github hook with name " + organizationName + " wasn't found");
    }
    String url = GITHUB_API_URL + organizationName + "/hooks/" + connectorHook.getHookRemoteId();
    URL obj;
    int responseCode;
    try {
      obj = new URL(url);
      HttpURLConnection deleteConnection = (HttpURLConnection) obj.openConnection();
      deleteConnection.setRequestMethod("DELETE");
      setAuthorizationHeader(deleteConnection);
      responseCode = deleteConnection.getResponseCode();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) { // success
      deleteWebhook(organizationName, currentUser);
    } else {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
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

  private void saveWebhook(long hookId, String organizationName, String secret, String currentUser) throws IOException,
                                                                                                    ObjectAlreadyExistsException,
                                                                                                    IllegalAccessException {
    URL obj = new URL(GITHUB_API_URL + organizationName);
    HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
    connection.setRequestMethod("GET");
    setAuthorizationHeader(connection);
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (InputStream in = connection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        ConnectorHook connectorHook = new ConnectorHook();
        connectorHook.setHookRemoteId(hookId);
        connectorHook.setOrganizationRemoteId(infoNode.get("id").longValue());
        connectorHook.setConnectorName(CONNECTOR_NAME);
        connectorHook.setName(organizationName);
        connectorHook.setSecret(secret);
        connectorHook.setTitle(infoNode.get("name").textValue());
        connectorHook.setDescription(infoNode.get("description").textValue());
        String userIdentityId = identityManager.getOrCreateUserIdentity(currentUser).getId();
        connectorHook.setWatchedBy(Long.parseLong(userIdentityId));
        long imageFileId = downloadAvatar(infoNode.get("avatar_url").textValue(), organizationName + "_avatar");
        connectorHook.setImageFileId(imageFileId);
        connectorHookService.createConnectorHook(connectorHook, currentUser);
      } catch (IOException e) {
        LOG.error(e);
      }
    } else {
      throw new IllegalArgumentException("Failed to get GitHub " + organizationName + " organization info.");
    }
  }

  private void deleteWebhook(String organizationName, String currentUser) throws IllegalAccessException {
    connectorHookService.deleteConnectorHook(CONNECTOR_NAME, organizationName, currentUser);
  }

  private long downloadAvatar(String avatarUrl, String fileName) {
    try {
      URL url = new URL(avatarUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          int data;
          while ((data = in.read()) != -1) {
            out.write(data);
          }
          byte[] imageData = out.toByteArray();
          FileItem fileItem;
          fileItem = getFileItem(fileName, imageData);
          return fileItem == null || fileItem.getFileInfo() == null ? 0 : fileItem.getFileInfo().getId();
        }
      } else {
        throw new IllegalStateException("Error while saving connector hook image file");
      }
    } catch (IOException e) {
      return 0;
    }
  }

  private FileItem getFileItem(String fileName, byte[] imageData) {
    FileItem fileItem;
    try {
      fileItem = new FileItem(null,
                              fileName,
                              "image/png",
                              "gamification",
                              imageData.length,
                              new Date(),
                              null,
                              false,
                              new ByteArrayInputStream(imageData));
      fileItem = fileService.writeFile(fileItem);
    } catch (Exception e) {
      throw new IllegalStateException("Error while saving connector hook image file");
    }
    return fileItem;
  }

  private String getAccessToken() {
    if (accessToken == null) {
      accessToken = connectorSettingService.getConnectorAccessToken(CONNECTOR_NAME);
    }
    return accessToken;
  }

  private void setAuthorizationHeader(HttpURLConnection connection) {
    connection.setRequestProperty("Authorization", "token " + getAccessToken());
  }
}
