<template>
  <div class="mcr-file-table-view">
    <b-table
      id="file-table"
      :current-page="currentPage"
      :fields="fields"
      :items="fs"
      :per-page="perPage"
      hover
      striped
    >
      <template v-if="!isRoot" #top-row="">
        <td></td>
        <td colspan="4">
          <a href="#" @click.prevent="backButtonClicked">..</a>
        </td>
      </template>
      <template #cell(download)="data">
        <span>
          <a
            v-if="
              !data.item.isDirectory &&
              data.item.capabilities.includes(FileCapability.DOWNLOAD)
            "
            href="#"
            @click.prevent="fileDownloadClicked(data.item)"
          >
            <i class="fa fa-download" />
          </a>
        </span>
      </template>
      <template #cell(name)="data">
        <a
          v-if="
            data.item.isDirectory || data.item.flags.includes(FileFlag.ARCHIVE)
          "
          href="#"
          @click.prevent="fileClicked(data.item)"
        >
          {{ data.item.name }}
        </a>
        <template v-else>
          {{ data.item.name }}
        </template>
      </template>
      <template #cell(lastModified)="data">
        <span
          v-if="data.item.lastModified"
          :title="new Date(data.item.lastModified).toLocaleString()"
        >
          {{ new Date(data.item.lastModified).toLocaleString() }}
        </span>
      </template>
      <template #cell(size)="data">
        <template v-if="!data.item.isDirectory && data.item.size !== undefined">
          {{ getSizeAsString(data.item.size) }}
        </template>
      </template>
    </b-table>
    <b-pagination
      v-model="currentPage"
      :per-page="perPage"
      :total-rows="fs.length"
      align="center"
    >
    </b-pagination>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, reactive } from 'vue';
import { BTable, BPagination } from 'bootstrap-vue-next';
import I18n from '@/i18n';
import { FileCapability, FileFlag, FileInfo } from '@/types';

interface Props {
  fs: FileInfo[];
  isRoot: boolean;
}
defineProps<Props>();

const perPage = ref(8);
const currentPage = ref(1);

const i18n = reactive({
  fileSize: '',
  fileDate: '',
  fileName: '',
});

const emit = defineEmits([
  'fileClicked',
  'fileDownloadClicked',
  'backButtonClicked',
]);

const fields = computed(() => [
  {
    key: 'download',
    label: '',
    sortable: false,
  },
  {
    key: 'name',
    sortable: true,
    label: i18n.fileName,
  },
  {
    key: 'lastModified',
    sortable: true,
    label: i18n.fileDate,
  },
  {
    key: 'size',
    sortable: true,
    label: i18n.fileSize,
  },
]);

onMounted(async () => {
  await I18n.loadToObject(i18n);
});

const fileClicked = (file: FileInfo): void => {
  emit('fileClicked', file);
};

const fileDownloadClicked = (file: FileInfo): void => {
  emit('fileDownloadClicked', file);
};

const backButtonClicked = (): void => {
  emit('backButtonClicked');
};

const getSizeAsString = (bytes: number): string => {
  // Values used in MIR
  const { radix, unit } = {
    radix: 1e3,
    unit: ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
  };
  let result = Math.abs(bytes);

  let loop = 0;
  // calculate
  while (result >= radix) {
    result /= radix;
    loop += 1;
  }
  return `${result.toFixed(1)} ${unit[loop]}`;
};
</script>
