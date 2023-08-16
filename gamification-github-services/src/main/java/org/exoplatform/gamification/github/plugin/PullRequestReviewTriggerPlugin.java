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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;
import static org.exoplatform.gamification.github.utils.Utils.extractSubItem;

public class PullRequestReviewTriggerPlugin extends GithubTriggerPlugin {

  @Override
  public String parseSenderGithubUserId(Map<String, Object> payload) {
    return extractSubItem(payload, PULL_REQUEST_REVIEW, USER, LOGIN);
  }

  @Override
  public String parseReceiverGithubUserId(Map<String, Object> payload) {
    String pullState = extractSubItem(payload, PULL_REQUEST_REVIEW, STATE);
    if (pullState != null && pullState.equals(PULL_REQUEST_COMMENTED)) {
      return parseSenderGithubUserId(payload);
    } else if (pullState != null && pullState.equals(PULL_REQUEST_VALIDATED)) {
      return extractSubItem(payload, PULL_REQUEST, USER, LOGIN);
    }
    return null;
  }

  @Override
  public String parseGithubObject(Map<String, Object> payload) {
    return extractSubItem(payload, PULL_REQUEST_REVIEW, HTML_URL);
  }

  @Override
  public String getEventName(Map<String, Object> payload) {
    String pullState = extractSubItem(payload, PULL_REQUEST_REVIEW, PULL_REQUEST_REVIEW_STATE);
    if (pullState != null && pullState.equals(PULL_REQUEST_COMMENTED)) {
      return REVIEW_PULL_REQUEST_EVENT_NAME;
    } else if (pullState != null && pullState.equals(PULL_REQUEST_VALIDATED)) {
      return PULL_REQUEST_VALIDATED_EVENT_NAME;
    }
    return null;
  }

  @Override
  public List<String> getEvents() {
    return new ArrayList<>(List.of(REVIEW_PULL_REQUEST_EVENT_NAME, PULL_REQUEST_VALIDATED_EVENT_NAME));
  }
}
