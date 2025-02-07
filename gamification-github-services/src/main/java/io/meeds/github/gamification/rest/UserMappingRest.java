/*
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2020 - 2025 Meeds Lab contact@meedslab.com
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

import org.apache.commons.lang3.StringUtils;
import io.meeds.gamification.service.ConnectorService;
import io.meeds.github.gamification.utils.Utils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("mapping")
@Tag(name = "mapping", description = "Map Github user to platform user") 
public class UserMappingRest {

    @Autowired
    private ConnectorService      connectorService;

    @GetMapping(path = "{githubId}")
    @Secured("users")
    public String getUserIdByGithubId(HttpServletRequest request,
                                    @Parameter(description = "Github user identifier", required = true)
                                    @PathVariable("githubId")
                                    String githubId) {
        if (StringUtils.isBlank(githubId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'githubId' parameter is mandatory");
        }
        String userId = connectorService.getAssociatedUsername(Utils.CONNECTOR_NAME, githubId);
        if(StringUtils.isNotEmpty(userId)) {
            return userId;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found for githubId: " + githubId);
        }
    }
}