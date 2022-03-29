package org.exoplatform.gamification.connectors.github.services;

import org.picocontainer.Startable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.profile.settings.IMType;
import org.exoplatform.social.core.profile.settings.UserProfileSettingsService;

public class GithubIMSetting implements Startable {

  public final String GITHUB_TYPE  = "github";

  public final String GITHUB_TITLE = "Github";

  private final Log   LOG          = ExoLogger.getLogger(GithubIMSetting.class);

  // private UserProfileSettingsService profileSettings;

  /**
   * Instantiates a new github IM provider.
   *
   * @throws ConfigurationException the configuration exception
   */
  public GithubIMSetting() throws ConfigurationException {
    // this.profileSettings=profileSettings;
  }

  @Override
  public void start() {
    UserProfileSettingsService profileSettings = PortalContainer.getInstance()
                                                                .getComponentInstanceOfType(UserProfileSettingsService.class);
    if (profileSettings != null) {
      profileSettings.addIMType(new IMType(GITHUB_TYPE, GITHUB_TITLE));
    } else {
      LOG.warn("Cannot get the Profile Settings");
    }
  }

  @Override
  public void stop() {

  }
}
