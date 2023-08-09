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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import io.meeds.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.github.exception.GithubConnectionException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.services.GithubTriggerService;
import org.exoplatform.gamification.github.services.WebhookService;
import org.exoplatform.gamification.github.storage.WebHookStorage;
import org.json.JSONObject;

import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.ws.rs.core.MediaType;

import static org.exoplatform.gamification.github.utils.Utils.*;

public class WebhookServiceImpl implements WebhookService {

  private static final Log           LOG                    = ExoLogger.getLogger(WebhookServiceImpl.class);

  private static final Context       GITHUB_WEBHOOK_CONTEXT = Context.GLOBAL.id("githubWebhook");

  private static final Scope         WATCH_LIMITED_SCOPE    = Scope.APPLICATION.id("watchLimited");

  private static final Scope         DISABLED_REPOS_SCOPE   = Scope.APPLICATION.id("disabledRepos");

  private final ListenerService      listenerService;

  private final SettingService       settingService;

  private final WebHookStorage       webHookStorage;

  private final GithubTriggerService githubTriggerService;

  private HttpClient                 client;

  public WebhookServiceImpl(ListenerService listenerService,
                            SettingService settingService,
                            GithubTriggerService githubTriggerService,
                            WebHookStorage webHookStorage) {
    this.listenerService = listenerService;
    this.settingService = settingService;
    this.githubTriggerService = githubTriggerService;
    this.webHookStorage = webHookStorage;
  }

  public List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS);
    }
    return getWebhooks(offset, limit, forceUpdate);
  }

  public List<WebHook> getWebhooks(int offset, int limit, boolean forceUpdate) {
    if (forceUpdate) {
      forceUpdateWebhooks();
    }
    return getWebhooks(offset, limit);
  }

  public List<WebHook> getWebhooks(int offset, int limit) {
    List<Long> hooksIds = webHookStorage.getWebhookIds(offset, limit);
    return hooksIds.stream().map(webHookStorage::getWebHookById).toList();
  }

  public int countWebhooks(String currentUser, boolean forceUpdate) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS);
    }
    if (forceUpdate) {
      forceUpdateWebhooks();
    }
    return webHookStorage.countWebhooks();
  }

  @SuppressWarnings("unchecked")
  public void createWebhook(String organizationName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
                                                                                             IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to create GitHub hook");
    }
    RemoteOrganization remoteOrganization;
    remoteOrganization = retrieveRemoteOrganization(organizationName, accessToken);
    WebHook existsWebHook = webHookStorage.getWebhookByOrganizationId(remoteOrganization.getId());
    if (existsWebHook != null) {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }
    String secret = generateRandomSecret(8);
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
    hook.put(EVENTS, githubTriggerService.getTriggers());
    URI uri = URI.create(url);
    try {
      String response = processPost(uri, hook.toString(), accessToken);
      if (response != null) {
        Map<String, Object> resultMap = fromJsonStringToMap(response);
        long hookId = Long.parseLong(resultMap.get(ID).toString());
        List<String> events = (List<String>) resultMap.get(EVENTS);
        WebHook webHook = new WebHook();
        webHook.setWebhookId(hookId);
        webHook.setOrganizationId(remoteOrganization.getId());
        webHook.setEvent(events);
        webHook.setWatchedBy(currentUser);
        webHook.setToken(accessToken);
        webHook.setSecret(secret);
        webHookStorage.saveWebHook(webHook);
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to open GitHub connection", e);
    }
  }

  public void updateWebHookAccessToken(long webHookId, String accessToken, String currentUser) throws IllegalAccessException,
                                                                                               ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS);
    }
    if (webHookId <= 0) {
      throw new IllegalArgumentException("webHook id must be positive");
    }
    WebHook webHook = webHookStorage.getWebHookById(webHookId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with id : " + webHookId + " wasn't found");
    }
    webHookStorage.updateWebHookAccessToken(webHookId, encode(accessToken));
  }

  public void deleteWebhookHook(long organizationId, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to delete GitHub hook");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationId);
    if (webHook == null) {
      throw new ObjectNotFoundException("Github hook for organization id : " + organizationId + " wasn't found");
    }
    URI uri = URI.create(GITHUB_API_URL + organizationId + "/hooks/" + webHook.getWebhookId());
    try {
      String response = processDelete(uri, webHook.getToken());
      if (response != null) {
        deleteWebhook(organizationId);
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
  }

  public void deleteWebhook(long organizationId) {
    webHookStorage.deleteWebHook(organizationId);
  }

  @Override
  public void forceUpdateWebhooks() {
    List<WebHook> webHook = getWebhooks(0, -1);
    webHook.forEach(this::forceUpdateWebhook);
  }

  @Override
  public boolean verifyWebhookSecret(String payload, String signature) {
    JSONObject jsonPayload = new JSONObject(payload);
    JSONObject organization = jsonPayload.getJSONObject("organization");
    long organizationId = organization.getLong(ID);
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationId);
    if (webHook != null) {
      return verifySignature(webHook.getSecret(), payload, signature);
    }
    return false;
  }

  @Override
  public boolean isWebHookRepositoryEnabled(String payload) {
    Map<String, Object> payloadMap = fromJsonStringToMap(payload);
    String organizationId = extractSubItem(payloadMap, "organization", "id");
    String repositoryId = extractSubItem(payloadMap, "repository", "id");
    if (organizationId != null && repositoryId != null) {
      return isWebHookRepositoryEnabled(Long.parseLong(organizationId), Long.parseLong(repositoryId));
    }
    return false;
  }

  @Override
  public boolean isWebHookRepositoryEnabled(long organizationId, long repositoryId) {
    List<Long> disabledRepositoryList = new ArrayList<>();
    SettingValue<?> settingValue =
                                 settingService.get(GITHUB_WEBHOOK_CONTEXT, DISABLED_REPOS_SCOPE, String.valueOf(organizationId));
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      disabledRepositoryList = Arrays.stream(settingValue.getValue().toString().split(":"))
                                     .map(Long::parseLong)
                                     .collect(Collectors.toList());
    }
    return !disabledRepositoryList.contains(repositoryId);
  }

  @Override
  public void setWebHookRepositoryEnabled(long organizationId,
                                          long repositoryId,
                                          boolean enabled,
                                          String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to update repository status");
    }
    List<Long> disabledRepositoryList = new ArrayList<>();
    SettingValue<?> settingValue =
                                 settingService.get(GITHUB_WEBHOOK_CONTEXT, DISABLED_REPOS_SCOPE, String.valueOf(organizationId));
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      disabledRepositoryList = Arrays.stream(settingValue.getValue().toString().split(":"))
                                     .map(Long::parseLong)
                                     .collect(Collectors.toList());
    }
    if (!enabled) {
      if (!disabledRepositoryList.contains(repositoryId)) {
        disabledRepositoryList.add(repositoryId);
      }
    } else {
      disabledRepositoryList.remove(repositoryId);
    }
    String disabledRepositories = disabledRepositoryList.stream().map(String::valueOf).collect(Collectors.joining(":"));
    settingService.set(GITHUB_WEBHOOK_CONTEXT,
                       DISABLED_REPOS_SCOPE,
                       String.valueOf(organizationId),
                       SettingValue.create(disabledRepositories));
  }

  @Override
  public boolean isWebHookWatchLimitEnabled(long organizationId) {
    SettingValue<?> settingValue =
                                 settingService.get(GITHUB_WEBHOOK_CONTEXT, WATCH_LIMITED_SCOPE, String.valueOf(organizationId));
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      return Boolean.parseBoolean(settingValue.getValue().toString());
    }
    return true;
  }

  @Override
  public void setWebHookWatchLimitEnabled(long organizationId,
                                          boolean enabled,
                                          String currentUser) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to update webHook watch limit status");
    }
    settingService.set(GITHUB_WEBHOOK_CONTEXT, WATCH_LIMITED_SCOPE, String.valueOf(organizationId), SettingValue.create(enabled));
  }

  @Override
  public List<RemoteRepository> retrieveOrganizationRepos(long organizationRemoteId,
                                                          String currentUser,
                                                          int page,
                                                          int perPage) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access organization repositories");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationRemoteId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with organization id '" + organizationRemoteId + "' doesn't exist");
    }
    List<RemoteRepository> remoteRepositories = new ArrayList<>();
    String url = GITHUB_API_URL + organizationRemoteId + "/repos?per_page=" + perPage + "&page=" + page;

    URI uri = URI.create(url);
    try {
      String response = processGet(uri, webHook.getToken());
      if (response != null) {
        Map<String, Object>[] repositoryMaps = fromJsonStringToMapCollection(response);
        for (Map<String, Object> repoMap : repositoryMaps) {
          RemoteRepository remoteRepository = new RemoteRepository();
          long repoId = Long.parseLong(repoMap.get("id").toString());
          String name = (String) repoMap.get("name");
          String description = (String) repoMap.get("description");
          remoteRepository.setId(repoId);
          remoteRepository.setName(name);
          remoteRepository.setDescription(description);
          remoteRepository.setEnabled(isWebHookRepositoryEnabled(organizationRemoteId, repoId));
          remoteRepositories.add(remoteRepository);
        }
        return remoteRepositories;
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization repos.");
    }
    return remoteRepositories;
  }

  @Override
  public int countOrganizationRepos(long organizationRemoteId, String currentUser) throws IllegalAccessException,
                                                                                   ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access organization repositories");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationRemoteId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with organization id '" + organizationRemoteId + "' doesn't exist");
    }
    String url = GITHUB_API_URL + organizationRemoteId + "/repos";
    URI uri = URI.create(url);
    try {
      String response = processGet(uri, webHook.getToken());
      if (response != null) {
        Map<String, Object>[] repositoryMaps = fromJsonStringToMapCollection(response);
        return (int) Arrays.stream(repositoryMaps).count();
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization repos.");
    }
    return 0;
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

  @SuppressWarnings("unchecked")
  private void forceUpdateWebhook(WebHook webHook) {
    long organizationId = webHook.getOrganizationId();
    URI uri = URI.create(GITHUB_API_URL + organizationId + "/hooks/" + webHook.getWebhookId());
    String response;
    try {
      response = processGet(uri, webHook.getToken());
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub webhook info.", e);
    }
    if (response == null) {
      webHookStorage.deleteWebHook(organizationId);
    } else {
      Map<String, Object> resultMap = fromJsonStringToMap(response);
      List<String> events = (List<String>) resultMap.get(EVENTS);
      if (!CollectionUtils.isEqualCollection(events, webHook.getEvent())) {
        webHook.setEvent(events);
        webHookStorage.updateWebHook(webHook, true);
      }
    }
  }

  private RemoteOrganization retrieveRemoteOrganization(String organizationName, String accessToken) {
    URI uri = URI.create(GITHUB_API_URL + organizationName);
    String response;
    try {
      response = processGet(uri, accessToken);
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization info.", e);
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteOrganization gitHubOrganization = new RemoteOrganization();
    gitHubOrganization.setId((Long.parseLong(resultMap.get(ID).toString())));
    gitHubOrganization.setName(resultMap.get(LOGIN).toString());
    gitHubOrganization.setTitle(resultMap.get(NAME).toString());
    gitHubOrganization.setDescription(resultMap.get(DESCRIPTION).toString());
    gitHubOrganization.setAvatarUrl(resultMap.get(AVATAR_URL).toString());
    return gitHubOrganization;
  }

  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) {
    URI uri = URI.create(GITHUB_API_URL + organizationId);
    String response;
    try {
      response = processGet(uri, accessToken);
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization info.", e);
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteOrganization gitHubOrganization = new RemoteOrganization();
    gitHubOrganization.setId((Long.parseLong(resultMap.get(ID).toString())));
    gitHubOrganization.setName(resultMap.get(ID).toString());
    gitHubOrganization.setTitle(resultMap.get(NAME).toString());
    gitHubOrganization.setDescription(resultMap.get(DESCRIPTION).toString());
    gitHubOrganization.setAvatarUrl(resultMap.get(AVATAR_URL).toString());
    return gitHubOrganization;
  }

  private String processGet(URI uri, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processPost(URI uri, String jsonString, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpPost request = new HttpPost(uri);
    StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    try {
      request.setHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      request.setEntity(entity);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processDelete(URI uri, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpDelete request = new HttpDelete(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processRequest(HttpClient httpClient, HttpRequestBase request) throws IOException, GithubConnectionException {
    HttpResponse response = httpClient.execute(request);
    boolean isSuccess = response != null
        && (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300);
    if (isSuccess) {
      return processSuccessResponse(response);
    } else if (response != null && response.getStatusLine().getStatusCode() == 404) {
      return null;
    } else {
      processErrorResponse(response);
      return null;
    }
  }

  private String processSuccessResponse(HttpResponse response) throws IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      return String.valueOf(HttpStatus.SC_NO_CONTENT);
    } else if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED
        || response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) && response.getEntity() != null
        && response.getEntity().getContentLength() != 0) {
      try (InputStream is = response.getEntity().getContent()) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } else {
      return null;
    }
  }

  private void processErrorResponse(HttpResponse response) throws GithubConnectionException, IOException {
    if (response == null) {
      throw new GithubConnectionException("Error when connecting github");
    } else if (response.getEntity() != null) {
      try (InputStream is = response.getEntity().getContent()) {
        String errorMessage = IOUtils.toString(is, StandardCharsets.UTF_8);
        if (StringUtils.contains(errorMessage, "")) {
          throw new GithubConnectionException(errorMessage);
        } else {
          throw new GithubConnectionException(GITHUB_CONNECTION_ERROR + errorMessage);
        }
      }
    } else {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR + response.getStatusLine().getStatusCode());
    }
  }

  private HttpClient getHttpClient() {
    if (client == null) {
      HttpClientConnectionManager clientConnectionManager = getClientConnectionManager();
      HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                       .setConnectionManager(clientConnectionManager)
                                                       .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
      client = httpClientBuilder.build();
    }
    return client;
  }

  private HttpClientConnectionManager getClientConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(10);
    return connectionManager;
  }
}
