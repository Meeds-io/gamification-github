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
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.rest.model.WebHookRestEntity;
import org.exoplatform.gamification.github.services.WebhookService;

public class WebHookBuilder {

  private WebHookBuilder() {
    // Class with static methods
  }

  public static WebHookRestEntity toRestEntity(WebhookService webhookService, WebHook webHook) {
    if (webHook == null) {
      return null;
    }

    RemoteOrganization remoteOrganization =
                                          webhookService.retrieveRemoteOrganization(webHook.getOrganizationId(),
                                                                                    webhookService.getHookAccessToken(webHook.getOrganizationId()));

    return new WebHookRestEntity(webHook.getId(),
                                 webHook.getWebhookId(),
                                 webHook.getOrganizationId(),
                                 webHook.getEvent(),
                                 webHook.getEnabled(),
                                 webHook.getWatchedDate(),
                                 webHook.getWatchedBy(),
                                 webHook.getUpdatedDate(),
                                 webHook.getRefreshDate(),
                                 remoteOrganization.getName(),
                                 remoteOrganization.getTitle(),
                                 remoteOrganization.getDescription(),
                                 remoteOrganization.getAvatarUrl());
  }

  public static List<WebHookRestEntity> toRestEntities(WebhookService webhookService, Collection<WebHook> webHooks) {
    return webHooks.stream().map(webHook -> toRestEntity(webhookService, webHook)).toList();
  }
}
