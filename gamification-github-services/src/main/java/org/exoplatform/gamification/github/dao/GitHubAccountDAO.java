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
package org.exoplatform.gamification.github.dao;

import java.util.List;

import javax.persistence.*;

import org.apache.commons.collections.CollectionUtils;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.github.entity.GitHubAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GitHubAccountDAO extends GenericDAOJPAImpl<GitHubAccountEntity, Long> {

  private static final Log LOG = ExoLogger.getLogger(GitHubAccountDAO.class);

  public GitHubAccountEntity getAccountByGithubId(String gitHubId) {

    TypedQuery<GitHubAccountEntity> query = getEntityManager()
                                                              .createNamedQuery("GitHubAccountEntity.getAccountByGithubId",
                                                                                GitHubAccountEntity.class)
                                                              .setParameter("gitHubId", gitHubId);

    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (NonUniqueResultException e) {
      List<GitHubAccountEntity> list = query.getResultList();
      GitHubAccountEntity gitHubAccountEntity = CollectionUtils.isEmpty(list) ? null : list.get(0);

      LOG.warn("Not only one single user found for github account {}. Try to retrieve only first one for user {}",
               gitHubId,
               gitHubAccountEntity == null ? null : gitHubAccountEntity.getUserName(),
               e);

      return gitHubAccountEntity;
    }
  }

  public GitHubAccountEntity getAccountByUserName(String userName) {

    TypedQuery<GitHubAccountEntity> query = getEntityManager()
                                                              .createNamedQuery("GitHubAccountEntity.getAccountByUserName",
                                                                                GitHubAccountEntity.class)
                                                              .setParameter("userName", userName);

    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (NonUniqueResultException e) {
      List<GitHubAccountEntity> list = query.getResultList();
      GitHubAccountEntity gitHubAccountEntity = CollectionUtils.isEmpty(list) ? null : list.get(0);

      LOG.warn("Not only one single github account found for user {}. Try to retrieve only first one for github account {}",
               userName,
               gitHubAccountEntity == null ? null : gitHubAccountEntity.getGitHubId(),
               e);

      return gitHubAccountEntity;
    }
  }
}
