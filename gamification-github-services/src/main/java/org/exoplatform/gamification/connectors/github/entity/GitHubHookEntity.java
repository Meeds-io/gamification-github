package org.exoplatform.gamification.connectors.github.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.Data;

@Entity(name = "GitHubHookEntity")
@ExoEntity
@Table(name = "GAM_GITHUB_HOOKS")
@Data
@NamedQueries({
        @NamedQuery(name = "GitHubHookEntity.getHooksByExoEnvironment", query = "SELECT hook FROM GitHubHookEntity hook where hook.exoEnvironment = :exoEnvironment "),
        @NamedQuery(name = "GitHubHookEntity.getHooksByOrgRepoAndEnvironment", query = "SELECT hook FROM GitHubHookEntity hook where hook.organization = :org and hook.repo = :repo and hook.exoEnvironment = :exoEnvironment")
})

public class GitHubHookEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_GAM_GITHUB_HOOKS_ID", sequenceName = "SEQ_GAM_GITHUB_HOOKS_ID", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_GAM_GITHUB_HOOKS_ID")
  @Column(name = "ID")
  protected Long    id;

  @Column(name = "GITHUB_ID")
  protected Long    githubId;

  @Column(name = "ORGANIZATION", nullable = false)
  protected String  organization;

  @Column(name = "REPO", nullable = false)
  protected String  repo;

  @Column(name = "HOOK_URL", unique = true, nullable = false)
  protected String  webhook;

  @Column(name = "EVENTS", nullable = false)
  protected String  events;

  @Column(name = "EXO_ENVIRONMENT", nullable = false)
  protected String  exoEnvironment;

  @Column(name = "ENABLED", nullable = false)
  protected Boolean enabled;

  @Column(name = "CREATED_DATE", nullable = false)
  protected Date    createdDate;

  @Column(name = "UPDATED_DATE", nullable = false)
  protected Date    updatedDate;
}
