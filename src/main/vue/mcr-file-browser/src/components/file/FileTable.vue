<template>
  <b-table
    class="esv-file-table"
    :current-page="currentPage"
    :fields="fields"
    :items="paginatedItems"
    :busy="isBusy"
    :per-page="perPage"
    small
    striped
    show-empty
    :empty-text="tp('emptyText')"
  >
    <template #table-busy>
      <div class="text-center" aria-busy="true">
        <div class="spinner-border" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>
    </template>
    <template v-if="!isRoot" #top-row="">
      <td :colspan="fields.length">
        <button class="btn btn-link p-0" @click="handleBackButton">..</button>
      </td>
    </template>
    <template #cell(name)="data">
      <button
        v-if="isNavigable(data.item)"
        class="btn btn-link p-0"
        :aria-label="data.item.name"
        @click.prevent="handleFile(data.item)"
      >
        {{ data.item.name }}
      </button>
      <template v-else>
        {{ data.item.name }}
      </template>
    </template>
    <template #cell(lastModified)="data">
      <span
        v-if="data.item.lastModified"
        :title="getDateString(data.item.lastModified, locale)"
      >
        {{ getDateString(data.item.lastModified, locale) }}
      </span>
    </template>
    <template #cell(checksum)="data">
      <span v-if="data.item.checksum" :title="data.item.checksum">
        {{ data.item.checksum }}
      </span>
    </template>
    <template #cell(size)="data">
      <span
        v-if="!data.item.isDirectory && data.item.size !== undefined"
        :title="getFileSizeAsString(data.item)"
      >
        {{ getFileSizeAsString(data.item) }}
      </span>
    </template>
    <template #cell(download)="data">
      <span>
        <button
          v-if="isDownloadable(data.item)"
          class="btn btn-link p-0"
          @click.prevent="handleDownload(data.item)"
        >
          <i class="fa fa-download" />
        </button>
      </span>
    </template>
  </b-table>
  <b-pagination
    v-if="fs.length > perPage"
    v-model="currentPage"
    :per-page="perPage"
    :total-rows="fs.length"
    align="center"
    small
  >
  </b-pagination>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { BTable, BPagination } from 'bootstrap-vue-next';
import { useI18nPrefix } from '@/composables/useI18nPrefix';
import { I18N_PREFIX } from '@/constants/ui';
import { getFileSizeAsString, isDownloadable, isNavigable } from '@/utils/file';
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

const perPage = ref(8);
const currentPage = ref(1);
const paginatedItems = computed(() => {
  const start = (currentPage.value - 1) * perPage.value;
  return fs.slice(start, start + perPage.value);
});
// TODO checksum type?
const fields = computed(() => [
  {
    key: 'name',
    sortable: false,
    label: tp('fileName'),
  },
  {
    key: 'checksum',
    sortable: false,
    label: tp('checksum'),
  },
  {
    key: 'lastModified',
    sortable: false,
    label: tp('fileDate'),
  },
  {
    key: 'size',
    sortable: false,
    label: tp('fileSize'),
  },
  {
    key: 'download',
    label: '',
    sortable: false,
  },
]);
const handleFile = (file: FileInfo): void => emit('fileClicked', file);
const handleDownload = (file: FileInfo): void =>
  emit('fileDownloadClicked', file);
const handleBackButton = (): void => emit('backButtonClicked');
</script>
