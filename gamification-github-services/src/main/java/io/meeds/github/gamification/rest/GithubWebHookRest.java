/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com
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
package io.meeds.github.gamification.rest;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import io.meeds.github.gamification.services.GithubTriggerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("webhooks")
@Tag(name = "webhooks", description = "Manage triggered github webhook events") // NOSONAR
public class GithubWebHookRest {

  @Autowired
  private GithubTriggerService githubTriggerService;

  @PostMapping
  public Response githubEvent(// NOSONAR
                              @RequestHeader("x-github-event") String event,
                              @RequestHeader("x-hub-signature") String signature,
                              @RequestBody String payload) {

    try {
      githubTriggerService.handleTriggerAsync(event, signature, payload);
      return Response.ok().build();
    } catch (Exception e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
    }
  }
}
