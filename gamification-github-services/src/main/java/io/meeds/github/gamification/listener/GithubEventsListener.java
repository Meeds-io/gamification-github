/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2022 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.github.gamification.listener;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.listener.ListenerService;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import static io.meeds.github.gamification.utils.Utils.GITHUB_ACTION_EVENT;
import static io.meeds.github.gamification.utils.Utils.GITHUB_CANCEL_ACTION_EVENT;

@Component
public class GithubEventsListener extends Listener<Map<String, String>, String> {

  private static final String[] LISTENER_EVENTS            = { "github.action.event", "github.cancel.action.event" };

  public static final String    GAMIFICATION_GENERIC_EVENT = "exo.gamification.generic.action";

  public static final String    GAMIFICATION_CANCEL_EVENT  = "gamification.cancel.event.action";

  @Autowired
  private ListenerService       listenerService;

  @PostConstruct
  public void init() {
    for (String eventName : LISTENER_EVENTS) {
      listenerService.addListener(eventName, this);
    }
  }

  @Override
  public void onEvent(Event<Map<String, String>, String> event) {
    Map<String, String> gam = new HashMap<>();
    gam.put("objectId", event.getSource().get("objectId"));
    gam.put("objectType", event.getSource().get("objectType"));
    gam.put("ruleTitle", event.getSource().get("ruleTitle"));
    gam.put("senderId", event.getSource().get("senderId"));
    gam.put("receiverId", event.getSource().get("receiverId"));
    gam.put("eventDetails", event.getSource().get("eventDetails"));

    listenerService.broadcast(getGamificationEventName(event.getEventName()), gam, "");
  }

  private String getGamificationEventName(String eventName) {
    return switch (eventName) {
    case GITHUB_ACTION_EVENT -> GAMIFICATION_GENERIC_EVENT;
    case GITHUB_CANCEL_ACTION_EVENT -> GAMIFICATION_CANCEL_EVENT;
    default -> throw new IllegalArgumentException("Unexpected listener event name: " + eventName);
    };
  }
}
