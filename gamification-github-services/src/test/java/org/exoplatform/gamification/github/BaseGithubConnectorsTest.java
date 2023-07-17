package org.exoplatform.gamification.github;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.*;

import org.junit.*;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.gamification.github.dao.GitHubHookDAO;
import org.exoplatform.gamification.github.entity.GitHubHookEntity;

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
    RequestLifeCycle.end();
    RequestLifeCycle.begin(container);
    if (!entitiesToClean.isEmpty()) {
      for (Serializable entity : entitiesToClean) {
        if (entity instanceof GitHubHookEntity) {
          gitHubHookDAO.delete((GitHubHookEntity) entity);
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

}
