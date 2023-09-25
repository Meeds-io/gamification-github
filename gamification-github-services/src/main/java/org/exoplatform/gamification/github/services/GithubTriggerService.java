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

import org.exoplatform.gamification.github.plugin.GithubTriggerPlugin;

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
   * Handle github trigger asynchronously
   *
   * @param trigger gitHub sent trigger.
   * @param signature The signature received from the external system.
   * @param payload payload The raw payload of the webhook request.
   */
  void handleTriggerAsync(String trigger, String signature, String payload);

  /**
   * Handle github trigger
   * 
   * @param trigger gitHub sent trigger.
   * @param signature The signature received from the external system.
   * @param payload payload The raw payload of the webhook request.
   */
  void handleTrigger(String trigger, String signature, String payload);

  /**
   * Gets list of configured github triggers
   *
   * @return list of configured github triggers
   */
  String[] getTriggers();
}
