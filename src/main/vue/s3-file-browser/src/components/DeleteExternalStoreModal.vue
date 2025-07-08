<template>
  <b-modal
    id="delelte-external-store-modal"
    v-model="model"
    :title="i18n.deleteBucket"
    ok-variant="danger"
    @ok="handleOk"
  >
    <div v-if="showDeleteBucketError" class="alert alert-danger" role="alert">
      {{ i18n.deleteBucketError }}
      {{ deleteErrorMessage }}
    </div>
    <p>{{ i18n.deleteBucketModal }}</p>
  </b-modal>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import I18n from '@/i18n';
import { ApiClient } from '@/api/client';
import { BModal, BvTriggerableEvent } from 'bootstrap-vue-next';

const model = defineModel<boolean>({
  default: false,
  required: true,
});

interface Props {
  objectId: string;
  derivateId: string;
  client: ApiClient;
}

const emit = defineEmits(['store-deleted']);

const props = defineProps<Props>();

const showDeleteBucketError = ref(false);
const deleteErrorMessage = ref('');

const i18n: Record<string, string> = {
  deleteBucket: '',
  deleteBucketModal: '',
};

onMounted(async () => {
  await I18n.loadToObject(i18n);
});

const removeBucket = async (): Promise<void> => {
  showDeleteBucketError.value = false;
  try {
    await props.client.removeStore(props.objectId, props.derivateId);
    emit('store-deleted');
  } catch {
    showDeleteBucketError.value = true;
    deleteErrorMessage.value = 'Error while removing bucket';
  }
};

const handleOk = async (e: BvTriggerableEvent) => {
  e.preventDefault();
  removeBucket();
};
</script>
