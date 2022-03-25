<template>
  <div id="app">
    <div>
      <!-- Loading bar -->
      <div class="mb-3" v-if="!loaded">
        <b-spinner label="Spinning"></b-spinner>
      </div>
      <!-- Normal viewer-->
      <div class="mb-3" v-else-if="derivateInfo.length>0">
        <h3> {{ i18n.headline }}</h3>
        <div class="card">
          <div class="card-header">
            <div class="row">
              <div class="col">
                <select class="form-control" v-model="current" v-on:change="derivateChanged()">
                  <option v-for="derivateInfo in derivateInfo" :value="derivateInfo" :key="derivateInfo">
                    {{ getTitle(derivateInfo) }}
                  </option>
                </select>
              </div>
              <div class="col-auto options" v-if="canWrite || (current!=null && current.delete)">
                <b-dropdown variant="link"
                            toggle-class="text-decoration-none">
                  <template #button-content>
                    <span class="fas fa-cog"></span><span> Aktionen</span>
                  </template>
                  <b-dropdown-item v-b-modal="'modal-1'" href="#" v-if="canCreate" v-on:click.prevent="">
                    {{ i18n.addBucket }}
                  </b-dropdown-item>
                  <b-dropdown-item v-if="current.write"
                                   :href="baseUrl + 'editor/editor-derivate.xed?derivateid='+current.id">
                    {{ i18n.manageDerivate }}
                  </b-dropdown-item>
                  <b-dropdown-item v-b-modal="'modal-2'" href="#" v-if="current.delete" v-on:click.prevent="">
                    {{ i18n.deleteBucket }}
                  </b-dropdown-item>
                </b-dropdown>
              </div>
            </div>
          </div>
          <div class="card-body p-0">
            <file-browser-derivate v-if="current!==null" :base-url="baseUrl" :object-id="objectId"
                                   :derivate-id="current.id"
                                   :token="token"
                                   :title="getTitle(current)"
            />
          </div>
        </div>
      </div>
      <!-- creation dialog, without viewer -->
      <div v-else-if="derivateInfo.length==0 && canCreate">
        <h3>{{ i18n.headline }}</h3>
        <div class="jumbotron text-center">
          <a v-b-modal="'modal-1'" href="#" v-if="canCreate" v-on:click.prevent="">{{ i18n.addBucket }}</a>
        </div>
      </div>
      <b-modal id="modal-1" :title="i18n.addBucket" hide-footer hide-backdrop>
        <new-file-system-form v-on:saveBucket="saveBucket">
          <b-alert v-model="showAddBucketError" dismissible variant="danger">
            {{ i18n.addBucketError }}
            {{ addBucketErrorMessage }}
          </b-alert>
        </new-file-system-form>
      </b-modal>
      <b-modal id="modal-2" :title="i18n.deleteBucket" hide-footer hide-backdrop>
        <b-alert v-model="showDeleteBucketError" dismissible variant="danger">
          {{ i18n.deleteBucketError }}
          {{ deleteErrorMessage }}
        </b-alert>
        <p>{{ i18n.deleteBucketModal }}</p>
        <b-button variant="danger" v-on:click="removeBucket(current)">{{ i18n.deleteBucket }}</b-button>
      </b-modal>
    </div>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import FileBrowserDerivate from "@/components/FileBrowserDerivate.vue";
import NewFileSystemForm from "@/components/NewFileSystemForm.vue";
import {S3BucketSettings} from "@/model/S3BucketSettings";
import {TokenResponse} from "@/model/TokenResponse";
import {
  BNavItem,
  BSpinner,
  TabsPlugin,
  ModalPlugin,
  AlertPlugin,
  BDropdown,
  BDropdownItem,
  BButton
} from 'bootstrap-vue'
import {I18n} from "@/i18n";
import {DerivateInformations} from "@/model/DerivateInformations";
import {DerivateInfo} from "@/model/DerivateInfo";

Vue.component('b-spinner', BSpinner);
Vue.component('b-nav-item', BNavItem);
Vue.component('b-dropdown', BDropdown);
Vue.component('b-dropdown-item', BDropdownItem);
Vue.component('b-button', BButton);
Vue.use(ModalPlugin);
Vue.use(TabsPlugin);
Vue.use(AlertPlugin);

@Component({
  components: {
    NewFileSystemForm,
    FileBrowserDerivate,
  },
})
export default class FileBrowser extends Vue {

  @Prop() private baseUrl?: string; // = "http://paschty.de:8080/mir/";
  @Prop() private objectId?: string; // = "odb_mods_00000006";

  private canCreate?: boolean = false;
  private current: DerivateInfo | null = null;
  private showAddBucketError = false;
  private showDeleteBucketError = false;
  private addBucketSuccess = false;
  private deleteErrorMessage = "";
  private addBucketErrorMessage = "";

  private derivateInfo: DerivateInfo[] = [];
  private token?: TokenResponse;
  private loaded = false;

  private i18n: Record<string, string> = {
    addBucketError: "",
    addBucket: "",
    deleteBucket: "",
    deleteBucketModal: "",
    headline: ""
  };

  async created() {
    await I18n.loadToObject(this.i18n);
    await this.loadToken();
    await this.loadContents();
  }

  derivateChanged() {
    console.log(this.current)
  }

  private getTitle(info: DerivateInfo) {
    if (info.titles.length > 0) {
      return info.titles[0].text;
    }
    return info.id;
  }

  private async removeBucket(info: DerivateInfo) {
    this.showDeleteBucketError = false;
    const token = await this.loadToken();
    try {
      let resp = await fetch(`${this.baseUrl}api/v2/objects/${this.objectId}/derivates/${info.id}`, {
        method: "DELETE",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `${token.token_type} ${token.access_token}`,
        }
      });
      if (resp.ok) {
        this.loadContents();
        this.$bvModal.hide('modal-2');
      } else {
        this.showDeleteBucketError = true;
        this.deleteErrorMessage = await resp.text();
      }
    } catch (e) {
      this.showDeleteBucketError = true;
      this.deleteErrorMessage = e;
    }
  }

  private async saveBucket(bucketSettings: S3BucketSettings) {
    const token = await this.loadToken();

    this.showAddBucketError = false;
    try {
      let resp = await fetch(`${this.baseUrl}api/v2/fs/${this.objectId}/add/S3/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `${token.token_type} ${token.access_token}`,
        },
        body: JSON.stringify(bucketSettings)
      });

      if (resp.ok) {
        this.$bvModal.hide('modal-1');
        this.loadContents();
      } else {
        this.showAddBucketError = true;
        this.addBucketErrorMessage = await resp.text();
      }
    } catch (e) {
      this.showAddBucketError = true;
      this.addBucketErrorMessage = e;
    }
  }


  private async loadToken(): Promise<TokenResponse> {
    if (this.token !== undefined) {
      return this.token;
    }
    let tokenResp = await fetch(`${this.baseUrl}rsc/jwt`, {
      credentials: "include"
    });
    return this.token = await tokenResp.json();
  }

  private async loadContents() {
    this.loaded = false;
    if (this.token == undefined) {
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
    const newInfos: DerivateInformations = await response.json();

    while (this.derivateInfo.length > 0) {
      this.derivateInfo.pop();
    }

    this.canCreate = newInfos.create;

    newInfos.derivates
        .filter(info => info.view)
        .forEach(info => {
          this.derivateInfo.push(info);
        });

    if (this.derivateInfo.length > 0) {
      this.current = this.derivateInfo[0];
    }
    this.loaded=true;
  }

}
</script>

<style scoped>
.options button * {
  color: #2c3e50;
  font-size: 12px;
}

.jumbotron {
  padding: 1.25rem;
  border: 1px solid rgba(0,0,0,0.125);
}
</style>
