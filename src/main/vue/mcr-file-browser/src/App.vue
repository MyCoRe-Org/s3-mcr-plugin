<template>
  <div id="app">
    <div>
      <b-tabs justified content-class="mt-3">
        <b-tab v-for="root in rootInfos" :key="root.id" :title="root.id" active lazy>
          <file-browser-tab :root-id="root.id" :base-url="baseUrl" :object-id="objectId"/>
        </b-tab>

        <template #tabs-end>
          <b-nav-item v-if="canWrite === 'true'" role="presentation" v-on:click.prevent="" v-b-modal="'modal-1'"
                      href="#"><b>+</b></b-nav-item>
        </template>
      </b-tabs>
      <b-modal id="modal-1" :title="i18n.addBucket" hide-footer>
        <b-alert v-model="showAddBucketError" variant="danger" dismissible>
          {{ i18n.addBucketError }}
        </b-alert>
        <new-file-system-form v-on:saveBucket="saveBucket"/>
      </b-modal>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import FileBrowserTab from "@/components/FileBrowserTab.vue";
import {RootInfo} from "@/model/FileBase";
import NewFileSystemForm from "@/components/NewFileSystemForm.vue";
import {S3BucketSettings} from "@/model/S3BucketSettings";
import {TokenResponse} from "@/model/TokenResponse";
import {BNavItem, BSpinner, TabsPlugin, ModalPlugin, AlertPlugin} from 'bootstrap-vue'
import {I18n} from "@/i18n";

Vue.component('b-spinner', BSpinner);
Vue.component('b-nav-item', BNavItem);
Vue.use(ModalPlugin);
Vue.use(TabsPlugin);
Vue.use(AlertPlugin);

@Component({
  components: {
    NewFileSystemForm,
    FileBrowserTab,
  },
})
export default class FileBrowser extends Vue {

  @Prop() private baseUrl?: string; // = "http://paschty.de:8080/mir/";
  @Prop() private objectId?: string; // = "odb_mods_00000006";
  @Prop() private canWrite?: string; // = "true";

  private showAddBucketError = false;

  private rootInfos: RootInfo[] = [];


  private i18n: Record<string, string> = {
    addBucketError: "",
    addBucket: ""
  };

  async created() {
    await I18n.loadToObject(this.i18n);
    await this.loadContents();
  }


  private async saveBucket(bucketSettings: S3BucketSettings) {
    this.showAddBucketError = false;
    try {
      let tokenResp = await fetch(`${this.baseUrl}rsc/jwt`, {
        credentials: "include"
      });
      let jwtObj: TokenResponse = await tokenResp.json();
      let resp = await fetch(`${this.baseUrl}api/v2/fs/${this.objectId}/add/S3/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `${jwtObj.token_type} ${jwtObj.access_token}`,
        },
        body: JSON.stringify(bucketSettings)
      });

      if (resp.ok) {
        this.loadContents();
      }
    } catch (e) {
      this.showAddBucketError = true;
      return;
    }
    this.$bvModal.hide('modal-1');

  }


  private async loadContents() {
    const reqUrl = `${this.baseUrl}api/v2/fs/${this.objectId}/info/`;
    let response = await fetch(reqUrl);
    const newInfos: RootInfo[] = await response.json();

    while (this.rootInfos.length > 0) {
      this.rootInfos.pop();
    }

    newInfos.forEach(info => this.rootInfos.push(info));
  }

}
</script>

<style>

</style>
