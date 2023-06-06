package org.exoplatform.gamification.github.plugin;

import com.github.scribejava.apis.GitHubApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import io.meeds.gamification.plugin.ConnectorPlugin;
import io.meeds.oauth.exception.OAuthException;
import io.meeds.oauth.exception.OAuthExceptionCode;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.gamification.github.model.GithubAccessTokenContext;
import org.exoplatform.gamification.github.model.GithubAccount;
import org.exoplatform.gamification.github.services.GithubAccountService;
import org.exoplatform.services.security.Identity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class GithubConnectorPlugin extends ConnectorPlugin {

  private static final String        CONNECTOR_NAME     = "github";

  private static final String        CONNECTOR_SCOPE    = "user:email";

  private static final String        CONNECTOR_REST_API = "https://api.github.com/user";

  private final String               connectorAPIKey;

  private final String               connectorSecretKey;

  private final String               connectorRedirectURL;

  private final GithubAccountService githubAccountService;

  private OAuth20Service             oAuth20Service;

  public GithubConnectorPlugin(GithubAccountService githubAccountService, InitParams initParams) {
    this.githubAccountService = githubAccountService;

    this.connectorAPIKey = initParams.containsKey("connectorAPIKey") ? initParams.getValueParam("connectorAPIKey").getValue()
                                                                     : null;

    this.connectorSecretKey = initParams.containsKey("connectorSecretKey") ? initParams.getValueParam("connectorSecretKey")
                                                                                       .getValue()
                                                                           : null;

    this.connectorRedirectURL = initParams.containsKey("connectorRedirectURL") ? initParams.getValueParam("connectorRedirectURL")
                                                                                           .getValue()
                                                                               : null;

    this.oAuth20Service = new ServiceBuilder(connectorAPIKey).apiSecret(connectorSecretKey)
                                                             .callback(connectorRedirectURL)
                                                             .build(GitHubApi.instance());

  }

  @Override
  public String connect(String accessToken,
                        Identity identity) throws IOException, ExecutionException, ObjectAlreadyExistsException {
    oAuth20Service = new ServiceBuilder(connectorAPIKey).apiSecret(connectorSecretKey)
                                                        .defaultScope(CONNECTOR_SCOPE)
                                                        .callback(connectorRedirectURL)
                                                        .build(GitHubApi.instance());
    if (StringUtils.isNotBlank(accessToken)) {
      OAuth2AccessToken oAuth2AccessToken = null;
      try {
        oAuth2AccessToken = oAuth20Service.getAccessToken(accessToken);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      GithubAccessTokenContext accessTokenContext = new GithubAccessTokenContext(oAuth2AccessToken);
      String githubIdentifier = fetchUsernameFromAccessToken(accessTokenContext);
      githubAccountService.saveGithubAccount(githubIdentifier, identity.getUserId());

      return githubIdentifier;
    } else {
      throw new OAuthException(OAuthExceptionCode.USER_DENIED_SCOPE, "User denied scope on Github authorization page");
    }
  }

  @Override
  public void disconnect(String username) throws ObjectNotFoundException {
    githubAccountService.deleteAccountByUsername(username);
  }

  @Override
  public boolean isConnected(String username) {
    return githubAccountService.getAccountByUserName(username) != null;
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

  @Override
  public String getConnectorApiKey() {
    return connectorAPIKey;
  }

  @Override
  public String getConnectorRedirectURL() {
    return connectorRedirectURL;
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
}
