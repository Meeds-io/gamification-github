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
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.github.gamification.services;

import io.meeds.github.gamification.model.WebHook;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import io.meeds.github.gamification.model.RemoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WebhookService {

  /**
   * Get available github hooks using offset and limit.
   *
   * @param currentUser user name attempting to access connector hooks
   * @param pageable {@link Pageable} the page to be returned.
   * @return {@link Pageable} of {@link WebHook}
   * @throws IllegalAccessException when user is not authorized to access github
   *           hooks
   */
  Page<WebHook> getWebhooks(String currentUser, Pageable pageable) throws IllegalAccessException;

  /**
   * Retrieves a webHook identified by its technical identifier.
   *
   * @param webhookId WebHook technical identifier
   * @return found {@link WebHook}
   */
  WebHook getWebhookId(long webhookId);

  /**
   * Retrieves a webHook identified by its technical identifier accessed by a user
   *
   * @param webhookId WebHook technical identifier
   * @param username user name attempting to access connector webhook
   * @return found {@link WebHook}
   * @throws IllegalAccessException when user is not authorized to access webhook
   * @throws ObjectNotFoundException webhook not found
   */
  WebHook getWebhookId(long webhookId, String username) throws IllegalAccessException, ObjectNotFoundException;

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
   * @return {@link WebHook}
   */
  WebHook createWebhook(String organizationName, String accessToken, String currentUser) throws ObjectAlreadyExistsException,
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
  void deleteWebhook(long organizationId, String currentUser) throws IllegalAccessException, ObjectNotFoundException;

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
   * Retrieve available github organization repositories.
   *
   * @param organizationRemoteId gitHub organization remote Id
   * @param currentUser user name attempting to access remote organization
   *          repositories
   * @param page page
   * @param perPage perPage
   * @param keyword to search in repositories title
   * @throws IllegalAccessException when user is not authorized to access remote
   *           organization repositories
   * @return {@link List} of {@link RemoteRepository}
   */
  List<RemoteRepository> retrieveOrganizationRepos(long organizationRemoteId,
                                                   String currentUser,
                                                   int page,
                                                   int perPage,
                                                   String keyword) throws IllegalAccessException, ObjectNotFoundException;
  /**
   * Force update the stored github organization webhooks if there is a change to
   * the remote webhooks, such as an entity deletion or the update event
   **/
  void forceUpdateWebhooks();


}
