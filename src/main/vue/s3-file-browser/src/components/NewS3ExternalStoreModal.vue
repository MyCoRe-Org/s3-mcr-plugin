<template>
  <b-modal
    id="add-bucket-modal"
    v-model="model"
    :title="i18n.addBucket"
    :ok-title="i18n.save"
    @ok="saveBucket"
    @cancel="resetForm"
    @close="resetForm"
  >
    <div class="mcr-file-system-tab">
      <div v-if="showAddBucketError" class="alert alert-danger" role="alert">
        {{ i18n.addBucketError }}
        {{ addBucketErrorMessage }}
      </div>
      <form @submit.prevent.stop="">
        <slot></slot>
        <div class="form-group">
          <label for="protocol">Protocol</label>
          <select
            id="protocol"
            v-model="bucketSettings.protocol"
            :class="{
              'is-valid': !isValid.protocol.clean && isValid.protocol.valid,
              'is-invalid': !isValid.protocol.clean && !isValid.protocol.valid,
            }"
            class="form-control"
          >
            <option disabled value="">
              {{ i18n.chooseProtocol }}
            </option>
            <option>http</option>
            <option>https</option>
          </select>
          <b-tooltip
            target="protocol"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpProtocol"
          />
          <div
            v-if="!isValid.protocol.clean && !isValid.protocol.valid"
            class="invalid-feedback"
          >
            {{ i18n.validationProtocolFail }}
          </div>
        </div>
        <div class="form-group">
          <label for="endpoint">
            {{ i18n.endpoint }}
          </label>
          <input
            id="endpoint"
            v-model="bucketSettings.endpoint"
            :class="{
              'is-valid': !isValid.endpoint.clean && isValid.endpoint.valid,
              'is-invalid': !isValid.endpoint.clean && !isValid.endpoint.valid,
            }"
            class="form-control"
            type="text"
          />
          <b-tooltip
            target="endpoint"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpEndpoint"
          />
          <div
            v-if="!isValid.endpoint.clean && !isValid.endpoint.valid"
            class="invalid-feedback"
          >
            {{ i18n.validationEndpointFail }}
          </div>
        </div>
        <div class="form-group">
          <label for="bucket">
            {{ i18n.bucket }}
          </label>
          <input
            id="bucket"
            v-model="bucketSettings.bucket"
            :class="{
              'is-valid': !isValid.bucket.clean && isValid.bucket.valid,
              'is-invalid': !isValid.bucket.clean && !isValid.bucket.valid,
            }"
            class="form-control"
            type="text"
          />
          <b-tooltip
            target="bucket"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpBucket"
          />
          <div
            v-if="!isValid.bucket.clean && !isValid.bucket.valid"
            class="invalid-feedback"
          >
            {{ i18n.validationBucketFail }}
          </div>
        </div>
        <div class="form-group">
          <label for="accessKey">
            {{ i18n.accessKey }}
          </label>
          <input
            id="accessKey"
            v-model="bucketSettings.accessKey"
            :class="{
              'is-valid': !isValid.accessKey.clean && isValid.accessKey.valid,
              'is-invalid':
                !isValid.accessKey.clean && !isValid.accessKey.valid,
            }"
            class="form-control"
            type="text"
          />
          <b-tooltip
            target="accessKey"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpAccessKey"
          />
          <div
            v-if="!isValid.accessKey.clean && !isValid.accessKey.valid"
            class="invalid-feedback"
          >
            {{ i18n.validationAccesKeyFail }}
          </div>
        </div>
        <div class="form-group">
          <label for="secretKey">{{ i18n.secretKey }}</label>
          <input
            id="secretKey"
            v-model="bucketSettings.secretKey"
            :class="{
              'is-valid': !isValid.secretKey.clean && isValid.secretKey.valid,
              'is-invalid':
                !isValid.secretKey.clean && !isValid.secretKey.valid,
            }"
            class="form-control"
            type="password"
            autocomplete="false"
          />
          <b-tooltip
            target="secretKey"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpSecretKey"
          />
          <div
            v-if="!isValid.secretKey.clean && !isValid.secretKey.valid"
            class="invalid-feedback"
          >
            {{ i18n.validationSecretKeyFail }}
          </div>
        </div>
        <div class="form-group">
          <label for="directory">
            {{ i18n.directory }}
          </label>
          <input
            id="directory"
            v-model="bucketSettings.directory"
            class="form-control"
            type="text"
          />
          <b-tooltip
            target="directory"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpDirectory"
          />
        </div>
        <div id="pathStyleAccessGroup" class="form-check form-group">
          <input
            id="pathStyleAccess"
            v-model="bucketSettings.pathStyleAccess"
            class="form-check-input"
            type="checkbox"
          />
          <label class="form-check-label" for="pathStyleAccess">
            {{ i18n.pathStyleAccess }}
          </label>
          <b-tooltip
            target="pathStyleAccessGroup"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpPathStyleAccess"
          />
        </div>
        <hr />
        <div id="useDownloadProxyGroup" class="form-check form-group pb-2">
          <input
            id="useDownloadProxy"
            v-model="bucketSettings.useDownloadProxy"
            class="form-check-input"
            type="checkbox"
          />
          <label class="form-check-label" for="useDownloadProxy">
            {{ i18n.useDownloadProxy }}
          </label>
          <b-tooltip
            target="useDownloadProxyGroup"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpUseDownloadProxy"
          />
        </div>
        <div class="form-group">
          <label for="customDownloadProxyUrl">
            {{ i18n.customDownloadProxyUrl }}
          </label>
          <input
            id="customDownloadProxyUrl"
            v-model="bucketSettings.customDownloadProxyUrl"
            class="form-control"
            :disabled="!bucketSettings.useDownloadProxy"
            type="text"
            :class="{
              'is-valid':
                !isValid.customDownloadProxyUrl.clean &&
                isValid.customDownloadProxyUrl.valid,
              'is-invalid':
                !isValid.customDownloadProxyUrl.clean &&
                !isValid.customDownloadProxyUrl.valid,
            }"
          />
          <b-tooltip
            target="customDownloadProxyUrl"
            :placement="TOOLTIP_PLACEMENT"
            :triggers="TOOLTIP_TRIGGERS"
            :title="i18n.helpCustomDownloadProxyUrl"
          />
        </div>
      </form>
    </div>
  </b-modal>
</template>

<script setup lang="ts">
import { reactive, watch, onMounted, ref } from 'vue';
import { S3BucketSettings } from '@/types';
import { BModal, BTooltip, BvTriggerableEvent } from 'bootstrap-vue-next';
import I18n from '@/i18n';
import { ApiClient } from '@/api/client';

const TOOLTIP_TRIGGERS = 'focus hover';
const TOOLTIP_PLACEMENT = 'left';

const model = defineModel<boolean>({
  default: false,
  required: true,
});

interface Props {
  objectId: string;
  client: ApiClient;
}

const props = defineProps<Props>();
const emit = defineEmits(['bucketCreated']);
const showAddBucketError = ref(false);
const addBucketErrorMessage = ref('');

const i18n: Record<string, string> = {
  addBucket: '',
  addBucketError: '',
  save: '',
  chooseProtocol: '',
  endpoint: '',
  bucket: '',
  accessKey: '',
  secretKey: '',
  pathStyleAccess: '',
  directory: '',
  useDownloadProxy: '',
  customDownloadProxyUrl: '',
  validationOk: '',
  validationEndpointFail: '',
  validationBucketFail: '',
  validationAccesKeyFail: '',
  validationSecretKeyFail: '',
  validationProtocolFail: '',
  helpEndpoint: '',
  helpBucket: '',
  helpAccessKey: '',
  helpSecretKey: '',
  helpProtocol: '',
  helpDirectory: '',
  helpPathStyleAccess: '',
  helpUseDownloadProxy: '',
  helpCustomDownloadProxyUrl: '',
};

const defaultBucketSettings = {
  endpoint: '',
  bucket: '',
  protocol: 'https',
  secretKey: '',
  accessKey: '',
  pathStyleAccess: true,
  directory: undefined,
  useDownloadProxy: false,
  customDownloadProxyUrl: undefined,
};

const bucketSettings = reactive<S3BucketSettings>({
  ...defaultBucketSettings,
});

type ValidationField = {
  clean: boolean;
  valid: boolean;
};

type ValidationState = {
  endpoint: ValidationField;
  bucket: ValidationField;
  protocol: ValidationField;
  secretKey: ValidationField;
  accessKey: ValidationField;
  customDownloadProxyUrl: ValidationField;
};

const isValid: ValidationState = reactive({
  endpoint: { clean: true, valid: false },
  bucket: { clean: true, valid: false },
  protocol: { clean: true, valid: false },
  secretKey: { clean: true, valid: false },
  accessKey: { clean: true, valid: false },
  customDownloadProxyUrl: { clean: true, valid: false },
});

const requiredFields: (keyof ValidationState)[] = [
  'endpoint',
  'bucket',
  'protocol',
  'secretKey',
  'accessKey',
];

onMounted(async () => {
  await I18n.loadToObject(i18n);
});

const resetValidation = (): void => {
  for (const key in isValid) {
    if (Object.prototype.hasOwnProperty.call(isValid, key)) {
      isValid[key as keyof ValidationState].clean = true;
      isValid[key as keyof ValidationState].valid = false;
    }
  }
};

const resetBucketSettings = (): void => {
  Object.assign(bucketSettings, defaultBucketSettings);
};

const resetError = (): void => {
  showAddBucketError.value = false;
  addBucketErrorMessage.value = '';
};

const resetForm = (): void => {
  resetError();
  resetValidation();
  resetBucketSettings();
};

watch(
  () => bucketSettings.useDownloadProxy,
  enabled => {
    if (!enabled) {
      bucketSettings.customDownloadProxyUrl = undefined;
    }
  }
);

const validateSettings = (): boolean => {
  resetValidation();
  let isFormValid = true;

  for (const key of requiredFields) {
    const field = key as keyof typeof bucketSettings;
    const value = (bucketSettings[field] as string)?.trim?.();

    isValid[key].clean = false;
    isValid[key].valid = !!value;

    if (key === 'protocol') {
      isValid.protocol.valid = value === 'http' || value === 'https';
    }

    if (!isValid[key].valid) {
      isFormValid = false;
    }
  }

  if (bucketSettings.useDownloadProxy) {
    const url = bucketSettings.customDownloadProxyUrl?.trim?.();
    isValid.customDownloadProxyUrl.clean = false;
    isValid.customDownloadProxyUrl.valid = !!url;
    if (!isValid.customDownloadProxyUrl.valid) {
      isFormValid = false;
    }
  }

  return isFormValid;
};

const saveBucket = async (e: BvTriggerableEvent): Promise<void> => {
  e.preventDefault();
  showAddBucketError.value = false;
  if (!validateSettings()) {
    return;
  }
  try {
    await props.client.saveS3Bucket(props.objectId, bucketSettings);
    emit('bucketCreated');
  } catch {
    showAddBucketError.value = true;
    addBucketErrorMessage.value = 'Error while saving bucket';
  }
};
</script>
