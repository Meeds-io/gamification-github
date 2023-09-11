/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 *
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

import java.util.*;
import java.util.stream.Collectors;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.service.EventService;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.services.GithubConsumerService;
import org.exoplatform.gamification.github.services.GithubTriggerService;
import org.exoplatform.gamification.github.services.WebhookService;
import org.exoplatform.gamification.github.storage.WebHookStorage;
import org.json.JSONObject;

import static org.exoplatform.gamification.github.utils.Utils.*;

public class WebhookServiceImpl implements WebhookService {

  private static final Context        GITHUB_WEBHOOK_CONTEXT = Context.GLOBAL.id("githubWebhook");

  private static final Scope          WATCH_LIMITED_SCOPE    = Scope.APPLICATION.id("watchLimited");

  private static final Scope          DISABLED_REPOS_SCOPE   = Scope.APPLICATION.id("disabledRepos");

  public static final String          ENABLED                = ".enabled";

  private final SettingService        settingService;

  private final WebHookStorage        webHookStorage;

  private final GithubTriggerService  githubTriggerService;

  private final GithubConsumerService githubServiceConsumer;

  private final EventService          eventService;

  public WebhookServiceImpl(SettingService settingService,
                            GithubTriggerService githubTriggerService,
                            WebHookStorage webHookStorage,
                            GithubConsumerService githubServiceConsumer,
                            EventService eventService) {
    this.settingService = settingService;
    this.githubTriggerService = githubTriggerService;
    this.webHookStorage = webHookStorage;
    this.githubServiceConsumer = githubServiceConsumer;
    this.eventService = eventService;
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

  public void createWebhook(String organizationName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
                                                                                             IllegalAccessException,
                                                                                             ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to create GitHub hook");
    }
    TokenStatus tokenStatus = githubServiceConsumer.checkGitHubTokenStatus(accessToken);
    if (!tokenStatus.isValid()) {
      throw new IllegalAccessException("github.tokenExpiredOrInvalid");
    }
    if (tokenStatus.getRemaining() == 0) {
      throw new IllegalAccessException("github.tokenRateLimitReached");
    }
    RemoteOrganization remoteOrganization;
    remoteOrganization = githubServiceConsumer.retrieveRemoteOrganization(organizationName, accessToken);
    WebHook existsWebHook = webHookStorage.getWebhookByOrganizationId(remoteOrganization.getId());
    if (existsWebHook != null) {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }
    WebHook webHook = githubServiceConsumer.createWebhook(organizationName, githubTriggerService.getTriggers(), accessToken);

    if (webHook != null) {
      webHook.setOrganizationId(remoteOrganization.getId());
      webHook.setWatchedBy(currentUser);
      webHookStorage.saveWebHook(webHook);
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
    String response = githubServiceConsumer.deleteWebhook(webHook);
    if (response != null) {
      deleteWebhook(organizationId);
    }
  }

  public void deleteWebhook(long organizationId) {
    webHookStorage.deleteWebHook(organizationId);
  }

  @Override
  public void forceUpdateWebhooks() {
    githubServiceConsumer.clearCache();
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
    return true;
  }

  @Override
  public boolean isWebHookRepositoryEnabled(long organizationId, long repositoryId) {
    List<Long> disabledRepositoryList = new ArrayList<>();
    SettingValue<?> settingValue =
                                 settingService.get(GITHUB_WEBHOOK_CONTEXT, DISABLED_REPOS_SCOPE, String.valueOf(organizationId));
    if (settingValue != null && settingValue.getValue() != null && StringUtils.isNotBlank(settingValue.getValue().toString())) {
      disabledRepositoryList = Arrays.stream(settingValue.getValue().toString().split(":")).map(Long::parseLong).toList();
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
    List<RemoteRepository> remoteRepositories = githubServiceConsumer.retrieveOrganizationRepos(webHook, page, perPage);
    remoteRepositories.forEach(remoteRepository -> remoteRepository.setEnabled(isWebHookRepositoryEnabled(webHook.getOrganizationId(),
                                                                                                          remoteRepository.getId())));
    return remoteRepositories;
  }

  public int countOrganizationRepos(long organizationRemoteId, String currentUser) throws IllegalAccessException,
                                                                                   ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access organization repositories");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationRemoteId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with organization id '" + organizationRemoteId + "' doesn't exist");
    }
    return githubServiceConsumer.countOrganizationRepos(webHook);
  }

  @Override
  public void setEventEnabledForOrganization(long eventId,
                                             long organizationId,
                                             boolean enabled,
                                             String currentUser) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to enable/disable event for organization");
    }
    EventDTO eventDTO = eventService.getEvent(eventId);
    if (eventDTO == null) {
      throw new ObjectNotFoundException("event not found");
    }
    Map<String, String> eventProperties = eventDTO.getProperties();
    if (eventProperties != null && !eventProperties.isEmpty()) {
      String eventProjectStatus = eventProperties.get(organizationId + ENABLED);
      if (StringUtils.isNotBlank(eventProjectStatus)) {
        eventProperties.remove(organizationId + ENABLED);
        eventProperties.put(organizationId + ENABLED, String.valueOf(enabled));
        eventDTO.setProperties(eventProperties);
      }
    } else {
      Map<String, String> properties = new HashMap<>();
      properties.put(organizationId + ENABLED, String.valueOf(enabled));
      eventDTO.setProperties(properties);
    }
    eventService.updateEvent(eventDTO);
  }

  @SuppressWarnings("unchecked")
  private void forceUpdateWebhook(WebHook webHook) {
    TokenStatus tokenStatus = githubServiceConsumer.checkGitHubTokenStatus(webHook.getToken());
    if (!tokenStatus.isValid() || tokenStatus.getRemaining() == 0) {
      return;
    }
    String response = githubServiceConsumer.forceUpdateWebhook(webHook);
    if (response == null) {
      webHookStorage.deleteWebHook(webHook.getOrganizationId());
    } else {
      Map<String, Object> resultMap = fromJsonStringToMap(response);
      List<String> events = (List<String>) resultMap.get(EVENTS);
      if (!CollectionUtils.isEqualCollection(events, webHook.getTriggers())) {
        webHook.setTriggers(events);
        webHookStorage.updateWebHook(webHook, true);
      }
    }
  }
}
