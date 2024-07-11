/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import static io.meeds.github.gamification.listener.GithubEventsListener.GAMIFICATION_CANCEL_EVENT;
import static io.meeds.github.gamification.listener.GithubEventsListener.GAMIFICATION_GENERIC_EVENT;
import static io.meeds.github.gamification.utils.Utils.GITHUB_ACTION_EVENT;
import static io.meeds.github.gamification.utils.Utils.GITHUB_CANCEL_ACTION_EVENT;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.ListenerService;

@SpringBootTest(classes = { GithubEventsListener.class, })
class GithubEventsListenerTest {

  @MockBean
  private ListenerService                    listenerService;

  @MockBean
  private Event<Map<String, String>, String> event;

  @Autowired
  private GithubEventsListener               githubEventsListener;

  @Test
  void createEvent() {
    Map<String, String> source = new HashMap<>();
    source.put("objectId", "objectId");
    source.put("objectType", "objectType");
    source.put("ruleTitle", "ruleTitle");
    source.put("senderId", "senderId");
    source.put("receiverId", "receiverId");
    source.put("eventDetails", "eventDetails");

    when(event.getSource()).thenReturn(source);
    when(event.getEventName()).thenReturn(GITHUB_ACTION_EVENT);
    githubEventsListener.onEvent(event);
    verify(listenerService, times(1)).broadcast(GAMIFICATION_GENERIC_EVENT, source, "");

    when(event.getEventName()).thenReturn(GITHUB_CANCEL_ACTION_EVENT);
    githubEventsListener.onEvent(event);
    verify(listenerService, times(1)).broadcast(GAMIFICATION_CANCEL_EVENT, source, "");
  }
}
