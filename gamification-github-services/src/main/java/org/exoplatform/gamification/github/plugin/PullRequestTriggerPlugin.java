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

import org.exoplatform.gamification.github.model.Event;

import java.util.*;

import static org.exoplatform.gamification.github.utils.Utils.*;
import static org.exoplatform.gamification.github.utils.Utils.extractSubItem;

public class PullRequestTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {
    String userId = extractSubItem(payload, SENDER, LOGIN);
    String object = extractSubItem(payload, PULL_REQUEST, HTML_URL);
    if (Objects.equals(extractSubItem(payload, ACTION), OPENED)) {
      return Collections.singletonList(new Event(CREATE_PULL_REQUEST_EVENT_NAME, null, userId, object));
    } else if (Objects.equals(extractSubItem(payload, ACTION), REVIEW_REQUESTED)) {
      return Collections.singletonList(new Event(REQUEST_REVIEW_FOR_PULL_REQUEST_EVENT_NAME, null, userId, object));
    }
    return Collections.emptyList();
  }
}
