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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.gamification.github.storage;

import java.util.Date;
import java.util.List;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.gamification.github.dao.WebHookDAO;
import org.exoplatform.gamification.github.entity.WebhookEntity;
import org.exoplatform.gamification.github.model.WebHook;

import static org.exoplatform.gamification.github.storage.mapper.WebHookMapper.fromEntity;
import static org.exoplatform.gamification.github.storage.mapper.WebHookMapper.toEntity;

public class WebHookStorage {

  private final WebHookDAO webHookDAO;

  public WebHookStorage(WebHookDAO gitHubHookDAO) {
    this.webHookDAO = gitHubHookDAO;
  }

  public WebHook saveWebHook(WebHook webHook) throws ObjectAlreadyExistsException {
    WebHook existsWebHook = getWebhookByOrganizationId(webHook.getOrganizationId());
    if (existsWebHook == null) {
      WebhookEntity webhookEntity = toEntity(webHook);
      webhookEntity.setWatchedDate(new Date());
      webhookEntity.setUpdatedDate(new Date());
      webhookEntity.setRefreshDate(new Date());
      webhookEntity.setEnabled(true);
      webhookEntity = webHookDAO.create(webhookEntity);
      return fromEntity(webhookEntity);
    } else {
      throw new ObjectAlreadyExistsException(existsWebHook);
    }
  }

  public WebHook updateWebHook(WebHook webHook, boolean forceUpdate) {
    WebhookEntity webhookEntity = webHookDAO.find(webHook.getId());
    if (forceUpdate) {
      webhookEntity.setRefreshDate(new Date());
      webhookEntity.setEvents(webHook.getEvent());
    }
    webhookEntity.setUpdatedDate(new Date());
    return fromEntity(webHookDAO.update(webhookEntity));
  }

  public WebHook updateWebHookAccessToken(long webhookId, String accessToken) {
    WebhookEntity webhookEntity = webHookDAO.find(webhookId);
    webhookEntity.setToken(accessToken);
    return fromEntity(webHookDAO.update(webhookEntity));
  }

  public WebHook getWebHookById(Long id) {
    return fromEntity(webHookDAO.find(id));
  }

  public List<Long> getWebhookIds(int offset, int limit) {
    return webHookDAO.getWebhookIds(offset, limit);
  }

  public int countWebhooks() {
    return webHookDAO.count().intValue();
  }

  public WebHook getWebhookByOrganizationId(long organizationId) {
    WebhookEntity connectorHookEntity = webHookDAO.getWebhookByOrganizationId(organizationId);
    return fromEntity(connectorHookEntity);
  }

  public WebHook deleteWebHook(long organizationId) {
    WebhookEntity webhookEntity = webHookDAO.getWebhookByOrganizationId(organizationId);
    if (webhookEntity != null) {
      webHookDAO.delete(webhookEntity);
    }
    return fromEntity(webhookEntity);
  }
}
