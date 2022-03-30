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
package org.exoplatform.gamification.github.listener;

import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.gamification.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.github.entity.GitHubAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

public class GithubProfileListener extends ProfileListenerPlugin {

  public static final String GITHUB_TYPE = "github";

  private static final Log   LOG         = ExoLogger.getLogger(GithubProfileListener.class);

  protected GitHubAccountDAO gitHubAccountDAO;

  public GithubProfileListener(GitHubAccountDAO gitHubAccountDAO) {
    this.gitHubAccountDAO = gitHubAccountDAO;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    String gitHubId = "";
    List<HashMap<String, String>> ims = (List<HashMap<String, String>>) event.getProfile().getProperty("ims");
    for (HashMap<String, String> map : ims) {
      if (map.get("key").equals(GITHUB_TYPE)) {
        gitHubId = map.get("value");
      }
    }
    if (!gitHubId.isEmpty()) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        GitHubAccountEntity entity = gitHubAccountDAO.getAccountByUserName(event.getUsername());
        if (entity == null || !entity.getGitHubId().equals(gitHubId)) {
          GitHubAccountEntity existingEntity = gitHubAccountDAO.getAccountByGithubId(gitHubId);
          if (existingEntity == null) {
            if (entity == null) {
              entity = new GitHubAccountEntity();
              entity.setUserName(event.getUsername());
              entity.setGitHubId(gitHubId);
              gitHubAccountDAO.create(entity);
            } else {
              entity.setGitHubId(gitHubId);
              gitHubAccountDAO.update(entity);
            }

          } else {
            LOG.warn("The provided Github ID {} is already used by {}", gitHubId, existingEntity.getUserName());
          }
        }
      } catch (Exception e) {
        LOG.error("Could not retrieve and save Github account in user profile");
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    // Not used
  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {
    // Not used
  }

}
