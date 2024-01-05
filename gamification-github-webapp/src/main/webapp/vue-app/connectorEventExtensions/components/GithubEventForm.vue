<template>
  <v-app>
    <v-card-text class="px-0 dark-grey-color font-weight-bold">
      {{ $t('gamification.event.form.organization') }}
    </v-card-text>
    <v-progress-circular
      v-if="loadingOrganizations"
      indeterminate
      color="primary"
      size="20"
      class="ms-3 my-auto" />
    <v-chip-group
      v-model="value"
      :show-arrows="false"
      active-class="primary white--text">
      <github-connector-organization-item
        v-for="organization in organizations"
        :key="organization.id"
        :organization="organization" />
    </v-chip-group>
    <template v-if="selected">
      <div class="d-flex flex-row">
        <v-card-text class="px-0 dark-grey-color font-weight-bold">
          {{ $t('gamification.event.form.repository') }}
        </v-card-text>
        <div class="d-flex flex-row">
          <div class="ma-auto"> {{ $t('gamification.event.form.any') }}</div>
          <v-checkbox
            v-model="anyRepo"
            class="mt-0 pt-0 align-center"
            color="primary"
            dense
            hide-details />
        </div>
      </div>
      <v-select
        v-model="repository"
        :items="repositories"
        :disabled="anyRepo"
        :label="$t('gamification.event.form.repository.placeholder')"
        class="py-0"
        outlined
        item-value="id"
        item-text="name"
        hide-details
        dense
        @change="repoSelected">
        <template v-if="hasMore" #append-item>
          <v-divider class="mb-2" />
          <v-btn
            :loading="loadingRepositories"
            class="btn ma-auto"
            text
            block
            @click="loadMore">
            {{ $t('rules.loadMore') }}
          </v-btn>
        </template>
      </v-select>
    </template>
  </v-app>
</template>

<script>
export default {
  props: {
    properties: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      page: 1,
      itemsPerPage: 10,
      organizations: [],
      selected: null,
      repository: null,
      repositories: [],
      value: null,
      loadingOrganizations: true,
      loadingRepositories: true,
      anyRepo: false,
      hasMore: false
    };
  },
  created() {
    this.retrieveOrganizations();
  },
  watch: {
    value() {
      this.selected = this.organizations[this.value];
    },
    selected() {
      this.repositories = [];
      this.repository = null;
      if (!this.properties) {
        this.anyRepo = true;
      } else {
        this.retrieveRepositories();
      }
    },
    anyRepo() {
      this.repository = null;
      if (this.anyRepo) {
        this.repoSelected('any');
      } else {
        this.retrieveRepositories();
        document.dispatchEvent(new CustomEvent('event-form-unfilled'));
      }
    }
  },
  methods: {
    retrieveOrganizations() {
      this.loadingOrganizations = true;
      return this.$githubConnectorService.getGithubWebHooks()
        .then(data => {
          this.organizations = data.webhooks;
        }).finally(() => {
          if (this.properties) {
            this.selected = this.organizations.find(r => Number(r.organizationId) === Number(this.properties.organizationId));
            this.value = this.organizations.indexOf(this.selected);
            this.anyRepo = this.properties?.repositoryId === 'any';
          } else if (this.organizations.length === 1) {
            this.selected = this.organizations[0];
            this.value = this.organizations.indexOf(this.selected);
          }
          this.loadingOrganizations = false;
        });
    },
    retrieveRepositories() {
      this.loadingRepositories = true;
      const page = this.page || 0;
      const itemsPerPage = this.itemsPerPage || 10;
      return this.$githubConnectorService.getWebHookRepos(this.selected?.organizationId, page, itemsPerPage, null)
        .then(data => {
          this.repositories.push(...data.remoteRepositories);
          this.selected = this.organizations.find(r => Number(r.organizationId) === Number(this.properties.organizationId));
          if (data.remoteRepositories.length <= itemsPerPage) {
            return this.$githubConnectorService.getWebHookRepos(this.selected?.organizationId, page + 1, itemsPerPage, null)
              .then(nextData => {
                this.hasMore = nextData.remoteRepositories.length > 0;
              });
          }
          return this.$nextTick();
        }).finally(() => {
          this.repository = this.properties?.repositoryId !== 'any' ?  this.repositories.find(r => Number(r.id) === Number(this.properties?.repositoryId)) : null;
          this.loadingRepositories = false;
        });
    },
    loadMore() {
      this.page += 1;
      this.retrieveRepositories();
    },
    repoSelected(repository) {
      const eventProperties = {
        organizationId: this.selected?.organizationId.toString(),
        repositoryId: repository ? repository : this.repository
      };
      document.dispatchEvent(new CustomEvent('event-form-filled', {detail: eventProperties}));
    },
  }
};
</script>