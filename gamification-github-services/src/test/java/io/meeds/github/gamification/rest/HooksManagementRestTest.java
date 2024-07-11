/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import io.meeds.github.gamification.model.TokenStatus;
import io.meeds.github.gamification.model.WebHook;
import io.meeds.github.gamification.services.GithubConsumerService;
import io.meeds.github.gamification.services.WebhookService;
import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.exoplatform.commons.exception.ObjectNotFoundException;

import io.meeds.spring.web.security.PortalAuthenticationManager;
import io.meeds.spring.web.security.WebSecurityConfiguration;
import jakarta.servlet.Filter;

@SpringBootTest(classes = { HooksManagementRest.class, PortalAuthenticationManager.class, })
@ContextConfiguration(classes = { WebSecurityConfiguration.class })
@AutoConfigureWebMvc
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class HooksManagementRestTest {

  private static final String   REST_PATH     = "/hooks";      // NOSONAR

  private static final String   SIMPLE_USER   = "simple";

  private static final String   TEST_PASSWORD = "testPassword";

  @MockBean
  private WebhookService        webhookService;

  @MockBean
  private GithubConsumerService githubConsumerService;

  @Autowired
  private SecurityFilterChain   filterChain;

  @Autowired
  private WebApplicationContext context;

  private MockMvc               mockMvc;

  @BeforeEach
  void setup() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).addFilters(filterChain.getFilters().toArray(new Filter[0])).build();
  }

  @Test
  void getWebHooksAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebHooksSimpleUser() throws Exception {
    TokenStatus tokenStatus = new TokenStatus(true, 12L, 15454L);
    when(webhookService.getWebhooks(eq(SIMPLE_USER), any())).thenReturn(new PageImpl<>(List.of(newWebHook())));
    when(githubConsumerService.checkGitHubTokenStatus(any())).thenReturn(tokenStatus);

    ResultActions response = mockMvc.perform(get(REST_PATH).with(testSimpleUser()));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).getWebhooks(eq(SIMPLE_USER), any());

    response = mockMvc.perform(get(REST_PATH).with(testSimpleUser()));
    response.andExpect(status().isUnauthorized());
  }

  @Test
  void getWebHookByIdAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/1"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebHookByIdSimpleUser() throws Exception {
    TokenStatus tokenStatus = new TokenStatus(true, 12L, 15454L);
    when(webhookService.getWebhooks(eq(SIMPLE_USER), any())).thenReturn(new PageImpl<>(List.of(newWebHook())));
    when(githubConsumerService.checkGitHubTokenStatus(any())).thenReturn(tokenStatus);
    ResultActions response = mockMvc.perform(get(REST_PATH + "/1").with(testSimpleUser()));
    response.andExpect(status().isOk());

    response = mockMvc.perform(get(REST_PATH + "/0").with(testSimpleUser()));
    response.andExpect(status().isBadRequest());

    doThrow(new IllegalAccessException()).when(webhookService).getWebhookId(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/1").with(testSimpleUser()));
    response.andExpect(status().isUnauthorized());

    doThrow(new IllegalArgumentException()).when(webhookService).getWebhookId(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/1").with(testSimpleUser()));
    response.andExpect(status().isBadRequest());

    doThrow(new ObjectNotFoundException("Webhook doesn't exist")).when(webhookService).getWebhookId(1, SIMPLE_USER);

    response = mockMvc.perform(get(REST_PATH + "/1").with(testSimpleUser()));
    response.andExpect(status().isNotFound());
  }

  @Test
  void createWebhookHookAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                                            .param("accessToken", "accessToken")
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void createWebhookHookSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH).param("accessToken", "accessToken")
                                                            .param("organizationName", "")
                                                            .contentType(MediaType.APPLICATION_JSON)
                                                            .accept(MediaType.APPLICATION_JSON)
                                                            .with(testSimpleUser()));

    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                              .param("accessToken", "")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .with(testSimpleUser()));

    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                              .param("accessToken", "accessToken")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .with(testSimpleUser()));

    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).createWebhook("organizationName", "accessToken", SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                              .param("accessToken", "accessToken")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .with(testSimpleUser()));

    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectAlreadyExistsException(newWebHook())).when(webhookService)
                                                           .createWebhook("organizationName", "accessToken", SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                              .param("accessToken", "accessToken")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .with(testSimpleUser()));

    response.andExpect(status().isConflict());

    doThrow(new ObjectNotFoundException("github.organizationNotFound")).when(webhookService)
                                                                       .createWebhook("organizationName",
                                                                                      "accessToken",
                                                                                      SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH).param("organizationName", "organizationName")
                                              .param("accessToken", "accessToken")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .accept(MediaType.APPLICATION_JSON)
                                              .with(testSimpleUser()));

    response.andExpect(status().isNotFound());
  }

  @Test
  void updateWebHookAccessTokenAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(patch(REST_PATH + "/1").param("accessToken", "accessToken")
                                                                    .contentType(MediaType.APPLICATION_JSON)
                                                                    .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void updateWebHookAccessTokenSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(patch(REST_PATH + "/-1").param("accessToken", "accessToken")
                                                                     .contentType(MediaType.APPLICATION_JSON)
                                                                     .accept(MediaType.APPLICATION_JSON)
                                                                     .with(testSimpleUser()));

    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(patch(REST_PATH + "/1").param("accessToken", "")
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .with(testSimpleUser()));

    response.andExpect(status().isBadRequest());

    response = mockMvc.perform(patch(REST_PATH + "/1").param("accessToken", "accessToken")
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .with(testSimpleUser()));

    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).updateWebHookAccessToken(1L, "accessToken", SIMPLE_USER);

    response = mockMvc.perform(patch(REST_PATH + "/1").param("accessToken", "accessToken")
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .with(testSimpleUser()));

    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectNotFoundException("github.organizationNotFound")).when(webhookService)
                                                                       .updateWebHookAccessToken(1L, "accessToken", SIMPLE_USER);

    response = mockMvc.perform(patch(REST_PATH + "/1").param("accessToken", "accessToken")
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .accept(MediaType.APPLICATION_JSON)
                                                      .with(testSimpleUser()));

    response.andExpect(status().isNotFound());
  }

  @Test
  void deleteWebhookAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH + "/" + 1).contentType(MediaType.APPLICATION_JSON)
                                                                        .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void deleteWebhookSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                                        .contentType(MediaType.APPLICATION_JSON)
                                                                        .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).deleteWebhook(1, SIMPLE_USER);

    response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectNotFoundException("github.organizationNotFound")).when(webhookService).deleteWebhook(1, SIMPLE_USER);

    response = mockMvc.perform(delete(REST_PATH + "/" + 1).with(testSimpleUser())
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isNotFound());
  }

  @Test
  void getWebHookReposAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(get(REST_PATH + "/" + 1 + "/repos").param("page", "1")
                                                                                .param("perPage", "5")
                                                                                .param("keyword", "keyword"));
    response.andExpect(status().isForbidden());
  }

  @Test
  void getWebHookReposSimpleUser() throws Exception {

    ResultActions response = mockMvc.perform(get(REST_PATH + "/" + 1 + "/repos").param("page", "1")
                                                                                .param("perPage", "5")
                                                                                .param("keyword", "keyword")
                                                                                .with(testSimpleUser()));
    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).retrieveOrganizationRepos(1, SIMPLE_USER, 1, 5, "keyword");

    response = mockMvc.perform(get(REST_PATH + "/" + 1 + "/repos").param("page", "1")
                                                                  .param("perPage", "5")
                                                                  .param("keyword", "keyword")
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isUnauthorized());

    doThrow(new ObjectNotFoundException("github.organizationNotFound")).when(webhookService)
                                                                       .retrieveOrganizationRepos(1,
                                                                                                  SIMPLE_USER,
                                                                                                  1,
                                                                                                  5,
                                                                                                  "keyword");

    response = mockMvc.perform(get(REST_PATH + "/" + 1 + "/repos").param("page", "1")
                                                                  .param("perPage", "5")
                                                                  .param("keyword", "keyword")
                                                                  .with(testSimpleUser()));
    response.andExpect(status().isNotFound());
  }

  @Test
  void updateWebHookRepoStatusAnonymously() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH + "/repo/status").param("organizationId", "1")
                                                                             .param("repositoryId", "125")
                                                                             .param("enabled", "false")
                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                             .accept(MediaType.APPLICATION_JSON));
    response.andExpect(status().isForbidden());
  }

  @Test
  void updateWebHookRepoStatusSimpleUser() throws Exception {
    ResultActions response = mockMvc.perform(post(REST_PATH + "/repo/status").param("organizationId", "1")
                                                                             .param("repositoryId", "125")
                                                                             .param("enabled", "false")
                                                                             .contentType(MediaType.APPLICATION_JSON)
                                                                             .accept(MediaType.APPLICATION_JSON)
                                                                             .with(testSimpleUser()));

    response.andExpect(status().isOk());

    doThrow(new IllegalAccessException()).when(webhookService).setWebHookRepositoryEnabled(1L, 125L, false, SIMPLE_USER);

    response = mockMvc.perform(post(REST_PATH + "/repo/status").param("organizationId", "1")
                                                               .param("repositoryId", "125")
                                                               .param("enabled", "false")
                                                               .contentType(MediaType.APPLICATION_JSON)
                                                               .accept(MediaType.APPLICATION_JSON)
                                                               .with(testSimpleUser()));

    response.andExpect(status().isUnauthorized());
  }

  private RequestPostProcessor testSimpleUser() {
    return user(SIMPLE_USER).password(TEST_PASSWORD).authorities(new SimpleGrantedAuthority("users"));
  }

  private WebHook newWebHook() {
    return new WebHook(1,
                       1234,
                       12345,
                       "organizationName",
                       List.of("trigger1", "trigger2"),
                       true,
                       "watchedDate",
                       "watchedBy",
                       "updatedDate",
                       "refreshDate",
                       "token",
                       "secret");
  }

}
