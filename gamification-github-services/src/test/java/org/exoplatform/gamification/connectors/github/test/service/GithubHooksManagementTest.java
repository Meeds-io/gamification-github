package org.exoplatform.gamification.connectors.github.test.service;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;
import org.exoplatform.gamification.connectors.github.services.GithubHooksManagement;
import org.exoplatform.gamification.connectors.github.test.BaseGithubConnectorsTest;

public class GithubHooksManagementTest extends BaseGithubConnectorsTest {

  protected long    id           = 1111;

  protected String  organization = "organization";

  protected String  repo         = "repository";

  protected String  webhook      = "webhook";

  protected String  eXoEnvironment     = "testEnvironment";

  protected String  events       =
                           "push, pull_request,pull_request_review,pull_request_review_comment,pull_request_review_comment";

  protected boolean enabled      = true;

  /**
   * Check that service is instantiated and functional
   */
  @Test
  public void testServiceInstantiated() {
    GithubHooksManagement githubHooksManagement = getService(GithubHooksManagement.class);
    assertNotNull(githubHooksManagement);
  }

  @Test
  public void testGetAllHooks() {

    GithubHooksManagement githubHooksManagement = getService(GithubHooksManagement.class);
    List<GitHubHookEntity> list = githubHooksManagement.getHooksByExoEnvironment(eXoEnvironment);
    assertNotNull(list);
    assertEquals(0, list.size());
    newGitHubHookEntity(id, organization, repo, webhook, events, eXoEnvironment, enabled);
    list = githubHooksManagement.getHooksByExoEnvironment(eXoEnvironment);
    assertNotNull(list);
    assertEquals(1, list.size());

  }

  @Test
  public void testCreateHook() {

    GithubHooksManagement githubHooksManagement = getService(GithubHooksManagement.class);
    List<GitHubHookEntity> list = githubHooksManagement.getHooksByExoEnvironment(eXoEnvironment);
    assertNotNull(list);
    assertEquals(0, list.size());
    newGitHubHookEntity(id, organization, repo, webhook, events, eXoEnvironment, enabled);
    list = githubHooksManagement.getHooksByExoEnvironment(eXoEnvironment);
    assertNotNull(list);
    assertEquals(1, list.size());
  }

  @Test
  public void testUpdateHookEntity() {

    GithubHooksManagement githubHooksManagement = getService(GithubHooksManagement.class);
    GitHubHookEntity hook = newGitHubHookEntity(id, organization, repo, webhook, events, eXoEnvironment, true);
    GitHubHookEntity entity = githubHooksManagement.getHookEntityById(hook.getId());
    assertNotNull(entity);
    assertEquals(true, entity.getEnabled());
    hook.setEnabled(false);
    githubHooksManagement.updateHookEntity(hook);
    entity = githubHooksManagement.getHookEntityById(hook.getId());
    assertNotNull(entity);
    assertEquals(false, entity.getEnabled());

  }

  @Test
  public void testDeleteHookEntity() {

    GithubHooksManagement githubHooksManagement = getService(GithubHooksManagement.class);
    GitHubHookEntity hook = newGitHubHookEntity(id, organization, repo, webhook, events, eXoEnvironment, enabled);
    GitHubHookEntity entity = githubHooksManagement.getHookEntityById(hook.getId());
    assertNotNull(entity);
    githubHooksManagement.deleteHookEntity(hook);
    entity = githubHooksManagement.getHookEntityById(hook.getId());
    assertNull(entity);

  }

}
