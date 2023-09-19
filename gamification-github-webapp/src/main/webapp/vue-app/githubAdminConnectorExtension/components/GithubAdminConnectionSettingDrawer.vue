<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

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
  <exo-drawer
    ref="githubConnectionSettingDrawer"
    v-model="drawer"
    right
    @closed="clear">
    <template #title>
      {{ $t('githubConnector.admin.label.connectProfile') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="ConnectionSettingForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat />
      <div class="d-flex flex-column">
        <div class="ps-4 pb-4 d-flex flex-column dark-grey-color">
          <v-card-text class="pb-3 ps-0">
            {{ $t('githubConnector.admin.label.instructions') }}
          </v-card-text>
          <v-card-text class="pb-0 ps-0 dark-grey-color">
            {{ $t('githubConnector.admin.label.instructions.stepOne') }} (<a href="https://github.com/settings/applications/new" target="_blank">
              {{ $t('githubConnector.admin.label.seeMore') }}
              <v-icon size="14" class="pb-1 pe-1">fas fa-external-link-alt</v-icon>
            </a>)
          </v-card-text>
          <v-card-text class="pt-0 ps-0 dark-grey-color">
            {{ $t('githubConnector.admin.label.instructions.stepTwo') }}
          </v-card-text>
        </div>
        <v-card-text class="text-left py-0 dark-grey-color">
          {{ $t('gamification.connectors.settings.apiKey') }}
        </v-card-text>
        <v-card-text>
          <input
            ref="connectorApiKey"
            v-model="apiKey"
            :placeholder="$t('gamification.connectors.settings.apiKey.placeholder')"
            type="text"
            class="ignore-vuetify-classes full-width"
            required
            @input="disabled = false"
            @change="disabled = false">
        </v-card-text>
        <v-card-text class="text-left py-0 dark-grey-color">
          {{ $t('gamification.connectors.settings.secretKey') }}
        </v-card-text>
        <v-card-text>
          <input
            ref="connectorSecretKey"
            v-model="secretKey"
            :placeholder="$t('gamification.connectors.settings.secretKey.placeholder')"
            type="text"
            class="ignore-vuetify-classes full-width"
            required
            @input="disabled = false"
            @change="disabled = false">
        </v-card-text>
        <v-card-text class="text-left py-0 dark-grey-color">
          {{ $t('gamification.connectors.settings.redirectUrl') }}
        </v-card-text>
        <v-card-text>
          <input
            ref="connectorRedirectUrl"
            v-model="redirectUrl"
            :placeholder="$t('gamification.connectors.settings.redirectUrl.placeholder')"
            type="text"
            class="ignore-vuetify-classes full-width"
            required
            @input="disabled = false"
            @change="disabled = false">
        </v-card-text>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('githubConnector.webhook.form.label.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="disabledSave"
          class="btn btn-primary"
          @click="saveConnectorSetting">
          {{ $t('githubConnector.webhook.form.label.button.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    apiKey: null,
    secretKey: null,
    redirectUrl: null,
    isValidForm: false,
    drawer: false,
    disabled: true,
  }),
  created() {
    this.$root.$on('github-connection-setting-drawer', this.open);
  },
  computed: {
    disabledSave() {
      return this.disabled || !this.secretKey || !this.apiKey || !this.redirectUrl;
    },
  },
  methods: {
    open(apiKey, secretKey, redirectUrl) {
      this.apiKey = apiKey;
      this.secretKey = secretKey;
      this.redirectUrl = redirectUrl;
      if (this.$refs.githubConnectionSettingDrawer) {
        this.$refs.githubConnectionSettingDrawer.open();
      }
    },
    close() {
      if (this.$refs.githubConnectionSettingDrawer) {
        this.$refs.githubConnectionSettingDrawer.close();
      }
    },
    saveConnectorSetting() {
      this.$root.$emit('connector-settings-updated', this.apiKey, this.secretKey, this.redirectUrl);
      this.close();
    },
    clear() {
      this.apiKey = null;
      this.secretKey = null;
      this.redirectUrl = null;
      this.disabled = true;
    },
  }
};
</script>