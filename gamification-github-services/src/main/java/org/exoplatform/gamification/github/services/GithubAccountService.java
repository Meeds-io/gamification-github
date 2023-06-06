/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2023 Meeds Association contact@meeds.io
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
import org.exoplatform.gamification.github.model.GithubAccount;

public interface GithubAccountService {

  /**
   * Retrieves a github account identified by its github technical identifier.
   *
   * @param gitHubId github technical identifier
   * @return Corresponding {@link GithubAccount} or null if not found
   */
  GithubAccount getAccountByGithubId(String gitHubId);

  /**
   * Deletes an existing calendar
   *
   * @param username Github account user name
   * @throws ObjectNotFoundException when the github account identified by the
   *           username is not found
   */
  void deleteAccountByUsername(String username) throws ObjectNotFoundException;

  /**
   * Retrieves a github account identified by the internal username.
   *
   * @param username The remote user Id.
   * @return Corresponding {@link GithubAccount} or null if not found
   */
  GithubAccount getAccountByUserName(String username);

  /**
   * Save {@link GithubAccount}
   *
   * @param gitHubId github technical identifier
   * @param username The remote user Id.
   * @return created {@link GithubAccount}
   */
  GithubAccount saveGithubAccount(String gitHubId, String username) throws ObjectAlreadyExistsException;

}
