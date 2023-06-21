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
package org.exoplatform.gamification.github.services.impl;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.github.entity.GitHubAccountEntity;
import org.exoplatform.gamification.github.model.GithubAccount;
import org.exoplatform.gamification.github.services.GithubAccountService;

public class GithubAccountServiceImpl implements GithubAccountService {

  private final GitHubAccountDAO gitHubAccountDAO;

  public GithubAccountServiceImpl(GitHubAccountDAO gitHubAccountDAO) {
    this.gitHubAccountDAO = gitHubAccountDAO;
  }

  @Override
  public GithubAccount getAccountByGithubId(String gitHubId) {
    GitHubAccountEntity gitHubAccountEntity = gitHubAccountDAO.getAccountByGithubId(gitHubId);
    return new GithubAccount(gitHubAccountEntity.getId(), gitHubAccountEntity.getGitHubId(), gitHubAccountEntity.getUserName());
  }

  @Override
  public void deleteAccountByUsername(String username) throws ObjectNotFoundException {
    if (username == null) {
      throw new IllegalArgumentException("Username is mandatory");
    }
    GitHubAccountEntity gitHubAccountEntity = gitHubAccountDAO.getAccountByUserName(username);
    if (gitHubAccountEntity == null) {
      throw new ObjectNotFoundException("Github account with username " + username + " wasn't found");
    }
    gitHubAccountDAO.delete(gitHubAccountEntity);
  }

  @Override
  public GithubAccount getAccountByUserName(String username) {
    GitHubAccountEntity gitHubAccountEntity = gitHubAccountDAO.getAccountByUserName(username);
    return gitHubAccountEntity != null ? new GithubAccount(gitHubAccountEntity.getId(),
                                                           gitHubAccountEntity.getGitHubId(),
                                                           gitHubAccountEntity.getUserName())
                                       : null;
  }

  @Override
  public GithubAccount saveGithubAccount(String gitHubId, String username) throws ObjectAlreadyExistsException {
    GitHubAccountEntity createdAccount;
    GitHubAccountEntity existingEntity = gitHubAccountDAO.getAccountByGithubId(gitHubId);
    if (existingEntity == null || existingEntity.getUserName().equals(username)) {
      GitHubAccountEntity gitHubAccountEntity = gitHubAccountDAO.getAccountByUserName(username);
      if (gitHubAccountEntity == null) {
        GitHubAccountEntity entity = new GitHubAccountEntity();
        entity.setUserName(username);
        entity.setGitHubId(gitHubId);
        createdAccount = gitHubAccountDAO.create(entity);
      } else {
        gitHubAccountEntity.setGitHubId(gitHubId);
        createdAccount = gitHubAccountDAO.update(gitHubAccountEntity);
      }
    } else {
      throw new ObjectAlreadyExistsException("The provided Github ID {} is already used", gitHubId);
    }
    return new GithubAccount(createdAccount.getId(), createdAccount.getGitHubId(), createdAccount.getUserName());
  }
}
