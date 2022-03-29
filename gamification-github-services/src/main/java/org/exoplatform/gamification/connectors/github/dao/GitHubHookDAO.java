package org.exoplatform.gamification.connectors.github.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class GitHubHookDAO extends GenericDAOJPAImpl<GitHubHookEntity, Long> {

  private static final Log LOG = ExoLogger.getLogger(GitHubHookDAO.class);

  public List<GitHubHookEntity> getHooksByExoEnvironment(String exoEnvironment) {

    TypedQuery<GitHubHookEntity>  query = getEntityManager().createNamedQuery("GitHubHookEntity.getHooksByExoEnvironment", GitHubHookEntity.class)
            .setParameter("exoEnvironment", exoEnvironment);

    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      LOG.error("Error occurred when trying to get list of hooks be exoEnvironment {}", exoEnvironment, e);
      return null;
    }
  }

  public List<GitHubHookEntity> getHooksByOrgRepoAndEnvironment(String org,String repo, String exoEnvironment) {

    TypedQuery<GitHubHookEntity>  query = getEntityManager().createNamedQuery("GitHubHookEntity.getHooksByOrgRepoAndEnvironment", GitHubHookEntity.class)
            .setParameter("org", org)
            .setParameter("repo", repo)
            .setParameter("exoEnvironment", exoEnvironment);

    try {
      return query.getResultList();
    } catch (NoResultException e) {
      return null;
    } catch (Exception e) {
      LOG.error("Error occurred when trying to get list of hooks by  Org {} And Repo {}", org,repo, e);
      return null;
    }
  }


}
