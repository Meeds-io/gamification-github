<template>
  <v-card flat>
    <div class="py-2 py-sm-5 d-flex align-center">
      <v-tooltip :disabled="$root.isMobile" bottom>
        <template #activator="{ on }">
          <v-card
            class="d-flex align-center"
            flat
            v-on="on"
            @click="backToSetting">
            <v-btn
              class="width-auto ms-n3"
              icon>
              <v-icon size="18" class="icon-default-color mx-2">fa-arrow-left</v-icon>
            </v-btn>
            <div class="text-header-title">{{ title }}</div>
          </v-card>
        </template>
        <span>{{ $t('gamification.connectors.settings.BackToDetail') }}</span>
      </v-tooltip>
    </div>
    <github-admin-connector-repository-list :hook="hook" />
    <github-admin-connector-event-list :hook="hook"/>
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
  computed: {
    title() {
      return this.hook?.title;
    },
  },
  methods: {
    backToSetting() {
      this.$emit('close');
    },
  }
};
</script>