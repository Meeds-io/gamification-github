package org.exoplatform.gamification.connectors.github.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.gamification.connectors.github.services.GithubHooksManagement;
import org.exoplatform.gamification.connectors.github.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

@Path("/gamification/connectors/github/")
@Produces(MediaType.APPLICATION_JSON)

public class GithubWebHookRest implements ResourceContainer {

  private final Log             LOG = ExoLogger.getLogger(GithubWebHookRest.class);

  private GithubHooksManagement githubHooksManagement;

  public GithubWebHookRest(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("webhooks")
  public Response gitHubEvent(@HeaderParam("x-github-event") String event,
                              @HeaderParam("x-hub-signature") String signature,
                              String obj) {

    if (Utils.verifySignature(obj, signature, githubHooksManagement.getSecret())) {

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(obj);
        String ruleTitle = "";
        String senderId = "";
        String receiverId = "";
        String object = "";
        String repository = "";
        String githubId = "";

        switch (event) {
        case "push": {
          ruleTitle = "pushCode";
          githubId=infoNode.get("pusher").get("name").textValue();
          senderId = githubHooksManagement.getUserByGithubId(githubId);
          repository = infoNode.get("repository").get("full_name").textValue();
          LOG.info("Github hook for the push event is called by {} with githubId : {}  on the repository : {}",senderId, githubId, repository);
          if (senderId != null) {
            Identity socialIdentity = getUserSocialId(senderId);
            if (socialIdentity != null) {
              receiverId = senderId;
              object = infoNode.get("head_commit").get("url").textValue();
              LOG.info("service=gamification-github-connector operation=push parameters=\"user_social_id:{},repository:{}\"", socialIdentity.getId(), repository);
              githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
            }
          }
        }

          break;
        case "pull_request": {
          githubId=infoNode.get("sender").get("login").textValue();
          senderId = githubHooksManagement.getUserByGithubId(githubId);
          repository = infoNode.get("repository").get("full_name").textValue();
          LOG.info("Github hook for the pull_request event is called by {} with githubId : {}  on the repository : {}",senderId, githubId, repository);
          if (infoNode.get("action").textValue().equals("opened")) {
            ruleTitle = "creatPullRequest";
            if (senderId != null) {
              Identity socialIdentity = getUserSocialId(senderId);
              if (socialIdentity != null) {
                receiverId = senderId;
                object = infoNode.get("pull_request").get("html_url").textValue();
                LOG.info("service=gamification-github-connector operation=pull_request parameters=\"user_social_id:{},repository:{}\"", socialIdentity.getId(), repository);
                githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
              }
            }
          }

        }
          break;
        case "pull_request_review_comment": {
          ruleTitle = "commentPullRequest";
          githubId=infoNode.get("comment").get("user").get("login").textValue();
          senderId = githubHooksManagement.getUserByGithubId(githubId);
          repository = infoNode.get("repository").get("full_name").textValue();
          LOG.info("Github hook for the pull_request_review_comment event is called by {} with githubId : {}  on the repository : {}",senderId, githubId, repository);
          if (senderId != null) {
            Identity socialIdentity = getUserSocialId(senderId);
            if (socialIdentity != null) {
              receiverId = senderId;
              object = infoNode.get("comment").get("_links").get("html").get("href").textValue();
              LOG.info("service=gamification-github-connector operation=pull_request_review_comment parameters=\"user_social_id:{},repository:{}\"", socialIdentity.getId(), repository);
              githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
            }
          }
        }
          break;
        case "pull_request_review": {
          ruleTitle = "reviewPullRequest";
          githubId=infoNode.get("review").get("user").get("login").textValue();
          senderId = githubHooksManagement.getUserByGithubId(githubId);
          repository = infoNode.get("repository").get("full_name").textValue();
          LOG.info("Github hook for the pull_request_review event is called by {} with githubId : {}  on the repository : {}",senderId, githubId, repository);
          if (senderId != null) {
            Identity socialIdentity = getUserSocialId(senderId);
            if (socialIdentity != null) {
              receiverId = senderId;
              object = infoNode.get("review").get("html_url").textValue();
              LOG.info("service=gamification-github-connector operation=pull_request_review parameters=\"user_social_id:{},repository:{}\"", socialIdentity.getId(), repository);
              if (!infoNode.get("review").get("state").textValue().equals("commented")) {
                githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
              }
              if (infoNode.get("review").get("state").textValue().equals("approved")) {
                receiverId =
                        githubHooksManagement.getUserByGithubId(infoNode.get("pull_request").get("user").get("login").textValue());
                ruleTitle = "pullRequestValidated";
                githubHooksManagement.broadcastGithubEvent(ruleTitle, receiverId, senderId, object);
              }
            }
          }
        }
          break;

        }

        return Response.ok().build();
      } catch (Exception e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
      }

    } else {
      LOG.warn("Github hook Rest invoked with wrong secret key");
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

  }


  public Identity getUserSocialId(String userName){
    IdentityManager identityManager = PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    return identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, false);

  }

}
