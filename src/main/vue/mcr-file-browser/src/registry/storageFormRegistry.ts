import S3BucketConfigForm from '@/components/storage/forms/s3/S3BucketConfigForm.vue';
import { useS3FormValidation } from '@/composables/storage/s3/useS3BucketConfigFormValidation';
import { StorageTypes } from '@/types/storage/core';
import { StorageFormConfig } from '@/types/storage/form-config';
import { useDefaultS3BucketConfig } from '@/composables/storage/s3/useDefaultS3BucketConfig';

export const storageFormRegistry: Record<
  StorageTypes,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  StorageFormConfig<any, any>
> = {
  s3: {
    component: S3BucketConfigForm,
    createDefaults: () => ({ ...useDefaultS3BucketConfig() }),
    useValidation: useS3FormValidation,
  },
};
