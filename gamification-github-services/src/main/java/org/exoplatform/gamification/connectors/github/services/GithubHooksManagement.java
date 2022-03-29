package org.exoplatform.gamification.connectors.github.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.gamification.connectors.github.dao.GitHubAccountDAO;
import org.exoplatform.gamification.connectors.github.dao.GitHubHookDAO;
import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;
import org.exoplatform.gamification.connectors.github.exception.GithubHookException;
import org.exoplatform.services.listener.ListenerService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class GithubHooksManagement {

  private final Log        LOG            = ExoLogger.getLogger(GithubHooksManagement.class);

  private final String[]   EVENTS         = { "push", "pull_request", "pull_request_review", "pull_request_review_comment",
      "pull_request_review_comment" };

  private String           TOKEN          = "";

  private String           SECRET         = "";

  private String           EXO_ENVIRONMENT         = "";

  private String           GITHUB_API_URL = "https://api.github.com/repos";

  private String          WEBHOOK_URL = "portal/rest/gamification/connectors/github/webhooks";

  private ListenerService  listenerService;

  private GitHubAccountDAO gitHubAccountDAO;

  private GitHubHookDAO    gitHubHookDAO;

  public GithubHooksManagement(ListenerService listenerService, GitHubAccountDAO gitHubAccountDAO, GitHubHookDAO gitHubHookDAO) {
    this.listenerService = listenerService;
    this.gitHubAccountDAO = gitHubAccountDAO;
    this.gitHubHookDAO = gitHubHookDAO;
    this.SECRET = System.getProperty("gamification.connectors.github.hook.secret");
    this.TOKEN = System.getProperty("gamification.connectors.github.hook.token");
    this.EXO_ENVIRONMENT = System.getProperty("gamification.connectors.github.exo.environment");
    if(System.getProperty("gamification.connectors.github.hook.url")!=null){
      this.WEBHOOK_URL = System.getProperty("gamification.connectors.github.hook.url");
    }
  }

  public int getHooksFromGithub(String org, String repo) throws IOException {
    String url = GITHUB_API_URL + "/" + org + "/" + repo + "/hooks";
    URL urlForGetRequest = new URL(url);
    String readLine = null;
    HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
    conection.setRequestMethod("GET");
    conection.setRequestProperty("Authorization", "token " + TOKEN);
    int responseCode = conection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(conection.getInputStream()))) {
        StringBuffer response = new StringBuffer();
        while ((readLine = in.readLine()) != null) {
          response.append(readLine);
        }
      } catch (IOException e) {
        LOG.error(e);
      }
    }
    return responseCode;
  }

  public Long addHook(String webhook, String org, String repo, boolean active) throws IOException, GithubHookException {
    if(getHooksByOrgRepoAndEnvironment(org,repo,EXO_ENVIRONMENT).size()>0){
      throw new GithubHookException("WebHook already exists");
    }

    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + "/" + org + "/" + repo + "/hooks";
    try {
      config.put("url", webhook);
      config.put("content_type", "json");
      config.put("insecure_ssl", "0");
      config.put("secret", SECRET);
      hook.put("name", "web");
      hook.put("active", active);
      hook.put("config", config);
      hook.put("events", EVENTS);
    } catch (JSONException e) {
      LOG.error(e);
    }

    URL obj = new URL(url);
    HttpURLConnection postConnection = (HttpURLConnection) obj.openConnection();
    postConnection.setRequestMethod("POST");
    postConnection.setRequestProperty("Authorization", "token " + TOKEN);
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
      try (BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getInputStream()))) {
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response.toString());
        return infoNode.get("id").longValue();
      } catch (IOException e) {
        LOG.error(e);
      }
    } else {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(postConnection.getErrorStream()))) {
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response.toString());
        String errMessage = infoNode.get("message").textValue();
        if (infoNode.get("errors") != null) {
          errMessage = errMessage + ": " + infoNode.get("errors").elements().next().get("message").textValue();
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
      config.put("secret", SECRET);
      hook.put("name", "web");
      hook.put("active", webhook.getEnabled());
      hook.put("config", config);
      hook.put("events", EVENTS);
    } catch (JSONException e) {
      LOG.error(e);
    }

    URL obj = new URL(url);
    HttpURLConnection patchConnection = (HttpURLConnection) obj.openConnection();
    patchConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
    patchConnection.setRequestMethod("POST");
    patchConnection.setRequestProperty("Authorization", "token " + TOKEN);
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
      try (BufferedReader in = new BufferedReader(new InputStreamReader(patchConnection.getErrorStream()))) {
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response.toString());
        String errMessage = infoNode.get("message").textValue();
        if (infoNode.get("errors") != null) {
          errMessage = errMessage + ": " + infoNode.get("errors").elements().next().get("message").textValue();
        }
        throw new GithubHookException(errMessage);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public void deleteHook(GitHubHookEntity webhook) throws IOException, GithubHookException {
    JSONObject config = new JSONObject();
    JSONObject hook = new JSONObject();
    String url = GITHUB_API_URL + "/" + webhook.getOrganization() + "/" + webhook.getRepo() + "/hooks/" + webhook.getGithubId();

    URL obj = new URL(url);
    HttpURLConnection deleteConnection = (HttpURLConnection) obj.openConnection();
    deleteConnection.setRequestMethod("DELETE");
    deleteConnection.setRequestProperty("Authorization", "token " + TOKEN);
    int responseCode = deleteConnection.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) { // success
      deleteHookEntity(webhook);
    } else {
      try (BufferedReader in = new BufferedReader(new InputStreamReader(deleteConnection.getErrorStream()))) {
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
          response.append(inputLine);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(response.toString());
        String errMessage = infoNode.get("message").textValue();
        if (infoNode.get("errors") != null) {
          errMessage = errMessage + ": " + infoNode.get("errors").elements().next().get("message").textValue();
        }
        throw new GithubHookException(errMessage);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  public List<GitHubHookEntity> getAllHooks() {
    return getHooksByExoEnvironment(EXO_ENVIRONMENT);
  }

  public List<GitHubHookEntity> getHooksByExoEnvironment(String environment) {
    return gitHubHookDAO.getHooksByExoEnvironment(environment);
  }

  public List<GitHubHookEntity> getHooksByOrgRepoAndEnvironment(String org,String repo,String env) {
    return gitHubHookDAO.getHooksByOrgRepoAndEnvironment(org,repo,env);
  }

  public GitHubHookEntity createHook(Long id, GitHubHookEntity hook, boolean enabled) {
    hook.setGithubId(id);
    hook.setEvents("push, pull_request,pull_request_review,pull_request_review_comment,pull_request_review_comment");
    hook.setEnabled(enabled);
    hook.setExoEnvironment(EXO_ENVIRONMENT);
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
      LOG.info("Github action {} gamified for user {} {} {}", ruleTitle, senderId, (ruleTitle.equals("pullRequestValidated")) ? "from" : "to", receiverId);
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

  public String getUserByGithubId(String id) {
    if (id == null)
      return null;
    try {
      return gitHubAccountDAO.getAccountByGithubId(id).getUserName();
    } catch (NullPointerException e) {
      LOG.error("Cannot get user with GithubId {}", id);
      return null;
    }catch (Exception e) {
      LOG.error("Cannot get user with GithubId {}", id, e);
      return null;
    }
  }

  public String getToken() {
    return TOKEN;
  }

  public String getSecret() {
    return SECRET;
  }

  public String getExoEnvironment() {
    return EXO_ENVIRONMENT;
  }

  public String getWEBHOOK_URL() {
    return WEBHOOK_URL;
  }
}
