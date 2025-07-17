export type S3BucketConfig = {
  endpoint: string;
  pathStyleAccess: boolean;
  signingRegion?: string;
  accessKey: string;
  secretKey: string;
  protocol: string;
  bucket: string;
  directory?: string;
  useDownloadProxy: boolean;
  customDownloadProxyUrl?: string;
};
