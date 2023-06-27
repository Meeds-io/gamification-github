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
  <v-app class="px-4">
    <div class="py-2 py-sm-5 d-flex align-center">
      <v-tooltip :disabled="$root.isMobile" bottom>
        <template #activator="{ on }">
          <v-card
            class="d-flex align-center"
            flat
            v-on="on"
            @click="backToConnectorDetail">
            <v-btn
              class="width-auto ms-n3"
              icon>
              <v-icon size="18" class="icon-default-color mx-2">fa-arrow-left</v-icon>
            </v-btn>
            <div class="text-header-title">{{ $t('githubConnector.admin.label.summary') }}</div>
          </v-card>
        </template>
        <span>{{ $t('gamification.connectors.settings.BackToDetail') }}</span>
      </v-tooltip>
    </div>
    <v-card-text class="px-0 pt-0 font-weight-bold">
      {{ $t('githubConnector.admin.label.connect') }}
    </v-card-text>
    <v-card-text class="px-0 pt-0">
      {{ $t('githubConnector.admin.label.allow.connect') }}
    </v-card-text>
    <div class="ps-4 pb-4 d-flex flex-column">
      <span class="pb-3">
        {{ $t('githubConnector.admin.label.instructions') }}
      </span>
      <span>
        {{ $t('githubConnector.admin.label.instructions.stepOne') }}
      </span>
      <span>
        {{ $t('githubConnector.admin.label.instructions.stepTwo') }}
      </span>
    </div>
    <div class="d-flex flex-row">
      <v-card
        class="d-flex flex-row"
        flat>
        <div>
          <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left ps-0 pt-0 pb-2">
            {{ $t('gamification.connectors.settings.apiKey') }}
          </v-card-text>
          <v-card-text class="d-flex py-0 ps-0">
            <input
              ref="connectorApiKey"
              v-model="apiKey"
              :disabled="!editing"
              :placeholder="$t('gamification.connectors.settings.apiKey.placeholder')"
              type="text"
              class="ignore-vuetify-classes flex-grow-1"
              required>
          </v-card-text>
        </div>
        <div>
          <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left ps-0 pt-0 pb-2">
            {{ $t('gamification.connectors.settings.secretKey') }}
          </v-card-text>
          <v-card-text class="d-flex py-0 ps-0">
            <input
              ref="connectorSecretKey"
              v-model="secretKey"
              :disabled="!editing"
              :placeholder="$t('gamification.connectors.settings.secretKey.placeholder')"
              type="text"
              class="ignore-vuetify-classes flex-grow-1"
              required>
          </v-card-text>
        </div>
        <div>
          <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left ps-0 pt-0 pb-2">
            {{ $t('gamification.connectors.settings.redirectUrl') }}
          </v-card-text>
          <v-card-text class="d-flex py-0 ps-0">
            <input
              ref="connectorRedirectUrl"
              v-model="redirectUrl"
              :disabled="!editing"
              :placeholder="$t('gamification.connectors.settings.redirectUrl.placeholder')"
              type="text"
              class="ignore-vuetify-classes flex-grow-1"
              required>
          </v-card-text>
        </div>
        <v-card-actions>
          <v-btn
            v-if="editing"
            :disabled="disabledSave"
            class="btn btn-primary ms-2 my-6"
            height="28"
            width="50"
            @click="saveConnectorSetting">
            {{ $t('gamification.connectors.settings.apply') }}
          </v-btn>
          <v-template v-else>
            <v-btn
              class="py-6"
              icon
              outlined
              small
              @click="editing = true">
              <v-icon class="primary--text" size="18">fas fa-edit</v-icon>
            </v-btn>
            <v-btn
              icon
              outlined
              small
              @click="deleteConfirmDialog">
              <v-icon class="error-color" size="18">fas fa-trash-alt</v-icon>
            </v-btn>
          </v-template>
        </v-card-actions>
      </v-card>
      <v-spacer />
      <v-switch
        v-if="canUpdateStatus"
        v-model="enabled"
        color="primary"
        class="py-6"
        @change="saveConnectorSetting" />
    </div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="$t('gamification.connectors.message.confirmDeleteConnectorSetting')"
      :title="$t('gamification.connectors.title.confirmDeleteConnectorSetting')"
      :ok-label="$t('confirm.yes')"
      :cancel-label="$t('confirm.no')"
      @ok="deleteSettings" />
  </v-app>
</template>

<script>
export default {
  props: {
    apiKey: {
      type: String,
      default: ''
    },
    secretKey: {
      type: String,
      default: ''
    },
    redirectUrl: {
      type: String,
      default: ''
    },
    enabled: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      editing: false,
    };
  },
  computed: {
    disabledSave() {
      return !this.apiKey
          || !this.secretKey
          || !this.redirectUrl;
    },
    canUpdateStatus() {
      return !this.editing && this.apiKey && this.secretKey && this.redirectUrl;
    }
  },
  created() {
    if (!this.apiKey && !this.secretKey && !this.redirectUrl) {
      this.editing = true;
    }
  },
  methods: {
    saveConnectorSetting() {
      const settings = {
        name: 'github',
        apiKey: this.apiKey,
        secretKey: this.secretKey,
        redirectUrl: this.redirectUrl,
        enabled: this.enabled
      };
      this.editing = false;
      document.dispatchEvent(new CustomEvent('save-connector-settings', {detail: settings}));
    },
    backToConnectorDetail() {
      document.dispatchEvent(new CustomEvent('close-connector-settings'));
    },
    deleteConfirmDialog() {
      this.$refs.deleteConfirmDialog.open();
    },
    deleteSettings() {
      this.apiKey = null;
      this.secretKey = null;
      this.redirectUrl = null;
      this.editing = true;
      document.dispatchEvent(new CustomEvent('delete-connector-settings', {detail: 'github'}));
    },
  }
};
</script>