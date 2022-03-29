package org.exoplatform.gamification.connectors.github.test.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.connectors.github.entity.GitHubAccountEntity;
import org.exoplatform.gamification.connectors.github.test.BaseGithubConnectorsTest;

public class GitHubAccountDAOTest extends BaseGithubConnectorsTest {

  protected String gitHubId = "root_git";

  protected String userName = "root";

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    GitHubAccountDAO gitHubHookDAO = getService(GitHubAccountDAO.class);
    assertNotNull(gitHubHookDAO);
  }

  @Test
  public void getAccountByGithubId() {
    GitHubAccountDAO gitHubHookDAO = getService(GitHubAccountDAO.class);
    GitHubAccountEntity entity = gitHubHookDAO.getAccountByGithubId(gitHubId);
    assertEquals(null, entity);
    newGitHubAccountEntity(gitHubId, userName);
    entity = gitHubHookDAO.getAccountByGithubId(gitHubId);
    assertNotNull(entity);
  }

  @Test
  public void getAccountByUserName() {
    GitHubAccountDAO gitHubHookDAO = getService(GitHubAccountDAO.class);
    GitHubAccountEntity entity = gitHubHookDAO.getAccountByUserName(userName);
    assertEquals(null, entity);
    newGitHubAccountEntity(gitHubId, userName);
    entity = gitHubHookDAO.getAccountByUserName(userName);
    assertNotNull(entity);
  }

}
