<template>
  <div class="mcr-file-table-view">
    <b-table
      id="file-table"
      :current-page="currentPage"
      :fields="fields"
      :sort-compare-options="{ numeric: true }"
      :items="fs"
      :per-page="perPage"
      hover
      striped
    >
      <template #head(download)="">
        <span> </span>
      </template>
      <template #head(name)="">
        <span class="text-info clickable">{{ i18n.fileName }} </span>
      </template>
      <template #head(lastModified)="">
        <span class="text-info clickable">{{ i18n.fileDate }} </span>
      </template>
      <template #head(size)="">
        <span class="text-info clickable">{{ i18n.fileSize }} </span>
      </template>
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
              data.item.capabilities.indexOf('DOWNLOAD') !== -1
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
            data.item.isDirectory || data.item.flags.indexOf('ARCHIVE') !== -1
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
          v-if="data.item.lastModified != null"
          :title="new Date(data.item.lastModified).toLocaleString()"
        >
          {{ new Date(data.item.lastModified).toLocaleString() }}
        </span>
      </template>
      <template #cell(size)="data">
        <template v-if="data.item.size > 0">
          {{ getSizeAsString(data.item.size) }}
        </template>
      </template>
    </b-table>

    <SimplePagination
      v-model="currentPage"
      :per-page="perPage"
      :total-rows="fs.length"
    >
    </SimplePagination>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { FileInfo } from '@/types';
import { SimplePagination } from '@mycore-org/vue-components';

interface Props {
  fs: FileInfo[];
  isRoot: boolean;
}
defineProps<Props>();

const perPage = ref(8);
const currentPage = ref(1);

const i18n: Record<string, string> = {
  fileSize: 'fileSize',
  fileDate: 'fileDate',
  fileName: 'fileName',
};

const emit = defineEmits([
  'fileClicked',
  'fileDownloadClicked',
  'backButtonClicked',
]);

const fields = [
  {
    key: 'download',
    sortable: false,
  },
  {
    key: 'name',
    sortable: true,
  },
  {
    key: 'lastModified',
    sortable: true,
  },
  {
    key: 'size',
    sortable: true,
  },
];

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
<style scoped>
.clickable {
  cursor: pointer;
}

[aria-sort='ascending'] span.text-info:after {
  font-family: 'Font Awesome 5 Free';
  content: '\f0de';
  font-weight: 900;
}

[aria-sort='descending'] span.text-info:after {
  font-family: 'Font Awesome 5 Free';
  content: '\f0dd';
  font-weight: 900;
}

[aria-sort='none'] span.text-info:after {
  font-family: 'Font Awesome 5 Free';
  content: '\f0dc';
  font-weight: 900;
}

.tooltip {
  opacity: 1 !important;
}
</style>
