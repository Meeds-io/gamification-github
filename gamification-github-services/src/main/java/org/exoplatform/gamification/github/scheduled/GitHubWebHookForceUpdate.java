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
package org.exoplatform.gamification.github.scheduled;

import org.exoplatform.gamification.github.services.WebhookService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;

/**
 * A service that will manage the periodic updating of github webhooks, ensuring
 * that webhooks data remains current with external sources.
 */
@DisallowConcurrentExecution
public class GitHubWebHookForceUpdate implements Job {

  private final WebhookService webhookService;

  public GitHubWebHookForceUpdate() {
    this.webhookService = ExoContainerContext.getService(WebhookService.class);
  }

  @Override
  @ExoTransactional
  public void execute(JobExecutionContext context) {
    webhookService.forceUpdateWebhooks();
  }
}
