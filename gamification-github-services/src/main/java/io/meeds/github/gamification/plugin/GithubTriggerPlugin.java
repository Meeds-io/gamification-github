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
package io.meeds.github.gamification.plugin;

import io.meeds.github.gamification.model.Event;
import io.meeds.github.gamification.services.WebhookService;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * A plugin that will be used by {@link WebhookService} to handle github
 * triggers
 */
@Component
public abstract class GithubTriggerPlugin extends BaseComponentPlugin {

  /**
   * Gets List of triggered events
   *
   * @param payload payload The raw payload of the webhook request.
   * @return List of triggered events
   */
  public abstract List<Event> getEvents(Map<String, Object> payload);

}
