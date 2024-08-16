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
package io.meeds.github.gamification.storage;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import io.meeds.github.gamification.dao.WebHookDAO;
import io.meeds.github.gamification.entity.WebhookEntity;
import io.meeds.github.gamification.model.WebHook;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.web.security.codec.CodecInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.exoplatform.commons.ObjectAlreadyExistsException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@SpringBootTest(classes = { WebHookStorage.class, })
@ExtendWith(MockitoExtension.class)
class WebHookStorageTest {

  private static final Long     ID              = 2L;

  private static final Long     ORGANIZATION_ID = 1232L;

  private static final Pageable PAGEABLE        = Pageable.ofSize(2);

  @Autowired
  private WebHookStorage        webHookStorage;

  @MockBean
  private WebHookDAO            webHookDAO;

  @MockBean
  private SettingService        settingService;

  @MockBean
  private CodecInitializer      codecInitializer;

  @BeforeEach
    void setup() {
        when(webHookDAO.save(any())).thenAnswer(invocation -> {
            WebhookEntity entity = invocation.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(ID);
            }
            when(webHookDAO.findById(ID)).thenReturn(Optional.of(entity));
            when(webHookDAO.findWebhookEntityByOrganizationId(ORGANIZATION_ID)).thenReturn(entity);
            when(webHookDAO.findAll(PAGEABLE)).thenReturn(new PageImpl<>(List.of(entity)));
            when(webHookDAO.count()).thenReturn(1L);
            return entity;
        });
        doAnswer(invocation -> {
            WebhookEntity entity = invocation.getArgument(0);
            when(webHookDAO.findById(entity.getId())).thenReturn(Optional.empty());
            return null;
        }).when(webHookDAO).delete(any());
    }

  @Test
  void testAddWebHook() throws Exception {
    // Given
    WebHook webHook = createWebHookInstance();

    // When
    WebHook createdWebHook = webHookStorage.saveWebHook(webHook);

    // Then
    assertNotNull(createdWebHook);
    assertEquals(createdWebHook.getEnabled(), webHook.getEnabled());
    assertEquals(createdWebHook.getOrganizationName(), webHook.getOrganizationName());
    assertEquals(createdWebHook.getOrganizationId(), webHook.getOrganizationId());

    assertThrows(ObjectAlreadyExistsException.class, () -> webHookStorage.saveWebHook(webHook));
  }

  @Test
  void testGetWebHooks() throws Exception {
    // Given
    WebHook webHook = createWebHookInstance();

    // When
    WebHook createdWebHook = webHookStorage.saveWebHook(webHook);

    // Then
    assertNotNull(createdWebHook);
    assertEquals(new PageImpl<>(List.of(createdWebHook)), webHookStorage.getWebhooks(PAGEABLE));
    assertEquals(1L, webHookStorage.countWebhooks());
  }

  protected WebHook createWebHookInstance() {
    return new WebHook(0,
                       1234,
                       ORGANIZATION_ID,
                       "organizationName",
                       List.of("trigger"),
                       true,
                       "watchedDate",
                       null,
                       "updatedDate",
                       "refreshDate",
                       "token",
                       "secret");
  }
}
