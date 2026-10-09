<template>
  <nav class="esv-breadcrumb" aria-label="breadcrumb">
    <ol class="breadcrumb">
      <li
        v-for="(crumb, index) in crumbs"
        :key="crumb.id ?? index"
        class="breadcrumb-item"
        :class="{ active: lastCrumb === crumb }"
        :aria-current="lastCrumb === crumb ? 'page' : undefined"
      >
        <a
          v-if="lastCrumb !== crumb"
          href="#"
          :title="crumb.label"
          @click.prevent="crumbClicked(crumb)"
        >
          {{ crumb.label }}
        </a>
        <template v-else>{{ crumb.label }}</template>
      </li>
    </ol>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Crumb } from '@/types/ui';
const { crumbs } = defineProps<{
  crumbs: Crumb[];
}>();
const emit = defineEmits<{
  (e: 'navigateTo', crumb: Crumb): void;
}>();
const lastCrumb = computed(() =>
  crumbs.length > 0 ? crumbs.at(-1) : undefined
);

const crumbClicked = (crumb: Crumb): void => emit('navigateTo', crumb);
</script>
<style scoped>
ol.breadcrumb {
  margin-bottom: 0px;
}
</style>
