<template>
  <tr>
    <td class="ps-0 no-border-bottom">
      <gamification-admin-connector-event
        :event="event"
        class="py-2" />
    </td>
    <td class="no-border-bottom d-flex justify-center py-2">
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="enabled"
          :ripple="false"
          color="primary"
          class="connectorSwitcher my-auto"
          @change="enableDisableEvent" />
      </div>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    event: {
      type: Object,
      default: null
    },
    organizationId: {
      type: String,
      default: null
    },
  },
  computed: {
    id() {
      return this.event?.id;
    },
    title() {
      return this.event?.title;
    },
    enabled() {
      const eventProperties = this.event?.properties;
      if (eventProperties && eventProperties[`${this.organizationId}.enabled`]) {
        return eventProperties[`${this.organizationId}.enabled`].toLowerCase() === 'true';
      }
      return true;
    },
    titleLabel() {
      return this.$t(`gamification.event.title.${this.title}`);
    },
    description() {
      return this.$t(`gamification.event.description.${this.title}`);
    },
  },
  methods: {
    enableDisableEvent() {
      this.$githubConnectorService.saveEventStatus(this.id, this.organizationId, !this.enabled);
    },
  }
};
</script>