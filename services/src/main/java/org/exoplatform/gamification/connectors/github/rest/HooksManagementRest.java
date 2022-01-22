package org.exoplatform.gamification.connectors.github.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.gamification.connectors.github.entity.GitHubHookEntity;
import org.exoplatform.gamification.connectors.github.services.GithubHooksManagement;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.service.rest.RestChecker;
import org.exoplatform.social.service.rest.Util;

import java.net.URI;

@Path("/gamification/connectors/github/hooksmanagement")
@Produces(MediaType.APPLICATION_JSON)

public class HooksManagementRest implements ResourceContainer {

  private final Log             LOG                 = ExoLogger.getLogger(GithubWebHookRest.class);

  private final String          portalContainerName = "portal";

  private final String[]        SUPPORTED_FORMATS   = new String[] { "json" };

  private GithubHooksManagement githubHooksManagement;

  public HooksManagementRest(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @GET
  @RolesAllowed("administrators")
  @Path("hooks")
  public Response gethooks(@Context UriInfo uriInfo) throws Exception {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    MediaType mediaType = RestChecker.checkSupportedFormat("json", SUPPORTED_FORMATS);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      return Response.ok(githubHooksManagement.getAllHooks(), mediaType).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @POST
  @RolesAllowed("administrators")
  @Path("hooks")
  public Response add(@Context UriInfo uriInfo, GitHubHookEntity hook) throws Exception {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      String baseUri = uriInfo.getBaseUri().toString();
      String server_domain = baseUri.split("portal")[0];
      hook.setWebhook(githubHooksManagement.getWEBHOOK_URL());
      String fullPath = server_domain + githubHooksManagement.getWEBHOOK_URL();
      Long id = githubHooksManagement.addHook(fullPath, hook.getOrganization(), hook.getRepo(), hook.getEnabled());
      githubHooksManagement.createHook(id, hook, true);
      LOG.info("New webhook added by {}",sourceIdentity.getRemoteId());
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @PUT
  @RolesAllowed("administrators")
  @Path("hooks/{id}")
  public Response edit(@Context UriInfo uriInfo, @PathParam("id") Long id, GitHubHookEntity hook) throws Exception {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      String baseUri = uriInfo.getBaseUri().toString();
      String server_domain = baseUri.split("portal")[0];
      hook.setWebhook(githubHooksManagement.getWEBHOOK_URL());
      String fullPath = server_domain + githubHooksManagement.getWEBHOOK_URL();
      githubHooksManagement.updateHook(hook,fullPath);
      LOG.info("Webhook {} edited by {}",id, sourceIdentity.getRemoteId());
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @DELETE
  @RolesAllowed("administrators")
  @Path("hooks/{id}")
  public Response deletehook(@Context UriInfo uriInfo, @PathParam("id") Long id) throws Exception {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(portalContainerName);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      GitHubHookEntity hook = githubHooksManagement.getHookEntityById(id);
      githubHooksManagement.deleteHook(hook);
      LOG.info("Webhook {} deleted by {}",id, sourceIdentity.getRemoteId());
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @GET
  @RolesAllowed("users")
  @Path("users/{id}")
  public Response getUserIdByGithubId(@Context UriInfo uriInfo, @PathParam("id") String githubId) {
    String userId = githubHooksManagement.getUserByGithubId(githubId);
    if(StringUtils.isNotEmpty(userId)) {
      return Response.ok(userId).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }
}
