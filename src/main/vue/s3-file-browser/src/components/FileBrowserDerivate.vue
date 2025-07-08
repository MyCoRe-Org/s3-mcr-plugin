<template>
  <div>
    <div>
      <breadcrumb-view
        :crumbs="crumbs"
        @crumb-clicked="crumbClickedBreadCrumbView"
      />
    </div>
    <div v-if="loading" class="d-flex justify-content-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
    <file-table-view
      v-else
      :fs="files"
      :is-root="crumbs.length === 1"
      @back-button-clicked="backButtonClickedFileTableView"
      @file-clicked="fileClickedFileTableView"
      @file-download-clicked="fileDownloadClickedFileTableView"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, toRefs } from 'vue';
import FileTableView from '@/components/FileTableView.vue';
import BreadcrumbView, { Crumb } from '@/components/BreadcrumbView.vue';
import { FileInfo, FileCapability, FileFlag } from '@/types';
import { ApiClient } from '@/api/client';

interface Props {
  baseUrl: string;
  objectId: string;
  derivateId: string;
  title: string;
  client: ApiClient;
}
const props = defineProps<Props>();
const { objectId, derivateId, title } = toRefs(props);

const directoryPath = ref('');
const files = ref<FileInfo[]>([]);
const crumbs = ref<Crumb[]>([]);
const loading = ref(false);

const breadcrumbChange = (path: string): void => {
  crumbs.value = [];
  const until = [];
  const crumbLabels = path.split('/');
  for (let i = 0; i < crumbLabels.length; i += 1) {
    if (crumbLabels[i] !== '') {
      until.push(crumbLabels[i]);
      crumbs.value.push({
        id: `${until.join('/')}`,
        label:
          until.length === 1 && title.value !== undefined
            ? title.value
            : crumbLabels[i],
      });
    }
  }
};

const onDirectoryChange = async (): Promise<void> => {
  loading.value = true;
  files.value = [];
  try {
    if (directoryPath.value !== '') {
      breadcrumbChange(`${props.derivateId}/${directoryPath.value}`);
    } else {
      breadcrumbChange(props.derivateId);
    }
    files.value = await props.client.listDirectory(
      props.objectId,
      props.derivateId,
      directoryPath.value
    );
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  onDirectoryChange();
});

watch(objectId, () => {
  onDirectoryChange();
});

watch(derivateId, () => {
  files.value = [];
  directoryPath.value = '';
  onDirectoryChange();
});

const crumbClickedBreadCrumbView = (crumb: Crumb): void => {
  directoryPath.value = crumb.id.substring(`${props.derivateId}/`.length);
  onDirectoryChange();
};

const backButtonClickedFileTableView = (): void => {
  if (directoryPath.value != null) {
    const parts = directoryPath.value.split('/');
    parts.pop();
    directoryPath.value = parts.length > 0 ? parts.join('/') : '';
    onDirectoryChange();
  }
};

const fileDownloadClickedFileTableView = async (
  file: FileInfo
): Promise<void> => {
  if (file.capabilities.indexOf(FileCapability.DOWNLOAD) !== -1) {
    try {
      const downloadToken = await props.client.getDownloadToken(
        props.objectId,
        props.derivateId,
        getPath(file)
      );
      window.open(downloadToken);
    } catch {
      console.error('Error while downloading file');
    }
  }
};

const getPath = (file: FileInfo): string => {
  return file.parentPath !== '' ? `${file.parentPath}/${file.name}` : file.name;
};

const fileClickedFileTableView = (file: FileInfo): void => {
  if (file.isDirectory || file.flags.indexOf(FileFlag.ARCHIVE) !== -1) {
    directoryPath.value = getPath(file);
    onDirectoryChange();
  }
};
</script>
