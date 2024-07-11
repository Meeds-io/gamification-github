/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2022 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package io.meeds.github.gamification.rest;

import io.meeds.github.gamification.model.RemoteRepository;
import io.meeds.github.gamification.model.WebHook;
import io.meeds.github.gamification.rest.builder.WebHookBuilder;
import io.meeds.github.gamification.services.GithubConsumerService;
import io.meeds.github.gamification.services.WebhookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.exoplatform.commons.exception.ObjectNotFoundException;
import io.meeds.github.gamification.rest.model.RepositoryList;
import io.meeds.github.gamification.rest.model.WebHookRestEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("hooks")
@Tag(name = "accounts", description = "Manage and access twitter watched accounts") // NOSONAR
public class HooksManagementRest {

  public static final String         GITHUB_HOOK_NOT_FOUND = "The GitHub hook doesn't exit";

  @Autowired
  private WebhookService webhookService;

  @Autowired
  private GithubConsumerService githubConsumerService;

  @GetMapping
  @Secured("users")
  @Operation(summary = "Retrieves the list GitHub webHooks", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public PagedModel<EntityModel<WebHookRestEntity>> getWebHooks(HttpServletRequest request,
                                                                Pageable pageable,
                                                                PagedResourcesAssembler<WebHookRestEntity> assembler) {

    try {
      Page<WebHookRestEntity> webHookRestEntities = getWebHookRestEntities(request.getRemoteUser(), pageable);
      return assembler.toModel(webHookRestEntities);
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  @GetMapping(path = "{webHookId}")
  @Secured("users")
  @Operation(summary = "Retrieves a webHook by its technical identifier", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "404", description = "Not found"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public WebHookRestEntity getWebHookById(HttpServletRequest request,
                                          @Parameter(description = "WebHook technical identifier", required = true)
                                          @PathVariable("webHookId")
                                          long webHookId) {
    if (webHookId == 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "WebHook Id must be not null");
    }
    try {
      WebHook webHook = webhookService.getWebhookId(webHookId, request.getRemoteUser());
      return WebHookBuilder.toRestEntity(webhookService, githubConsumerService, webHook);
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @PostMapping
  @Secured("users")
  @Operation(summary = "Create a organization webhook for Remote GitHub connector.", description = "Create a organization webhook for Remote GitHub connector.", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public void createWebhookHook(HttpServletRequest request,
                                @Parameter(description = "GitHub organization name", required = true) @RequestParam("organizationName") String organizationName,
                                @Parameter(description = "GitHub personal access token", required = true)
                                @RequestParam("accessToken")
                                String accessToken) {

    if (StringUtils.isBlank(organizationName)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'organizationName' parameter is mandatory");
    }
    if (StringUtils.isBlank(accessToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'accessToken' parameter is mandatory");
    }
    try {
      webhookService.createWebhook(organizationName, accessToken, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectAlreadyExistsException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @PatchMapping(path = "{webHookId}")
  @Secured("users")
  @Operation(summary = "Update a organization webhook personal access token.", description = "Update a organization webhook personal access token.", method = "PATCH")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Invalid query input"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error") })
  public void updateWebHookAccessToken(HttpServletRequest request,
                                       @Parameter(description = "WebHook technical identifier", required = true)
                                       @PathVariable("webHookId")
                                       long webHookId,
                                       @Parameter(description = "GitHub personal access token", required = true)
                                       @RequestParam("accessToken")
                                       String accessToken) {

    if (webHookId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'webHookId' must be positive");
    }
    if (StringUtils.isBlank(accessToken)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'accessToken' parameter is mandatory");
    }
    try {
      webhookService.updateWebHookAccessToken(webHookId, accessToken, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, GITHUB_HOOK_NOT_FOUND);
    }
  }

  @DeleteMapping(path = "{organizationId}")
  @Secured("users")
  @Operation(summary = "Deletes gitHub organization webhook", description = "Deletes gitHub organization webhook", method = "DELETE")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public void deleteWebhook(HttpServletRequest request,
                            @Parameter(description = "GitHub organization id", required = true)
                                @PathVariable("organizationId")
                                long organizationId) {

    try {
      webhookService.deleteWebhook(organizationId, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
    }
  }

  @GetMapping(path = "{organizationId}/repos")
  @Secured("users")
  @Operation(summary = "Retrieves the list GitHub organization repositories", method = "GET")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "Request fulfilled"),
         @ApiResponse(responseCode = "401", description = "Unauthorized operation"), })
  public RepositoryList getWebHookRepos(HttpServletRequest request,
                                        @Parameter(description = "GitHub organization id", required = true)
                                        @PathVariable("organizationId")
                                        long organizationId,
                                        @Parameter(description = "Query page number", required = true)
                                        @RequestParam("page")
                                        int page,
                                        @Parameter(description = "Query item per page", required = true)
                                        @RequestParam("perPage")
                                        int perPage,
                                        @Parameter(description = "Keyword to search in repositories title", required = true)
                                        @RequestParam("keyword")
                                        String keyword) {

    List<RemoteRepository> remoteRepositories;
    try {
      RepositoryList repositoryList = new RepositoryList();
      remoteRepositories = webhookService.retrieveOrganizationRepos(organizationId,
                                                                    request.getRemoteUser(),
                                                                    page,
                                                                    perPage,
                                                                    keyword);
      repositoryList.setRemoteRepositories(remoteRepositories);
      repositoryList.setPage(page);
      repositoryList.setPerPage(perPage);
      return repositoryList;
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    } catch (ObjectNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, GITHUB_HOOK_NOT_FOUND);
    }
  }

  @PostMapping(path = "repo/status")
  @Secured("users")
  @Operation(summary = "enables/disables webhook repository.", description = "enables/disables webhook repository", method = "POST")
  @ApiResponses(value = {
          @ApiResponse(responseCode = "204", description = "Request fulfilled"),
          @ApiResponse(responseCode = "400", description = "Bad request"),
          @ApiResponse(responseCode = "401", description = "Unauthorized operation"),
          @ApiResponse(responseCode = "500", description = "Internal server error"), })
  public void updateWebHookRepoStatus(HttpServletRequest request,
                                      @Parameter(description = "GitHub organization remote Id", required = true)
                                      @RequestParam("organizationId")
                                      long organizationId,
                                      @Parameter(description = "Organization repository remote Id", required = true)
                                      @RequestParam("repositoryId")
                                      long repositoryId,
                                      @Parameter(description = "Organization repository status enabled/disabled. possible values: true for enabled, else false", required = true)
                                      @RequestParam("enabled")
                                      boolean enabled) {

    try {
      webhookService.setWebHookRepositoryEnabled(organizationId, repositoryId, enabled, request.getRemoteUser());
    } catch (IllegalAccessException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
  }

  private Page<WebHookRestEntity> getWebHookRestEntities(String username, Pageable pageable) throws IllegalAccessException {
    Page<WebHook> webHooks = webhookService.getWebhooks(username, pageable);
    return WebHookBuilder.toRestEntities(webhookService, githubConsumerService, webHooks);
  }
}
