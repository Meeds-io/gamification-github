/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
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

import io.meeds.gamification.model.TriggerProperties;
import io.meeds.gamification.service.TriggerService;

public class TriggerServiceMock implements TriggerService {
  @Override
  public TriggerProperties getTriggerProperties(String triggerName) {
    return null;
  }

  @Override
  public void setTriggerEnabledForAccount(String triggerName, long accountId, boolean enabled, String currentUser) {

  }

  @Override
  public boolean isTriggerEnabledForAccount(String triggerName, long accountId) {
    return false;
  }
}
