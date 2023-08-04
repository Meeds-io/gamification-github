/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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
package org.exoplatform.gamification.github.services;

import org.picocontainer.Startable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A service that will manage the periodic updating of github webhooks, ensuring
 * that webhooks data remains current with external sources.
 */
public class WebhookUpdateService implements Startable {

  ScheduledExecutorService     scheduler = Executors.newScheduledThreadPool(1);

  private final WebhookService webhookService;

  public WebhookUpdateService(WebhookService webhookService) {
    this.webhookService = webhookService;
  }

  @Override
  public void start() {
    scheduler.scheduleAtFixedRate(webhookService::forceUpdateWebhooks, 0, 1, TimeUnit.HOURS);
  }

  @Override
  public void stop() {
    scheduler.shutdown();
  }
}
