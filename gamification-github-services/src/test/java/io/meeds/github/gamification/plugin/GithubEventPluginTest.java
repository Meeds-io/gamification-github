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
package io.meeds.github.gamification.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.meeds.github.gamification.plugin.GithubEventPlugin.EVENT_TYPE;
import static io.meeds.github.gamification.utils.Utils.*;
import static io.meeds.github.gamification.utils.Utils.COMMENT_ISSUE_EVENT_NAME;
import static org.junit.Assert.*;

@SpringBootTest(classes = { GithubEventPlugin.class, })
public class GithubEventPluginTest {

  @Test
  public void testIsValidEvent() {
    GithubEventPlugin githubEventPlugin = new GithubEventPlugin();
    assertEquals(EVENT_TYPE, githubEventPlugin.getEventType());
    assertEquals(List.of(ADD_ISSUE_LABEL_EVENT_NAME,
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
                         COMMENT_ISSUE_EVENT_NAME),
                 githubEventPlugin.getTriggers());

    Map<String, String> eventProperties = new HashMap<>();
    eventProperties.put(ORGANIZATION_ID, "132452");
    eventProperties.put(REPOSITORY_IDS, "1115454,2225454");
    assertFalse(githubEventPlugin.isValidEvent(eventProperties,
                                               "{" + ORGANIZATION_ID + ": " + 132452 + ", " + REPOSITORY_ID + ": " + "221545"
                                                   + "}"));

    assertFalse(githubEventPlugin.isValidEvent(eventProperties,
                                               "{" + ORGANIZATION_ID + ": " + 132453 + ", " + REPOSITORY_ID + ": " + "221545"
                                                   + "}"));
    assertTrue(githubEventPlugin.isValidEvent(eventProperties,
                                              "{" + ORGANIZATION_ID + ": " + 132452 + ", " + REPOSITORY_ID + ": " + "2225454"
                                                  + "}"));
    assertTrue(githubEventPlugin.isValidEvent(eventProperties,
                                              "{" + ORGANIZATION_ID + ": " + 132452 + ", " + REPOSITORY_ID + ": " + "1115454"
                                                  + "}"));

  }
}
