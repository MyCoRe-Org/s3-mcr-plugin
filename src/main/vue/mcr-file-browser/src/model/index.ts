/*
* This file is part of ***  M y C o R e  ***
* See http://www.mycore.de/ for details.
*
* MyCoRe is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* MyCoRe is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
*/

// eslint-disable-next-line no-shadow
export enum FileFlag {
  ARCHIVE = 'ARCHIVE',
  ARCHIVE_ENTRY = 'ARCHIVE_ENTRY',
}

// eslint-disable-next-line no-shadow
export enum FileCapability {
  DOWNLOAD = 'DOWNLOAD',
}

export type FileInfo = {
  name: string,
  parentPath: string,
  size?: number,
  lastModified?: Date,
  checksum?: string,
  isDirectory: boolean,
  capabilities: FileCapability[],
  flags: FileFlag[],
};

export type DerivateTitle = {
  text: string;
  lang: string;
  form: string;
};

// eslint-disable-next-line no-shadow
export enum StoreCapability {
  CREATE = 'CREATE',
  EDIT = 'EDIT',
  DELETE = 'DELETE',
}

export type DerivateInfo = {
  id: string;
  type: string;
  capabilities: StoreCapability[];
  titles: DerivateTitle[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  metadata: Record<string, any>
}

export type DerivateInformations = {
  stores: DerivateInfo[];
  capabilities: StoreCapability[];
};

export type TokenResponse = {
  // eslint-disable-next-line camelcase
  token_type: 'Bearer';
  // eslint-disable-next-line camelcase
  access_token: string;
  // eslint-disable-next-line camelcase
  login_success: boolean;
};

export type Token = {
  tokenType: string;
  accessToken: string;
}

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
}
