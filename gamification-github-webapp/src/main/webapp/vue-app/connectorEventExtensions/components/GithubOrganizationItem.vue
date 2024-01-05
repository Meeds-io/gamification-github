<template>
  <v-chip dark class="width-fit-content">
    <v-avatar left>
      <v-img :src="avatarUrl" />
    </v-avatar>
    <span class="text-truncate">
      {{ title }}
    </span>
  </v-chip>
</template>

<script>
export default {
  props: {
    organization: {
      type: Object,
      default: null
    },
  },
  computed: {
    title() {
      return this.organization?.title || this.organization?.name;
    },
    avatarUrl() {
      if (this.organization?.avatarUrl) {
        return `${this.organization.avatarUrl}?version=${new Date().getTime()}`;
      } else {
        return '/gamification-github/skin/images/GitHubDefaultAvatar.png';
      }
    }
  }
};
</script>

<style scoped>

</style>