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
package org.exoplatform.gamification.github.rest;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.gamification.github.entity.GitHubHookEntity;
import org.exoplatform.gamification.github.services.GithubHooksManagement;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.service.rest.Util;

@Path("/gamification/connectors/github/hooksmanagement")
public class HooksManagementRest implements ResourceContainer {

  private static final String   PORTAL_CONTAINER_NAME = "portal";

  private static final Log      LOG                   = ExoLogger.getLogger(HooksManagementRest.class);

  private GithubHooksManagement githubHooksManagement;

  public HooksManagementRest(GithubHooksManagement githubHooksManagement) {
    this.githubHooksManagement = githubHooksManagement;
  }

  @GET
  @RolesAllowed("administrators")
  @Produces(MediaType.APPLICATION_JSON)
  @Path("hooks")
  public Response getHooks(
                           @Context
                           UriInfo uriInfo) {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(PORTAL_CONTAINER_NAME);
    if (sourceIdentity == null
        || githubHooksManagement.getSecret() == null
        || githubHooksManagement.getToken() == null
        || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    return Response.ok(githubHooksManagement.getAllHooks()).build();
  }

  @POST
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("hooks")
  public Response createHook(
                             @Context
                             UriInfo uriInfo,
                             GitHubHookEntity hook) {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(PORTAL_CONTAINER_NAME);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null
        || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    try {
      String baseUri = uriInfo.getBaseUri().toString();
      String serverDomain = baseUri.split(PORTAL_CONTAINER_NAME)[0];
      hook.setWebhook(githubHooksManagement.getWebhookUrl());
      String fullPath = serverDomain + githubHooksManagement.getWebhookUrl();
      Long id = githubHooksManagement.addHook(fullPath, hook.getOrganization(), hook.getRepo(), hook.getEnabled());
      githubHooksManagement.createHook(id, hook, true);
      LOG.info("New webhook added by {}", sourceIdentity.getRemoteId());
      return Response.status(Response.Status.CREATED).build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @PUT
  @RolesAllowed("administrators")
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("hooks/{id}")
  public Response editHook(
                           @Context
                           UriInfo uriInfo,
                           @PathParam("id")
                           Long id,
                           GitHubHookEntity hook) {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(PORTAL_CONTAINER_NAME);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null
        || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      String baseUri = uriInfo.getBaseUri().toString();
      String serverDomain = baseUri.split(PORTAL_CONTAINER_NAME)[0];
      hook.setWebhook(githubHooksManagement.getWebhookUrl());
      String fullPath = serverDomain + githubHooksManagement.getWebhookUrl();
      githubHooksManagement.updateHook(hook, fullPath);
      LOG.info("Webhook {} edited by {}", id, sourceIdentity.getRemoteId());
      return Response.noContent().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @DELETE
  @RolesAllowed("administrators")
  @Path("hooks/{id}")
  public Response deleteHook(
                             @Context
                             UriInfo uriInfo,
                             @PathParam("id")
                             Long id) {
    Identity sourceIdentity = Util.getAuthenticatedUserIdentity(PORTAL_CONTAINER_NAME);
    if (sourceIdentity == null || githubHooksManagement.getSecret() == null || githubHooksManagement.getToken() == null
        || githubHooksManagement.getExoEnvironment() == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
    try {
      GitHubHookEntity hook = githubHooksManagement.getHookEntityById(id);
      githubHooksManagement.deleteHook(hook);
      LOG.info("Webhook {} deleted by {}", id, sourceIdentity.getRemoteId());
      return Response.noContent().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  @GET
  @RolesAllowed("users")
  @Produces(MediaType.TEXT_PLAIN)
  @Path("users/{id}")
  public Response getUserIdByGithubId(
                                      @Context
                                      UriInfo uriInfo,
                                      @PathParam("id")
                                      String githubId) {
    String userId = githubHooksManagement.getUserByGithubId(githubId);
    if (StringUtils.isNotEmpty(userId)) {
      return Response.ok(userId).build();
    } else {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
  }

}
