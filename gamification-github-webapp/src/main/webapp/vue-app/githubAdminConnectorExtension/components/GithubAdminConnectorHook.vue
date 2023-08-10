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
  <v-card
    flat
    @click="openHookDetail">
    <div class="d-flex flex-row">
      <div class="d-flex">
        <div class="d-flex align-center">
          <v-img
            :src="avatarUrl"
            :alt="title"
            height="60"
            width="60" />
        </div>
        <v-list class="ms-3">
          <v-list-item-title>
            {{ title }}
          </v-list-item-title>
          <v-list-item-subtitle class="text-truncate d-flex caption mt-1">{{ description }}</v-list-item-subtitle>
          <div class="d-flex flex-row">
            <span class="text-truncate d-flex caption d-content pt-2px"> {{ watchedByLabel }} </span>
            <exo-user-avatar
              :profile-id="watchedBy"
              extra-class="ms-1"
              fullname
              popover />
          </div>
        </v-list>
      </div>
      <v-spacer />
      <div class="d-flex align-center px-2">
        <v-btn
          small
          icon
          @click="editGithubWebHook">
          <v-icon class="primary--text" size="18">fas fa-edit</v-icon>
        </v-btn>
      </div>
      <div class="d-flex align-center px-2">
        <v-btn
          small
          icon
          @click="deleteConfirmDialog">
          <v-icon class="error-color" size="18">fas fa-trash-alt</v-icon>
        </v-btn>
      </div>
    </div>
    <exo-confirm-dialog
      ref="deleteHookConfirmDialog"
      :message="$t('githubConnector.webhook.message.confirmDeleteConnectorHook')"
      :title="$t('githubConnector.webhook.title.confirmDeleteProject')"
      :ok-label="$t('confirm.yes')"
      :cancel-label="$t('confirm.no')"
      @ok="deleteHook" />
  </v-card>
</template>

<script>

export default {
  props: {
    hook: {
      type: Object,
      default: null
    },
    hooksLoaded: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      loading: true,
    };
  },
  computed: {
    title() {
      return this.hook?.title;
    },
    description() {
      return this.hook?.description;
    },
    watchedDate() {
      return this.hook?.watchedDate && new Date(this.hook.watchedDate);
    },
    avatarUrl() {
      return this.hook?.avatarUrl;
    },
    watchedByLabel() {
      return this.$t('githubConnector.webhook.label.watchedBy', {0: this.$dateUtil.formatDateObjectToDisplay(this.watchedDate, {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
      }, eXo.env.portal.language)});
    },
    watchedBy() {
      return this.hook?.watchedBy;
    },
    organizationId() {
      return this.hook?.organizationId;
    },
  },
  methods: {
    deleteConfirmDialog(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$refs.deleteHookConfirmDialog.open();
    },
    deleteHook() {
      return this.$githubConnectorService.deleteGithubWebHook(this.organizationId).then(() => {
        this.$root.$emit('github-hooks-updated');
      });
    },
    editGithubWebHook(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$root.$emit('github-hook-form-drawer', this.hook);
    },
    openHookDetail() {
      this.$root.$emit('github-hook-detail', this.hook);
    }
  }
};
</script>