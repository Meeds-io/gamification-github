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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PullRequestReviewTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {
    String pullState = extractSubItem(payload, PULL_REQUEST_REVIEW, STATE);
    if (pullState != null && pullState.equals(PULL_REQUEST_COMMENTED)) {
      return Collections.singletonList(new Event(REVIEW_PULL_REQUEST_EVENT_NAME,
                                                 extractSubItem(payload, PULL_REQUEST_REVIEW, USER, LOGIN),
                                                 extractSubItem(payload, PULL_REQUEST_REVIEW, USER, LOGIN),
                                                 extractSubItem(payload, PULL_REQUEST_REVIEW, HTML_URL),
                                                 PR_TYPE,
                                                 extractSubItem(payload, ORGANIZATION, ID),
                                                 extractSubItem(payload, REPOSITORY, ID)));
    } else if (pullState != null && pullState.equals(PULL_REQUEST_VALIDATED)) {
      return Arrays.asList(new Event(PULL_REQUEST_VALIDATED_EVENT_NAME,
                                     extractSubItem(payload, PULL_REQUEST, USER, LOGIN),
                                     extractSubItem(payload, PULL_REQUEST, USER, LOGIN),
                                     extractSubItem(payload, PULL_REQUEST_REVIEW, HTML_URL),
                                     PR_TYPE,
                                     extractSubItem(payload, ORGANIZATION, ID),
                                     extractSubItem(payload, REPOSITORY, ID)),
                           new Event(VALIDATE_PULL_REQUEST_EVENT_NAME,
                                     extractSubItem(payload, PULL_REQUEST_REVIEW, USER, LOGIN),
                                     extractSubItem(payload, PULL_REQUEST_REVIEW, USER, LOGIN),
                                     extractSubItem(payload, PULL_REQUEST_REVIEW, HTML_URL),
                                     PR_TYPE,
                                     extractSubItem(payload, ORGANIZATION, ID),
                                     extractSubItem(payload, REPOSITORY, ID)));
    }
    return Collections.emptyList();
  }
}
