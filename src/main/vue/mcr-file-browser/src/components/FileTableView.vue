<!--
  - This file is part of ***  M y C o R e  ***
  - See http://www.mycore.de/ for details.
  -
  - MyCoRe is free software: you can redistribute it and/or modify
  - it under the terms of the GNU General Public License as published by
  - the Free Software Foundation, either version 3 of the License, or
  - (at your option) any later version.
  -
  - MyCoRe is distributed in the hope that it will be useful,
  - but WITHOUT ANY WARRANTY; without even the implied warranty of
  - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  - GNU General Public License for more details.
  -
  - You should have received a copy of the GNU General Public License
  - along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
-->

<template>
  <div class="mcr-file-table-view">
    <b-table id="file-table" :current-page="currentPage" :fields="fields"
        :sort-compare-options="{ numeric: true }" :items="fs" :per-page="perPage" hover striped>
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
      <template #top-row="" v-if="!isRoot">
        <td></td>
        <td colspan="4"><a href="#" v-on:click.prevent="backButtonClicked">..</a></td>
      </template>
      <template #cell(download)="data">
        <span>
          <a v-if="!data.item.isDirectory
            && data.item.capabilities.indexOf('DOWNLOAD') !== -1" href="#"
            v-on:click.prevent="fileDownloadClicked(data.item)">
            <i class="fa fa-download" />
          </a>
        </span>
      </template>
      <template #cell(name)="data">
        <a v-if="data.item.isDirectory || data.item.flags.indexOf('ARCHIVE') !== -1" href="#"
           v-on:click.prevent="fileClicked(data.item)"> {{ data.item.name }}</a>
        <template v-else>
          {{ data.item.name }}
        </template>
      </template>
      <template #cell(lastModified)="data">
        <span v-if="data.item.lastModified != null"
          :title="new Date(data.item.lastModified).toLocaleString()">
          {{ new Date(data.item.lastModified).toLocaleString() }}
        </span>
      </template>
      <template #cell(size)="data">
        <template v-if="data.item.size > 0">
          {{ getSizeAsString(data.item.size) }}
        </template>
      </template>
    </b-table>

    <b-pagination v-model="currentPage" :fields="fields" :per-page="perPage"
      :total-rows="fs.length" aria-controls="file-table" align="center"
      v-show="Math.ceil(fs.length / perPage) > 1" />
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import I18n from '@/i18n';
import { BPagination, BTable, BTooltip } from 'bootstrap-vue';
import { FileInfo } from '@/model';

@Component({
  components: {
    BPagination,
    BTable,
    BTooltip,
  },
})
export default class FileTableView extends Vue {
  @Prop({
    required: true,
  })
  fs!: FileInfo[];

  @Prop({
    required: true,
  })
  isRoot!: boolean;

  perPage = 8;

  currentPage = 1;

  i18n: Record<string, string> = {
    fileSize: '',
    fileDate: '',
    fileName: '',
  };

  fields = [
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

  async created(): Promise<void> {
    await I18n.loadToObject(this.i18n);
  }

  fileClicked(file: FileInfo): void {
    this.$emit('fileClicked', file);
  }

  fileDownloadClicked(file: FileInfo): void {
    this.$emit('fileDownloadClicked', file);
  }

  backButtonClicked(): void {
    this.$emit('backButtonClicked');
  }

  getSizeAsString(bytes: number): string {
    // Values used in MIR
    const { radix, unit } = { radix: 1e3, unit: ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'] };
    let result = Math.abs(bytes);

    let loop = 0;
    // calculate
    while (result >= radix) {
      result /= radix;
      loop += 1;
    }
    return `${result.toFixed(1)} ${unit[loop]}`;
  }

  generateId(file: FileInfo) {
    return `info${btoa(file.name)}`;
  }
}
</script>
<style scoped>
.clickable {
  cursor: pointer;
}

[aria-sort=ascending] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0de';
  font-weight: 900;
}

[aria-sort=descending] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0dd';
  font-weight: 900;
}

[aria-sort=none] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0dc';
  font-weight: 900;
}

.tooltip {
  opacity: 1 !important;
}
</style>
