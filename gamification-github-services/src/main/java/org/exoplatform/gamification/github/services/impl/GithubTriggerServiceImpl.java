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

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.model.filter.EventFilter;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.EventService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.gamification.github.model.Event;
import org.exoplatform.gamification.github.plugin.GithubTriggerPlugin;
import org.exoplatform.gamification.github.services.GithubTriggerService;
import org.exoplatform.gamification.github.services.WebhookService;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.picocontainer.Startable;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.exoplatform.gamification.github.utils.Utils.*;

public class GithubTriggerServiceImpl implements GithubTriggerService, Startable {

  private static final Log                       LOG            = ExoLogger.getLogger(GithubTriggerServiceImpl.class);

  private final Map<String, GithubTriggerPlugin> triggerPlugins = new HashMap<>();

  private final ConnectorService                 connectorService;

  private final EventService                     eventService;

  private WebhookService                         webhookService;

  private final IdentityManager                  identityManager;

  private final ListenerService                  listenerService;

  private ExecutorService                        executorService;

  public GithubTriggerServiceImpl(ListenerService listenerService,
                                  ConnectorService connectorService,
                                  IdentityManager identityManager,
                                  EventService eventService) {
    this.listenerService = listenerService;
    this.connectorService = connectorService;
    this.identityManager = identityManager;
    this.eventService = eventService;
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
    processEvents(events, trigger, organizationId);
  }

  private void processEvents(List<Event> events, String trigger, String organizationId) {
    events.stream().filter(event -> isEventEnabled(event.getName(), trigger, organizationId)).forEach(this::processEvent);
  }

  private boolean isEventEnabled(String eventName, String trigger, String organizationId) {
    EventDTO eventDTO = eventService.getEventByTitleAndTrigger(eventName, trigger);
    if (eventDTO != null) {
      return isOrganizationEventEnabled(eventDTO, organizationId);
    }
    return true;
  }

  private boolean isOrganizationEventEnabled(EventDTO eventDTO, String organizationId) {
    String organizationPropertyKey = organizationId + ".enabled";
    Map<String, String> properties = eventDTO.getProperties();
    if (properties != null && !properties.isEmpty()) {
      return Boolean.parseBoolean(properties.get(organizationPropertyKey));
    }
    return true;
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
        broadcastGithubEvent(event.getName(), senderId, receiverId, event.getObjectId(), event.getObjectType());
      }
    }
  }

  public String[] getTriggers() {
    return triggerPlugins.values().stream().map(GithubTriggerPlugin::getName).toArray(String[]::new);
  }

  private void broadcastGithubEvent(String ruleTitle, String senderId, String receiverId, String objectId, String objectType) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("objectId", objectId);
      gam.put("objectType", objectType);
      EventDTO eventDTO = eventService.getEventByTypeAndTitle("github", ruleTitle);
      if (eventDTO != null) {
        gam.put("ruleTitle", eventDTO.getTitle());
        listenerService.broadcast(GITHUB_ACTION_EVENT, gam, "");
      } else {
        List<EventDTO> events = eventService.getEvents(new EventFilter("github", null), 0, 0);
        List<EventDTO> eventsToCancel = events.stream()
                                              .filter(event -> event.getCancellerEvents() != null
                                                  && event.getCancellerEvents().contains(ruleTitle))
                                              .toList();
        if (CollectionUtils.isNotEmpty(eventsToCancel)) {
          for (EventDTO eventToCancel : eventsToCancel) {
            gam.put("ruleTitle", eventToCancel.getTitle());
            listenerService.broadcast(GITHUB_CANCEL_ACTION_EVENT, gam, "");
          }
        }
      }
      LOG.info("Github action {} broadcasted for user {}", ruleTitle, senderId);
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
