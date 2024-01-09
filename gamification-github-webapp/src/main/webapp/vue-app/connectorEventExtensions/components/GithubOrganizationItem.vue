<!--
This file is part of the Meeds project (https://meeds.io/).

Copyright (C) 2020 - 2023 Meeds Association contact@meeds.io

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->
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