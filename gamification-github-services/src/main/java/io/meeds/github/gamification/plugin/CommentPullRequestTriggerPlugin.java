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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.meeds.github.gamification.utils.Utils.*;

import java.util.*;

@Component
public class CommentPullRequestTriggerPlugin extends GithubTriggerPlugin {

  private static final String  NAME = "pull_request_review_comment";

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
    return Collections.singletonList(new Event(PULL_REQUEST_REVIEW_COMMENT_EVENT_NAME,
                                               null,
                                               extractSubItem(payload, COMMENT, USER, LOGIN),
                                               extractSubItem(payload, COMMENT, LINKS, HTML, HREF),
                                               REVIEW_COMMENT_TYPE,
                                               extractSubItem(payload, ORGANIZATION, ID),
                                               extractSubItem(payload, REPOSITORY, ID)));
  }

}
