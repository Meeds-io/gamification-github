<template>
  <v-card flat class="pb-8">
    <v-card-text class="dark-grey-color font-weight-bold text-subtitle-1 pa-0">{{ $t('githubConnector.admin.label.repositories') }}</v-card-text>
    <v-card-text class="dark-grey-color text-subtitle-1 pa-0">{{ $t('githubConnector.admin.label.repositories.placeholder') }}</v-card-text>
    <div class="d-flex flex-row pt-4">
      <div class="d-flex flex-column">
        <v-card-text class="dark-grey-color text-subtitle-1 pa-0">{{ $t('githubConnector.admin.label.watchScope') }}</v-card-text>
        <v-card-text class="text-sub-title pa-0">{{ $t('githubConnector.admin.label.watchScope.placeholder') }}</v-card-text>
      </div>
      <v-spacer />
      <v-switch
        v-model="hook.watchScopeLimited"
        color="primary"
        class="px-2"
        hide-details
        @change="enableDisableWatchScope" />
    </div>
    <v-data-table
      :headers="repositoriesHeaders"
      :items="repositories"
      :options.sync="options"
      :server-items-length="totalSize"
      :show-rows-border="false"
      :loading="loading"
      class="pt-5"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort>
      <template slot="item" slot-scope="props">
        <github-admin-connector-repository-item :repository="props.item" :organization-id="organizationId" />
      </template>
      <template v-if="displayFooter" #footer="{props}">
        <v-divider />
        <div class="text-center">
          <v-pagination
            v-model="options.page"
            :length="props.pagination.pageCount"
            circle
            light
            flat
            @input="retrieveHookRepositories" />
        </div>
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
export default {
  props: {
    hook: {
      type: Object,
      default: null
    },
  },
  data() {
    return {
      repositories: [],
      options: {
        page: 1,
        itemsPerPage: 5,
      },
      totalSize: 0,
      loading: false
    };
  },
  computed: {
    title() {
      return this.hook?.title;
    },
    organizationId() {
      return this.hook?.organizationId;
    },
    repositoriesHeaders() {
      return [
        {text: this.$t('githubConnector.webhook.details.repository'), value: 'title', align: 'start', width: '20%' , class: 'dark-grey-color ps-0',},
        {text: this.$t('githubConnector.webhook.details.description'), value: 'description', align: 'start', width: '60%', class: 'dark-grey-color'},
        {text: this.$t('githubConnector.webhook.details.status'), value: 'enabled', align: 'center', width: '20%', class: 'dark-grey-color'},];
    },
    displayFooter() {
      return this.totalSize > this.options.itemsPerPage;
    },
  },
  created() {
    this.retrieveHookRepositories();
  },
  methods: {
    retrieveHookRepositories() {
      this.loading = true;
      const page = this.options?.page || 0;
      const itemsPerPage = this.options?.itemsPerPage || 10;
      return this.$githubConnectorService.getWebHookRepos(this.organizationId, page, itemsPerPage)
        .then(data => {
          this.repositories = data.remoteRepositories;
          this.totalSize = data.size || 0;
          return this.$nextTick();
        }).finally(() => this.loading = false);
    },
    enableDisableWatchScope() {
      this.$githubConnectorService.enableDisableWatchScope(this.organizationId, this.hook.watchScopeLimited);
    },
  }
};
</script>