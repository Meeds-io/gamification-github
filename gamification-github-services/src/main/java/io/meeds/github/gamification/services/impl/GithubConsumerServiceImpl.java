/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
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
package io.meeds.github.gamification.services.impl;

import io.meeds.github.gamification.model.RemoteOrganization;
import io.meeds.github.gamification.model.WebHook;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import io.meeds.github.gamification.model.RemoteRepository;
import io.meeds.github.gamification.model.TokenStatus;
import io.meeds.github.gamification.services.GithubConsumerService;
import io.meeds.github.gamification.storage.GithubConsumerStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Primary
@Service
public class GithubConsumerServiceImpl implements GithubConsumerService {

  @Autowired
  private GithubConsumerStorage githubConsumerStorage;

  @Override
  public WebHook createWebhook(String organizationName, String[] triggers, String accessToken) throws IllegalAccessException {
    return githubConsumerStorage.createWebhook(organizationName, triggers, accessToken);
  }

  @Override
  public String deleteWebhook(WebHook webHook) {
    return githubConsumerStorage.deleteWebhookHook(webHook);
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
  public List<RemoteRepository> retrieveOrganizationRepos(WebHook webHook, int page, int perPage, String keyword) {
    return githubConsumerStorage.retrieveOrganizationRepos(webHook.getOrganizationName(),
                                                           webHook.getToken(),
                                                           page,
                                                           perPage,
                                                           keyword);
  }

  @Override
  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) {
    return githubConsumerStorage.retrieveRemoteOrganization(organizationId, accessToken);
  }

  @Override
  public TokenStatus checkGitHubTokenStatus(String token) {
    return githubConsumerStorage.checkGitHubTokenStatus(token);
  }
}
