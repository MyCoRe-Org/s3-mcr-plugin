<template>
  <table class="table table-sm table-striped esv-file-table">
    <thead>
      <tr>
        <th v-for="field in fields" :key="field.key" scope="col">
          {{ field.label }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-if="isBusy">
        <td :colspan="fields.length">
          <div class="text-center" aria-busy="true">
            <div class="spinner-border" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
          </div>
        </td>
      </tr>
      <template v-else>
        <tr v-if="!isRoot">
          <td :colspan="fields.length">
            <button class="btn btn-link p-0" @click="handleBackButton">
              ..
            </button>
          </td>
        </tr>
        <tr v-if="fs.length === 0">
          <td :colspan="fields.length" class="text-center">
            {{ tp('emptyText') }}
          </td>
        </tr>
        <tr v-for="item in paginatedItems" :key="resolvePath(item)">
          <td>
            <button
              v-if="isNavigable(item)"
              class="btn btn-link p-0"
              :aria-label="item.name"
              @click.prevent="handleFile(item)"
            >
              {{ item.name }}
            </button>
            <template v-else>
              {{ item.name }}
            </template>
          </td>
          <td>
            <span v-if="item.checksum" :title="item.checksum">
              {{ item.checksum }}
            </span>
          </td>
          <td>
            <span
              v-if="item.lastModified"
              :title="getDateString(item.lastModified, locale)"
            >
              {{ getDateString(item.lastModified, locale) }}
            </span>
          </td>
          <td>
            <span
              v-if="!item.isDirectory && item.size !== undefined"
              :title="getFileSizeAsString(item)"
            >
              {{ getFileSizeAsString(item) }}
            </span>
          </td>
          <td>
            <button
              v-if="isDownloadable(item)"
              class="btn btn-link p-0"
              @click.prevent="handleDownload(item)"
            >
              <i class="fa fa-download" />
            </button>
          </td>
        </tr>
      </template>
    </tbody>
  </table>
  <nav v-if="pageCount > 1" aria-label="Pagination">
    <ul class="pagination pagination-sm justify-content-center">
      <li class="page-item" :class="{ disabled: currentPage === 1 }">
        <button
          class="page-link"
          type="button"
          aria-label="Previous"
          :disabled="currentPage === 1"
          @click="currentPage -= 1"
        >
          &laquo;
        </button>
      </li>
      <li
        v-for="page in visiblePages"
        :key="page"
        class="page-item"
        :class="{ active: page === currentPage }"
      >
        <button
          class="page-link"
          type="button"
          :aria-current="page === currentPage ? 'page' : undefined"
          @click="currentPage = page"
        >
          {{ page }}
        </button>
      </li>
      <li class="page-item" :class="{ disabled: currentPage === pageCount }">
        <button
          class="page-link"
          type="button"
          aria-label="Next"
          :disabled="currentPage === pageCount"
          @click="currentPage += 1"
        >
          &raquo;
        </button>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';
import {
  getFileSizeAsString,
  isDownloadable,
  isNavigable,
  resolvePath,
} from '@/utils/file';
import { FileInfo } from '@/types/file';
import { getDateString } from '@/utils/ui';

const { tp } = useI18nPrefix(I18N_PREFIX);

const { fs, isBusy, isRoot, locale } = defineProps<{
  fs: FileInfo[];
  isRoot: boolean;
  isBusy: boolean;
  locale: string;
}>();
const emit = defineEmits([
  'fileClicked',
  'fileDownloadClicked',
  'backButtonClicked',
]);

const perPage = 8;
const maxVisiblePages = 5;
const currentPage = ref(1);
const pageCount = computed(() => Math.ceil(fs.length / perPage));
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * perPage;
  return fs.slice(start, start + perPage);
});
const visiblePages = computed(() => {
  const count = Math.min(maxVisiblePages, pageCount.value);
  const first = Math.min(
    Math.max(1, currentPage.value - Math.floor(maxVisiblePages / 2)),
    pageCount.value - count + 1
  );
  return Array.from({ length: count }, (_, i) => first + i);
});

watch(
  () => fs,
  () => {
    currentPage.value = 1;
  }
);

// TODO checksum type?
const fields = computed(() => [
  { key: 'name', label: tp('fileName') },
  { key: 'checksum', label: tp('checksum') },
  { key: 'lastModified', label: tp('fileDate') },
  { key: 'size', label: tp('fileSize') },
  { key: 'download', label: '' },
]);
const handleFile = (file: FileInfo): void => emit('fileClicked', file);
const handleDownload = (file: FileInfo): void =>
  emit('fileDownloadClicked', file);
const handleBackButton = (): void => emit('backButtonClicked');
</script>
