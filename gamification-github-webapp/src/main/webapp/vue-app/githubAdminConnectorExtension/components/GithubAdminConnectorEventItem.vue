<template>
  <tr>
    <td>
      <div class="clickable d-flex flex-row ma-auto">
        <div class="d-flex flex-column pa-0 justify-center pe-3">
          <v-icon size="40" class="text-color">fab fa-github</v-icon>
        </div>
        <div class="d-flex flex-column pa-0 text-truncate">
          <span class="text-caption">{{ titleLabel }} </span>
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
    title() {
      return this.event?.title;
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
      this.$gamificationConnectorService.saveEventStatus('github', this.organizationId, this.title, this.event.enabled);
    },
  }
};
</script>