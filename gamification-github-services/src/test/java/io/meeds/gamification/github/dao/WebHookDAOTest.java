/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
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
package io.meeds.gamification.github.dao;

import io.meeds.gamification.github.BaseGithubTest;
import io.meeds.gamification.github.entity.WebhookEntity;
import org.junit.Test;

import java.util.Date;
import java.util.List;

public class WebHookDAOTest extends BaseGithubTest {

  @Test
  public void testCreateWebhook() {
    WebhookEntity webhookEntity = new WebhookEntity();
    webhookEntity.setWebhookId(112448L);
    webhookEntity.setOrganizationId(555452L);
    webhookEntity.setOrganizationName("organizationName");
    webhookEntity.setSecret("secret");
    webhookEntity.setToken("token");
    webhookEntity.setTriggers(List.of("trigger1", "trigger2"));
    webhookEntity.setUpdatedDate(new Date());
    webhookEntity.setWatchedDate(new Date());
    webhookEntity.setWatchedBy(1L);
    webhookEntity.setRefreshDate(new Date());
    webhookEntity.setEnabled(true);

    // When
    webhookEntity = webHookDAO.create(webhookEntity);

    // Then
    assertNotNull(webhookEntity.getId());
    assertEquals("organizationName", webhookEntity.getOrganizationName());
    assertEquals("token", webhookEntity.getToken());
    assertEquals("secret", webhookEntity.getSecret());
    assertNotNull(webhookEntity.getOrganizationId());
    assertNotNull(webhookEntity.getWebhookId());

    assertEquals(webhookEntity, webHookDAO.getWebhookByOrganizationId(555452L));
    assertNotNull(webHookDAO.getWebhookIds(0, 10));
  }

  @Test
  public void testDeleteWebhook() {
    WebhookEntity webhookEntity = new WebhookEntity();

    webhookEntity.setWebhookId(112448L);
    webhookEntity.setOrganizationId(555452L);
    webhookEntity.setOrganizationName("organizationName");
    webhookEntity.setSecret("secret");
    webhookEntity.setToken("token");
    webhookEntity.setTriggers(List.of("trigger1", "trigger2"));
    webhookEntity.setUpdatedDate(new Date());
    webhookEntity.setWatchedDate(new Date());
    webhookEntity.setWatchedBy(1L);
    webhookEntity.setRefreshDate(new Date());
    webhookEntity.setEnabled(true);

    webhookEntity = webHookDAO.create(webhookEntity);

    assertNotNull(webhookEntity.getId());

    webHookDAO.delete(webhookEntity);

    webhookEntity = webHookDAO.find(webhookEntity.getId());
    assertNull(webhookEntity);
  }
}
