import { S3BucketSettings, Token } from '@/model';

export const saveS3Bucket = (
  baseUrl: string,
  objectId: string,
  bucketSettings: S3BucketSettings,
  token?: Token,
) => fetch(`${baseUrl}api/v2/es/${objectId}/add/s3/`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
  body: JSON.stringify(bucketSettings),
});

export const removeStore = (
  baseUrl: string,
  objectId: string,
  derivateId: string,
  token?: Token,
) => fetch(`${baseUrl}api/v2/objects/${objectId}/derivates/${derivateId}`, {
  method: 'DELETE',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
});

export const getInfo = (
  baseUrl: string,
  objectId: string,
  token?: Token,
) => fetch(`${baseUrl}api/v2/es/${objectId}/info/`, {
  headers: {
    'Content-Type': 'application/json',
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
});

export const getDownloadToken = (
  baseUrl: string,
  objectId: string,
  derivateId: string,
  path: string,
  token?: Token,
) => fetch(`${baseUrl}api/v2/es/${objectId}/download/${btoa(derivateId)}/${btoa(path)}`, {
  headers: {
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
});

export const listDirectory = (
  baseUrl: string,
  objectId: string,
  derivateId: string,
  path?: string,
  token?: Token,
) => {
  const url = (path !== undefined) ? `${baseUrl}api/v2/es/${objectId}/list/${btoa(derivateId)}/${btoa(path)}`
    : `${baseUrl}api/v2/es/${objectId}/list/${btoa(derivateId)}`;
  return fetch(url, {
    headers: {
      Authorization: `${token?.tokenType} ${token?.accessToken}`,
    },
  });
};

export const getJWT = (baseUrl: string) => fetch(`${baseUrl}rsc/jwt`, {
  credentials: 'include',
});
