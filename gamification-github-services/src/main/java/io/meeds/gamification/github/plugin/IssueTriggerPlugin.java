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
package io.meeds.gamification.github.plugin;

import io.meeds.gamification.github.model.Event;
import static io.meeds.gamification.github.utils.Utils.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IssueTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {
    String issueState = extractSubItem(payload, ACTION);
    String objectId = extractSubItem(payload, ISSUE, HTML_URL);
    String userId = extractSubItem(payload, SENDER, LOGIN);
    if (Objects.equals(issueState, OPENED)) {
      return Collections.singletonList(new Event(CREATE_ISSUE_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 ISSUE_TYPE,
                                                 extractSubItem(payload, "organization", "id"),
                                                 extractSubItem(payload, "repository", "id")));
    } else if (Objects.equals(issueState, CLOSED)) {
      if (Objects.equals(extractSubItem(payload, ISSUE, STATE_REASON), NOT_PLANNED)) {
        return Collections.singletonList(new Event(CLOSE_ISSUE_EVENT_NAME,
                                                   userId,
                                                   userId,
                                                   objectId,
                                                   ISSUE_TYPE,
                                                   extractSubItem(payload, "organization", "id"),
                                                   extractSubItem(payload, "repository", "id")));
      }
      return Collections.emptyList();
    } else if (Objects.equals(issueState, LABELED)) {
      objectId = objectId + "?label=" + extractSubItem(payload, LABEL, NAME);
      return Collections.singletonList(new Event(ADD_ISSUE_LABEL_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 ISSUE_TYPE,
                                                 extractSubItem(payload, ORGANIZATION, ID),
                                                 extractSubItem(payload, REPOSITORY, ID)));
    } else if (Objects.equals(issueState, UNLABELED)) {
      objectId = objectId + "?label=" + extractSubItem(payload, LABEL, NAME);
      return Collections.singletonList(new Event(DELETE_ISSUE_LABEL_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 ISSUE_TYPE,
                                                 extractSubItem(payload, ORGANIZATION, ID),
                                                 extractSubItem(payload, REPOSITORY, ID)));
    }
    return Collections.emptyList();
  }
}
