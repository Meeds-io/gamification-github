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

import io.meeds.gamification.model.RemoteConnector;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.gamification.service.ConnectorService;
import org.exoplatform.services.security.Identity;

import java.util.Collection;

public class ConnectorServiceMock implements ConnectorService {
    @Override
    public void addPlugin(ConnectorPlugin connectorPlugin) {

    }

    @Override
    public void removePlugin(String name) {

    }

    @Override
    public Collection<ConnectorPlugin> getConnectorPlugins() {
        return null;
    }

    @Override
    public Collection<RemoteConnector> getConnectors(String username) {
        return null;
    }

    @Override
    public String connect(String connectorName, String connectorUserId, String accessToken, Identity identity) {
        return null;
    }

    @Override
    public void disconnect(String connectorName, String username) {

    }

    @Override
    public String getConnectorRemoteId(String connectorName, String username) {
        return null;
    }

    @Override
    public String getAssociatedUsername(String connectorName, String connectorRemoteId) {
        return null;
    }
}
