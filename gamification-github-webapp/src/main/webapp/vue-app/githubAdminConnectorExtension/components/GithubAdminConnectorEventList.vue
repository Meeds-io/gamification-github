<template>
  <v-card flat>
    <v-card-text class="dark-grey-color font-weight-bold text-subtitle-1 pa-0">{{ $t('githubConnector.admin.label.events') }}</v-card-text>
    <v-card-text class="dark-grey-color text-subtitle-1 pa-0">{{ $t('githubConnector.admin.label.events.placeholder') }}</v-card-text>
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
      loading: false
    };
  },
  computed: {
    organizationId() {
      return this.hook?.organizationId;
    },
    events() {
      return this.hook?.events || [];
    },
    eventsHeaders() {
      return [
        {text: this.$t('githubConnector.webhook.details.event'), align: 'start', width: '80%'},
        {text: this.$t('githubConnector.webhook.details.status'), align: 'center', width: '20%'},];
    },
  },
};
</script>