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
package org.exoplatform.gamification.github.services;

import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import java.util.List;

public interface GithubConsumerService {

  /**
   * create github organization hook.
   *
   * @param organizationName github organization name
   * @param accessToken gitHub personal access token
   * @throws IllegalAccessException when user is not authorized to create github
   *           webhook
   */
  WebHook createWebhook(String organizationName, String[] triggers, String accessToken) throws IllegalAccessException;

  /**
   * delete gitHub webhook
   *
   * @param webHook github webHook
   */
  String deleteWebhook(WebHook webHook);

  /**
   * Retrieve available github organization info.
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param accessToken gitHub personal access token
   * @return {@link RemoteOrganization}
   */
  RemoteOrganization retrieveRemoteOrganization(long organizationRemoteId, String accessToken);

  /**
   * Retrieve available github organization info.
   *
   * @param organizationName gitHub organization name
   * @param accessToken gitHub personal access token
   * @throws ObjectNotFoundException when the github organization identified by
   *           its technical name is not found
   * @return {@link RemoteOrganization}
   */
  RemoteOrganization retrieveRemoteOrganization(String organizationName, String accessToken) throws ObjectNotFoundException;

  /**
   * Retrieve available github organization repositories.
   *
   * @param webHook webHook
   *          repositories
   * @throws ObjectNotFoundException when the github organization identified by
   *           its technical name is not found
   * @throws IllegalAccessException when user is not authorized to access remote
   *           organization repositories
   *
   * @return {@link List} of {@link RemoteRepository}
   */
  List<RemoteRepository> retrieveOrganizationRepos(WebHook webHook,
                                                   int page,
                                                   int perPage);

  /**
   * Count github organization repositories.
   *
   * @param webHook webHook
   *          repositories
   *
   * @return repositories count
   */
  int countOrganizationRepos(WebHook webHook);

  /**
   * Force Update Webhook
   *
   * @param webHook webHook
   *
   * @return response status
   */
  String forceUpdateWebhook(WebHook webHook);

  /**
   * Check gitHub personal access token
   *
   * @param accessToken gitHub personal access token
   * @return {@link TokenStatus}
   */
  TokenStatus checkGitHubTokenStatus(String accessToken);

  /**
   * clear remote webhook entittes cache
   */
  void clearCache();
}
