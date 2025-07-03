<template>
  <base-modal
    id="storageConfigModal"
    v-model:visible="model"
    :title="tp('storageConfig')"
    ok-only
  >
    <dl v-for="(value, name) in derivateInfo.metadata" :key="name">
      <dt>{{ getLabel(name) }}</dt>
      <dd>{{ value }}</dd>
    </dl>
  </base-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BaseModal from '@mycore-org/vue-components/src/components/modal/BaseModal.vue';
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

// settings shared by all storage types have no type specific message
const COMMON_SETTINGS = ['useDownloadProxy', 'customDownloadProxyUrl'];

const getLabel = (name: string | number): string =>
  COMMON_SETTINGS.includes(String(name))
    ? tp(String(name))
    : tp(`${type.value}.${name}`);
</script>
