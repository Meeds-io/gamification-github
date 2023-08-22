<template>
  <v-card flat>
    <v-card-text class="dark-grey-color font-weight-bold text-subtitle-1 pa-0">{{ $t('gamification.label.events') }}</v-card-text>
    <v-card-text class="dark-grey-color text-subtitle-1 pa-0">{{ $t('gamification.label.events.placeholder') }}</v-card-text>
    <v-data-table
      :headers="eventsHeaders"
      :items="events"
      :options.sync="options"
      :server-items-length="totalSize"
      :show-rows-border="false"
      :loading="loading"
      class="pt-5"
      mobile-breakpoint="0"
      hide-default-footer
      disable-sort>
      <template slot="item" slot-scope="props">
        <github-admin-connector-event-item :event="props.item" :organization-id="organizationId" />
      </template>
    </v-data-table>
    <div v-if="hasMoreEvents" class="d-flex justify-center py-4">
      <v-btn
        :loading="loading"
        min-width="95%"
        class="btn"
        text
        @click="loadMore">
        {{ $t('rules.loadMore') }}
      </v-btn>
    </div>
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
      options: {
        page: 1,
        itemsPerPage: 10,
      },
      events: [],
      eventsSize: 0,
      pageSize: 5,
      limit: 5,
      loading: true,
    };
  },
  computed: {
    organizationId() {
      return this.hook?.organizationId;
    },
    hasMoreEvents() {
      return this.eventsSize > this.limit;
    },
    triggers() {
      return this.hook?.triggers || [];
    },
    eventsHeaders() {
      return [
        {text: this.$t('githubConnector.webhook.details.event'), align: 'start', width: '80%'},
        {text: this.$t('githubConnector.webhook.details.status'), align: 'center', width: '20%'},];
    },
  },
  created() {
    this.retrieveWebHookEvents();
  },
  methods: {
    retrieveWebHookEvents() {
      this.$gamificationConnectorService.getEvents('github', this.organizationId, this.triggers, 0 , this.limit)
        .then(data => {
          this.events = data.entities;
          this.eventsSize = data.size;
        })
        .finally(() => this.loading = false);
    },
    loadMore() {
      this.limit += this.pageSize;
      this.retrieveWebHookEvents();
    },
  }
};
</script>