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
package org.exoplatform.gamification.github.services.impl;

import io.meeds.gamification.service.ConnectorService;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.gamification.github.plugin.GithubTriggerPlugin;
import org.exoplatform.gamification.github.services.GithubTriggerService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.HashMap;
import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;

public class GithubTriggerServiceImpl implements GithubTriggerService {

  private static final Log                       LOG            = ExoLogger.getLogger(GithubTriggerServiceImpl.class);

  private final Map<String, GithubTriggerPlugin> triggerPlugins = new HashMap<>();

  private final ConnectorService                 connectorService;

  private final IdentityManager                  identityManager;

  private final ListenerService                  listenerService;

  public GithubTriggerServiceImpl(ListenerService listenerService,
                                  ConnectorService connectorService,
                                  IdentityManager identityManager) {
    this.listenerService = listenerService;
    this.connectorService = connectorService;
    this.identityManager = identityManager;
  }

  @Override
  public void addPlugin(GithubTriggerPlugin githubTriggerPlugin) {
    triggerPlugins.put(githubTriggerPlugin.getName(), githubTriggerPlugin);
  }

  @Override
  public void removePlugin(String name) {
    triggerPlugins.remove(name);
  }

  @Override
  public void handleTrigger(String payload, String trigger) {
    Map<String, Object> payloadMap = fromJsonStringToMap(payload);
    String receiverGithubUserId = null;
    String senderGithubUserId = null;
    String object = null;
    String ruleTitle = null;
    GithubTriggerPlugin triggerPlugin = getGithubTriggerPlugin(trigger);
    if (triggerPlugin != null) {
      object = triggerPlugin.parseGithubObject(payloadMap);
      receiverGithubUserId = triggerPlugin.parseReceiverGithubUserId(payloadMap);
      senderGithubUserId = triggerPlugin.parseSenderGithubUserId(payloadMap);
      ruleTitle = triggerPlugin.getEventName(payloadMap);
    }
    String receiverId = connectorService.getAssociatedUsername(CONNECTOR_NAME, receiverGithubUserId);
    String senderId;
    if (senderGithubUserId != null && !StringUtils.equals(receiverGithubUserId, senderGithubUserId)) {
      senderId = connectorService.getAssociatedUsername(CONNECTOR_NAME, senderGithubUserId);
    } else {
      senderId = receiverId;
    }
    if (senderId != null && ruleTitle != null) {
      Identity socialIdentity = identityManager.getOrCreateUserIdentity(senderId);
      if (socialIdentity != null) {
        broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
      }
    }
  }

  public String[] getTriggers() {
    return triggerPlugins.values()
                         .stream()
                         .map(GithubTriggerPlugin::getName)
                         .toArray(String[]::new);
  }

  private void broadcastGithubEvent(String ruleTitle, String senderId, String receiverId, String object) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", ruleTitle);
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("object", object);
      listenerService.broadcast("exo.github.event", gam, "");
      LOG.info("Github action {} broadcasted for user {}", ruleTitle, senderId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast github event", e);
    }
  }

  private GithubTriggerPlugin getGithubTriggerPlugin(String trigger) {
    return triggerPlugins.get(trigger);
  }
}
