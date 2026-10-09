import type { S3BucketConfig } from '@/types/storage';

export const useDefaultS3BucketConfig = (): S3BucketConfig => {
  return {
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
};
