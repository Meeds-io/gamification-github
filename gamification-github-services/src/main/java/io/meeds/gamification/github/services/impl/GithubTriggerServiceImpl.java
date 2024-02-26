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
package io.meeds.gamification.github.services.impl;

import io.meeds.gamification.github.model.Event;
import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;
import io.meeds.gamification.service.TriggerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;
import io.meeds.gamification.github.plugin.GithubTriggerPlugin;
import io.meeds.gamification.github.services.GithubTriggerService;
import io.meeds.gamification.github.services.WebhookService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.picocontainer.Startable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.meeds.gamification.github.utils.Utils.*;

public class GithubTriggerServiceImpl implements GithubTriggerService, Startable {

  private static final Log                       LOG            = ExoLogger.getLogger(GithubTriggerServiceImpl.class);

  private final Map<String, GithubTriggerPlugin> triggerPlugins = new HashMap<>();

  private final ConnectorService                 connectorService;

  private final EventService                     eventService;

  private final TriggerService                   triggerService;

  private WebhookService                         webhookService;

  private final IdentityManager                  identityManager;

  private final ListenerService                  listenerService;

  private ExecutorService                        executorService;

  public GithubTriggerServiceImpl(ListenerService listenerService,
                                  ConnectorService connectorService,
                                  IdentityManager identityManager,
                                  EventService eventService,
                                  TriggerService triggerService) {
    this.listenerService = listenerService;
    this.connectorService = connectorService;
    this.identityManager = identityManager;
    this.eventService = eventService;
    this.triggerService = triggerService;
  }

  @Override
  public void start() {
    QueuedThreadPool threadFactory = new QueuedThreadPool(5, 1, 1);
    threadFactory.setName("Gamification - Github connector");
    executorService = Executors.newCachedThreadPool(threadFactory);
  }

  @Override
  public void stop() {
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  @Override
  public void addPlugin(GithubTriggerPlugin githubTriggerPlugin) {
    triggerPlugins.put(githubTriggerPlugin.getName(), githubTriggerPlugin);
  }

  @Override
  public void removePlugin(String name) {
    triggerPlugins.remove(name);
  }

  public void handleTriggerAsync(String trigger, String signature, String payload) {
    executorService.execute(() -> handleTriggerAsyncInternal(trigger, signature, payload));
  }

  @ExoTransactional
  public void handleTriggerAsyncInternal(String trigger, String signature, String payload) {
    handleTrigger(trigger, signature, payload);
  }

  @Override
  public void handleTrigger(String trigger, String signature, String payload) {
    if (!getWebhookService().verifyWebhookSecret(payload, signature)
        || !getWebhookService().isWebHookRepositoryEnabled(payload)) {
      return;
    }
    Map<String, Object> payloadMap = fromJsonStringToMap(payload);
    String organizationId = extractSubItem(payloadMap, "organization", ID);
    List<Event> events = new ArrayList<>();
    GithubTriggerPlugin triggerPlugin = getGithubTriggerPlugin(trigger);
    if (triggerPlugin != null) {
      events = triggerPlugin.getEvents(payloadMap);
    }
    processEvents(events, organizationId);
  }

  private void processEvents(List<Event> events, String organizationId) {
    events.stream().filter(event -> isTriggerEnabled(event.getName(), organizationId)).forEach(this::processEvent);
  }

  private boolean isTriggerEnabled(String trigger, String organizationId) {
    return triggerService.isTriggerEnabledForAccount(trigger, Long.parseLong(organizationId));
  }

  private void processEvent(Event event) {
    String receiverId = connectorService.getAssociatedUsername(CONNECTOR_NAME, event.getReceiver());
    String senderId;
    if (event.getSender() != null && !StringUtils.equals(event.getReceiver(), event.getSender())) {
      senderId = connectorService.getAssociatedUsername(CONNECTOR_NAME, event.getSender());
    } else {
      senderId = receiverId;
    }
    if (StringUtils.isNotBlank(senderId)) {
      Identity socialIdentity = identityManager.getOrCreateUserIdentity(senderId);
      if (socialIdentity != null) {
        broadcastGithubEvent(event, senderId, receiverId);
      }
    }
  }

  private void broadcastGithubEvent(Event event, String senderId, String receiverId) {
    try {
      String eventDetails = "{" + "organizationId" + ": " + event.getOrganizationId() + ", " + "repositoryId" + ": "
          + event.getRepositoryId() + "}";
      Map<String, String> gam = new HashMap<>();
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("objectId", event.getObjectId());
      gam.put("objectType", event.getObjectType());
      gam.put("eventDetails", eventDetails);
      List<EventDTO> eventDTOList = eventService.getEventsByTitle(event.getName(), 0, -1);
      if (CollectionUtils.isNotEmpty(eventDTOList)) {
        gam.put("ruleTitle", event.getName());
        listenerService.broadcast(GITHUB_ACTION_EVENT, gam, "");
      } else {
        List<EventDTO> eventsToCancel = eventService.getEventsByCancellerTrigger(CONNECTOR_NAME, event.getName(), 0, -1);
        if (CollectionUtils.isNotEmpty(eventsToCancel)) {
          for (EventDTO eventToCancel : eventsToCancel) {
            gam.put("ruleTitle", eventToCancel.getTitle());
            listenerService.broadcast(GITHUB_CANCEL_ACTION_EVENT, gam, "");
          }
        }
      }
      LOG.info("Github action {} broadcasted for user {}", event.getName(), senderId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast github event", e);
    }
  }

  private GithubTriggerPlugin getGithubTriggerPlugin(String trigger) {
    return triggerPlugins.get(trigger);
  }

  private WebhookService getWebhookService() {
    if (webhookService == null) {
      webhookService = ExoContainerContext.getService(WebhookService.class);
    }
    return webhookService;
  }
}
