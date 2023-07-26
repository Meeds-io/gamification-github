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
  <exo-drawer
    ref="githubHookFormDrawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('githubConnector.admin.label.addOrganization') }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="OrganizationForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left dark-grey-color text-font-size pb-2">
          {{ $t('githubConnector.admin.label.enableWatch') }}
        </v-card-text>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left dark-grey-color text-subtitle-1 pb-2">
          {{ $t('githubConnector.admin.label.organization') }}
        </v-card-text>
        <v-card-text class="d-flex py-0">
          <input
            ref="organizationNameInput"
            v-model="organizationName"
            :placeholder="$t('githubConnector.admin.label.organization.placeholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>
        <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left dark-grey-color text-subtitle-1 pb-2">
          {{ $t('githubConnector.admin.label.hookSecret') }}
        </v-card-text>
        <v-card-text class="d-flex py-0">
          <input
            ref="hookSecretInput"
            v-model="hookSecret"
            :placeholder="$t('githubConnector.admin.label.hookSecret.placeholder')"
            type="text"
            class="ignore-vuetify-classes flex-grow-1"
            maxlength="2000"
            required>
        </v-card-text>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('gamification.connectors.settings.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="disabledSave"
          @click="createHook">
          {{ $t('gamification.connectors.settings.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    isValidForm: false,
    drawer: false,
    organizationName: null,
    hookSecret: null,
  }),
  created() {
    document.addEventListener('gamification-github-hook-form-open', this.open);
  },
  computed: {
    disabledSave() {
      return !this.organizationName || !this.hookSecret;
    },
  },
  methods: {
    open() {
      if (this.$refs.githubHookFormDrawer) {
        this.$refs.githubHookFormDrawer.open();
      }
    },
    close() {
      if (this.$refs.githubHookFormDrawer) {
        this.$refs.githubHookFormDrawer.close();
      }
    },
    createHook() {
      this.$root.$emit('save-connector-hook', this.organizationName, this.hookSecret);
      this.close();
    }
  }
};
</script>