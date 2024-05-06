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
  <div>
    <div>
      <BreadcrumbView :crumbs="crumbs" v-on:crumbClicked="crumbClickedBreadCrumbView"/>
    </div>
    <div v-if="loading" class="text-center">
      <b-spinner label="Spinning"></b-spinner>
    </div>
    <FileTableView v-else :fs="files" :isRoot="crumbs.length === 1"
      v-on:backButtonClicked="backButtonClickedFileTableView"
      v-on:fileClicked="fileClickedFileTableView"
      v-on:fileDownloadClicked="fileDownloadClickedFileTableView"
    />
  </div>
</template>

<script lang="ts">
import {
  Component,
  Prop,
  Vue,
  Watch,
} from 'vue-property-decorator';
import FileTableView from '@/components/FileTableView.vue';
import BreadcrumbView, { Crumb } from '@/components/BreadcrumbView.vue';
import {
  FileInfo,
  FileCapability,
  FileFlag,
  Token,
} from '@/model';
import { BSpinner } from 'bootstrap-vue';
import { getDownloadToken, listDirectory } from '@/api/Client';

@Component({
  components: {
    BreadcrumbView,
    FileTableView,
    BSpinner,
  },
})
export default class FileBrowserDerivate extends Vue {
  @Prop({
    required: true,
  })
  objectId!: string;

  @Prop({
    required: true,
  })
  baseUrl!: string;

  @Prop({
    required: true,
  })
  derivateId!: string;

  @Prop({
    required: true,
  })
  title!: string;

  @Prop({
    required: false,
  })
  token?: Token;

  directoryPath = '';

  files: FileInfo[] = [];

  crumbs: Crumb[] = [];

  loading = false;

  breadcrumbChange(path: string): void {
    this.crumbs = [];
    const until = [];
    const crumbLabels = path.split('/');
    for (let i = 0; i < crumbLabels.length; i += 1) {
      if (crumbLabels[i] !== '') {
        until.push(crumbLabels[i]);
        this.crumbs.push({
          id: `${until.join('/')}`,
          label: until.length === 1 && this.title !== undefined ? this.title : crumbLabels[i],
        });
      }
    }
  }

  async onDirectoryChange(): Promise<void> {
    this.loading = true;
    this.files = [];
    try {
      if (this.directoryPath !== '') {
        this.breadcrumbChange(`${this.derivateId}/${this.directoryPath}`);
      } else {
        this.breadcrumbChange(this.derivateId);
      }
      const resp = await listDirectory(
        this.baseUrl,
        this.objectId,
        this.derivateId,
        this.directoryPath,
        this.token,
      );
      if (!resp.ok) {
        console.error('Error while loading directory');
        return;
      }
      this.files = await resp.json();
    } finally {
      this.loading = false;
    }
  }

  created(): void {
    this.onDirectoryChange();
  }

  @Watch('objectId')
  @Watch('baseUrl')
  @Watch('rootId')
  onPropChange(): void {
    this.onDirectoryChange();
  }

  @Watch('derivateId')
  onDerivateChange(): void {
    this.files = [];
    this.directoryPath = '';
    this.onDirectoryChange();
  }

  crumbClickedBreadCrumbView(crumb: Crumb): void {
    this.directoryPath = crumb.id.substring((`${this.derivateId}/`).length);
    this.onDirectoryChange();
  }

  backButtonClickedFileTableView(): void {
    if (this.directoryPath != null) {
      const parts = this.directoryPath.split('/');
      parts.pop();
      this.directoryPath = parts.length > 0 ? parts.join('/') : '';
      this.onDirectoryChange();
    }
  }

  async fileDownloadClickedFileTableView(file: FileInfo): Promise<void> {
    if (file.capabilities.indexOf(FileCapability.DOWNLOAD) !== -1) {
      const resp = await getDownloadToken(
        this.baseUrl,
        this.objectId,
        this.derivateId,
        this.getPath(file),
        this.token,
      );
      if (!resp.ok) {
        console.error('Error while downloading file');
        return;
      }
      const downloadToken = await resp.text();
      window.open(`${this.baseUrl}api/v2/es/download/${downloadToken}`);
    }
  }

  getPath(file: FileInfo): string {
    return (file.parentPath !== '') ? `${file.parentPath}/${file.name}` : file.name;
  }

  fileClickedFileTableView(file: FileInfo): void {
    if (file.isDirectory || file.flags.indexOf(FileFlag.ARCHIVE) !== -1) {
      this.directoryPath = this.getPath(file);
      this.onDirectoryChange();
    }
  }
}
</script>
