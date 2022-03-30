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
package org.exoplatform.gamification.github.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.Data;

@Entity(name = "GitHubAccountEntity")
@ExoEntity
@Table(name = "GAM_GITHUB_ACCOUNTS")
@Data
@NamedQueries(
  {
      @NamedQuery(
          name = "GitHubAccountEntity.getAccountByGithubId",
          query = "SELECT account FROM GitHubAccountEntity account where account.gitHubId = :gitHubId "
      ),
      @NamedQuery(
          name = "GitHubAccountEntity.getAccountByUserName",
          query = "SELECT account FROM GitHubAccountEntity account where account.userName = :userName "
      )

  }
)
public class GitHubAccountEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_GAM_GITHUB_ACCOUNTS_ID", sequenceName = "SEQ_GAM_GITHUB_ACCOUNTS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GAM_GITHUB_ACCOUNTS_ID")
  @Column(name = "ID")
  protected Long   id;

  @Column(name = "GITHUB_ID", unique = true, nullable = false)
  protected String gitHubId;

  @Column(name = "USER_NAME", unique = true, nullable = false)
  protected String userName;
}
