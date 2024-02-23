/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2022 Meeds Association contact@meeds.io
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

package io.meeds.gamification.github.storage.cached.model;

import java.io.Serializable;

import io.meeds.gamification.model.filter.ProgramFilter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CacheKey implements Serializable {

  private static final long serialVersionUID = -8995567724453740730L;

  private ProgramFilter     programFilter;

  private int               page;

  private int               perPage;

  private long              organizationId;

  private String              organizationName;

  private String            accessToken;

  private String            keyword;

  private Integer           context;

  public CacheKey(Integer context, String organizationName, String accessToken, int page, int perPage, String keyword) {
    this.organizationName = organizationName;
    this.accessToken = accessToken;
    this.page = page;
    this.perPage = perPage;
    this.keyword = keyword;
    this.context = context;
  }

  public CacheKey(Integer context, long organizationId, String accessToken) {
    this.organizationId = organizationId;
    this.accessToken = accessToken;
    this.context = context;
  }

  public CacheKey(Integer context, String accessToken) {
    this.accessToken = accessToken;
    this.context = context;
  }

}
