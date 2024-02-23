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
package io.meeds.gamification.github.plugin;

import io.meeds.gamification.github.model.Event;
import static io.meeds.gamification.github.utils.Utils.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CommentTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {

    String pullRequest = extractSubItem(payload, ISSUE, PULL_REQUEST);
    String action = extractSubItem(payload, ACTION);
    String comment = extractSubItem(payload, COMMENT, HTML_URL);
    String userId = extractSubItem(payload, SENDER, LOGIN);
    String eventName;
    String eventType = StringUtils.isBlank(pullRequest) ? COMMENT_ISSUE_TYPE : COMMENT_PR_TYPE;
    if (action != null) {
      switch (action) {
      case CREATED:
        eventName = StringUtils.isBlank(pullRequest) ? COMMENT_ISSUE_EVENT_NAME : COMMENT_PULL_REQUEST_EVENT_NAME;
        break;
      case DELETED:
        eventName = StringUtils.isBlank(pullRequest) ? DELETE_ISSUE_COMMENT_EVENT_NAME : DELETE_PULL_REQUEST_COMMENT_EVENT_NAME;
        break;
      default:
        return Collections.emptyList();
      }
      return Collections.singletonList(new Event(eventName,
                                                 null,
                                                 userId,
                                                 comment,
                                                 eventType,
                                                 extractSubItem(payload, ORGANIZATION, ID),
                                                 extractSubItem(payload, REPOSITORY, ID)));
    }
    return Collections.emptyList();
  }
}
