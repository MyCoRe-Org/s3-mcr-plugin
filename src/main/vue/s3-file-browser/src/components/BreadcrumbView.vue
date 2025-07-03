<template>
  <div class="mcr-breadcrumb-view">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li
          v-for="crumb in crumbs"
          :key="crumb.id"
          class="breadcrumb-item"
          :class="{ active: crumbs[crumbs.length - 1] === crumb }"
          :aria-current="
            crumbs[crumbs.length - 1] === crumb ? 'page' : undefined
          "
        >
          <a
            v-if="crumbs[crumbs.length - 1] !== crumb"
            href="#"
            @click.prevent="crumbClicked(crumb)"
          >
            {{ crumb.label }}
          </a>
          <template v-else>{{ crumb.label }}</template>
        </li>
      </ol>
    </nav>
  </div>
</template>

<script setup lang="ts">
export interface Crumb {
  id: string;
  label: string;
}

interface Props {
  crumbs: Crumb[];
}
defineProps<Props>();

const emit = defineEmits(['crumbClicked']);

const crumbClicked = (crumb: Crumb): void => {
  emit('crumbClicked', crumb);
};
</script>
<style scoped>
.breadcrumb {
  margin-bottom: 0px;
}
</style>
