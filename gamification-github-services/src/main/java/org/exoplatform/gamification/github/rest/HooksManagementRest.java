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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import org.exoplatform.gamification.github.model.RemoteRepository;
import org.exoplatform.gamification.github.model.WebHook;
import org.exoplatform.gamification.github.rest.builder.WebHookBuilder;
import org.exoplatform.gamification.github.rest.model.RepositoryList;
import org.exoplatform.gamification.github.rest.model.WebHookList;
import org.exoplatform.gamification.github.rest.model.WebHookRestEntity;
import org.exoplatform.gamification.github.services.GithubConsumerService;
import org.exoplatform.gamification.github.services.WebhookService;
import org.exoplatform.services.rest.http.PATCH;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;

import java.util.Collection;
import java.util.List;

import static io.meeds.gamification.utils.Utils.getCurrentUser;

@Path("/gamification/connectors/github/hooks")
public class HooksManagementRest implements ResourceContainer {

  public static final String         GITHUB_HOOK_NOT_FOUND = "The GitHub hook doesn't exit";

  private final WebhookService        webhookService;

  private final GithubConsumerService githubConsumerService;

  public HooksManagementRest(WebhookService webhookService, GithubConsumerService githubConsumerService) {
    this.webhookService = webhookService;
    this.githubConsumerService = githubConsumerService;
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Retrieves the list GitHub webHooks", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public Response getWebHooks(@QueryParam("offset") int offset,
                              @Parameter(description = "Query results limit", required = true) @QueryParam("limit") int limit,
                              @Parameter(description = "WebHook total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {

    String currentUser = getCurrentUser();
    List<WebHookRestEntity> webHookRestEntities;
    try {
      WebHookList webHookList = new WebHookList();
      webHookRestEntities = getWebHookRestEntities(currentUser);
      if (returnSize) {
        int webHookSize = webhookService.countWebhooks(currentUser, false);
        webHookList.setSize(webHookSize);
      }
      webHookList.setWebhooks(webHookRestEntities);
      webHookList.setOffset(offset);
      webHookList.setLimit(limit);
      return Response.ok(webHookList).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("{webHookId}")
  @RolesAllowed("users")
  @Operation(summary = "Retrieves a webHook by its technical identifier", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response getWebHookById(@Parameter(description = "WebHook technical identifier", required = true) @PathParam("webHookId") long webHookId) {
    if (webHookId == 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("WebHook Id must be not null").build();
    }
    String currentUser = getCurrentUser();
    try {
      WebHook webHook = webhookService.getWebhookId(webHookId, currentUser);
      return Response.ok(WebHookBuilder.toRestEntity(webhookService, githubConsumerService, webHook)).build();
    } catch (IllegalArgumentException e) {
      return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @RolesAllowed("users")
  @Operation(summary = "Create a organization webhook for Remote GitHub connector.", description = "Create a organization webhook for Remote GitHub connector.", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response createWebhookHook(@Parameter(description = "GitHub organization name", required = true) @FormParam("organizationName") String organizationName,
                                    @Parameter(description = "GitHub personal access token", required = true) @FormParam("accessToken") String accessToken) {

    if (StringUtils.isBlank(organizationName)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'organizationName' parameter is mandatory").build();
    }
    if (StringUtils.isBlank(accessToken)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'accessToken' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      webhookService.createWebhook(organizationName, accessToken, currentUser);
      return Response.status(Response.Status.CREATED).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectAlreadyExistsException e) {
      return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
    }
  }

  @PATCH
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @RolesAllowed("users")
  @Operation(summary = "Update a organization webhook personal access token.", description = "Update a organization webhook personal access token.", method = "PATCH")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response updateWebHookAccessToken(@Parameter(description = "webHook id", required = true) @FormParam("webHookId") long webHookId,
                                           @Parameter(description = "GitHub personal access token", required = true) @FormParam("accessToken") String accessToken) {

    if (webHookId <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'webHookId' must be positive").build();
    }
    if (StringUtils.isBlank(accessToken)) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'accessToken' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      webhookService.updateWebHookAccessToken(webHookId, accessToken, currentUser);
      return Response.status(Response.Status.CREATED).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(GITHUB_HOOK_NOT_FOUND).build();
    }
  }

  @DELETE
  @Path("{organizationId}")
  @RolesAllowed("users")
  @Operation(summary = "Deletes gitHub organization webhook", description = "Deletes gitHub organization webhook", method = "DELETE")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response deleteWebhookHook(@Parameter(description = "GitHub organization id", required = true) @PathParam("organizationId") long organizationId) {
    if (organizationId <= 0) {
      return Response.status(Response.Status.BAD_REQUEST).entity("'hookName' parameter is mandatory").build();
    }
    String currentUser = ConversationState.getCurrent().getIdentity().getUserId();
    try {
      webhookService.deleteWebhookHook(organizationId, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(GITHUB_HOOK_NOT_FOUND).build();
    }
  }

  @GET
  @Path("{organizationId}/repos")
  @Produces(MediaType.APPLICATION_JSON)
  @RolesAllowed("users")
  @Operation(summary = "Retrieves the list GitHub organization repositories", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
         @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public Response getWebHookRepos(@Parameter(description = "GitHub organization id", required = true) @PathParam("organizationId") long organizationId,
                                  @QueryParam("offset") int offset,
                                  @Parameter(description = "Query results limit", required = true) @QueryParam("limit") int limit,
                                  @Parameter(description = "Repositories total size") @Schema(defaultValue = "false") @QueryParam("returnSize") boolean returnSize) {

    String currentUser = getCurrentUser();
    List<RemoteRepository> remoteRepositories;
    try {
      RepositoryList repositoryList = new RepositoryList();
      remoteRepositories = webhookService.retrieveOrganizationRepos(organizationId, currentUser, offset, limit);
      if (returnSize) {
        int size = webhookService.countOrganizationRepos(organizationId, currentUser);
        repositoryList.setSize(size);
      }
      repositoryList.setRemoteRepositories(remoteRepositories);
      repositoryList.setOffset(offset);
      repositoryList.setLimit(limit);
      return Response.ok(repositoryList).build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity(GITHUB_HOOK_NOT_FOUND).build();
    }
  }

  @Path("repo/status")
  @POST
  @RolesAllowed("users")
  @Operation(summary = "enables/disables webhook repository.", description = "enables/disables webhook repository", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response updateWebHookRepoStatus(@Parameter(description = "GitHub organization remote Id", required = true) @FormParam("organizationId") long organizationId,
                                          @Parameter(description = "Organization repository remote Id", required = true) @FormParam("repositoryId") long repositoryId,
                                          @Parameter(description = "Organization repository status enabled/disabled. possible values: true for enabled, else false", required = true) @FormParam("enabled") boolean enabled) {

    String currentUser = getCurrentUser();
    try {
      webhookService.setWebHookRepositoryEnabled(organizationId, repositoryId, enabled, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
  }

  @Path("events/status")
  @POST
  @RolesAllowed("users")
  @Operation(summary = "enables/disables event for gitHub organization.", description = "enables/disables event for gitHub organization", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response updateWebHookEventStatus(@Parameter(description = "Event Id", required = true) @FormParam("eventId") long eventId,
                                    @Parameter(description = "Organization remote Id", required = true) @FormParam("organizationId") long organizationId,
                                    @Parameter(description = "Event status enabled/disabled. possible values: true for enabled, else false", required = true) @FormParam("enabled") boolean enabled) {

    String currentUser = getCurrentUser();
    try {
      webhookService.setEventEnabledForOrganization(eventId, organizationId, enabled, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    } catch (ObjectNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).entity("Event not found").build();
    }
  }

  @Path("watchScope/status")
  @POST
  @RolesAllowed("users")
  @Operation(summary = "Limit webhook watch scope or not", description = "Limit webhook watch scope or not", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public Response updateWebHookWatchScope(@Parameter(description = "GitHub organization remote Id", required = true) @FormParam("organizationId") long organizationId,
                                          @Parameter(description = "webhook watch scope limited status enabled/disabled. possible values: true for enabled, else false", required = true) @FormParam("enabled") boolean enabled) {

    String currentUser = getCurrentUser();
    try {
      webhookService.setWebHookWatchLimitEnabled(organizationId, enabled, currentUser);
      return Response.noContent().build();
    } catch (IllegalAccessException e) {
      return Response.status(Response.Status.UNAUTHORIZED).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
    }
  }

  @Path("forceUpdate")
  @PATCH
  @RolesAllowed("users")
  @Operation(summary = "Force Update a github stored webhooks", description = "Force Update a github stored webhooks", method = "PATCH")
  @ApiResponses(value = { @ApiResponse(responseCode = "500", description = "Internal server error") })
  public Response forceUpdateWebhooks() {
    try {
      webhookService.forceUpdateWebhooks();
      return Response.noContent().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }

  private List<WebHookRestEntity> getWebHookRestEntities(String username) throws IllegalAccessException {
    Collection<WebHook> webHooks = webhookService.getWebhooks(username, 0, 20, false);
    return WebHookBuilder.toRestEntities(webhookService, githubConsumerService, webHooks);
  }
}
