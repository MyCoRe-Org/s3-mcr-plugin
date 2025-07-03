<template>
  <FormSelect
    id="protocol"
    v-model="config.protocol"
    :label="tp('protocol')"
    :placeholder="tp('protocol')"
    :tooltip="tp('help.protocol')"
    :error-message="tp('validation.protocol')"
    :options="['http', 'https']"
    :validation="isValid.protocol"
  />
  <FormInput
    id="endpoint"
    v-model="config.endpoint"
    class="pt-2"
    :label="tp('endpoint')"
    :tooltip="tp('help.endpoint')"
    :error-message="tp('validation.endpoint')"
    :validation="isValid.endpoint"
  />
  <FormInput
    id="bucket"
    v-model="config.bucket"
    class="pt-2"
    :label="tp('bucket')"
    :tooltip="tp('help.bucket')"
    :error-message="tp('validation.bucket')"
    :validation="isValid.bucket"
  />
  <FormInput
    id="accessKey"
    v-model="config.accessKey"
    class="pt-2"
    :label="tp('accessKey')"
    :tooltip="tp('help.accessKey')"
    :error-message="tp('validation.accessKey')"
    :validation="isValid.accessKey"
  />
  <FormInput
    id="secretKey"
    v-model="config.secretKey"
    class="pt-2"
    :label="tp('secretKey')"
    :tooltip="tp('help.secretKey')"
    :error-message="tp('validation.secretKey')"
    :validation="isValid.secretKey"
    type="password"
    autocomplete="off"
  />
  <FormInput
    id="directory"
    v-model="config.directory"
    class="pt-2"
    :label="tp('directory')"
    :tooltip="tp('help.directory')"
    :error-message="tp('validation.directory')"
  />
  <FormCheckbox
    id="pathStyleAccess"
    v-model="config.pathStyleAccess"
    class="pt-2"
    :label="tp('pathStyleAccess')"
    :tooltip="tp('help.pathStyleAccess')"
  />
</template>

<script setup lang="ts">
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';
import { S3BucketConfig } from '@/types/storage/s3';
import FormSelect from '../common/FormSelect.vue';
import FormInput from '../common/FormInput.vue';
import FormCheckbox from '../common/FormCheckbox.vue';
import { S3ValidatedKeys } from '@/composables/storage/s3/useS3BucketConfigFormValidation';
import { ValidationState } from '@/types/storage/validation';

const { tp } = useI18nPrefix(`${I18N_PREFIX}.s3`);

const { isValid } = defineProps<{
  isValid: ValidationState<S3ValidatedKeys>;
}>();

const config = defineModel<S3BucketConfig>({
  required: true,
});
</script>
