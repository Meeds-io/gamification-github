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
package org.exoplatform.gamification.github.rest.builder;

import java.util.Collection;
import java.util.List;

import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.rest.model.WebHookRestEntity;
import org.exoplatform.gamification.github.services.GithubConsumerService;
import org.exoplatform.gamification.github.services.WebhookService;

public class WebHookBuilder {

  private WebHookBuilder() {
    // Class with static methods
  }

  public static WebHookRestEntity toRestEntity(WebhookService webhookService, GithubConsumerService githubConsumerService, WebHook webHook) {
    if (webHook == null) {
      return null;
    }
    RemoteOrganization remoteOrganization = null;
    TokenStatus tokenStatus = githubConsumerService.checkGitHubTokenStatus(webHook.getToken());
    if (tokenStatus.isValid() && tokenStatus.getRemaining() > 0) {
      remoteOrganization = githubConsumerService.retrieveRemoteOrganization(webHook.getOrganizationId(), webHook.getToken());
    }

    return new WebHookRestEntity(webHook.getId(),
                                 webHook.getWebhookId(),
                                 webHook.getOrganizationId(),
                                 webHook.getTriggers(),
                                 webHook.getEnabled(),
                                 webHook.getWatchedDate(),
                                 webHook.getWatchedBy(),
                                 webHook.getUpdatedDate(),
                                 webHook.getRefreshDate(),
                                 webHook.getOrganizationName(),
                                 remoteOrganization != null ? remoteOrganization.getTitle() : null,
                                 remoteOrganization != null ? remoteOrganization.getDescription() : null,
                                 remoteOrganization != null ? remoteOrganization.getAvatarUrl() : null,
                                 webhookService.isWebHookWatchLimitEnabled(webHook.getOrganizationId()),
                                 tokenStatus);
  }

  public static List<WebHookRestEntity> toRestEntities(WebhookService webhookService, GithubConsumerService githubConsumerService, Collection<WebHook> webHooks) {
    return webHooks.stream().map(webHook -> toRestEntity(webhookService, githubConsumerService, webHook)).toList();
  }
}
