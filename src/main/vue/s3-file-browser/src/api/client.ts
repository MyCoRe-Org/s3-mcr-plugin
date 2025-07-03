import { DerivateInformations, FileInfo, S3BucketSettings } from '@/types';
import { AuthStrategy } from '@jsr/mycore__js-common/auth';
import { handleError, ensureOk } from '@jsr/mycore__js-common/utils/http';

export class ApiClient {
  constructor(
    private baseUrl: string | URL,
    private authStrategy?: AuthStrategy
  ) {}

  public async saveS3Bucket(
    objectId: string,
    bucketSettings: S3BucketSettings
  ): Promise<void> {
    try {
      const response = await fetch(
        `${this.baseUrl}api/v2/es/${objectId}/add/s3/`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(await this.getAuthHeaders()),
          },
          body: JSON.stringify(bucketSettings),
        }
      );
      ensureOk(response);
    } catch (error) {
      throw handleError(error, 'Failed to remove store');
    }
  }

  public async removeStore(
    objectId: string,
    derivateId: string
  ): Promise<void> {
    try {
      const response = await fetch(
        `${this.baseUrl}api/v2/objects/${objectId}/derivates/${derivateId}`,
        {
          method: 'DELETE',
          headers: {
            ...(await this.getAuthHeaders()),
          },
        }
      );
      ensureOk(response);
    } catch (error) {
      throw handleError(error, 'Failed to remove store');
    }
  }

  public async getInfo(objectId: string): Promise<DerivateInformations> {
    try {
      const response = await fetch(
        `${this.baseUrl}api/v2/es/${objectId}/info`,
        {
          headers: {
            'Content-Type': 'application/json',
            ...(await this.getAuthHeaders()),
          },
        }
      );
      ensureOk(response);
      return (await response.json()) as DerivateInformations;
    } catch (error) {
      throw handleError(error, 'Failed to get derivate informations');
    }
  }

  public async getDownloadToken(
    objectId: string,
    derivateId: string,
    path: string
  ): Promise<string> {
    try {
      const response = await fetch(
        `${this.baseUrl}api/v2/es/${objectId}/download/${btoa(derivateId)}/${btoa(path)}`,
        {
          headers: {
            ...(await this.getAuthHeaders()),
          },
        }
      );
      ensureOk(response);
      return await response.text();
    } catch (error) {
      throw handleError(error, 'Failed to get derivate informations');
    }
  }

  public async listDirectory(
    objectId: string,
    derivateId: string,
    path?: string
  ): Promise<FileInfo[]> {
    const url =
      path !== undefined
        ? `${this.baseUrl}api/v2/es/${objectId}/list/${btoa(derivateId)}/${btoa(path)}`
        : `${this.baseUrl}api/v2/es/${objectId}/list/${btoa(derivateId)}`;
    try {
      const response = await fetch(url, {
        headers: {
          ...(await this.getAuthHeaders()),
        },
      });
      ensureOk(response);
      return (await response.json()) as FileInfo[];
    } catch (error) {
      throw handleError(error, 'Failed to get derivate informations');
    }
  }

  private async getAuthHeaders(): Promise<Record<string, string>> {
    return this.authStrategy ? await this.authStrategy.getHeaders() : {};
  }
}
