<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Lab contact@meedslab.com

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
<template>
  <v-card class="pt-5 px-4" flat>
    <div class="d-flex flex-row">
      <div>
        <v-card-text class="px-0 py-0 text-color font-weight-bold">
          {{ $t('githubConnector.webhook.label.watchProject') }}
        </v-card-text>
        <v-card-text class="dark-grey-color px-0 pt-0">
          {{ $t('githubConnector.webhook.label.watchProject.placeholder') }}
        </v-card-text>
      </div>
      <v-spacer />
      <v-btn
        v-if="!emptyHookList"
        class="ma-auto"
        icon
        @click="createGithubWebHook">
        <v-icon class="mx-2 primary--text" size="20">fas fa-plus</v-icon>
      </v-btn>
    </div>
    <div v-if="emptyHookList" class="d-flex align-center py-5">
      <v-btn
        class="btn btn-primary ma-auto"
        small
        @click="createGithubWebHook">
        <v-icon size="14" dark>
          fas fa-plus
        </v-icon>
        <span class="ms-2 subtitle-2 font-weight-bold">
          {{ $t('githubConnector.webhook.label.addOrganization') }}
        </span>
      </v-btn>
    </div>
    <v-progress-linear
      v-show="loading"
      color="primary"
      height="2"
      indeterminate />
    <div
      v-for="hook in hooks"
      :key="hook.name">
      <github-admin-connector-hook
        class="full-height"
        :hook="hook" />
    </div>
    <template v-if="hasMore">
      <v-btn
        :loading="loading"
        class="btn pa-0 mb-5"
        text
        block
        @click="loadMore">
        {{ $t('githubConnector.webhook.label.loadMore') }}
      </v-btn>
    </template>
  </v-card>
</template>

<script>
export default {
  data() {
    return {
      showLoadMoreButton: false,
      hooksCount: 0,
      pageSize: 5,
      limit: 5,
      offset: 0,
      loading: true,
      hooks: [],
    };
  },
  computed: {
    hasMore() {
      return this.hooksCount > this.limit;
    },
    emptyHookList() {
      return this.hooks?.length === 0;
    }
  },
  created() {
    this.$root.$on('github-hooks-updated', this.refreshHooks);
    if (!this.apiKey && !this.secretKey && !this.redirectUrl) {
      this.editing = true;
    }
    this.refreshHooks();
  },
  methods: {
    refreshHooks() {
      this.loading = true;
      return this.$githubConnectorService.getGithubWebHooks({
        page: 0,
        size: 5,
      })
        .then(data => {
          this.hooks = data?._embedded?.webHookRestEntityList;
          this.hooksCount = data?.page?.totalElements || 0;
          return this.$nextTick()
            .then(() => {
              this.$emit('updated', this.hooks);
            });
        }).finally(() => this.loading = false);
    },
    createGithubWebHook() {
      this.$root.$emit('github-hook-form-drawer');
    },
    loadMore() {
      this.limit += this.pageSize;
      this.refreshHooks();
    },
  }
};
</script>