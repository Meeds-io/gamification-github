/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
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

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.WebHook;

import java.io.IOException;
import java.util.List;

public interface WebhookService {

  /**
   * Get available github hooks using offset and limit.
   *
   * @param currentUser user name attempting to access connector hooks
   * @param offset Offset of result
   * @param limit Limit of result
   * @throws IllegalAccessException when user is not authorized to access github
   *           hooks
   * @return {@link List} of {@link WebHook}
   */
  List<WebHook> getWebhooks(String currentUser, int offset, int limit) throws IllegalAccessException;

  /**
   * Count all gitHub webhooks
   *
   * @param currentUser User name accessing webhooks
   * @return webhooks count
   * @throws IllegalAccessException when user is not authorized to get github
   *           webhooks
   */
  int countWebhooks(String currentUser) throws IllegalAccessException;

  /**
   * create github organization hook.
   *
   * @param organizationName github organization name
   * @param secret hook secret
   * @param accessToken gitHub personal access token
   * @param currentUser user name attempting to create github hook
   */
  void createWebhook(String organizationName,
                     String secret,
                     String accessToken,
                     String currentUser) throws ObjectAlreadyExistsException, IllegalAccessException;

  /**
   * delete gitHub webhook
   *
   * @param organizationId github remote organization id
   * @param currentUser user name attempting to delete gitHub hook
   * @throws IllegalAccessException when user is not authorized to delete the
   *           gitHub hook
   */
  void deleteConnectorHook(long organizationId, String currentUser) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * @param organizationRemoteId gitHub organization remote Id
   * @return {@link String} connector hook secret
   */
  String getHookSecret(long organizationRemoteId);

  /**
   * @param organizationRemoteId gitHub organization remote Id
   * @return {@link String} connector hook access token
   */
  String getHookAccessToken(long organizationRemoteId);

  /**
   * Retrieve available github organization info.
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param accessToken gitHub personal access token
   * @return {@link RemoteOrganization}
   */
  RemoteOrganization retrieveRemoteOrganization(long organizationRemoteId, String accessToken) throws IOException;
}
