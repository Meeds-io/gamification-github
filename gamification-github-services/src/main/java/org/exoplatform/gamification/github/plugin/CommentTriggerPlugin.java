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

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.gamification.github.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;
import static org.exoplatform.gamification.github.utils.Utils.extractSubItem;

public class CommentTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {
    String pullRequest = extractSubItem(payload, ISSUE, PULL_REQUEST);
    String userId = extractSubItem(payload, SENDER, LOGIN);
    if (StringUtils.isNotBlank(pullRequest)) {
      return Collections.singletonList(new Event(COMMENT_PULL_REQUEST_EVENT_NAME, null, userId, pullRequest));
    } else {
      return Collections.singletonList(new Event(COMMENT_ISSUE_EVENT_NAME,
                                                 null,
                                                 userId,
                                                 extractSubItem(payload, ISSUE, HTML_URL)));
    }
  }
}
