<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 - 2023 Meeds Association
contact@meeds.io
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
  <v-card flat>
    <div
      class="py-4">
      <v-btn
        class="btn btn-primary"
        small
        @click="createGithubWebHook">
        <v-icon size="14" dark>
          fas fa-plus
        </v-icon>
        <span class="ms-2 d-none d-lg-inline subtitle-1">
          {{ $t('githubConnector.webhook.label.watchProject') }}
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
      :key="hook.name"
      class="py-4 d-flex flex-column">
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
      return this.$githubConnectorService.getGithubWebHooks(this.offset, this.limit)
        .then(data => {
          this.hooks = data.webhooks;
          this.hooksCount = data.size || 0;
          return this.$nextTick();
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