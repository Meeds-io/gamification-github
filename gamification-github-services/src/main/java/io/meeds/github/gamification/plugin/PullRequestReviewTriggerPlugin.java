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
package io.meeds.github.gamification.plugin;

import io.meeds.github.gamification.model.Event;
import io.meeds.github.gamification.services.GithubTriggerService;
import io.meeds.github.gamification.utils.Utils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class PullRequestReviewTriggerPlugin extends GithubTriggerPlugin {

  private static final String  NAME = "pull_request_review";

  @Autowired
  private GithubTriggerService githubTriggerService;

  @PostConstruct
  public void init() {
    githubTriggerService.addPlugin(this);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public List<Event> getEvents(Map<String, Object> payload) {
    String pullState = Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.STATE);
    if (pullState != null && pullState.equals(Utils.PULL_REQUEST_COMMENTED)) {
      return Collections.singletonList(new Event(Utils.REVIEW_PULL_REQUEST_EVENT_NAME,
                                                 Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.USER, Utils.LOGIN),
                                                 Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.USER, Utils.LOGIN),
                                                 Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.HTML_URL),
                                                 Utils.PR_TYPE,
                                                 Utils.extractSubItem(payload, Utils.ORGANIZATION, Utils.ID),
                                                 Utils.extractSubItem(payload, Utils.REPOSITORY, Utils.ID)));
    } else if (pullState != null && pullState.equals(Utils.PULL_REQUEST_VALIDATED)) {
      return Arrays.asList(new Event(Utils.PULL_REQUEST_VALIDATED_EVENT_NAME,
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST, Utils.USER, Utils.LOGIN),
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST, Utils.USER, Utils.LOGIN),
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.HTML_URL),
                                     Utils.PR_TYPE,
                                     Utils.extractSubItem(payload, Utils.ORGANIZATION, Utils.ID),
                                     Utils.extractSubItem(payload, Utils.REPOSITORY, Utils.ID)),
                           new Event(Utils.VALIDATE_PULL_REQUEST_EVENT_NAME,
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.USER, Utils.LOGIN),
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.USER, Utils.LOGIN),
                                     Utils.extractSubItem(payload, Utils.PULL_REQUEST_REVIEW, Utils.HTML_URL),
                                     Utils.PR_TYPE,
                                     Utils.extractSubItem(payload, Utils.ORGANIZATION, Utils.ID),
                                     Utils.extractSubItem(payload, Utils.REPOSITORY, Utils.ID)));
    }
    return Collections.emptyList();
  }
}
