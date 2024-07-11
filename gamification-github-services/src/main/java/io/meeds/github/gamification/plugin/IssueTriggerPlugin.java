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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class IssueTriggerPlugin extends GithubTriggerPlugin {

  private static final String  NAME = "issues";

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
    String issueState = Utils.extractSubItem(payload, Utils.ACTION);
    String objectId = Utils.extractSubItem(payload, Utils.ISSUE, Utils.HTML_URL);
    String userId = Utils.extractSubItem(payload, Utils.SENDER, Utils.LOGIN);
    if (Objects.equals(issueState, Utils.OPENED)) {
      return Collections.singletonList(new Event(Utils.CREATE_ISSUE_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 Utils.ISSUE_TYPE,
                                                 Utils.extractSubItem(payload, "organization", "id"),
                                                 Utils.extractSubItem(payload, "repository", "id")));
    } else if (Objects.equals(issueState, Utils.CLOSED)) {
      if (Objects.equals(Utils.extractSubItem(payload, Utils.ISSUE, Utils.STATE_REASON), Utils.NOT_PLANNED)) {
        return Collections.singletonList(new Event(Utils.CLOSE_ISSUE_EVENT_NAME,
                                                   userId,
                                                   userId,
                                                   objectId,
                                                   Utils.ISSUE_TYPE,
                                                   Utils.extractSubItem(payload, "organization", "id"),
                                                   Utils.extractSubItem(payload, "repository", "id")));
      }
      return Collections.emptyList();
    } else if (Objects.equals(issueState, Utils.LABELED)) {
      objectId = objectId + "?label=" + Utils.extractSubItem(payload, Utils.LABEL, Utils.NAME);
      return Collections.singletonList(new Event(Utils.ADD_ISSUE_LABEL_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 Utils.ISSUE_TYPE,
                                                 Utils.extractSubItem(payload, Utils.ORGANIZATION, Utils.ID),
                                                 Utils.extractSubItem(payload, Utils.REPOSITORY, Utils.ID)));
    } else if (Objects.equals(issueState, Utils.UNLABELED)) {
      objectId = objectId + "?label=" + Utils.extractSubItem(payload, Utils.LABEL, Utils.NAME);
      return Collections.singletonList(new Event(Utils.DELETE_ISSUE_LABEL_EVENT_NAME,
                                                 userId,
                                                 userId,
                                                 objectId,
                                                 Utils.ISSUE_TYPE,
                                                 Utils.extractSubItem(payload, Utils.ORGANIZATION, Utils.ID),
                                                 Utils.extractSubItem(payload, Utils.REPOSITORY, Utils.ID)));
    }
    return Collections.emptyList();
  }
}
