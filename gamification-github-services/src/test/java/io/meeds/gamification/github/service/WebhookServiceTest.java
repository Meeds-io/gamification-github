/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package io.meeds.gamification.github.service;

import static io.meeds.gamification.github.utils.Utils.AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS;
import static io.meeds.gamification.github.utils.Utils.GITHUB_TRIGGERS;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

import io.meeds.gamification.github.BaseGithubTest;
import io.meeds.gamification.github.model.RemoteOrganization;
import io.meeds.gamification.github.model.TokenStatus;
import io.meeds.gamification.github.model.WebHook;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.junit.Test;

import java.util.Arrays;

public class WebhookServiceTest extends BaseGithubTest {

  private static final String ADMIN_USER = "root1";

  private static final String USER       = "root";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    registerAdministratorUser(ADMIN_USER);
    registerInternalUser(USER);
  }

  @Test
  public void testCreateWebhook() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.createWebhook("organizationName", "accessToken", USER));
    assertEquals("The user is not authorized to create GitHub hook", exception.getMessage());

    TokenStatus tokenStatus = new TokenStatus();
    tokenStatus.setValid(false);
    when(githubConsumerService.checkGitHubTokenStatus("tokenExpiredOrInvalid")).thenReturn(tokenStatus);
    exception = assertThrows(IllegalAccessException.class,
                             () -> webhookService.createWebhook("organizationName", "tokenExpiredOrInvalid", ADMIN_USER));
    assertEquals("github.tokenExpiredOrInvalid", exception.getMessage());

    tokenStatus = new TokenStatus();
    tokenStatus.setValid(true);
    tokenStatus.setRemaining(0L);
    when(githubConsumerService.checkGitHubTokenStatus("tokenRateLimitReached")).thenReturn(tokenStatus);
    exception = assertThrows(IllegalAccessException.class,
                             () -> webhookService.createWebhook("organizationName", "tokenRateLimitReached", ADMIN_USER));
    assertEquals("github.tokenRateLimitReached", exception.getMessage());

    tokenStatus = new TokenStatus();
    tokenStatus.setValid(true);
    tokenStatus.setRemaining(11254545L);
    when(githubConsumerService.checkGitHubTokenStatus("accessToken")).thenReturn(tokenStatus);
    RemoteOrganization remoteOrganization = new RemoteOrganization(1,
                                                                   "organizationName",
                                                                   "organizationTitle",
                                                                   "description",
                                                                   "avatarUrl");
    when(githubConsumerService.retrieveRemoteOrganization("organizationName", "accessToken")).thenReturn(remoteOrganization);
    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setOrganizationId(11245L);
    webhook.setOrganizationName("organizationName");
    webhook.setTriggers(Arrays.asList(GITHUB_TRIGGERS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");
    when(githubConsumerService.createWebhook("organizationName", GITHUB_TRIGGERS, "accessToken")).thenReturn(webhook);

    // When
    WebHook createdWebHook = webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER);

    // Then
    assertNotNull(webhookService.getWebhookId(createdWebHook.getId(), ADMIN_USER));
    exception = assertThrows(IllegalAccessException.class, () -> webhookService.getWebhookId(createdWebHook.getId(), USER));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());
    assertThrows(ObjectNotFoundException.class, () -> webhookService.getWebhookId(10, ADMIN_USER));
    assertThrows(IllegalArgumentException.class, () -> webhookService.getWebhookId(-10, ADMIN_USER));
    assertThrows(ObjectAlreadyExistsException.class,
                 () -> webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER));

    // When
    exception = assertThrows(IllegalAccessException.class,
                             () -> webhookService.updateWebHookAccessToken(createdWebHook.getId(), "newAccessToken", USER));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());
    assertThrows(IllegalArgumentException.class,
                 () -> webhookService.updateWebHookAccessToken(-10, "newAccessToken", ADMIN_USER));
    assertThrows(ObjectNotFoundException.class, () -> webhookService.updateWebHookAccessToken(10, "newAccessToken", ADMIN_USER));
    webhookService.updateWebHookAccessToken(createdWebHook.getId(), "newAccessToken", ADMIN_USER);

    // Then
    assertEquals("newAccessToken", webhookService.getWebhookId(createdWebHook.getId(), ADMIN_USER).getToken());

  }

  @Test
  public void testGetWebhooks() throws Exception {
    Throwable exception = assertThrows(IllegalAccessException.class, () -> webhookService.getWebhooks(USER, 0, -1, false));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());
    exception = assertThrows(IllegalAccessException.class, () -> webhookService.countWebhooks(USER, false));
    assertEquals(AUTHORIZED_TO_ACCESS_GIT_HUB_HOOKS, exception.getMessage());

    TokenStatus tokenStatus = new TokenStatus();
    tokenStatus.setValid(true);
    tokenStatus.setRemaining(11254545L);
    when(githubConsumerService.checkGitHubTokenStatus("accessToken")).thenReturn(tokenStatus);
    RemoteOrganization remoteOrganization = new RemoteOrganization(1,
                                                                   "organizationName",
                                                                   "organizationTitle",
                                                                   "description",
                                                                   "avatarUrl");
    RemoteOrganization remoteOrganization1 = new RemoteOrganization(2,
                                                                    "organizationName1",
                                                                    "organizationTitle1",
                                                                    "description1",
                                                                    "avatarUrl1");
    when(githubConsumerService.retrieveRemoteOrganization("organizationName", "accessToken")).thenReturn(remoteOrganization);
    when(githubConsumerService.retrieveRemoteOrganization("organizationName1", "accessToken")).thenReturn(remoteOrganization1);
    WebHook webhook = new WebHook();
    webhook.setWebhookId(1245L);
    webhook.setOrganizationId(11245L);
    webhook.setOrganizationName("organizationName");
    webhook.setTriggers(Arrays.asList(GITHUB_TRIGGERS));
    webhook.setEnabled(true);
    webhook.setToken("accessToken");
    webhook.setSecret("secret");
    WebHook webhook1 = new WebHook();
    webhook1.setWebhookId(222545L);
    webhook1.setOrganizationId(888655L);
    webhook1.setOrganizationName("organizationName1");
    webhook1.setTriggers(Arrays.asList(GITHUB_TRIGGERS));
    webhook1.setEnabled(true);
    webhook1.setToken("accessToken");
    webhook1.setSecret("secret");
    when(githubConsumerService.createWebhook("organizationName", GITHUB_TRIGGERS, "accessToken")).thenReturn(webhook);
    when(githubConsumerService.createWebhook("organizationName1", GITHUB_TRIGGERS, "accessToken")).thenReturn(webhook1);

    // When
    WebHook webHook = webhookService.createWebhook("organizationName", "accessToken", ADMIN_USER);
    webhookService.createWebhook("organizationName1", "accessToken", ADMIN_USER);

    // Then
    assertNotNull(webhookService.getWebhooks(ADMIN_USER, 0, 10, false));
    assertEquals(2, webhookService.countWebhooks(ADMIN_USER, false));

    // When
    exception = assertThrows(IllegalAccessException.class, () -> webhookService.deleteWebhook(webHook.getOrganizationId(), USER));
    assertEquals("The user is not authorized to delete GitHub hook", exception.getMessage());
    assertThrows(ObjectNotFoundException.class, () -> webhookService.deleteWebhook(1000, ADMIN_USER));
    when(githubConsumerService.deleteWebhook(webHook)).thenReturn("response");
    webhookService.deleteWebhook(webHook.getOrganizationId(), ADMIN_USER);

    // Then
    assertNotNull(webhookService.getWebhooks(ADMIN_USER, 0, 10, false));
    assertEquals(1, webhookService.countWebhooks(ADMIN_USER, false));
  }

  @Test
  public void testIsWebHookRepositoryEnabled() throws Exception {
    // When
    String payload = "{\"organization\":{\"id\":\"14524\"},\"repository\":{\"id\":\"555564545\"}}";

    // Then
    assertTrue(webhookService.isWebHookRepositoryEnabled(payload));

    // When
    Exception exception = assertThrows(IllegalAccessException.class,
                                       () -> webhookService.setWebHookRepositoryEnabled(14524L, 555564545L, false, USER));
    assertEquals("The user is not authorized to update repository status", exception.getMessage());

    webhookService.setWebHookRepositoryEnabled(14524L, 555564545L, false, ADMIN_USER);
    assertFalse(webhookService.isWebHookRepositoryEnabled(payload));

    webhookService.setWebHookRepositoryEnabled(14524L, 555564545L, true, ADMIN_USER);
    assertTrue(webhookService.isWebHookRepositoryEnabled(payload));
  }
}
