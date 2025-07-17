<template>
  <b-modal
    id="storageConfigModal"
    v-model="model"
    :title="tp('storageConfig')"
    ok-only
    hide-footer
    hide-backdrop
  >
    <dl v-for="(value, name) in derivateInfo.metadata" :key="name">
      <dt>{{ tp(`${type}.${name}`) }}</dt>
      <dd>{{ value }}</dd>
    </dl>
  </b-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { BModal } from 'bootstrap-vue-next';
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';
import { DerivateInfo } from '@/types/storage';

const { tp } = useI18nPrefix(I18N_PREFIX);

const { derivateInfo } = defineProps<{
  derivateInfo: DerivateInfo;
}>();

const model = defineModel<boolean>({
  default: false,
  required: true,
});

const type = computed(() => derivateInfo.type ?? 's3');
</script>
