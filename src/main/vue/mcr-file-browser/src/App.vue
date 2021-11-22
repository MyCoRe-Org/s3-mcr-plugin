<template>
  <div id="app">
    <div>
      <b-tabs content-class="mt-3" justified>
        <b-tab v-for="root in rootInfos" :key="root.id" :title="root.id" active lazy>
          <file-browser-tab :base-url="baseUrl" :object-id="objectId" :root-id="root.id" :token="token"/>
        </b-tab>

        <template #tabs-end>
          <b-nav-item v-if="canWrite === 'true'" v-b-modal="'modal-1'" href="#" role="presentation"
                      v-on:click.prevent=""><b>+</b></b-nav-item>
        </template>
      </b-tabs>
      <b-modal id="modal-1" :title="i18n.addBucket" hide-footer>
        <new-file-system-form v-on:saveBucket="saveBucket">
          <b-alert v-model="showAddBucketError" dismissible variant="danger">
            {{ i18n.addBucketError }}
          </b-alert>
        </new-file-system-form>
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
  private addBucketSuccess = false;

  private rootInfos: RootInfo[] = [];
  private token?: TokenResponse;

  private i18n: Record<string, string> = {
    addBucketError: "",
    addBucket: ""
  };

  async created() {
    await I18n.loadToObject(this.i18n);
    await this.loadToken();
    await this.loadContents();
  }


  private async saveBucket(bucketSettings: S3BucketSettings) {
    if(this.token == undefined){
      await this.loadToken();
      this.saveBucket(bucketSettings);
      return;
    }

    this.showAddBucketError = false;
    try {
      let resp = await fetch(`${this.baseUrl}api/v2/fs/${this.objectId}/add/S3/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `${this.token.token_type} ${this.token.access_token}`,
        },
        body: JSON.stringify(bucketSettings)
      });

      if (resp.ok) {
        this.$bvModal.hide('modal-1');
        this.loadContents();
      } else {
        this.showAddBucketError = true;
        return;
      }
    } catch (e) {
      this.showAddBucketError = true;
      return;
    }

  }


  private async loadToken() {
    let tokenResp = await fetch(`${this.baseUrl}rsc/jwt`, {
      credentials: "include"
    });
    this.token = await tokenResp.json();
  }

  private async loadContents() {
    if(this.token == undefined){
      await this.loadToken();
      await this.loadContents();
      return;
    }
    const reqUrl = `${this.baseUrl}api/v2/fs/${this.objectId}/info/`;
    let response = await fetch(reqUrl, {
      headers: {
        "Content-Type": "application/json",
        "Authorization": `${this.token.token_type} ${this.token.access_token}`,
      },
    });
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
