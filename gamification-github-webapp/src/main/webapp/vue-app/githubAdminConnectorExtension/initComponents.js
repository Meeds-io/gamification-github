/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 - 2023 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import GithubAdminConnectorItem from './components/GithubAdminConnectorItem.vue';
import GithubAdminHookFormDrawer from './components/GithubAdminHookFormDrawer.vue';
import GithubAdminConnectorHookList from './components/GithubAdminConnectorHookList.vue';
import GithubAdminConnectorHook from './components/GithubAdminConnectorHook.vue';
import GithubAdminConnectorHookDetail from './components/GithubAdminConnectorHookDetail.vue';
import GithubAdminConnectorRepositoryItem from './components/GithubAdminConnectorRepositoryItem.vue';

const components = {
  'github-admin-connector-item': GithubAdminConnectorItem,
  'github-admin-hook-form-drawer': GithubAdminHookFormDrawer,
  'github-admin-connector-hook-list': GithubAdminConnectorHookList,
  'github-admin-connector-hook': GithubAdminConnectorHook,
  'github-admin-connector-hook-detail': GithubAdminConnectorHookDetail,
  'github-admin-connector-repository-item': GithubAdminConnectorRepositoryItem,
};

for (const key in components) {
  Vue.component(key, components[key]);
}