<template>
  <b-modal
    id="show-bucket-info-modal"
    v-model="model"
    :title="i18n.displayInfo"
    ok-only
    hide-footer
    hide-backdrop
  >
    <dl v-for="(value, name) in derivateInfo.metadata" :key="name">
      <dt>{{ i18n[name] }}</dt>
      <dd>{{ value }}</dd>
    </dl>
  </b-modal>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { DerivateInfo } from '@/types';
import { BModal } from 'bootstrap-vue-next';
import I18n from '@/i18n';
const model = defineModel<boolean>({
  default: false,
  required: true,
});

interface Props {
  derivateInfo: DerivateInfo;
}

defineProps<Props>();

const i18n: Record<string, string> = {
  actions: '',
  addBucket: '',
  deleteBucket: '',
  deleteBucketModal: '',
  headline: '',
  displayInfo: '',
  endpoint: '',
  bucket: '',
  accessKey: '',
  scretKey: '',
  pathStyleAccess: '',
  directory: '',
  protocol: '',
  useDownloadProxy: '',
  customDownloadProxyUrl: '',
};

onMounted(async () => {
  await I18n.loadToObject(i18n);
});
</script>
