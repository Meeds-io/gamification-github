<template>
  <v-card flat>
    <v-card-text class="dark-grey-color font-weight-bold text-subtitle-1 pa-0">{{ $t('gamification.label.events') }}</v-card-text>
    <v-card-text class="dark-grey-color text-subtitle-1 pa-0">{{ $t('gamification.label.events.placeholder') }}</v-card-text>
    <v-data-table
      :headers="eventsHeaders"
      :items="eventToDisplay"
      :options.sync="options"
      :server-items-length="pageSize"
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
        itemsPerPage: 5,
      },
      events: [],
      eventsSize: 0,
      pageSize: 5,
      loading: true,
    };
  },
  computed: {
    organizationId() {
      return this.hook?.organizationId;
    },
    triggers() {
      return this.hook?.triggers || [];
    },
    eventsHeaders() {
      return [
        {text: this.$t('githubConnector.webhook.details.event'), align: 'start', width: '80%' , class: 'dark-grey-color ps-0'},
        {text: this.$t('githubConnector.webhook.details.status'), align: 'center', width: '20%', class: 'dark-grey-color'},];
    },
    hasMoreEvents() {
      return this.keyword ? this.sortedEvent.length > this.pageSize : this.eventsSize > this.pageSize;
    },
    sortedEvent() {
      let filteredEvent = this.events;
      if (this.keyword) {
        filteredEvent = this.events.filter(item =>
          this.getEventLabel(item).toLowerCase().includes(this.keyword.toLowerCase())
        );
      }
      return filteredEvent.sort((a, b) => this.getEventLabel(a).localeCompare(b.title));
    },
    eventToDisplay() {
      return this.sortedEvent.slice(0, this.pageSize);
    },
  },
  created() {
    this.retrieveWebHookEvents();
  },
  methods: {
    retrieveWebHookEvents() {
      this.$gamificationConnectorService.getEvents('github', this.triggers)
        .then(data => {
          this.events = data.entities;
          this.eventsSize = data.size;
        })
        .finally(() => this.loading = false);
    },
    loadMore() {
      this.pageSize += this.pageSize;
      this.retrieveWebHookEvents();
    },
    getEventLabel(event) {
      return this.$t(`gamification.event.title.${event.title}`);
    }
  }
};
</script>