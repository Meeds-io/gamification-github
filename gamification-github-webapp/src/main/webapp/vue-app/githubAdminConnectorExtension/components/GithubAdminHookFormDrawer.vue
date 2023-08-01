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
    right
    @opened="stepper = 1"
    @closed="clear">
    <template #title>
      {{ drawerTitle }}
    </template>
    <template v-if="drawer" #content>
      <v-form
        ref="OrganizationForm"
        v-model="isValidForm"
        class="form-horizontal pt-0 pb-4"
        flat
        @submit="createHook">
        <v-stepper
          v-model="stepper"
          class="ma-0 py-0 d-flex flex-column"
          vertical
          flat>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              step="1"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1"><v-icon size="14" class="pe-2">fas fa-key</v-icon>{{ $t('githubConnector.admin.label.accessToken') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper === 1" class="px-6">
                <v-card-text class="d-flex py-0">
                  <v-text-field
                    ref="accessTokenInput"
                    v-model="accessTokenInput"
                    :readonly="!isTokenEditing"
                    :placeholder="$t('githubConnector.admin.label.accessToken.placeholder')"
                    :class="accessToken ? 'mx-2 pa-0' : 'mx-2 pa-0 me-8' "
                    dense>
                    <template #append-outer>
                      <v-slide-x-reverse-transition mode="out-in">
                        <v-icon
                          :key="`icon-${isTokenEditing}`"
                          :color="isTokenEditing ? 'success' : 'info'"
                          :disabled="!accessTokenInput && isTokenEditing"
                          class="text-header-title"
                          @click="editAccessToken"
                          v-text="isTokenEditing ? 'fas fa-check' : 'fas fa fa-edit'" />
                      </v-slide-x-reverse-transition>
                    </template>
                  </v-text-field>
                </v-card-text>
              </div>
            </v-slide-y-transition>
          </div>
          <div class="flex-grow-1 flex-shrink-0">
            <v-stepper-step
              :complete="stepper > 2"
              step="2"
              class="ma-0">
              <span class="font-weight-bold dark-grey-color text-subtitle-1"><v-icon size="16" class="pe-2">mdi-webhook</v-icon>{{ $t('githubConnector.admin.label.Webhook') }}</span>
            </v-stepper-step>
            <v-slide-y-transition>
              <div v-show="stepper > 1" class="px-6">
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
                    required
                    @change="hookEdited = true">
                </v-card-text>
                <v-card-text class="d-flex flex-grow-1 text-no-wrap text-left dark-grey-color text-subtitle-1 pb-2">
                  {{ $t('githubConnector.admin.label.hookSecret') }}
                </v-card-text>
                <v-card-text class="d-flex py-0">
                  <v-text-field
                    ref="hookSecretInput"
                    v-model="hookSecretInput"
                    :readonly="!isHookSecretEditing"
                    :placeholder="$t('githubConnector.admin.label.hookSecret.placeholder')"
                    :class="hookSecret ? 'mx-2 pa-0' : 'mx-2 pa-0 me-8' "
                    dense>
                    <template #append-outer>
                      <v-slide-x-reverse-transition mode="out-in">
                        <v-icon
                          :key="`icon-${isHookSecretEditing}`"
                          :color="isHookSecretEditing ? 'success' : 'info'"
                          :disabled="!hookSecretInput && isHookSecretEditing"
                          class="text-header-title"
                          @click="editHookSecret"
                          v-text="isHookSecretEditing ? 'fas fa-check' : 'fas fa fa-edit'" />
                      </v-slide-x-reverse-transition>
                    </template>
                  </v-text-field>
                </v-card-text>
              </div>
            </v-slide-y-transition>
          </div>
        </v-stepper>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          v-if="stepper === 2"
          class="btn me-2"
          @click="previousStep">
          {{ $t('githubConnector.webhook.form.label.button.back') }}
        </v-btn>
        <v-btn
          v-else
          class="btn me-2"
          @click="close">
          {{ $t('githubWebhookManagement.cancel') }}
        </v-btn>
        <v-btn
          v-if="stepper === 1"
          :disabled="disableNextStepButton"
          class="btn btn-primary"
          @click="nextStep">
          {{ $t('githubConnector.webhook.form.label.button.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="disabledSave"
          class="btn btn-primary"
          @click="createHook">
          {{ $t('githubWebhookManagement.save') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  data: () => ({
    hook: {},
    stepper: 0,
    isValidForm: false,
    drawer: false,
    organizationName: null,
    hookSecret: null,
    accessToken: null,
    isTokenEditing: false,
    accessTokenInput: null,
    accessTokenStored: false,
    hookSecretInput: null,
    isHookSecretEditing: false,
    hookSecretStored: false,
    hookEdited: false
  }),
  created() {
    this.$root.$on('github-hook-form-drawer', this.open);
  },
  computed: {
    organizationRemoteId() {
      return this.hook?.organizationId;
    },
    disabledSave() {
      return !this.hookEdited || !this.organizationName || (this.organizationRemoteId && !this.hookSecretStored) || (this.organizationRemoteId && !this.hookSecret) || this.isHookSecretEditing;
    },
    disableNextStepButton() {
      return (this.organizationRemoteId && !this.accessTokenStored) || (this.organizationRemoteId && !this.accessToken && this.isTokenEditing) || this.isTokenEditing;
    },
    drawerTitle() {
      return this.organizationRemoteId ? this.$t('githubConnector.admin.label.editOrganization') : this.$t('githubConnector.admin.label.addOrganization');
    },
  },
  methods: {
    open(hook) {
      if (hook) {
        this.hook = hook;
        this.organizationName = hook?.name || null;
        this.accessTokenInput = '*'.repeat(8);
        this.accessTokenStored = true;
        this.hookSecretInput = '*'.repeat(8);
        this.hookSecretStored = true;
      } else {
        this.accessTokenInput = null;
        this.isTokenEditing = true;
        this.hookSecretInput = null;
        this.isHookSecretEditing = true;
      }
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
      return this.$githubConnectorService.saveGithubWebHook(this.organizationName, this.accessToken, this.hookSecret).then(() => {
        this.$root.$emit('github-hooks-updated');
        this.close();
      });
    },
    editAccessToken() {
      if (this.isTokenEditing) {
        this.accessToken = this.accessTokenInput;
        this.accessTokenInput = '*'.repeat(16);
        this.isTokenEditing = false;
        this.hookEdited = true;
        this.stepper++;
      } else {
        this.accessTokenInput =null;
        this.isTokenEditing = true;
        this.$nextTick(() => {
          const $input = this.$refs['accessTokenInput'];
          if ($input) {
            $input.focus();
          }
        });
      }
    },
    editHookSecret() {
      if (this.isHookSecretEditing) {
        this.hookSecret = this.hookSecretInput;
        this.hookSecretInput = '*'.repeat(16);
        this.isHookSecretEditing = false;
        this.hookEdited = true;
      } else {
        this.hookSecretInput =null;
        this.isHookSecretEditing = true;
      }
    },
    previousStep() {
      this.stepper--;
      this.$forceUpdate();
    },
    nextStep(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.stepper++;
    },
    clear() {
      this.stepper = 0;
      this.accessToken = null;
      this.organizationName = null;
      this.hookSecret = null;
      this.hook = {};
    },
  }
};
</script>