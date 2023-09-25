/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io
 * 
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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.exoplatform.gamification.github.services.GithubTriggerService;
import org.exoplatform.services.rest.resource.ResourceContainer;

@Path("/gamification/connectors/github/")
public class GithubWebHookRest implements ResourceContainer {

  private final GithubTriggerService githubTriggerService;

  public GithubWebHookRest(GithubTriggerService githubTriggerService) {
    this.githubTriggerService = githubTriggerService;
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Path("webhooks")
  public Response githubEvent(// NOSONAR
                              @HeaderParam("x-github-event") String event,
                              @HeaderParam("x-hub-signature") String signature,
                              String payload) {

    try {
      githubTriggerService.handleTriggerAsync(event, signature, payload);
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
