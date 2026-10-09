<template>
  <div class="esv-file-browser">
    <file-breadcrumb
      v-if="!isRoot"
      :crumbs="crumbs"
      @navigate-to="handleNavigateTo"
    />
    <file-table
      :fs="directoryFiles"
      :locale="locale"
      :is-busy="isLoadingDirectory"
      :is-root="isRoot"
      @back-button-clicked="handleBackClick"
      @file-clicked="handleFileClick"
      @file-download-clicked="handleFileDownload"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, toRefs } from 'vue';
import FileTable from '@/components/file/FileTable.vue';
import FileBreadcrumb from '@/components/file/FileBreadcrumb.vue';
import { FileInfo } from '@/types/file';
import { Crumb } from '@/types/ui';
import { StorageApiClient } from '@/api/StorageApiClient';
import {
  resolvePath,
  getParentPath,
  isNavigable,
  isDownloadable,
} from '@/utils/file';
import { buildBreadcrumbs } from '@/utils/ui';

const props = defineProps<{
  objectId: string;
  derivateId: string;
  title: string;
  locale: string;
  client: StorageApiClient;
}>();

const { derivateId } = toRefs(props);
const currentPath = ref<string>('');
const directoryFiles = ref<FileInfo[]>([]);
const crumbs = computed(() =>
  currentPath.value ? buildBreadcrumbs(props.title, currentPath.value) : []
);
const isRoot = computed(() => crumbs.value.length === 0);
const isLoadingDirectory = ref(false);

const loadDirectory = async (): Promise<void> => {
  isLoadingDirectory.value = true;
  try {
    directoryFiles.value = await props.client.listDirectory(
      props.objectId,
      props.derivateId,
      currentPath.value
    );
  } catch (error) {
    console.error('Failed to load directory:', error);
    directoryFiles.value = [];
  } finally {
    isLoadingDirectory.value = false;
  }
};

const updateDirectory = async (newPath: string = '') => {
  currentPath.value = newPath;
  await loadDirectory();
};

watch(derivateId, () => updateDirectory(''), { immediate: true });

const handleNavigateTo = (crumb: Crumb) => updateDirectory(crumb.id);
const handleBackClick = () => {
  if (currentPath.value.length > 0) {
    updateDirectory(getParentPath(currentPath.value));
  }
};

const handleFileDownload = async (file: FileInfo): Promise<void> => {
  if (!isDownloadable(file)) {
    return;
  }
  try {
    const downloadToken = await props.client.getDownloadToken(
      props.objectId,
      props.derivateId,
      resolvePath(file)
    );
    window.open(downloadToken);
  } catch (error) {
    console.error('Download error:', error);
  }
};

const handleFileClick = (file: FileInfo) => {
  if (isNavigable(file)) {
    updateDirectory(resolvePath(file));
  }
};
</script>
