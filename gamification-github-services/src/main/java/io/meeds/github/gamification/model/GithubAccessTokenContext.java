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
package io.meeds.github.gamification.model;

import java.io.Serializable;
import java.util.Objects;

import com.github.scribejava.core.model.OAuth2AccessToken;
import io.meeds.oauth.spi.AccessTokenContext;
import org.apache.commons.lang3.StringUtils;

public class GithubAccessTokenContext extends AccessTokenContext implements Serializable {

  private static final long      serialVersionUID = 659507333834536163L;

  public final OAuth2AccessToken accessToken;

  public GithubAccessTokenContext(OAuth2AccessToken accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String getAccessToken() {
    return accessToken.getAccessToken();
  }

  @Override
  public boolean equals(Object obj) {
    if (!super.equals(obj)) {
      return false;
    }
    GithubAccessTokenContext accessTokenContext = (GithubAccessTokenContext) obj;
    return StringUtils.equals(this.accessToken.getAccessToken(), accessTokenContext.getAccessToken());
  }

  @Override
  public int hashCode() {
    return super.hashCode() * 13 + Objects.hash(accessToken);
  }
}
