<template>
  <div class="mcr-file-system-tab">
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
        <simple-tooltip
          target="protocol"
          placement="left"
          triggers="hover focus"
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
        <simple-tooltip
          target="endpoint"
          placement="left"
          triggers="hover focus"
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
        <simple-tooltip
          target="bucket"
          placement="left"
          triggers="hover focus"
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
            'is-invalid': !isValid.accessKey.clean && !isValid.accessKey.valid,
          }"
          class="form-control"
          type="text"
        />
        <simple-tooltip
          target="accessKey"
          placement="left"
          triggers="hover focus"
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
            'is-invalid': !isValid.secretKey.clean && !isValid.secretKey.valid,
          }"
          class="form-control"
          type="password"
        />
        <simple-tooltip
          target="secretKey"
          placement="left"
          triggers="hover focus"
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
        <simple-tooltip
          target="directory"
          placement="left"
          triggers="hover focus"
          :title="i18n.helpDirectory"
        />
      </div>
      <div id="pta" class="form-check form-group">
        <input
          id="pathStyleAccess"
          v-model="bucketSettings.pathStyleAccess"
          class="form-check-input"
          type="checkbox"
        />
        <label class="form-check-label" for="pathStyleAccess">
          {{ i18n.pathStyleAccess }}
        </label>
        <simple-tooltip
          target="pta"
          placement="left"
          triggers="focus hover"
          :title="i18n.helpPathStyleAccess"
        />
      </div>
      <hr />
      <div id="useDownloadProxy" class="form-check form-group">
        <input
          id="useDownloadProxy"
          v-model="bucketSettings.useDownloadProxy"
          class="form-check-input"
          type="checkbox"
        />
        <label class="form-check-label" for="useDownloadProxy">
          {{ i18n.useDownloadProxy }}
        </label>
        <simple-tooltip
          target="useDownloadProxy"
          placement="left"
          triggers="focus hover"
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
        />
        <simple-tooltip
          target="customDownloadProxyUrl"
          placement="left"
          triggers="focus hover"
          :title="i18n.helpCustomDownloadProxyUrl"
        />
      </div>
      <button class="btn btn-primary" type="submit" @click="save()">
        {{ i18n.save }}
      </button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue';
import { S3BucketSettings } from '@/types';
import SimpleTooltip from './SimpleTooltip.vue';

const emit = defineEmits(['saveBucket']);

const i18n: Record<string, string> = {
  save: 'save',
  chooseProtocol: 'chooseProtocol',
  endpoint: 'endpoint',
  bucket: 'bucket',
  accessKey: 'accessKey',
  scretKey: 'scretKey',
  pathStyleAccess: 'pathStyleAccess',
  directory: 'directory',
  useDownloadProxy: 'useDownloadProxy',
  customDownloadProxyUrl: 'customDownloadProxyUrl',
  validationOk: 'validationOk',
  validationEndpointFail: 'validationEndpointFail',
  validationBucketFail: 'validationBucketFail',
  validationAccesKeyFail: 'validationAccesKeyFail',
  validationSecretKeyFail: 'validationSecretKeyFail',
  validationProtocolFail: 'validationProtocolFail',
  helpEndpoint: 'helpEndpoint',
  helpBucket: 'helpBucket',
  helpAccessKey: 'helpAccessKey',
  helpSecretKey: 'helpSecretKey',
  helpProtocol: 'helpProtocol',
  helpDirectory: 'helpDirectory',
  helpPathStyleAccess: 'helpPathStyleAccess',
  helpUseDownloadProxy: 'helpUseDownloadProxy',
  helpCustomDownloadProxyUrl: 'helpCustomDownloadProxyUrl',
};

const bucketSettings = reactive<S3BucketSettings>({
  endpoint: '',
  bucket: '',
  protocol: '',
  secretKey: '',
  accessKey: '',
  pathStyleAccess: true,
  directory: undefined,
  useDownloadProxy: false,
  customDownloadProxyUrl: undefined,
});

const isValid = reactive({
  endpoint: { clean: true, valid: false },
  bucket: { clean: true, valid: false },
  protocol: { clean: true, valid: false },
  secretKey: { clean: true, valid: false },
  accessKey: { clean: true, valid: false },
});

/*
onMounted(async () => {
  await I18n.loadToObject(i18n);
});
*/

const save = (): void => {
  isValid.endpoint.clean = false;
  isValid.bucket.clean = false;
  isValid.protocol.clean = false;
  isValid.secretKey.clean = false;
  isValid.accessKey.clean = false;

  isValid.endpoint.valid = bucketSettings.endpoint.trim().length > 0;
  isValid.bucket.valid = bucketSettings.bucket.trim().length > 0;
  isValid.protocol.valid =
    bucketSettings.protocol.trim().length > 0 &&
    (bucketSettings.protocol === 'http' || bucketSettings.protocol === 'https');
  isValid.secretKey.valid = bucketSettings.secretKey.trim().length > 0;
  isValid.accessKey.valid = bucketSettings.accessKey.trim().length > 0;

  if (
    isValid.endpoint.valid &&
    isValid.bucket.valid &&
    isValid.protocol.valid &&
    isValid.secretKey.valid &&
    isValid.accessKey.valid
  ) {
    emit('saveBucket', bucketSettings);
  }
};

watch(
  () => bucketSettings.useDownloadProxy,
  () => {
    bucketSettings.customDownloadProxyUrl = undefined;
  }
);

/*
const resetBucketSettings = (): void => {
  bucketSettings.endpoint = '';
  bucketSettings.bucket = '';
  bucketSettings.protocol = 'https';
  bucketSettings.secretKey = '';
  bucketSettings.accessKey = '';
  bucketSettings.pathStyleAccess = true;
  bucketSettings.directory = undefined;
  bucketSettings.useDownloadProxy = false;
  bucketSettings.customDownloadProxyUrl = undefined;
};
*/
</script>
<style scoped>
.clickable {
  cursor: pointer;
}
</style>
