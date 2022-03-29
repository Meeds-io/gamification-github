package org.exoplatform.gamification.connectors.github.test;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.connectors.github.dao.GitHubHookDAO;
import org.exoplatform.gamification.connectors.github.entity.GitHubAccountEntity;
import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;

public abstract class BaseGithubConnectorsTest {

  protected static PortalContainer container;

  protected List<Serializable>     entitiesToClean = new ArrayList<>();

  @BeforeClass
  public static void beforeTest() {
    container = PortalContainer.getInstance();
    assertNotNull(container);
    assertTrue(container.isStarted());
  }

  @Before
  public void beforeMethodTest() {
    RequestLifeCycle.begin(container);
  }

  @After
  public void afterMethodTest() {
    GitHubHookDAO gitHubHookDAO = getService(GitHubHookDAO.class);
    GitHubAccountDAO gitHubAccountDAO = getService(GitHubAccountDAO.class);

    RequestLifeCycle.end();
    RequestLifeCycle.begin(container);

    if (!entitiesToClean.isEmpty()) {
      for (Serializable entity : entitiesToClean) {
        if (entity instanceof GitHubHookEntity) {
          gitHubHookDAO.delete((GitHubHookEntity) entity);
        } else if (entity instanceof GitHubAccountEntity) {
          gitHubAccountDAO.delete((GitHubAccountEntity) entity);
        } else {
          throw new IllegalStateException("Entity not managed" + entity);
        }
      }
    }

    int hooksCount = gitHubHookDAO.findAll().size();
    assertEquals("The previous test didn't cleaned hooks entities correctly, should add entities to clean into 'entitiesToClean' list.",
                 0,
                 hooksCount);

    RequestLifeCycle.end();
  }

  protected <T> T getService(Class<T> componentType) {
    return container.getComponentInstanceOfType(componentType);
  }

  protected GitHubHookEntity newGitHubHookEntity(long id,
                                                 String organization,
                                                 String repo,
                                                 String webhook,
                                                 String events,
                                                 String exoEnvironment,
                                                 boolean enabled) {
    GitHubHookDAO gitHubHookDAO = getService(GitHubHookDAO.class);
    GitHubHookEntity gitHubHookEntity = new GitHubHookEntity();
    gitHubHookEntity.setGithubId(id);
    gitHubHookEntity.setOrganization(organization);
    gitHubHookEntity.setRepo(repo);
    gitHubHookEntity.setWebhook(webhook);
    gitHubHookEntity.setEnabled(enabled);
    gitHubHookEntity.setExoEnvironment(exoEnvironment);
    gitHubHookEntity.setEvents(events);
    gitHubHookEntity.setCreatedDate(new Date());
    gitHubHookEntity.setUpdatedDate(new Date());
    gitHubHookEntity = gitHubHookDAO.create(gitHubHookEntity);
    entitiesToClean.add(gitHubHookEntity);
    return gitHubHookEntity;
  }

  protected GitHubAccountEntity newGitHubAccountEntity(String gitHubId, String userName) {
    GitHubAccountDAO gitHubAccountDAO = getService(GitHubAccountDAO.class);
    GitHubAccountEntity gitHubAccountEntity = new GitHubAccountEntity();
    gitHubAccountEntity.setGitHubId(gitHubId);
    gitHubAccountEntity.setUserName(userName);
    gitHubAccountEntity = gitHubAccountDAO.create(gitHubAccountEntity);
    entitiesToClean.add(gitHubAccountEntity);
    return gitHubAccountEntity;
  }

}
