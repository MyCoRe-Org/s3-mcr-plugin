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
  <div id="app">
    <section v-if="this.currentRequestedPath">
      <BreadcrumbView :path="this.currentRequestedPath" v-on:crumbClicked="crumbClickedBreadCrumbView"/>
    </section>
    <section v-if="loading" class="text-center">
      <b-spinner label="Spinning"></b-spinner>
    </section>
    <section v-if="currentDirectory!=null && !loading">
      <FileTableView :fs="currentDirectory"
                     v-on:backButtonClicked="backButtonClickedFileTableView"
                     v-on:childClicked="childClickedFileTableView"/>
    </section>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue, Watch} from 'vue-property-decorator';
import FileTableView from "@/components/FileTableView.vue";
import {FileBase} from "@/model/FileBase";
import BreadcrumbView, {Crumb} from "@/components/BreadcrumbView.vue";
import {TokenResponse} from "@/model/TokenResponse";

@Component({
  components: {
    BreadcrumbView,
    FileTableView
  },
})
export default class FileBrowserTab extends Vue {

  @Prop() private objectId?: string;
  @Prop() private baseUrl?: string;
  @Prop() private rootId?: string;
  @Prop() private token?: TokenResponse;
  private directoryPath?: string | null = "";
  private url: string | null = null;
  private loading = false;
  private currentDirectory: FileBase | null = null;
  private currentRequestedPath: string | null = null;

  childClickedFileTableView(child: FileBase) {
    if (child.type == "DIRECTORY") {
      this.directoryPath = child.path;
      this.onDirectoryChange();
    } else if (child.type == "FILE") {
      this.openFile(child.path);
    }
  }

  openFile(path: string) {
    if (this.rootId != null) {
      //window.open(`${this.baseUrl}api/v2/fs/${this.objectId}/download/${btoa(this.rootId)}/${btoa(path)}`);
    }
  }

  crumbClickedBreadCrumbView(crumb: Crumb) {
    this.directoryPath = crumb.id.substr((this.objectId + "/" + this.rootId + "/").length);
    this.onDirectoryChange();
  }

  backButtonClickedFileTableView() {
    if (this.directoryPath != null) {
      let parts = this.directoryPath.split("/");
      parts.pop();
      parts.pop();
      this.directoryPath = parts.length > 0 ? parts.join("/") + "/" : null;
      this.onDirectoryChange();
    }
  }

  created() {
    this.onDirectoryChange();
  }

  @Watch('objectId')
  @Watch('baseUrl')
  @Watch('rootId')
  onPropChange() {
    this.onDirectoryChange();
  }

  private async onDirectoryChange() {
    if (!this.token || this.rootId == null || this.objectId == null || this.baseUrl == null) {
      return;
    }
    try {
      this.loading = true;
      let loadingURL;
      if (this.currentDirectory != null) {
        // shortcut to disable requests to directories which are already present in json.

        /*let matchingFile = this.currentDirectory.children.filter(child=>this.directoryPath==child.path && child.children.length>0);
        if(matchingFile.length==1){
          this.currentRequestedPath = null;
          this.currentDirectory = matchingFile[0];
          return;
        }*/
      }
      if (this.directoryPath != null) {
        loadingURL = `${this.baseUrl}api/v2/fs/${this.objectId}/list/${btoa(this.rootId)}/${btoa(this.directoryPath)}`;
        this.currentRequestedPath = this.objectId + "/" + this.rootId + "/" + this.directoryPath;
      } else {
        loadingURL = `${this.baseUrl}api/v2/fs/${this.objectId}/list/${btoa(this.rootId)}`;
        this.currentRequestedPath = this.objectId + "/" + this.rootId + "/";
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
