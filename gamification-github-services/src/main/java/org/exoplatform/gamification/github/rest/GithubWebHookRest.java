package org.exoplatform.gamification.github.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.gamification.github.services.GithubHooksManagement;
import org.exoplatform.gamification.github.utils.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;

@Path("/gamification/connectors/github/")
@Produces(MediaType.APPLICATION_JSON)
public class GithubWebHookRest implements ResourceContainer {

  private static final String   PULL_REQUEST_REVIEW_NODE_NAME          = "review";

  private static final String   PULL_REQUEST_REVIEW_EVENT_NAME         = "pull_request_review";

  private static final String   PULL_REQUEST_REVIEW_COMMENT_EVENT_NAME = "pull_request_review_comment";

  private static final String   PUSH_EVENT_NAME                        = "push";

  private static final String   GITHUB_USERNAME                        = "login";

  private static final String   PULL_REQUEST_EVENT_NAME                = "pull_request";

  private static final Log      LOG                                    = ExoLogger.getLogger(GithubWebHookRest.class);

  private GithubHooksManagement githubHooksManagement;

  public GithubWebHookRest(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("webhooks")
  public Response githubEvent(// NOSONAR
                              @HeaderParam("x-github-event")
                              String event,
                              @HeaderParam("x-hub-signature")
                              String signature,
                              String obj) {
    if (Utils.verifySignature(obj, signature, githubHooksManagement.getSecret())) {

      try {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode infoNode = objectMapper.readTree(obj);
        String ruleTitle = "";
        String senderId = "";
        String receiverId = "";
        String object = "";
        String githubId = "";

        switch (event) {
          case PUSH_EVENT_NAME: {
            ruleTitle = "pushCode";
            githubId = infoNode.get("pusher").get("name").textValue();
            senderId = githubHooksManagement.getUserByGithubId(githubId);
            if (senderId != null) {
              Identity socialIdentity = getUserSocialId(senderId);
              if (socialIdentity != null) {
                receiverId = senderId;
                object = infoNode.get("head_commit").get("url").textValue();
                githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
              }
            }
          }
            break;
          case PULL_REQUEST_EVENT_NAME: {
            githubId = infoNode.get("sender").get(GITHUB_USERNAME).textValue();
            senderId = githubHooksManagement.getUserByGithubId(githubId);
            if (infoNode.get("action").textValue().equals("opened")) {
              ruleTitle = "creatPullRequest";
              if (senderId != null) {
                Identity socialIdentity = getUserSocialId(senderId);
                if (socialIdentity != null) {
                  receiverId = senderId;
                  object = infoNode.get(PULL_REQUEST_EVENT_NAME).get("html_url").textValue();
                  githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
                }
              }
            }

          }
            break;
          case PULL_REQUEST_REVIEW_COMMENT_EVENT_NAME: {
            ruleTitle = "commentPullRequest";
            githubId = infoNode.get("comment").get("user").get(GITHUB_USERNAME).textValue();
            senderId = githubHooksManagement.getUserByGithubId(githubId);
            if (senderId != null) {
              Identity socialIdentity = getUserSocialId(senderId);
              if (socialIdentity != null) {
                receiverId = senderId;
                object = infoNode.get("comment").get("_links").get("html").get("href").textValue();
                githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
              }
            }
          }
            break;
          case PULL_REQUEST_REVIEW_EVENT_NAME: {
            ruleTitle = "reviewPullRequest";
            githubId = infoNode.get(PULL_REQUEST_REVIEW_NODE_NAME).get("user").get(GITHUB_USERNAME).textValue();
            senderId = githubHooksManagement.getUserByGithubId(githubId);
            if (senderId != null) {
              Identity socialIdentity = getUserSocialId(senderId);
              if (socialIdentity != null) {
                receiverId = senderId;
                object = infoNode.get(PULL_REQUEST_REVIEW_NODE_NAME).get("html_url").textValue();
                if (!infoNode.get(PULL_REQUEST_REVIEW_NODE_NAME).get("state").textValue().equals("commented")) {
                  githubHooksManagement.broadcastGithubEvent(ruleTitle, senderId, receiverId, object);
                }
                if (infoNode.get(PULL_REQUEST_REVIEW_NODE_NAME).get("state").textValue().equals("approved")) {
                  receiverId =
                             githubHooksManagement.getUserByGithubId(infoNode.get(PULL_REQUEST_EVENT_NAME)
                                                                             .get("user")
                                                                             .get(GITHUB_USERNAME)
                                                                             .textValue());
                  ruleTitle = "pullRequestValidated";
                  githubHooksManagement.broadcastGithubEvent(ruleTitle, receiverId, senderId, object);
                }
              }
            }
          }
            break;
          default:
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

  public Identity getUserSocialId(String userName) {
    IdentityManager identityManager = PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    return identityManager.getOrCreateUserIdentity(userName);
  }

}
