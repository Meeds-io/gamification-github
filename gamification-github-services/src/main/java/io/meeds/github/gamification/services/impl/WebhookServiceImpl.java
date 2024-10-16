/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
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
package io.meeds.github.gamification.services.impl;

import java.util.*;
import java.util.stream.Collectors;

import io.meeds.github.gamification.model.RemoteOrganization;
import io.meeds.github.gamification.model.WebHook;
import io.meeds.github.gamification.storage.WebHookStorage;
import io.meeds.gamification.model.RuleDTO;
import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import io.meeds.github.gamification.model.RemoteRepository;
import io.meeds.github.gamification.model.TokenStatus;
import io.meeds.github.gamification.services.GithubConsumerService;
import io.meeds.github.gamification.services.WebhookService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import static io.meeds.github.gamification.utils.Utils.*;

@Service
public class WebhookServiceImpl implements WebhookService {

  private static final Log            LOG                    = ExoLogger.getLogger(WebhookServiceImpl.class);

  private static final Context        GITHUB_WEBHOOK_CONTEXT = Context.GLOBAL.id("githubWebhook");

  private static final Scope          WATCH_LIMITED_SCOPE    = Scope.APPLICATION.id("watchLimited");

  private static final Scope          DISABLED_REPOS_SCOPE   = Scope.APPLICATION.id("disabledRepos");

  @Autowired
  private SettingService        settingService;

  @Autowired
  private WebHookStorage        webHookStorage;

  @Autowired
  private GithubConsumerService githubServiceConsumer;

  @Autowired
  private RuleService           ruleService;

  public Page<WebHook> getWebhooks(String currentUser, Pageable pageable) throws IllegalAccessException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS);
    }
    return getWebhooks(pageable);
  }

  public WebHook getWebhookId(long webhookId, String username) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(username)) {
      throw new IllegalAccessException(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS);
    }
    WebHook webHook = getWebhookId(webhookId);
    if (webHook == null) {
      throw new ObjectNotFoundException("Webhook doesn't exist");
    }
    return webHook;
  }

  @Override
  public WebHook getWebhookId(long webhookId) {
    if (webhookId <= 0) {
      throw new IllegalArgumentException("Webhook id is mandatory");
    }
    return webHookStorage.getWebHookById(webhookId);
  }

  public Page<WebHook> getWebhooks(Pageable pageable) {
    return webHookStorage.getWebhooks(pageable);
  }

  public WebHook createWebhook(String organizationName,
                               String accessToken,
                               String currentUser) throws ObjectAlreadyExistsException,
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
    WebHook webHook = githubServiceConsumer.createWebhook(organizationName, GITHUB_TRIGGERS, accessToken);

    if (webHook != null) {
      webHook.setOrganizationId(remoteOrganization.getId());
      webHook.setWatchedBy(currentUser);
      return webHookStorage.saveWebHook(webHook);
    }
    return null;
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

  public void deleteWebhook(long organizationId, String currentUser) throws IllegalAccessException, ObjectNotFoundException {
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
    RuleFilter ruleFilter = new RuleFilter(true);
    ruleFilter.setEventType(CONNECTOR_NAME);
    ruleFilter.setIncludeDeleted(true);
    List<RuleDTO> rules = ruleService.getRules(ruleFilter, 0, -1);
    rules.stream()
         .filter(r -> !r.getEvent().getProperties().isEmpty()
             && r.getEvent().getProperties().get(ORGANIZATION_ID).equals(String.valueOf(organizationId)))
         .forEach(rule -> {
           try {
             rule.setEnabled(false);
             ruleService.updateRule(rule);
           } catch (ObjectNotFoundException e) {
             LOG.warn("Error while automatically switching rule status. Rule = {} ", rule, e);
           }
         });
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

  public List<RemoteRepository> retrieveOrganizationRepos(long organizationRemoteId,
                                                          String currentUser,
                                                          int page,
                                                          int perPage,
                                                          String keyword) throws IllegalAccessException, ObjectNotFoundException {
    if (!Utils.isRewardingManager(currentUser)) {
      throw new IllegalAccessException("The user is not authorized to access organization repositories");
    }
    WebHook webHook = webHookStorage.getWebhookByOrganizationId(organizationRemoteId);
    if (webHook == null) {
      throw new ObjectNotFoundException("webhook with organization id '" + organizationRemoteId + "' doesn't exist");
    }
    List<RemoteRepository> remoteRepositories = githubServiceConsumer.retrieveOrganizationRepos(webHook, page, perPage, keyword);
    remoteRepositories.forEach(remoteRepository -> remoteRepository.setEnabled(isWebHookRepositoryEnabled(webHook.getOrganizationId(),
                                                                                                          remoteRepository.getId())));
    return remoteRepositories;
  }

  @Override
  public void forceUpdateWebhooks() {
    List<WebHook> webHook = webHookStorage.getWebhooks();
    webHook.forEach(this::forceUpdateWebhook);
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
