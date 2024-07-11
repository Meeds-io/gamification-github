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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.gamification.github.rest.model;

import lombok.*;
import io.meeds.gamification.github.model.TokenStatus;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class WebHookRestEntity {

  private long         id;

  private long         webhookId;

  private long         organizationId;

  private List<String> triggers;

  private Boolean      enabled;

  private String       watchedDate;

  private String       watchedBy;

  private String       updatedDate;

  private String       refreshDate;

  private String       name;

  private String       title;

  private String       description;

  private String       avatarUrl;

  private boolean watchScopeLimited;

  private TokenStatus tokenStatus;

  public WebHookRestEntity(long id, // NOSONAR
                           long webhookId,
                           long organizationId,
                           List<String> triggers,
                           boolean enabled,
                           String watchedDate,
                           String watchedBy,
                           String updatedDate,
                           String refreshDate,
                           String name,
                           String title,
                           String description,
                           String avatarUrl,
                           boolean watchScopeLimited,
                           TokenStatus tokenStatus) {

    this.id = id;
    this.webhookId = webhookId;
    this.organizationId = organizationId;
    this.triggers = triggers;
    this.enabled = enabled;
    this.name = name;
    this.watchedDate = watchedDate;
    this.watchedBy = watchedBy;
    this.refreshDate = refreshDate;
    this.updatedDate = updatedDate;
    this.title = title;
    this.description = description;
    this.avatarUrl = avatarUrl;
    this.watchScopeLimited = watchScopeLimited;
    this.tokenStatus = tokenStatus;
  }
}
