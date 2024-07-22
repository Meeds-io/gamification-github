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
package io.meeds.github.gamification.service;

import static io.meeds.github.gamification.utils.Utils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.meeds.gamification.model.filter.RuleFilter;
import io.meeds.gamification.service.RuleService;
import io.meeds.github.gamification.model.RemoteOrganization;
import io.meeds.github.gamification.model.TokenStatus;
import io.meeds.github.gamification.model.WebHook;
import io.meeds.github.gamification.services.GithubConsumerService;
import io.meeds.github.gamification.services.WebhookService;
import io.meeds.github.gamification.services.impl.WebhookServiceImpl;
import io.meeds.github.gamification.storage.WebHookStorage;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;

@SpringBootTest(classes = { WebhookServiceImpl.class })
class WebhookServiceTest {

  private static final String   ADMIN_USER = "root";

  private static final String   USER       = "user";

  private static final Pageable PAGEABLE   = Pageable.ofSize(2);

  @MockBean
  private GithubConsumerService githubConsumerService;

  @MockBean
  private WebHookStorage        webHookStorage;

  @MockBean
  private RuleService           ruleService;

  @MockBean
  private SettingService        settingService;

  @Autowired
  private WebhookService        webhookService;

  @Test
  void testGetWebhooks() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class, () -> webhookService.getWebhooks(USER, PAGEABLE));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());

    webhookService.getWebhooks(ADMIN_USER, PAGEABLE);
    verify(webHookStorage, times(1)).getWebhooks(PAGEABLE);
  }

  @Test
  void testGetWebhookId() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class, () -> webhookService.getWebhookId(1L, USER));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class, () -> webhookService.getWebhookId(0L, ADMIN_USER));
    assertEquals("Webhook id is mandatory", exception.getMessage());

    when(webHookStorage.getWebHookById(2L)).thenReturn(new WebHook());
    webhookService.getWebhookId(2L, ADMIN_USER);
    verify(webHookStorage, times(1)).getWebHookById(2L);

    when(webHookStorage.getWebHookById(1L)).thenReturn(null);
    exception = assertThrows(ObjectNotFoundException.class, () -> webhookService.getWebhookId(1L, ADMIN_USER));
    assertEquals("Webhook doesn't exist", exception.getMessage());
  }

  @Test
  void testCreateWebhook() throws Exception {

    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.createWebhook("organizationName", "accessToken", USER));
    assertEquals("The user is not authorized to create GitHub hook", exception.getMessage());

    TokenStatus tokenStatus = new TokenStatus(false, 12L, 15454L);
    when(githubConsumerService.checkGitHubTokenStatus(any())).thenReturn(tokenStatus);

    exception = assertThrows(IllegalAccessException.class,
                             () -> webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER));
    assertEquals("github.tokenExpiredOrInvalid", exception.getMessage());

    tokenStatus.setValid(true);
    tokenStatus.setRemaining(0L);
    when(githubConsumerService.checkGitHubTokenStatus(any())).thenReturn(tokenStatus);

    exception = assertThrows(IllegalAccessException.class,
                             () -> webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER));
    assertEquals("github.tokenRateLimitReached", exception.getMessage());

    RemoteOrganization remoteOrganization = new RemoteOrganization(12345, "name", "title", "description", "avatarUrl");
    WebHook existsWebHook = new WebHook();
    when(githubConsumerService.retrieveRemoteOrganization("organizationName", "accessToken")).thenReturn(remoteOrganization);
    when(webHookStorage.getWebhookByOrganizationId(12345)).thenReturn(existsWebHook);
    tokenStatus.setRemaining(1221544L);
    when(githubConsumerService.checkGitHubTokenStatus(any())).thenReturn(tokenStatus);

    assertThrows(ObjectAlreadyExistsException.class,
                 () -> webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER));

    when(webHookStorage.getWebhookByOrganizationId(12345)).thenReturn(null);
    WebHook webHook = new WebHook();

    when(githubConsumerService.createWebhook("organizationName", GITHUB_TRIGGERS, "accessToken")).thenReturn(webHook);

    webHook.setOrganizationId(remoteOrganization.getId());
    webHook.setWatchedBy(ADMIN_USER);
    webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER);
    verify(webHookStorage, times(1)).saveWebHook(webHook);
  }

  @Test
  void testUpdateWebHookAccessToken() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.updateWebHookAccessToken(124L, "accessToken", USER));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());

    exception = assertThrows(IllegalArgumentException.class,
                             () -> webhookService.updateWebHookAccessToken(0L, "accessToken", ADMIN_USER));
    assertEquals("webHook id must be positive", exception.getMessage());

    when(webHookStorage.getWebHookById(2L)).thenReturn(new WebHook());
    webhookService.updateWebHookAccessToken(2L, "accessToken", ADMIN_USER);
    verify(webHookStorage, times(1)).updateWebHookAccessToken(anyLong(), anyString());

    when(webHookStorage.getWebHookById(1L)).thenReturn(null);
    exception = assertThrows(ObjectNotFoundException.class,
                             () -> webhookService.updateWebHookAccessToken(1L, "accessToken", ADMIN_USER));
    assertEquals("webhook with id : 1 wasn't found", exception.getMessage());
  }

  @Test
  void testDeleteWebhook() throws ObjectNotFoundException, IllegalAccessException {
    Throwable exception = assertThrows(IllegalAccessException.class, () -> webhookService.deleteWebhook(124L, USER));
    assertEquals("The user is not authorized to delete GitHub hook", exception.getMessage());

    when(webHookStorage.getWebhookByOrganizationId(124L)).thenReturn(null);
    exception = assertThrows(ObjectNotFoundException.class, () -> webhookService.deleteWebhook(124L, ADMIN_USER));
    assertEquals("Github hook for organization id : 124 wasn't found", exception.getMessage());

    WebHook webHook = new WebHook();
    when(webHookStorage.getWebhookByOrganizationId(124L)).thenReturn(webHook);
    when(githubConsumerService.deleteWebhook(webHook)).thenReturn("Deleted");

    webhookService.deleteWebhook(124L, ADMIN_USER);
    verify(webHookStorage, times(1)).deleteWebHook(124L);
    RuleFilter ruleFilter = new RuleFilter(true);
    ruleFilter.setEventType(CONNECTOR_NAME);
    ruleFilter.setIncludeDeleted(true);
    verify(ruleService, times(1)).getRules(ruleFilter, 0, -1);
  }

  @Test
  void testVerifyWebhookSecret() {
    String payload = "{\"organization\":{\"id\":\"14524\"},\"repository\":{\"id\":\"555564545\"}}";

    when(webHookStorage.getWebhookByOrganizationId(14524L)).thenReturn(null);
    assertFalse(webhookService.verifyWebhookSecret(payload, "signature"));
  }

  @Test
  void testIsWebHookRepositoryEnabled() throws Exception {
    // When
    String payload = "{\"organization\":{\"id\":\"14524\"},\"repository\":{\"id\":\"555564545\"}}";

    // Then
    assertTrue(webhookService.isWebHookRepositoryEnabled(payload));

    // When
    Exception exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.setWebHookRepositoryEnabled(14525L, 555564545L, false, USER));
    assertEquals("The user is not authorized to update repository status", exception.getMessage());

    webhookService.setWebHookRepositoryEnabled(14524L, 555564545L, false, ADMIN_USER);
    verify(settingService, times(2)).get(Context.GLOBAL.id("githubWebhook"), Scope.APPLICATION.id("disabledRepos"), "14524");
    verify(settingService, times(1)).set(any(), any(), anyString(), any());

    webhookService.isWebHookRepositoryEnabled(payload);

    webhookService.setWebHookRepositoryEnabled(145211L, 555564545L, true, ADMIN_USER);
    verify(settingService, times(1)).get(Context.GLOBAL.id("githubWebhook"), Scope.APPLICATION.id("disabledRepos"), "145211");
  }
}
