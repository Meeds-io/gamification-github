/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association
 * contact@meeds.io
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
 */
package org.exoplatform.gamification.github.storage.mapper;

import io.meeds.gamification.utils.Utils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.github.entity.WebhookEntity;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.social.core.manager.IdentityManager;

public class WebHookMapper {

  private WebHookMapper() {
    // Class with static methods
  }

  public static WebhookEntity toEntity(WebHook webHook) {
    if (webHook == null) {
      return null;
    }
    WebhookEntity webhookEntity = new WebhookEntity();
    if (webHook.getOrganizationId() > 0) {
      webhookEntity.setOrganizationId(webHook.getOrganizationId());
    }
    if (webHook.getWebhookId() > 0) {
      webhookEntity.setWebhookId(webHook.getWebhookId());
    }
    if (webHook.getWatchedBy() != null) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      String userIdentityId = identityManager.getOrCreateUserIdentity(webHook.getWatchedBy()).getId();
      webhookEntity.setWatchedBy(Long.parseLong(userIdentityId));
    }
    return webhookEntity;
  }

  public static WebHook fromEntity(WebhookEntity webhookEntity) {
    if (webhookEntity == null) {
      return null;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String watchedBy = identityManager.getIdentity(String.valueOf(webhookEntity.getWatchedBy())).getRemoteId();
    return new WebHook(webhookEntity.getWebhookId(),
                       webhookEntity.getOrganizationId(),
                       webhookEntity.getEnabled(),
                       webhookEntity.getWatchedDate() != null ? Utils.toSimpleDateFormat(webhookEntity.getWatchedDate()) : null,
                       watchedBy,
                       webhookEntity.getUpdatedDate() != null ? Utils.toSimpleDateFormat(webhookEntity.getUpdatedDate()) : null);
  }

}
