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
package org.exoplatform.gamification.github.services;

import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.plugin.GithubTriggerPlugin;
import org.exoplatform.gamification.github.model.Event;
import java.util.List;

public interface GithubTriggerService {

  /**
   * Add a new {@link GithubTriggerPlugin} for a given github trigger name
   *
   * @param githubTriggerPlugin {@link GithubTriggerPlugin}
   */
  void addPlugin(GithubTriggerPlugin githubTriggerPlugin);

  /**
   * Removes a {@link GithubTriggerPlugin} identified by its trigger name
   *
   * @param triggerName trigger name
   */
  void removePlugin(String triggerName);

  /**
   * To handle the event sent by github.
   *
   * @param payload payload The raw payload of the webhook request.
   * @param event gitHub sent event.
   */
  void handleTrigger(String payload, String event);

  /**
   * Gets list of configured github triggers
   *
   * @return list of configured github triggers
   */
  String[] getTriggers();

  /**
   * Gets events that can be reached form all triggers
   * 
   * @param webHook webHook
   * @return {@link List} of {@link Event} of events
   */
  List<Event> getEvents(WebHook webHook);

  /**
   * Check if webhook event is enabled
   *
   * @param organizationId github remote organization id
   * @param event event name
   * @return true if the webhook event is enabled, else false.
   */
  boolean isEventEnabled(long organizationId, String event);

  /**
   * Enables/disables webhook event
   *
   * @param organizationId github remote organization id
   * @param event event name
   * @param enabled true to enabled, else false
   * @param currentUser user name attempting to enables/disables event.
   * @throws IllegalAccessException when user is not authorized enables/disables
   *           webhook event
   */
  void setEventEnabled(long organizationId, String event, boolean enabled, String currentUser) throws IllegalAccessException;
}
