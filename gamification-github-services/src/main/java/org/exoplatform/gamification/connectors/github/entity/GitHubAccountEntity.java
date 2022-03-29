package org.exoplatform.gamification.connectors.github.entity;

import java.io.Serializable;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

import lombok.Data;

@Entity(name = "GitHubAccountEntity")
@ExoEntity
@Table(name = "GAM_GITHUB_ACCOUNTS")
@Data
@NamedQueries({
    @NamedQuery(name = "GitHubAccountEntity.getAccountByGithubId", query = "SELECT account FROM GitHubAccountEntity account where account.gitHubId = :gitHubId "),
    @NamedQuery(name = "GitHubAccountEntity.getAccountByUserName", query = "SELECT account FROM GitHubAccountEntity account where account.userName = :userName ")

})
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
