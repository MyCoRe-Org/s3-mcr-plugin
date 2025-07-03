export enum FileFlag {
  ARCHIVE = 'ARCHIVE',
  ARCHIVE_ENTRY = 'ARCHIVE_ENTRY',
}

export enum FileCapability {
  DOWNLOAD = 'DOWNLOAD',
}

export type FileInfo = {
  name: string;
  parentPath: string;
  size?: number;
  lastModified?: Date;
  checksum?: string;
  isDirectory: boolean;
  capabilities: FileCapability[];
  flags: FileFlag[];
};

export type DerivateTitle = {
  text: string;
  lang: string;
  form: string;
};

export type DerivateInfo = {
  id: string;
  view: boolean;
  write: boolean;
  delete: boolean;
  titles: DerivateTitle[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  metadata: Record<string, any>;
};

export type DerivateInformations = {
  derivates: DerivateInfo[];
  create: boolean;
};

export type S3BucketSettings = {
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
