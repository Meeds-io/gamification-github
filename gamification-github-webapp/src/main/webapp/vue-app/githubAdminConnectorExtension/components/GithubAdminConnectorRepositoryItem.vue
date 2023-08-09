<template>
  <tr>
    <td>
      {{ name }}
    </td>
    <td class="py-3">
      {{ description }}
    </td>
    <td>
      <div class="d-flex flex-column align-center">
        <v-switch
          v-model="repository.enabled"
          :ripple="false"
          color="primary"
          class="connectorSwitcher my-auto"
          @change="enableDisableRepository" />
      </div>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    repository: {
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
      return this.repository?.id;
    },
    name() {
      return this.repository?.name;
    },
    description() {
      return this.repository?.description;
    },
  },
  methods: {
    enableDisableRepository() {
      this.$githubConnectorService.saveRepositoryStatus(this.id, this.organizationId, this.repository.enabled);
    },
  }
};
</script>