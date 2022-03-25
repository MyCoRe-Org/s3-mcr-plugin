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
    <div v-if="this.currentRequestedPath">
      <BreadcrumbView :crumbs="this.crumbs" v-on:crumbClicked="crumbClickedBreadCrumbView"/>
    </div>
    <div v-if="loading" class="text-center">
      <b-spinner label="Spinning"></b-spinner>
    </div>
    <div v-if="currentDirectory!=null && !loading">
      <FileTableView :fs="currentDirectory"
                     v-on:backButtonClicked="backButtonClickedFileTableView"
                     v-on:childClicked="childClickedFileTableView"/>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue, Watch} from 'vue-property-decorator';
import FileTableView from "@/components/FileTableView.vue";
import {FileBase} from "@/model/FileBase";
import BreadcrumbView, {Crumb} from "@/components/BreadcrumbView.vue";
import {TokenResponse} from "@/model/TokenResponse";
import {BSpinner} from "bootstrap-vue";

@Component({
  components: {
    BreadcrumbView,
    FileTableView,
    BSpinner
  },
})
export default class FileBrowserDerivate extends Vue {

  @Prop() private objectId?: string;
  @Prop() private baseUrl?: string;
  @Prop() private derivateId?: string;
  @Prop() private token?: TokenResponse;
  @Prop() private title?: string;

  private breadcrumbChange() {
    while (this.crumbs.length > 0) {
      this.crumbs.pop();
    }

    if (this.currentRequestedPath == null) {
      return;
    }

    let until = [];
    for (const crumbLabel of this.currentRequestedPath.split("/")) {
      if (crumbLabel !== "") {
        until.push(crumbLabel);
        this.crumbs.push({
          id: until.join("/") + "/",
          label: until.length == 1 && this.title != undefined ? this.title : crumbLabel
        })
      }
    }
  }

  private directoryPath?: string | null = "";
  private url: string | null = null;
  private loading = false;
  private currentDirectory: FileBase | null = null;
  private currentRequestedPath: string | null = null;
  private crumbs: Crumb[] = [];

  childClickedFileTableView(child: FileBase): void {
    if (child.type == "DIRECTORY" || child.type == "BROWSABLE_FILE") {
      this.directoryPath = child.path;
      this.onDirectoryChange();
    } else if (child.type == "FILE") {
      if (this.derivateId != null) {
        //window.open(`${this.baseUrl}api/v2/fs/${this.objectId}/download/${btoa(this.rootId)}/${btoa(path)}`);
      }
    }
  }

  crumbClickedBreadCrumbView(crumb: Crumb): void {
    this.directoryPath = crumb.id.substr((this.derivateId + "/").length);
    this.onDirectoryChange();
  }

  backButtonClickedFileTableView(): void {
    if (this.directoryPath != null) {
      let parts = this.directoryPath.split("/");
      parts.pop();
      parts.pop();
      this.directoryPath = parts.length > 0 ? parts.join("/") + "/" : null;
      this.onDirectoryChange();
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

  @Watch("derivateId")
  onDerivateChange(): void {
    this.currentDirectory = null;
    this.directoryPath = "";
    this.onDirectoryChange();
  }

  private async onDirectoryChange() {
    if (!this.token || this.derivateId == null || this.objectId == null || this.baseUrl == null) {
      return;
    }
    try {
      this.loading = true;
      let loadingURL;
      if (this.currentDirectory != null) {
        // shortcut to disable requests to directories which are already present in json.

        let matchingFile = this.currentDirectory.children
            .filter(child => this.directoryPath == child.path && child.children != null && child.children.length > 0);
        if (matchingFile.length == 1) {
          this.currentRequestedPath = this.derivateId + "/" + this.directoryPath;
          this.breadcrumbChange();
          this.currentDirectory = matchingFile[0];
          this.loading = false;
          return;
        }
      }
      if (this.directoryPath != null) {
        loadingURL = `${this.baseUrl}api/v2/fs/${this.objectId}/list/${btoa(this.derivateId)}/${btoa(this.directoryPath)}`;
        this.currentRequestedPath = this.derivateId + "/" + this.directoryPath;
        this.breadcrumbChange();
      } else {
        loadingURL = `${this.baseUrl}api/v2/fs/${this.objectId}/list/${btoa(this.derivateId)}`;
        this.currentRequestedPath = this.derivateId + "/";
        this.breadcrumbChange();
      }


      this.url = loadingURL;
      let response = await fetch(this.url, {
        headers: {
          "Authorization": `${this.token.token_type} ${this.token.access_token}`,
        },
      });
      if (this.url == loadingURL) {
        this.currentDirectory = await response.json();
      }
    } finally {
      this.loading = false;
    }
  }

}
</script>

<style>

</style>
