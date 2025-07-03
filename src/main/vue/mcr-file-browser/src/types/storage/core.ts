export enum StorageType {
  S3 = 's3',
}

export type DerivateTitle = {
  text: string;
  lang: string;
  form: string;
};

export type DerivateInfo = {
  id: string;
  type: StorageType;
  view: boolean;
  write: boolean;
  delete: boolean;
  titles: DerivateTitle[];
  metadata: Record<string, unknown>;
};

export type DerivateInformations = {
  derivates: DerivateInfo[];
  create: boolean;
};
