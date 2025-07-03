import { reactive } from 'vue';
import { S3BucketConfig, ValidationState } from '@/types/storage';

export type S3ValidatedKeys =
  | 'endpoint'
  | 'bucket'
  | 'protocol'
  | 'secretKey'
  | 'accessKey'
  | 'customDownloadProxyUrl';

type ValidatorFn<T> = (value: unknown, config: T) => boolean;

const validators: Record<S3ValidatedKeys, ValidatorFn<S3BucketConfig>> = {
  endpoint: val => typeof val === 'string' && val.trim().length > 0,
  bucket: val => typeof val === 'string' && val.trim().length > 0,
  protocol: val => val === 'http' || val === 'https',
  secretKey: val => typeof val === 'string' && val.trim().length > 0,
  accessKey: val => typeof val === 'string' && val.trim().length > 0,
  customDownloadProxyUrl: (val, config) =>
    !config.useDownloadProxy ||
    (typeof val === 'string' && val.trim().length > 0),
};

export function useS3FormValidation() {
  const initialState = (): ValidationState<S3ValidatedKeys> =>
    Object.keys(validators).reduce((acc, key) => {
      acc[key as S3ValidatedKeys] = { clean: true, valid: false };
      return acc;
    }, {} as ValidationState<S3ValidatedKeys>);

  const isValid = reactive(initialState());

  const resetValidation = (): void => {
    for (const key of Object.keys(isValid) as S3ValidatedKeys[]) {
      isValid[key].clean = true;
      isValid[key].valid = false;
    }
  };

  const validate = (config: S3BucketConfig): boolean => {
    resetValidation();
    let formValid = true;

    for (const key of Object.keys(validators) as S3ValidatedKeys[]) {
      isValid[key].clean = false;

      const value = config[key];
      const valid = validators[key](value, config);

      isValid[key].valid = valid;
      if (!valid) {
        formValid = false;
      }
    }

    return formValid;
  };

  return {
    isValid,
    resetValidation,
    validate,
  };
}
