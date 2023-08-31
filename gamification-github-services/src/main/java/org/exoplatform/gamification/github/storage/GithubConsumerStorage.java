package org.exoplatform.gamification.github.storage;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HTTP;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.gamification.github.exception.GithubConnectionException;
import org.exoplatform.gamification.github.model.RemoteOrganization;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.TokenStatus;
import org.exoplatform.gamification.github.model.WebHook;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.exoplatform.gamification.github.utils.Utils.*;
import static org.exoplatform.gamification.github.utils.Utils.extractSubItem;

public class GithubConsumerStorage {

  private HttpClient client;

  @SuppressWarnings("unchecked")
  public WebHook createWebhook(String organizationName, String[] triggers, String accessToken) throws IllegalAccessException {
    String secret = generateRandomSecret(8);
    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + organizationName + "/hooks";
    config.put("url", CommonsUtils.getCurrentDomain() + "/portal/rest/gamification/connectors/github/webhooks");
    config.put("content_type", "json");
    config.put("insecure_ssl", "0");
    config.put("secret", secret);
    hook.put("name", "web");
    hook.put("active", true);
    hook.put("config", config);
    hook.put(EVENTS, triggers);
    URI uri = URI.create(url);
    try {
      String response = processPost(uri, hook.toString(), accessToken);
      if (response != null) {
        Map<String, Object> resultMap = fromJsonStringToMap(response);
        long hookId = Long.parseLong(resultMap.get(ID).toString());
        List<String> events = (List<String>) resultMap.get(EVENTS);
        WebHook webHook = new WebHook();
        webHook.setWebhookId(hookId);
        webHook.setOrganizationName(organizationName);
        webHook.setTriggers(events);
        webHook.setToken(accessToken);
        webHook.setSecret(secret);
        return webHook;
      } else {
        throw new IllegalAccessException("github.unauthorizedOperation");
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to open GitHub connection", e);
    }
  }

  public String deleteWebhookHook(WebHook webHook) {
    URI uri = URI.create(GITHUB_API_URL + webHook.getOrganizationId() + "/hooks/" + webHook.getWebhookId());
    try {
      return processDelete(uri, webHook.getToken());
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to delete GitHub hook");
    }
  }

  public List<RemoteRepository> retrieveOrganizationRepos(WebHook webHook, int page, int perPage) {

    List<RemoteRepository> remoteRepositories = new ArrayList<>();
    String url = GITHUB_API_URL + webHook.getOrganizationId() + "/repos?per_page=" + perPage + "&page=" + page;

    URI uri = URI.create(url);
    try {
      String response = processGet(uri, webHook.getToken());
      if (response != null) {
        Map<String, Object>[] repositoryMaps = fromJsonStringToMapCollection(response);
        for (Map<String, Object> repoMap : repositoryMaps) {
          RemoteRepository remoteRepository = new RemoteRepository();
          long repoId = Long.parseLong(repoMap.get("id").toString());
          String name = (String) repoMap.get("name");
          String description = (String) repoMap.get("description");
          remoteRepository.setId(repoId);
          remoteRepository.setName(name);
          remoteRepository.setDescription(description);
          remoteRepositories.add(remoteRepository);
        }
        return remoteRepositories;
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization repos.");
    }
    return remoteRepositories;
  }

  public int countOrganizationRepos(WebHook webHook) {
    String url = GITHUB_API_URL + webHook.getOrganizationId() + "/repos";
    URI uri = URI.create(url);
    try {
      String response = processGet(uri, webHook.getToken());
      if (response != null) {
        Map<String, Object>[] repositoryMaps = fromJsonStringToMapCollection(response);
        return (int) Arrays.stream(repositoryMaps).count();
      }
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization repos.");
    }
    return 0;
  }

  public RemoteOrganization retrieveRemoteOrganization(String organizationName,
                                                       String accessToken) throws ObjectNotFoundException {
    URI uri = URI.create(GITHUB_API_URL + organizationName);
    String response;
    try {
      response = processGet(uri, accessToken);
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization info.", e);
    }
    if (response == null) {
      throw new ObjectNotFoundException("github.organizationNotFound");
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteOrganization gitHubOrganization = new RemoteOrganization();
    gitHubOrganization.setId((Long.parseLong(resultMap.get(ID).toString())));
    gitHubOrganization.setName(resultMap.get(LOGIN).toString());
    gitHubOrganization.setTitle(resultMap.get(NAME) != null ? resultMap.get(NAME).toString() : resultMap.get(LOGIN).toString());
    gitHubOrganization.setDescription(resultMap.get(DESCRIPTION) != null ? resultMap.get(DESCRIPTION).toString() : "");
    gitHubOrganization.setAvatarUrl(resultMap.get(AVATAR_URL).toString());
    return gitHubOrganization;
  }

  public RemoteOrganization retrieveRemoteOrganization(long organizationId, String accessToken) {
    URI uri = URI.create(GITHUB_API_URL + organizationId);
    String response;
    try {
      response = processGet(uri, accessToken);
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub organization info.", e);
    }
    Map<String, Object> resultMap = fromJsonStringToMap(response);
    RemoteOrganization gitHubOrganization = new RemoteOrganization();
    gitHubOrganization.setId((Long.parseLong(resultMap.get(ID).toString())));
    gitHubOrganization.setName(resultMap.get(LOGIN).toString());
    gitHubOrganization.setTitle(resultMap.get(NAME) != null ? resultMap.get(NAME).toString() : resultMap.get(LOGIN).toString());
    gitHubOrganization.setDescription(resultMap.get(DESCRIPTION) != null ? resultMap.get(DESCRIPTION).toString() : "");
    gitHubOrganization.setAvatarUrl(resultMap.get(AVATAR_URL).toString());
    return gitHubOrganization;
  }

  public TokenStatus checkGitHubTokenStatus(String token) {
    URI uri = URI.create("https://api.github.com/rate_limit");
    String response;
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    request.setHeader(AUTHORIZATION, TOKEN + token);
    HttpResponse httpResponse;
    try {
      httpResponse = httpClient.execute(request);
      boolean isSuccess = httpResponse != null
          && (httpResponse.getStatusLine().getStatusCode() >= 200 && httpResponse.getStatusLine().getStatusCode() < 300);
      if (isSuccess) {
        response = processSuccessResponse(httpResponse);
        Map<String, Object> resultMap = fromJsonStringToMap(response);
        String remaining = extractSubItem(resultMap, "resources", "core", "remaining");
        String reset = extractSubItem(resultMap, "resources", "core", "reset");
        TokenStatus tokenStatus = new TokenStatus();
        tokenStatus.setValid(true);
        if (StringUtils.isNotBlank(remaining)) {
          tokenStatus.setRemaining(Long.parseLong(remaining));
        }
        if (StringUtils.isNotBlank(reset)) {
          tokenStatus.setReset(Long.parseLong(reset));
        }
        return tokenStatus;
      } else if (httpResponse != null && httpResponse.getStatusLine().getStatusCode() == 401) {
        return new TokenStatus(false, null, null);
      } else {
        return null;
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to retrieve personal access token status", e);
    }
  }

  public String forceUpdateWebhook(WebHook webHook) {
    long organizationId = webHook.getOrganizationId();
    URI uri = URI.create(GITHUB_API_URL + organizationId + "/hooks/" + webHook.getWebhookId());
    try {
      return processGet(uri, webHook.getToken());
    } catch (GithubConnectionException e) {
      throw new IllegalStateException("Unable to retrieve GitHub webhook info.", e);
    }
  }

  private String processGet(URI uri, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpGet request = new HttpGet(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processPost(URI uri, String jsonString, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpPost request = new HttpPost(uri);
    StringEntity entity = new StringEntity(jsonString, ContentType.APPLICATION_JSON);
    try {
      request.setHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      request.setEntity(entity);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processDelete(URI uri, String accessToken) throws GithubConnectionException {
    HttpClient httpClient = getHttpClient();
    HttpDelete request = new HttpDelete(uri);
    try {
      request.setHeader(AUTHORIZATION, TOKEN + accessToken);
      return processRequest(httpClient, request);
    } catch (IOException e) {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR, e);
    }
  }

  private String processRequest(HttpClient httpClient, HttpRequestBase request) throws IOException, GithubConnectionException {
    HttpResponse response = httpClient.execute(request);
    boolean isSuccess = response != null
        && (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 300);
    if (isSuccess) {
      return processSuccessResponse(response);
    } else if (response != null && response.getStatusLine().getStatusCode() == 404) {
      return null;
    } else {
      processErrorResponse(response);
      return null;
    }
  }

  private String processSuccessResponse(HttpResponse response) throws IOException {
    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
      return String.valueOf(HttpStatus.SC_NO_CONTENT);
    } else if ((response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED
        || response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) && response.getEntity() != null
        && response.getEntity().getContentLength() != 0) {
      try (InputStream is = response.getEntity().getContent()) {
        return IOUtils.toString(is, StandardCharsets.UTF_8);
      }
    } else {
      return null;
    }
  }

  private void processErrorResponse(HttpResponse response) throws GithubConnectionException, IOException {
    if (response == null) {
      throw new GithubConnectionException("Error when connecting github");
    } else if (response.getEntity() != null) {
      try (InputStream is = response.getEntity().getContent()) {
        String errorMessage = IOUtils.toString(is, StandardCharsets.UTF_8);
        if (StringUtils.contains(errorMessage, "")) {
          throw new GithubConnectionException(errorMessage);
        } else {
          throw new GithubConnectionException(GITHUB_CONNECTION_ERROR + errorMessage);
        }
      }
    } else {
      throw new GithubConnectionException(GITHUB_CONNECTION_ERROR + response.getStatusLine().getStatusCode());
    }
  }

  private HttpClient getHttpClient() {
    if (client == null) {
      HttpClientConnectionManager clientConnectionManager = getClientConnectionManager();
      HttpClientBuilder httpClientBuilder = HttpClients.custom()
                                                       .setConnectionManager(clientConnectionManager)
                                                       .setConnectionReuseStrategy(new DefaultConnectionReuseStrategy());
      client = httpClientBuilder.build();
    }
    return client;
  }

  private HttpClientConnectionManager getClientConnectionManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(10);
    return connectionManager;
  }

  public void clearCache() { // NOSONAR
    // implemented in cached storage
  }

  public void clearCache(WebHook webHook) {
    // implemented in cached storage
  }
}
