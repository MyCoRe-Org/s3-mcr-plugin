<template>
  <b-modal
    id="unlinkStorageModal"
    v-model="model"
    :title="tp('unlinkStorage')"
    :busy="isBusy"
    :cancel-title="tp('cancel')"
    ok-variant="danger"
    @ok="unlinkStorage"
  >
    <div v-if="showUnlinkStorageError" class="alert alert-danger" role="alert">
      {{ unlinkErrorMessage }}
    </div>
    <p>{{ tp('unlinkStorageInfo') }}</p>
  </b-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { BModal, BvTriggerableEvent } from 'bootstrap-vue-next';
import { StorageApiClient } from '@/api/StorageApiClient';
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';

const { tp } = useI18nPrefix(I18N_PREFIX);

const { objectId, derivateId, client } = defineProps<{
  objectId: string;
  derivateId: string;
  client: StorageApiClient;
}>();
const emit = defineEmits(['storage-unlinked']);

const model = defineModel<boolean>({
  default: false,
  required: true,
});

const showUnlinkStorageError = ref(false);
const unlinkErrorMessage = ref('');
const isBusy = ref(false);

const resetError = (): void => {
  showUnlinkStorageError.value = false;
  unlinkErrorMessage.value = '';
};

const showError = (message: string): void => {
  unlinkErrorMessage.value = message;
  showUnlinkStorageError.value = true;
};

const unlinkStorage = async (e: BvTriggerableEvent) => {
  e.preventDefault();
  isBusy.value = true;
  resetError();
  try {
    await client.unlinkStorage(objectId, derivateId);
    emit('storage-unlinked');
    model.value = false;
  } catch {
    showError(tp('error.unlinkStorage'));
  } finally {
    isBusy.value = false;
  }
};
</script>
