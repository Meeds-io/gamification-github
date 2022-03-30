/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.gamification.github.listener;

import java.util.Map;

import org.exoplatform.gamification.github.services.GithubHooksManagement;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;

public class GithubEventsListener extends Listener<Map<String, String>, String> {

  private GithubHooksManagement githubHooksManagement;

  public GithubEventsListener(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @Override
  public void onEvent(Event<Map<String, String>, String> event) throws Exception {
    String ruleTitle = event.getSource().get("ruleTitle");
    String senderId = event.getSource().get("senderId");
    String receiverId = event.getSource().get("receiverId");
    String object = event.getSource().get("object");
    githubHooksManagement.createGamificationHistory(ruleTitle, senderId, receiverId, object);
  }
}
