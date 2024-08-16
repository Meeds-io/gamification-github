/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Lab contact@meedslab.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package io.meeds.github.gamification;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;

import io.meeds.spring.AvailableIntegration;
import io.meeds.spring.kernel.PortalApplicationContextInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    GamificationGithubApplication.MODULE_NAME,
    AvailableIntegration.KERNEL_MODULE,
    AvailableIntegration.JPA_MODULE,
    AvailableIntegration.LIQUIBASE_MODULE,
    AvailableIntegration.WEB_MODULE,
  })
@EnableCaching
@EnableJpaRepositories(basePackages = GamificationGithubApplication.MODULE_NAME)
@PropertySource("classpath:application.properties")
@PropertySource("classpath:application-common.properties")
@PropertySource("classpath:github.properties")
public class GamificationGithubApplication extends PortalApplicationContextInitializer {

  public static final String MODULE_NAME = "io.meeds.github.gamification";
}
