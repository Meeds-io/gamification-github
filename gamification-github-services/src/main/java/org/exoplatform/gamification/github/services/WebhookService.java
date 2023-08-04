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

import java.util.List;

public interface WebhookService {

  /**
   * Get available github hooks using offset and limit.
   *
   * @param currentUser user name attempting to access connector hooks
   * @param offset Offset of result
   * @param limit Limit of result
   * @param forceUpdate force Load remote webhook or not.
   * @return {@link List} of {@link WebHook}
   * @throws IllegalAccessException when user is not authorized to access github
   *           hooks
   */
  List<WebHook> getWebhooks(String currentUser, int offset, int limit, boolean forceUpdate) throws IllegalAccessException;

  /**
   * Get available github hooks using offset and limit.
   *
   * @param offset Offset of result
   * @param limit Limit of result
   * @param forceUpdate force Load remote webhooks or not.
   * @return {@link List} of {@link WebHook}
   */
  List<WebHook> getWebhooks(int offset, int limit, boolean forceUpdate);

  /**
   * Count all gitHub webhooks
   *
   * @param currentUser User name accessing webhooks
   * @param forceUpdate force Load remote webhooks count or not.
   * @return webhooks count
   * @throws IllegalAccessException when user is not authorized to get github
   *           webhooks
   */
  int countWebhooks(String currentUser, boolean forceUpdate) throws IllegalAccessException;

  /**
   * create github organization hook.
   *
   * @param organizationName github organization name
   * @param accessToken gitHub personal access token
   * @param currentUser user name attempting to create github hook
   */
  void createWebhook(String organizationName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
                                                                                      IllegalAccessException;

  /**
   * Update github organization hook.
   *
   * @param webHookId webHook Id
   * @param accessToken gitHub personal access token
   * @param currentUser user name attempting to update github hook
   */
  void updateWebHookAccessToken(long webHookId, String accessToken, String currentUser) throws IllegalAccessException,
                                                                                        ObjectNotFoundException;

  /**
   * delete gitHub webhook
   *
   * @param organizationId github remote organization id
   * @param currentUser user name attempting to delete gitHub hook
   * @throws IllegalAccessException when user is not authorized to delete the
   *           gitHub hook
   */
  void deleteWebhookHook(long organizationId, String currentUser) throws IllegalAccessException, ObjectNotFoundException;

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
  RemoteOrganization retrieveRemoteOrganization(long organizationRemoteId, String accessToken);

  /**
   * Force update the stored github organization webhooks if there is a change to
   * the remote webhooks, such as an entity deletion or the update event
   **/
  void forceUpdateWebhooks();

  /**
   * @param payload payload The raw payload of the webhook request.
   * @param signature The signature received from the external system.
   * @return true if the computed signature matches the provided signature.
   */
  boolean verifyWebhookSecret(String payload, String signature);
}
