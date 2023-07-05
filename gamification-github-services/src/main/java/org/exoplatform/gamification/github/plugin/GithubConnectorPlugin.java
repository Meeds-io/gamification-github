/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.exoplatform.gamification.github.plugin;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.meeds.gamification.model.RemoteConnectorSettings;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.gamification.service.ConnectorSettingService;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.model.GithubAccessTokenContext;
import org.exoplatform.gamification.github.model.GithubAccount;
import org.exoplatform.gamification.github.services.GithubAccountService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.Identity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class GithubConnectorPlugin extends ConnectorPlugin {

  private static final Log              LOG                = ExoLogger.getLogger(GithubConnectorPlugin.class);

  private static final String           CONNECTOR_NAME     = "github";

  private static final String           CONNECTOR_SCOPE    = "user:email";

  private static final String           CONNECTOR_REST_API = "https://api.github.com/user";

  private final GithubAccountService    githubAccountService;

  private final ConnectorSettingService connectorSettingService;

  private OAuth20Service                oAuthService;

  private long                          remoteConnectorId;

  public GithubConnectorPlugin(GithubAccountService githubAccountService, ConnectorSettingService connectorSettingService) {
    this.githubAccountService = githubAccountService;
    this.connectorSettingService = connectorSettingService;
  }

  @Override
  public String connect(String accessToken, Identity identity) throws OAuthException, ObjectAlreadyExistsException {
    RemoteConnectorSettings remoteConnectorSettings = connectorSettingService.getConnectorSettings(CONNECTOR_NAME);
    if (StringUtils.isBlank(remoteConnectorSettings.getApiKey()) || StringUtils.isBlank(remoteConnectorSettings.getSecretKey())) {
      LOG.warn("Missing '{}' connector settings", CONNECTOR_NAME);
      return null;
    }
    if (StringUtils.isNotBlank(accessToken)) {
      try {
        OAuth2AccessToken oAuth2AccessToken = getOAuthService(remoteConnectorSettings).getAccessToken(accessToken);
        GithubAccessTokenContext accessTokenContext = new GithubAccessTokenContext(oAuth2AccessToken);
        String githubIdentifier = fetchUsernameFromAccessToken(accessTokenContext);
        if (StringUtils.isBlank(githubIdentifier)) {
          throw new OAuthException(OAuthExceptionCode.INVALID_STATE, "User Github identifier is empty");
        }
        githubAccountService.saveGithubAccount(githubIdentifier, identity.getUserId());
        return githubIdentifier;
      } catch (InterruptedException | IOException e) { // NOSONAR
        throw new OAuthException(OAuthExceptionCode.IO_ERROR, e);
      } catch (ExecutionException e) {
        throw new OAuthException(OAuthExceptionCode.UNKNOWN_ERROR, e);
      }
    } else {
      throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, "User denied scope on Github authorization page");
    }
  }

  @Override
  public void disconnect(String username) throws ObjectNotFoundException {
    githubAccountService.deleteAccountByUsername(username);
  }

  @Override
  public String getIdentifier(String username) {
    GithubAccount gitHubAccount = githubAccountService.getAccountByUserName(username);
    return gitHubAccount != null ? gitHubAccount.getGitHubId() : null;
  }

  @Override
  public String getConnectorName() {
    return CONNECTOR_NAME;
  }

  private static String fetchUsernameFromAccessToken(GithubAccessTokenContext accessToken) throws IOException {
    URL url = new URL(CONNECTOR_REST_API);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setRequestProperty("Authorization", "Bearer " + accessToken.getAccessToken());
    int responseCode = connection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          response.append(line);
        }
        return response.toString().split("\"login\":")[1].split(",")[0].replace("\"", "").trim();
      }
    } else {
      throw new IOException("Error retrieving user information from GitHub. Response code: " + responseCode);
    }
  }

  public OAuth20Service getOAuthService(RemoteConnectorSettings remoteConnectorSettings) {
    if (oAuthService == null || remoteConnectorSettings.hashCode() != remoteConnectorId) {
      remoteConnectorId = remoteConnectorSettings.hashCode();
      oAuthService = new ServiceBuilder(remoteConnectorSettings.getApiKey()).apiSecret(remoteConnectorSettings.getSecretKey())
                                                                            .callback(remoteConnectorSettings.getRedirectUrl())
                                                                            .defaultScope(CONNECTOR_SCOPE)
                                                                            .build(GitHubApi.instance());
    }
    return oAuthService;
  }

}
