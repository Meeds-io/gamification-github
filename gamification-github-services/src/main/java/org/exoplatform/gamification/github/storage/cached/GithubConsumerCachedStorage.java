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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.gamification.github.storage.cached;

import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.storage.GithubConsumerStorage;
import org.exoplatform.gamification.github.storage.cached.model.CacheKey;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;
import java.util.*;

public class GithubConsumerCachedStorage extends GithubConsumerStorage {

  public static final String                                   GITHUB_CACHE_NAME    = "github.connector";

  private static final int                                     ORG_REPOS_CONTEXT    = 0;

  private static final int                                     ORG_BY_ID_CONTEXT    = 1;

  private static final int                                     TOKEN_STATUS_CONTEXT = 2;

  private final FutureExoCache<Serializable, Object, CacheKey> githubFutureCache;

  public GithubConsumerCachedStorage(CacheService cacheService) {
    ExoCache<Serializable, Object> cacheInstance = cacheService.getCacheInstance(GITHUB_CACHE_NAME);
    this.githubFutureCache = new FutureExoCache<>((context, key) -> {
      if (ORG_REPOS_CONTEXT == context.getContext()) {
        return GithubConsumerCachedStorage.super.retrieveOrganizationRepos(context.getOrganizationName(),
                                                                           context.getAccessToken(),
                                                                           context.getPage(),
                                                                           context.getPerPage(),
                                                                           context.getKeyword());
      } else if (ORG_BY_ID_CONTEXT == context.getContext()) {
        return GithubConsumerCachedStorage.super.retrieveRemoteOrganization(context.getOrganizationId(),
                                                                            context.getAccessToken());
      } else if (TOKEN_STATUS_CONTEXT == context.getContext()) {
        return GithubConsumerCachedStorage.super.checkGitHubTokenStatus(context.getAccessToken());
      } else {
        throw new UnsupportedOperationException();
      }
    }, cacheInstance);
  }

  @Override
  public String deleteWebhookHook(WebHook webHook) {
    try {
      return super.deleteWebhookHook(webHook);
    } finally {
      clearCache(webHook);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<RemoteRepository> retrieveOrganizationRepos(String organizationName,
                                                          String accessToken,
                                                          int page,
                                                          int perPage,
                                                          String keyword) {
    CacheKey cacheKey = new CacheKey(ORG_REPOS_CONTEXT, organizationName, accessToken, page, perPage, keyword);
    List<RemoteRepository> remoteRepositories =
                                              (List<RemoteRepository>) this.githubFutureCache.get(cacheKey, cacheKey.hashCode());
    return remoteRepositories == null ? Collections.emptyList() : remoteRepositories;
  }

  @Override
  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) {
    CacheKey cacheKey = new CacheKey(ORG_BY_ID_CONTEXT, organizationId, accessToken);
    return (RemoteOrganization) this.githubFutureCache.get(cacheKey, cacheKey.hashCode());
  }

  @Override
  public TokenStatus checkGitHubTokenStatus(String token) {
    CacheKey cacheKey = new CacheKey(TOKEN_STATUS_CONTEXT, token);
    return (TokenStatus) this.githubFutureCache.get(cacheKey, cacheKey.hashCode());
  }

  @Override
  public void clearCache() {
    this.githubFutureCache.clear();
  }

  @Override
  public void clearCache(WebHook webHook) {
    this.githubFutureCache.remove(new CacheKey(ORG_BY_ID_CONTEXT, webHook.getOrganizationId(), webHook.getToken()).hashCode());
    this.githubFutureCache.remove(new CacheKey(TOKEN_STATUS_CONTEXT, webHook.getToken()).hashCode());
  }
}
