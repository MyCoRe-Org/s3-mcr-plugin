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
