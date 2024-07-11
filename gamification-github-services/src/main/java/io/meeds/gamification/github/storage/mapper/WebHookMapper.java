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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.github.storage.mapper;

import io.meeds.gamification.github.model.WebHook;
import io.meeds.gamification.utils.Utils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import io.meeds.gamification.github.entity.WebhookEntity;
import org.exoplatform.social.core.manager.IdentityManager;

import static io.meeds.gamification.github.utils.Utils.*;

public class WebHookMapper {

  private WebHookMapper() {
    // Class with static methods
  }

  public static WebhookEntity toEntity(WebHook webHook) {
    if (webHook == null) {
      return null;
    }
    WebhookEntity webhookEntity = new WebhookEntity();

    if (webHook.getId() > 0) {
      webhookEntity.setId(webHook.getId());
    }
    if (webHook.getOrganizationId() > 0) {
      webhookEntity.setOrganizationId(webHook.getOrganizationId());
    }
    if (StringUtils.isNotEmpty(webHook.getOrganizationName())) {
      webhookEntity.setOrganizationName(webHook.getOrganizationName());
    }
    if (webHook.getWebhookId() > 0) {
      webhookEntity.setWebhookId(webHook.getWebhookId());
    }
    if (webHook.getWatchedBy() != null) {
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      String userIdentityId = identityManager.getOrCreateUserIdentity(webHook.getWatchedBy()).getId();
      webhookEntity.setWatchedBy(Long.parseLong(userIdentityId));
    }
    if (CollectionUtils.isNotEmpty(webHook.getTriggers())) {
      webhookEntity.setTriggers(webHook.getTriggers());
    }
    if (StringUtils.isNotEmpty(webHook.getToken())) {
      webhookEntity.setToken(encode(webHook.getToken()));
    }
    if (StringUtils.isNotEmpty(webHook.getSecret())) {
      webhookEntity.setSecret(encode(webHook.getSecret()));
    }
    return webhookEntity;
  }

  public static WebHook fromEntity(WebhookEntity webhookEntity) {
    if (webhookEntity == null) {
      return null;
    }
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    String watchedBy = identityManager.getIdentity(String.valueOf(webhookEntity.getWatchedBy())).getRemoteId();
    return new WebHook(webhookEntity.getId(),
                       webhookEntity.getWebhookId(),
                       webhookEntity.getOrganizationId(),
                       webhookEntity.getOrganizationName(),
                       webhookEntity.getTriggers(),
                       webhookEntity.getEnabled(),
                       webhookEntity.getWatchedDate() != null ? Utils.toSimpleDateFormat(webhookEntity.getWatchedDate()) : null,
                       watchedBy,
                       webhookEntity.getUpdatedDate() != null ? Utils.toSimpleDateFormat(webhookEntity.getUpdatedDate()) : null,
                       webhookEntity.getRefreshDate() != null ? Utils.toSimpleDateFormat(webhookEntity.getRefreshDate()) : null,
                       decode(webhookEntity.getToken()),
                       decode(webhookEntity.getSecret()));
  }

}
