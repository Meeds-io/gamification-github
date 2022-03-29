package org.exoplatform.gamification.connectors.github.listener;

import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.connectors.github.entity.GitHubAccountEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

public class GithubProfileListener extends ProfileListenerPlugin {

  public static final String GITHUB_TYPE = "github";

  private static final Log   LOG         = ExoLogger.getLogger(GithubProfileListener.class);

  protected GitHubAccountDAO gitHubAccountDAO;

  public GithubProfileListener(GitHubAccountDAO gitHubAccountDAO) {
    this.gitHubAccountDAO = gitHubAccountDAO;
  }

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void basicInfoUpdated(ProfileLifeCycleEvent event) {
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    String gitHubId = "";
    List<HashMap<String, String>> ims = (List<HashMap<String, String>>) event.getProfile().getProperty("ims");
    for (HashMap<String, String> map : ims) {
      if (map.get("key").equals(GITHUB_TYPE)) {
        gitHubId = map.get("value");
      }
    }
    if (!gitHubId.isEmpty()) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        GitHubAccountEntity entity = gitHubAccountDAO.getAccountByUserName(event.getUsername());
        if (entity == null || !entity.getGitHubId().equals(gitHubId)) {
          GitHubAccountEntity existingEntity = gitHubAccountDAO.getAccountByGithubId(gitHubId);
          if (existingEntity == null) {
            if (entity == null) {
              entity = new GitHubAccountEntity();
              entity.setUserName(event.getUsername());
              entity.setGitHubId(gitHubId);
              gitHubAccountDAO.create(entity);
            } else {
              entity.setGitHubId(gitHubId);
              gitHubAccountDAO.update(entity);
            }

          } else {
            LOG.warn("The provided Github ID {} is already used by {}", gitHubId, existingEntity.getUserName());
          }
        }
      } catch (Exception e) {
        LOG.error("Could not retrieve and save Github account in user profile");
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void headerSectionUpdated(ProfileLifeCycleEvent event) {

  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {

  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {

  }

}
