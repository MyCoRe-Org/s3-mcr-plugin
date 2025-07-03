import { DerivateInformations } from '@/types/storage/core';
import { FileInfo } from '@/types/file';
import { AuthStrategy } from '@jsr/mycore__js-common/auth';
import { handleError, ensureOk } from '@jsr/mycore__js-common/utils/http';
import { StorageTypes } from '@/types/storage/core';

export class StorageApiClient {
  constructor(
    private baseUrl: string | URL,
    private authStrategy?: AuthStrategy
  ) {}

  public async linkStorage<T extends Record<string, unknown>>(
    objectId: string,
    type: StorageTypes,
    storageConfig: T
  ): Promise<void> {
    try {
      const response = await fetch(
        `${this.baseUrl}api/v2/es/${objectId}/add/${type}/`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            ...(await this.getAuthHeaders()),
          },
          body: JSON.stringify(storageConfig),
        }
      );
      ensureOk(response);
    } catch (error) {
      throw handleError(error, 'Failed to link storage');
    }
  }

  public async unlinkStorage(
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
      throw handleError(error, 'Failed to unlink storage');
    }
  }

  public async getDerivateInfo(
    objectId: string
  ): Promise<DerivateInformations> {
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
        `${this.baseUrl}api/v2/es/${objectId}/download/${this.encodePath(derivateId, path)}`,
        {
          headers: {
            ...(await this.getAuthHeaders()),
          },
        }
      );
      ensureOk(response);
      return await response.text();
    } catch (error) {
      throw handleError(error, 'Failed to create download link');
    }
  }

  public async listDirectory(
    objectId: string,
    derivateId: string,
    path?: string
  ): Promise<FileInfo[]> {
    const url =
      path !== undefined
        ? `${this.baseUrl}api/v2/es/${objectId}/list/${this.encodePath(derivateId, path)}`
        : `${this.baseUrl}api/v2/es/${objectId}/list/${this.encodePath(derivateId)}`;
    try {
      const response = await fetch(url, {
        headers: {
          ...(await this.getAuthHeaders()),
        },
      });
      ensureOk(response);
      return (await response.json()) as FileInfo[];
    } catch (error) {
      throw handleError(error, 'Failed to list directory');
    }
  }

  private encodePath(...parts: string[]): string {
    return parts.map(part => btoa(part)).join('/');
  }

  private async getAuthHeaders(): Promise<Record<string, string>> {
    return this.authStrategy ? await this.authStrategy.getHeaders() : {};
  }
}
