/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
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
package io.meeds.gamification.github.mock;

import io.meeds.gamification.model.EventDTO;
import io.meeds.gamification.model.filter.EventFilter;
import io.meeds.gamification.plugin.EventPlugin;
import io.meeds.gamification.service.EventService;

import java.util.List;

public class EventServiceMock implements EventService {
  @Override
  public void addPlugin(EventPlugin eventPlugin) {

  }

  @Override
  public void removePlugin(String eventType) {

  }

  @Override
  public EventPlugin getEventPlugin(String eventType) {
    return null;
  }

  @Override
  public List<EventDTO> getEvents(EventFilter eventFilter, int offset, int limit) {
    return null;
  }

  @Override
  public List<EventDTO> getEventsByTitle(String title, int offset, int limit) {
    return null;
  }

  @Override
  public int countEvents(EventFilter eventFilter) {
    return 0;
  }

  @Override
  public EventDTO getEventByTitleAndTrigger(String title, String trigger) {
    return null;
  }

  @Override
  public EventDTO createEvent(EventDTO eventDTO) {
    return null;
  }

  @Override
  public EventDTO updateEvent(EventDTO eventDTO) {
    return null;
  }

  @Override
  public EventDTO getEvent(long eventId) {
    return null;
  }

  @Override
  public EventDTO deleteEventById(long eventId) {
    return null;
  }
}
