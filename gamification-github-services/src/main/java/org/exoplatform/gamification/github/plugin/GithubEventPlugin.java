/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
package org.exoplatform.gamification.github.plugin;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.plugin.EventPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;

public class GithubEventPlugin extends EventPlugin {

  public static final String EVENT_TYPE = "github";

  public GithubEventPlugin() {
  }

  @Override
  public String getEventType() {
    return EVENT_TYPE;
  }

  public List<String> getTriggers() {
    return List.of(ADD_ISSUE_LABEL_EVENT_NAME,
                   PUSH_CODE_EVENT_NAME,
                   PULL_REQUEST_VALIDATED_EVENT_NAME,
                   REVIEW_PULL_REQUEST_EVENT_NAME,
                   VALIDATE_PULL_REQUEST_EVENT_NAME,
                   CREATE_PULL_REQUEST_EVENT_NAME,
                   PULL_REQUEST_REVIEW_COMMENT_EVENT_NAME,
                   COMMENT_PULL_REQUEST_EVENT_NAME,
                   CREATE_ISSUE_EVENT_NAME,
                   ADD_ISSUE_LABEL_EVENT_NAME,
                   REQUEST_REVIEW_FOR_PULL_REQUEST_EVENT_NAME,
                   COMMENT_ISSUE_EVENT_NAME);
  }

  @Override
  public boolean isValidEvent(EventDTO eventDTO, String triggerDetails) {
    Map<String, String> properties = eventDTO.getProperties();
    if (properties.isEmpty()) {
      return true;
    } else {
      String desiredOrganizationId = eventDTO.getProperties().get("organizationId");
      String desiredRepositoryId = eventDTO.getProperties().get("repositoryId");
      Map<String, String> triggerDetailsMop = stringToMap(triggerDetails);
      return desiredOrganizationId.equals(triggerDetailsMop.get("organizationId"))
          && (desiredRepositoryId.equals("any") || desiredRepositoryId.equals(triggerDetailsMop.get("repositoryId")));
    }
  }

  private static Map<String, String> stringToMap(String mapAsString) {
    Map<String, String> map = new HashMap<>();
    mapAsString = mapAsString.substring(1, mapAsString.length() - 1);
    String[] pairs = mapAsString.split(", ");
    for (String pair : pairs) {
      String[] keyValue = pair.split(": ");
      String key = keyValue[0].trim();
      String value = keyValue[1].trim();
      map.put(key, value);
    }
    return map;
  }
}
