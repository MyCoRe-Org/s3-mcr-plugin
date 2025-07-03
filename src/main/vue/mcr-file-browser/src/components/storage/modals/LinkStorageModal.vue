<template>
  <b-modal
    id="linkStorageModal"
    v-model="model"
    :title="tp('linkStorage')"
    :ok-title="tp('save')"
    :busy="isBusy"
    :cancel-title="tp('cancel')"
    @ok="linkStorage"
    @show="resetForm"
    @cancel="resetForm"
    @close="resetForm"
    @backdrop="resetForm"
  >
    <div v-if="showLinkStorageError" class="alert alert-danger" role="alert">
      {{ linkStorageErrorMessage }}
    </div>
    <div class="mb-3">
      <label for="storageSelect" class="form-label">
        {{ tp('chooseStorageType') }}
      </label>
      <select
        id="storageSelect"
        v-model="storageType"
        class="form-select"
        aria-label="Select storage type"
      >
        <option
          v-for="option in Object.values(StorageType)"
          :key="option"
          :value="option"
        >
          {{ tp(`storageType.${option}`) }}
        </option>
      </select>
    </div>
    <div id="storageFormContainer" class="p-3 border rounded shadow-sm">
      <form @submit.prevent>
        <component
          :is="currentForm.component"
          v-model="formData"
          :is-valid="currentValidation.isValid"
        />
        <hr />
        <FormCheckbox
          id="useDownloadProxy"
          v-model="formData.useDownloadProxy"
          :label="tp('useDownloadProxy')"
          :tooltip="tp('help.useDownloadProxy')"
        />
        <FormInput
          id="customDownloadProxyUrl"
          v-model="formData.customDownloadProxyUrl"
          class="pt-2"
          :label="tp('customDownloadProxyUrl')"
          :tooltip="tp('help.customDownloadProxyUrl')"
          :disabled="!formData.useDownloadProxy"
          :validation="isValid.customDownloadProxyUrl"
        />
      </form>
    </div>
  </b-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { BModal, BvTriggerableEvent } from 'bootstrap-vue-next';
import { StorageApiClient } from '@/api/StorageApiClient';
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';
import { storageFormRegistry } from '@/registry/storageFormRegistry';
import { StorageType } from '@/types/storage';
import FormCheckbox from '../forms/common/FormCheckbox.vue';
import FormInput from '../forms/common/FormInput.vue';

const { tp } = useI18nPrefix(I18N_PREFIX);

const { objectId, client } = defineProps<{
  objectId: string;
  client: StorageApiClient;
}>();
const emit = defineEmits(['storageLinked']);

const model = defineModel<boolean>({
  default: false,
  required: true,
});

const storageType = ref<StorageType>(StorageType.S3);
const currentForm = computed(() => storageFormRegistry[storageType.value]);
const formData = ref(currentForm.value.createDefaults());
const { isValid, validate, resetValidation } =
  currentForm.value.useValidation();
const currentValidation = { isValid, validate, resetValidation };
const showLinkStorageError = ref(false);
const linkStorageErrorMessage = ref('');
const isBusy = ref(false);

watch(
  () => formData.value.useDownloadProxy,
  enabled => {
    if (!enabled) {
      formData.value.customDownloadProxyUrl = undefined;
    }
  }
);

watch(storageType, () => {
  resetForm();
});

const showError = (message: string): void => {
  linkStorageErrorMessage.value = message;
  showLinkStorageError.value = true;
};

const resetError = (): void => {
  showLinkStorageError.value = false;
  linkStorageErrorMessage.value = '';
};

const resetFormData = (): void => {
  formData.value = currentForm.value.createDefaults();
};

const resetForm = (): void => {
  resetError();
  resetFormData();
  resetValidation();
};

const linkStorage = async (e: BvTriggerableEvent): Promise<void> => {
  isBusy.value = true;
  e.preventDefault();
  resetError();
  resetValidation();
  if (!currentValidation.validate(formData.value)) {
    isBusy.value = false;
    return;
  }
  try {
    await client.linkStorage(objectId, storageType.value, formData.value);
    emit('storageLinked');
  } catch {
    showError(tp('error.linkStorage'));
  } finally {
    isBusy.value = false;
  }
};
</script>
