/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2022 Meeds Association contact@meeds.io
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
package org.exoplatform.gamification.github.services;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.gamification.github.dao.GitHubHookDAO;
import org.exoplatform.gamification.github.entity.GitHubHookEntity;
import org.exoplatform.gamification.github.exception.GithubHookException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GithubHooksManagement {

  /**
   * 
   */
  private static final String   ERRORS_GITHUB_NODE  = "errors";

  private static final Log      LOG                 = ExoLogger.getLogger(GithubHooksManagement.class);

  private static final String   GITHUB_API_URL      = "https://api.github.com/repos";

  private static final String   MESSAGE_GITHUB_NODE = "message";

  private static final String[] GITHUB_EVENTS       = {
      "push",
      "pull_request",
      "pull_request_review",
      "pull_request_review_comment",
      "pull_request_review_comment"
  };

  private String                token               = "";

  private String                secret              = "";

  private String                environment         = "";

  private String                webhookUrl          = "portal/rest/gamification/connectors/github/webhooks";

  private ListenerService       listenerService;

  private GitHubHookDAO         gitHubHookDAO;

  public GithubHooksManagement(ListenerService listenerService, GitHubHookDAO gitHubHookDAO) {
    this.listenerService = listenerService;
    this.gitHubHookDAO = gitHubHookDAO;
    this.secret = System.getProperty("gamification.connectors.github.hook.secret");
    this.token = System.getProperty("gamification.connectors.github.hook.token");
    this.environment = System.getProperty("gamification.connectors.github.exo.environment");
    if (System.getProperty("gamification.connectors.github.hook.url") != null) {
      this.webhookUrl = System.getProperty("gamification.connectors.github.hook.url");
    }
  }

  public Long addHook(String webhook, String org, String repo, boolean active) throws IOException, GithubHookException {
    List<GitHubHookEntity> webhooks = getHooksByOrgRepoAndEnvironment(org, repo, environment);
    if (!webhooks.isEmpty()) {
      throw new GithubHookException("WebHook already exists");
    }

    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + "/" + org + "/" + repo + "/hooks";
    try {
      config.put("url", webhook);
      config.put("content_type", "json");
      config.put("insecure_ssl", "0");
      config.put("secret", secret);
      hook.put("name", "web");
      hook.put("active", active);
      hook.put("config", config);
      hook.put("events", GITHUB_EVENTS);
    } catch (JSONException e) {
      LOG.error(e);
    }

    URL obj = new URL(url);
    HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
    postConnection.setRequestMethod("POST");
    setAuthorizationHeader(postConnection);
    postConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    postConnection.setDoOutput(true);
    try (OutputStream os = postConnection.getOutputStream()) {
      os.write(hook.toString().getBytes(StandardCharsets.UTF_8));
      os.flush();
    } catch (IOException e) {
      LOG.error(e);
    }
    int responseCode = postConnection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_CREATED) { // success
      try (InputStream in = postConnection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        return infoNode.get("id").longValue();
      } catch (IOException e) {
        LOG.error(e);
      }
    } else {
      try (InputStream in = postConnection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        String errMessage = infoNode.get(MESSAGE_GITHUB_NODE).textValue();
        if (infoNode.get(ERRORS_GITHUB_NODE) != null) {
          errMessage = errMessage + ": " + infoNode.get(ERRORS_GITHUB_NODE)
                                                   .elements()
                                                   .next()
                                                   .get(MESSAGE_GITHUB_NODE)
                                                   .textValue();
        }
        throw new GithubHookException(errMessage);
      } catch (IOException e) {
        LOG.error(e);
      }
    }
    return null;
  }

  public void updateHook(GitHubHookEntity webhook, String fullPath) throws IOException, GithubHookException {
    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + "/" + webhook.getOrganization() + "/" + webhook.getRepo() + "/hooks/" + webhook.getGithubId();
    try {
      config.put("url", fullPath);
      config.put("content_type", "json");
      config.put("insecure_ssl", "0");
      config.put("secret", secret);
      hook.put("name", "web");
      hook.put("active", webhook.getEnabled());
      hook.put("config", config);
      hook.put("events", GITHUB_EVENTS);
    } catch (JSONException e) {
      LOG.error(e);
    }

    URL obj = new URL(url);
    HttpURLConnection patchConnection = (HttpURLConnection) obj.openConnection();
    patchConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
    patchConnection.setRequestMethod("POST");
    setAuthorizationHeader(patchConnection);
    patchConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    patchConnection.setDoOutput(true);
    try (OutputStream os = patchConnection.getOutputStream()) {
      os.write(hook.toString().getBytes(StandardCharsets.UTF_8));
      os.flush();
    } catch (IOException e) {
      LOG.error(e);
    }
    int responseCode = patchConnection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) { // success
      updateHookEntity(webhook);
    } else {
      try (InputStream in = patchConnection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        String errMessage = infoNode.get(MESSAGE_GITHUB_NODE).textValue();
        if (infoNode.get(ERRORS_GITHUB_NODE) != null) {
          errMessage =
                     errMessage + ": " + infoNode.get(ERRORS_GITHUB_NODE).elements().next().get(MESSAGE_GITHUB_NODE).textValue();
        }
        throw new GithubHookException(errMessage);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public void deleteHook(GitHubHookEntity webhook) throws IOException, GithubHookException {
    String url = GITHUB_API_URL + "/" + webhook.getOrganization() + "/" + webhook.getRepo() + "/hooks/" + webhook.getGithubId();

    URL obj = new URL(url);
    HttpURLConnection deleteConnection = (HttpURLConnection) obj.openConnection();
    deleteConnection.setRequestMethod("DELETE");
    setAuthorizationHeader(deleteConnection);
    int responseCode = deleteConnection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) { // success
      deleteHookEntity(webhook);
    } else {
      try (InputStream in = deleteConnection.getInputStream()) {
        String response = IOUtil.getStreamContentAsString(in);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response);
        String errMessage = infoNode.get(MESSAGE_GITHUB_NODE).textValue();
        if (infoNode.get(ERRORS_GITHUB_NODE) != null) {
          errMessage =
                     errMessage + ": " + infoNode.get(ERRORS_GITHUB_NODE).elements().next().get(MESSAGE_GITHUB_NODE).textValue();
        }
        throw new GithubHookException(errMessage);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public List<GitHubHookEntity> getAllHooks() {
    return getHooksByExoEnvironment(environment);
  }

  public List<GitHubHookEntity> getHooksByExoEnvironment(String environment) {
    return gitHubHookDAO.getHooksByExoEnvironment(environment);
  }

  public List<GitHubHookEntity> getHooksByOrgRepoAndEnvironment(String org, String repo, String env) {
    return gitHubHookDAO.getHooksByOrgRepoAndEnvironment(org, repo, env);
  }

  public GitHubHookEntity createHook(Long id, GitHubHookEntity hook, boolean enabled) {
    hook.setGithubId(id);
    hook.setEvents("push, pull_request,pull_request_review,pull_request_review_comment,pull_request_review_comment");
    hook.setEnabled(enabled);
    hook.setExoEnvironment(environment);
    hook.setCreatedDate(new Date());
    hook.setUpdatedDate(new Date());
    return gitHubHookDAO.create(hook);
  }

  public GitHubHookEntity updateHookEntity(GitHubHookEntity hook) {
    hook.setUpdatedDate(new Date());
    return gitHubHookDAO.update(hook);
  }

  public void deleteHookEntity(GitHubHookEntity hook) {
    gitHubHookDAO.delete(hook);
  }

  public GitHubHookEntity getHookEntityById(long id) {
    return gitHubHookDAO.find(id);
  }

  public void createGamificationHistory(String ruleTitle, String senderId, String receiverId, String object) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", ruleTitle);
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("object", object);
      listenerService.broadcast("exo.gamification.generic.action", gam, "");
      LOG.info("Github action {} gamified for user {} {} {}",
               ruleTitle,
               senderId,
               (ruleTitle.equals("pullRequestValidated")) ? "from" : "to",
               receiverId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast gamification event", e);
    }
  }

  public void broadcastGithubEvent(String ruleTitle, String senderId, String receiverId, String object) {
    try {
      Map<String, String> gam = new HashMap<>();
      gam.put("ruleTitle", ruleTitle);
      gam.put("senderId", senderId);
      gam.put("receiverId", receiverId);
      gam.put("object", object);
      listenerService.broadcast("exo.github.event", gam, "");
      LOG.info("Github action {} brodcasted for user {}", ruleTitle, senderId);
    } catch (Exception e) {
      LOG.error("Cannot broadcast github event", e);
    }
  }

  public String getToken() {
    return token;
  }

  public String getSecret() {
    return secret;
  }

  public String getExoEnvironment() {
    return environment;
  }

  public String getWebhookUrl() {
    return webhookUrl;
  }

  private void setAuthorizationHeader(HttpURLConnection conection) {
    conection.setRequestProperty("Authorization", "token " + token);
  }

}
