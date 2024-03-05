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

import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.gamification.service.ConnectorSettingService;
import org.exoplatform.services.security.Identity;

import java.util.List;

public class ConnectorSettingServiceMock implements ConnectorSettingService {
  @Override
  public void saveConnectorSettings(RemoteConnectorSettings remoteConnectorSettings, Identity aclIdentity) {

  }

  @Override
  public void deleteConnectorSettings(String connectorName, Identity aclIdentity) {

  }

  @Override
  public RemoteConnectorSettings getConnectorSettings(String connectorName, Identity aclIdentity) {
    return null;
  }

  @Override
  public RemoteConnectorSettings getConnectorSettings(String connectorName) {
    return null;
  }

  @Override
  public String getConnectorSecretKey(String connectorName) {
    return null;
  }

  @Override
  public List<RemoteConnectorSettings> getConnectorsSettings(ConnectorService connectorService, Identity aclIdentity) {
    return null;
  }

  @Override
  public boolean canManageConnectorSettings(Identity aclIdentity) {
    return false;
  }
}
