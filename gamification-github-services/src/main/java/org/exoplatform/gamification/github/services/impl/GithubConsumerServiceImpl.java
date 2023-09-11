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

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.services.GithubConsumerService;
import org.exoplatform.gamification.github.storage.GithubConsumerStorage;

import java.util.List;

public class GithubConsumerServiceImpl implements GithubConsumerService {

  private GithubConsumerStorage githubConsumerStorage;

  public GithubConsumerServiceImpl(GithubConsumerStorage githubConsumerStorage) {
    this.githubConsumerStorage = githubConsumerStorage;
  }

  @Override
  public WebHook createWebhook(String organizationName, String[] triggers, String accessToken) throws IllegalAccessException {
    return githubConsumerStorage.createWebhook(organizationName, triggers, accessToken);
  }

  @Override
  public String deleteWebhook(WebHook webHook) {
    return githubConsumerStorage.deleteWebhookHook(webHook);
  }

  @Override
  public int countOrganizationRepos(WebHook webHook) {
    return githubConsumerStorage.countOrganizationRepos(webHook.getOrganizationId(), webHook.getToken());
  }

  @Override
  public String forceUpdateWebhook(WebHook webHook) {
    return githubConsumerStorage.forceUpdateWebhook(webHook);
  }

  @Override
  public RemoteOrganization retrieveRemoteOrganization(String organizationName,
                                                       String accessToken) throws ObjectNotFoundException {
    return githubConsumerStorage.retrieveRemoteOrganization(organizationName, accessToken);
  }

  @Override
  public List<RemoteRepository> retrieveOrganizationRepos(WebHook webHook, int page, int perPage) {
    return githubConsumerStorage.retrieveOrganizationRepos(webHook.getOrganizationId(), webHook.getToken(), page, perPage);
  }

  @Override
  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) {
    return githubConsumerStorage.retrieveRemoteOrganization(organizationId, accessToken);
  }

  @Override
  public TokenStatus checkGitHubTokenStatus(String token) {
    return githubConsumerStorage.checkGitHubTokenStatus(token);
  }

  @Override
  public void clearCache() {
    githubConsumerStorage.clearCache();
  }
}
