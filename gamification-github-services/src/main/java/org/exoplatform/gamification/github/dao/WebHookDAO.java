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

import org.apache.commons.collections.CollectionUtils;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.github.entity.WebhookEntity;

import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class WebHookDAO extends GenericDAOJPAImpl<WebhookEntity, Long> {

  public static final String ORGANIZATION_ID = "organizationId";

  public WebhookEntity getWebhookByOrganizationId(long organizationId) {
    TypedQuery<WebhookEntity> query = getEntityManager().createNamedQuery("GitHubWebhooks.getWebhookByOrganizationId",
                                                                          WebhookEntity.class);
    query.setParameter(ORGANIZATION_ID, organizationId);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public List<Long> getWebhookIds(int offset, int limit) {
    TypedQuery<Tuple> query = getEntityManager().createNamedQuery("GitHubWebhooks.getWebhookIds", Tuple.class);
    List<Tuple> result = query.getResultList();
    if (CollectionUtils.isEmpty(result)) {
      return Collections.emptyList();
    } else {
      Stream<Long> resultStream = result.stream().map(tuple -> tuple.get(0, Long.class));
      if (offset > 0) {
        resultStream = resultStream.skip(offset);
      }
      if (limit > 0) {
        resultStream = resultStream.limit(limit);
      }
      return resultStream.toList();
    }
  }

  public String getWebHookHookSecret(long organizationId) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("GitHubWebhooks.getWebHookHookSecret", String.class);
    query.setParameter(ORGANIZATION_ID, organizationId);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public String getWebHookAccessToken(long organizationId) {
    TypedQuery<String> query = getEntityManager().createNamedQuery("GitHubWebhooks.getWebHookAccessToken", String.class);
    query.setParameter(ORGANIZATION_ID, organizationId);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }
}
