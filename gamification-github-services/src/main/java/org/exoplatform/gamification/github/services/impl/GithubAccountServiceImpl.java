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
