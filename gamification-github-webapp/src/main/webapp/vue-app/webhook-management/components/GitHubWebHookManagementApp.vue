<template>
  <v-app
    id="gitHubWebHookManagementApp"
    color="transparent"
    class="VuetifyApp">
    <main>
      <div
        v-if="alert"
        class="alert"
        :class="alert_type">
        <i :class="alertIcon"></i>{{ message }}
      </div>
      <v-layout>
        <v-data-table
          :headers="headers"
          :items="hookList"
          :search="search"
          sort-by="id">
          <template #top>
            <v-toolbar
              flat
              color="white"
              class="mt-3">
              <div class="flex-grow-1"></div>
              <v-col
                cols="12"
                sm="6"
                md="3">
                <v-text-field
                  v-model="search"
                  :placeholder="$t('githubWebhookManagement.search')"
                  append-icon="search"
                  class="pt-0" />
              </v-col>
              <v-divider
                class="mx-4"
                inset
                vertical />
              <v-dialog v-model="dialog" max-width="500px">
                <template #activator="{ on }">
                  <v-btn
                    color="primary"
                    dark
                    class="mb-2"
                    v-on="on">
                    {{ $t('githubWebhookManagement.newConnector') }}
                  </v-btn>
                </template>
                <v-card>
                  <v-card-title>
                    <span class="headline">{{ formTitle }}</span>
                  </v-card-title>
                  <div
                    v-if="alert_add"
                    class="alert"
                    :class="alert_type_add"
                    id="">
                    <i class="uiIconError"></i>{{ message_add }}
                  </div>
                  <v-card-text>
                    <v-container>
                      <v-row>
                        <v-col
                          cols="12"
                          sm="6"
                          md="6">
                          <v-text-field
                            v-model="editedItem.organization"
                            :rules="[rules.required, rules.counter]"
                            :label="$t('githubWebhookManagement.organization')" />
                        </v-col>
                        <v-col
                          cols="12"
                          sm="6"
                          md="6">
                          <v-text-field
                            v-model="editedItem.repo"
                            :rules="[rules.required, rules.counter]"
                            :label="$t('githubWebhookManagement.repository')" />
                        </v-col>
                        <v-col
                          cols="12"
                          sm="6"
                          md="4">
                          <v-checkbox v-model="editedItem.enabled" :label="$t('githubWebhookManagement.enabled')" />
                        </v-col>
                      </v-row>
                    </v-container>
                  </v-card-text>

                  <v-card-actions>
                    <div class="flex-grow-1"></div>
                    <div class="uiAction">
                      <button
                        type="button"
                        class="btn btn-primary"
                        text
                        @click="save">
                        {{ $t('githubWebhookManagement.save') }}
                      </button>
                      <button
                        type="button"
                        class="btn"
                        @click="close">
                        {{ $t('githubWebhookManagement.cancel') }}
                      </button>
                    </div>
                  </v-card-actions>
                </v-card>
              </v-dialog>
              <v-dialog v-model="confirmDialog" max-width="290">
                <v-card>
                  <v-card-title class="headline">
                    {{ $t('githubWebhookManagement.confirmation') }}
                  </v-card-title>
                  <v-card-text>
                    {{ $t('githubWebhookManagement.deleteConfirmationMessage') }}
                  </v-card-text>
                  <v-card-actions>
                    <div class="flex-grow-1"></div>
                    <div class="uiAction">
                      <button
                        type="button"
                        class="btn btn-primary"
                        @click="delete_()">
                        {{ $t('githubWebhookManagement.delete') }}
                      </button>
                      <button
                        type="button"
                        class="btn"
                        @click="confirmDialog = false">
                        {{ $t('githubWebhookManagement.cancel') }}
                      </button>
                    </div>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-toolbar>
          </template>
          <template #[`item.enabled`]="{ item }">
            <form class="switchEnabled">
              <label class="switch">
                <input
                  type="checkbox"
                  v-model="item.enabled"
                  @change="editItem(item)">
                <span class="slider round"></span>
                <span class="absolute-no">{{ $t('githubWebhookManagement.no') }}</span>
              </label>
            </form>
          </template>
          <template #[`item.action`]="{ item }">
            <a
              role="button"
              class="actionIcon"
              @click="deleteItem(item)"><v-icon class="error-color" size="18">fas fa-trash-alt</v-icon></a>
          </template>
          <template #no-data>
            {{ $t('githubWebhookManagement.noWebhooks') }}
          </template>
        </v-data-table>
      </v-layout>
    </main>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    search: '',
    dialog: false,
    confirmDialog: false,
    itemToDelete: 0,
    alert: false,
    message: '',
    alert_type: '',
    alertIcon: '',
    alert_add: false,
    message_add: '',
    alert_type_add: '',
    hookList: [],
    editedIndex: -1,
    editedItem: {
      organization: '',
      repo: '',
      webhook: '',
      events: '',
      enabled: true,
    },
    defaultItem: {
      organization: '',
      repo: '',
      webhook: '',
      events: '',
      enabled: true,
    },
    rules: {
      required: value => !!value || 'Required.',
      counter: value => value.length >= 3 || 'Min 3 characters',
      startWith: value => value.startsWith('portal/rest') || 'Should start with \'portal/rest\', please don\'t include the server host',
    },
  }),
  computed: {
    formTitle() {
      return this.editedIndex === -1 ? this.$t('githubWebhookManagement.newConnector') : this.$t('githubWebhookManagement.editConnector');
    },
    headers() {
      return [{
        text: this.$t('githubWebhookManagement.organization'),
        align: 'center',
        sortable: true,
        value: 'organization',
      },
      {
        text: this.$t('githubWebhookManagement.repository'),
        align: 'center',
        sortable: true,
        value: 'repo',
      },
      {
        text: this.$t('githubWebhookManagement.enabled'),
        align: 'center',
        sortable: true,
        value: 'enabled',
      },
      {
        text: this.$t('githubWebhookManagement.actions'),
        align: 'center',
        value: 'action',
        name: 'action',
        sortable: false
      }];
    }
  },
  watch: {
    dialog(val) {
      return val === true || this.close() === true;
    },
  },
  created() {
    this.initialize();
  },
  methods: {
    initialize() {
      return fetch('/portal/rest/gamification/connectors/github/hooksmanagement/hooks', {
        credentials: 'include',
      })
        .then((resp) => resp.json())
        .then((resp) => this.hookList = resp);
    },
    editItem(item) {
      return fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks/${  item.id}`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(item),
      })
        .then((result) => {
          if (!result.ok) {
            throw result;
          }
        })
        .then(() => {
          this.initialize();
          this.displaySusccessMessage(this.$t('githubWebhookManagement.webhookUpdated'));
        })
        .catch((result) => {
          this.initialize();
          result.text().then((body) => {
            this.displayErrorMessage(body);
          });
        });
    },
    deleteItem(item) {
      this.itemToDelete = item.id;
      this.confirmDialog = true;
    },
    delete_() {
      fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks/${  this.itemToDelete}`, {
        method: 'delete',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
      })
        .then((result) => {
          if (!result.ok) {
            throw result;
          }
        })
        .then(() => {
          this.confirmDialog = false;
          this.initialize();
          this.displaySusccessMessage(this.$t('githubWebhookManagement.webhookDeleted'));
        })
        .catch((result) => {
          this.confirmDialog = false;
          this.initialize();
          result.text().then((body) => {
            this.displayErrorMessage(body);
          });
        });
    },
    close() {
      this.dialog = false;
      this.editedItem = this.defaultItem;
      setTimeout(() => {
        this.editedIndex = -1;
        this.initialize();
      }, 300);
    },
    save() {
      if (this.editedItem.organization === '' || this.editedItem.repo === '') {
        this.alert_type_add = 'alert-error';
        this.alert_add = true;
        this.message_add = this.$t('githubWebhookManagement.allFieldsShouldBeFilled');
        setTimeout(() => (this.alert_add = false), 5000);
      } else {
        if (this.editedIndex > -1) {
          fetch(`/portal/rest/gamification/connectors/github/hooksmanagement/hooks/${  this.editedItem.id}`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(this.editedItem),
          })
            .then((result) => {
              if (!result.ok) {
                throw result;
              }
            })
            .then(() => {
              this.initialize();
              this.displaySusccessMessage(this.$t('githubWebhookManagement.webhookUpdated'));
            })
            .catch((result) => {
              this.initialize();
              result.text().then((body) => {
                this.displayErrorMessage(body);
              });
            });
        } else {
          this.hookList.push(this.editedItem);

          fetch('/portal/rest/gamification/connectors/github/hooksmanagement/hooks', {
            method: 'post',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify(this.editedItem),
          })
            .then((result) => {
              if (!result.ok) {
                throw result;
              }
            })
            .then(() => {
              this.initialize();
              this.displaySusccessMessage(this.$t('githubWebhookManagement.webhookCreated'));
            })
            .catch((result) => {
              this.initialize();
              result.text().then((body) => {
                this.displayErrorMessage(body);
              });
            });
        }
        this.close();
      }
    },

    displaySusccessMessage(message) {
      this.message = message;
      this.alert_type = 'alert-success';
      this.alertIcon = 'uiIconSuccess';
      this.alert = true;
      setTimeout(() => (this.alert = false), 5000);
      this.editedItem = this.defaultItem;
    },
    displayErrorMessage(message) {
      this.isUpdating = false;

      this.message = message;
      this.alert_type = 'alert-error';
      this.alertIcon = 'uiIconError';
      this.alert = true;
      setTimeout(() => (this.alert = false), 5000);
    },
  },
};
</script>
