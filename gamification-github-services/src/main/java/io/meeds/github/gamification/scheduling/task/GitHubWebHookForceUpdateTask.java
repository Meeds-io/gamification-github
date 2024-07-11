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
package io.meeds.github.gamification.scheduling.task;

import io.meeds.github.gamification.services.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import org.exoplatform.commons.api.persistence.ExoTransactional;

/**
 * A service that will manage the periodic updating of twitter events.
 */
@Component
public class GitHubWebHookForceUpdateTask {

  @Autowired
  private WebhookService webhookService;

  @ExoTransactional
  @Scheduled(cron = "${io.meeds.gamification.GitHubWebHookForceUpdate.expression:0 0 * * * *}")
  public void execute() {
    webhookService.forceUpdateWebhooks();
  }
}
