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
package org.exoplatform.gamification.github.plugin;

import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;
import static org.exoplatform.gamification.github.utils.Utils.extractSubItem;

public class PushCodeTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public String parseSenderGithubUserId(Map<String, Object> payload) {
    return null;
  }

  @Override
  public String parseReceiverGithubUserId(Map<String, Object> payload) {
    return extractSubItem(payload, PUSHER, NAME);
  }

  @Override
  public String parseGithubObject(Map<String, Object> payload) {
    return extractSubItem(payload, HEAD_COMMIT, URL);
  }

  @Override
  public String getEventName(Map<String, Object> payload) {
    return PUSH_CODE_EVENT_NAME;
  }
}
