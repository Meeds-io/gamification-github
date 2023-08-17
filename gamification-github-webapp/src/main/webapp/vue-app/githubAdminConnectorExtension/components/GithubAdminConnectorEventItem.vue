<template>
  <tr>
    <td>
      <div class="clickable d-flex flex-row ma-auto">
        <div class="d-flex flex-column pa-0 justify-center pe-3">
          <v-icon size="40">mdi-github</v-icon>
        </div>
        <div class="d-flex flex-column pa-0 text-truncate">
          <span class="text-caption">{{ title }} </span>
          <span class="text-caption text-sub-title">{{ description }} </span>
        </div>
      </div>
    </td>
    <td>
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="event.enabled"
          :ripple="false"
          color="primary"
          class="connectorSwitcher my-auto"
          @change="enableDisableEvent"/>
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
    name() {
      return this.event?.name;
    },
    title() {
      return this.$t(`githubConnector.webhook.event.title.${this.name}`);
    },
    description() {
      return this.$t(`githubConnector.webhook.event.description.${this.name}`);
    },
  },
  methods: {
    enableDisableEvent() {
      this.$githubConnectorService.saveEventStatus(this.organizationId, this.name, this.event.enabled);
    },
  }
};
</script>