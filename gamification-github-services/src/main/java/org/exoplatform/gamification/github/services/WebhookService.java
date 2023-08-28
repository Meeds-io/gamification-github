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
import org.exoplatform.gamification.github.model.RemoteRepository;
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
   * @throws ObjectAlreadyExistsException when webhook already exists
   * @throws IllegalAccessException when user is not authorized to create github
   *           webhook
   * @throws ObjectNotFoundException when the github organization identified by
   *           its technical name is not found
   */
  void createWebhook(String organizationName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
                                                                                      IllegalAccessException,
                                                                                      ObjectNotFoundException;

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

  /**
   * Check if webhook repository is enabled
   *
   * @param payload payload The raw payload of the webhook request.
   * @return true if the intended repository is enabled, else false.
   */
  boolean isWebHookRepositoryEnabled(String payload);

  /**
   * Check if webhook repository is enabled
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param repositoryRemoteId gitHub repository remote Id
   * @return true if the intended repository is enabled, else false.
   */
  boolean isWebHookRepositoryEnabled(long organizationRemoteId, long repositoryRemoteId);

  /**
   * enables/disables repository
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param repositoryRemoteId gitHub repository remote Id
   * @param enabled true to enabled, else false
   * @param currentUser user name attempting to enables/disables repository.
   * @throws IllegalAccessException when user is not authorized enables/disables
   *           repository
   */
  void setWebHookRepositoryEnabled(long organizationRemoteId,
                                   long repositoryRemoteId,
                                   boolean enabled,
                                   String currentUser) throws IllegalAccessException;

  /**
   * Check if webhook watch limit is enabled
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @return true if webHook watch limit is enabled, else false.
   */
  boolean isWebHookWatchLimitEnabled(long organizationRemoteId);

  /**
   * Limit webhook watch scope or not
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param enabled true to enabled, else false
   * @param currentUser user name attempting to enables/disables webHook watch
   *          limit.
   * @throws IllegalAccessException when user is not authorized Limit webhook
   *           watch scope
   */
  void setWebHookWatchLimitEnabled(long organizationRemoteId, boolean enabled, String currentUser) throws IllegalAccessException;

  /**
   * Retrieve available github organization repositories.
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param currentUser user name attempting to access remote organization
   *          repositories
   * @throws IllegalAccessException when user is not authorized to access remote
   *           organization repositories
   * @return {@link List} of {@link RemoteRepository}
   */
  List<RemoteRepository> retrieveOrganizationRepos(long organizationRemoteId,
                                                   String currentUser,
                                                   int page,
                                                   int perPage) throws IllegalAccessException, ObjectNotFoundException;

  /**
   * Count available github organization repositories.
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param currentUser user name attempting to access remote organization
   *          repositories
   * @throws IllegalAccessException when user is not authorized to access remote
   *           organization repositories
   * @return Repositories count
   */
  int countOrganizationRepos(long organizationRemoteId, String currentUser) throws IllegalAccessException,
                                                                            ObjectNotFoundException;

  /**
   * create gamification history
   *
   * @param ruleTitle Rule title
   * @param senderId sender username
   * @param receiverId receiver username
   * @param object Object link
   */
  void createGamificationHistory(String ruleTitle, String senderId, String receiverId, String object);

}
