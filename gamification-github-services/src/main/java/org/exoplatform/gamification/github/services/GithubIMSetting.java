/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.gamification.github.services;

import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;

public class GithubIMSetting implements Startable {

  public static final String GITHUB_TYPE  = "github";

  public static final String GITHUB_TITLE = "Github";

  private static final Log   LOG          = ExoLogger.getLogger(GithubIMSetting.class);

  @Override
  public void start() {
    UserProfileSettingsService profileSettings = PortalContainer.getInstance()
                                                                .getComponentInstanceOfType(UserProfileSettingsService.class);
    if (profileSettings != null) {
      profileSettings.addIMType(new IMType(GITHUB_TYPE, GITHUB_TITLE));
    } else {
      LOG.warn("Cannot get the Profile Settings");
    }
  }

  @Override
  public void stop() {
    // Nothing to stop
  }
}
