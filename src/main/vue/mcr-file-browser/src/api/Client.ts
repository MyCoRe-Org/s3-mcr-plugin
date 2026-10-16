import { S3BucketSettings, Token } from '@/model';

const getStoresUrl = (baseUrl: string, objectId: string) => `${baseUrl}api/v2/es/${objectId}/stores`;

const encodePath = (path: string) => path.split('/').map(encodeURIComponent).join('/');

export const getErrorMessage = async (response: Response): Promise<string> => {
  try {
    const error = await response.json();
    return [error.message, error.detail].filter((m) => m).join(': ');
  } catch (e) {
    return response.statusText;
  }
};

export const saveS3Bucket = (
  baseUrl: string,
  objectId: string,
  bucketSettings: S3BucketSettings,
  token?: Token,
) => fetch(getStoresUrl(baseUrl, objectId), {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
  body: JSON.stringify({ type: 's3', settings: bucketSettings }),
});

export const removeStore = (
  baseUrl: string,
  objectId: string,
  derivateId: string,
  token?: Token,
) => fetch(`${getStoresUrl(baseUrl, objectId)}/${derivateId}`, {
  method: 'DELETE',
  headers: {
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
});

export const getInfo = (
  baseUrl: string,
  objectId: string,
  token?: Token,
) => fetch(getStoresUrl(baseUrl, objectId), {
  headers: {
    Accept: 'application/json',
    Authorization: `${token?.tokenType} ${token?.accessToken}`,
  },
});

export const getDownloadToken = (
  baseUrl: string,
  objectId: string,
  derivateId: string,
  path: string,
  token?: Token,
) => fetch(`${getStoresUrl(baseUrl, objectId)}/${derivateId}/download-url/${encodePath(path)}`, {
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
  const filesUrl = `${getStoresUrl(baseUrl, objectId)}/${derivateId}/files`;
  const url = path ? `${filesUrl}/${encodePath(path)}` : filesUrl;
  return fetch(url, {
    headers: {
      Authorization: `${token?.tokenType} ${token?.accessToken}`,
    },
  });
};

export const getJWT = (baseUrl: string) => fetch(`${baseUrl}rsc/jwt`, {
  credentials: 'include',
});
