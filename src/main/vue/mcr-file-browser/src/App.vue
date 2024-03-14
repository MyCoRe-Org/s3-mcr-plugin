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
    <!-- Loading bar -->
    <div class="mb-3" v-if="!loaded">
      <b-spinner label="Spinning"></b-spinner>
    </div>
    <!-- Normal viewer-->
    <div class="mb-3" v-else-if="derivateInfos.length > 0">
      <h3> {{ i18n.headline }}</h3>
      <div class="card">
        <div class="card-header">
          <div class="row">
            <div class="col">
              <select v-if="derivateInfos.length > 1" class="form-control" v-model="current">
                <option v-for="info in derivateInfos" :value="info" :key="info.id">
                  {{ getTitle(info) }}
                </option>
              </select>
              <span v-else class="title">{{getTitle(derivateInfos[0])}}</span>
            </div>
            <div class="col-auto options" v-if="current?.write || current?.delete">
              <b-dropdown variant="link"
                          toggle-class="text-decoration-none">
                <template #button-content>
                  <span class="fas fa-cog"></span><span> Aktionen</span>
                </template>
                <b-dropdown-item v-b-modal.modal-1 v-if="canCreate">
                  {{ i18n.addBucket }}
                </b-dropdown-item>
                <b-dropdown-item v-b-modal.modal-3 v-if="current.write">
                  {{ i18n.displayInfo }}
                </b-dropdown-item>
                <b-dropdown-item v-if="current.write"
                  :href="baseUrl + 'editor/editor-derivate.xed?derivateid='+current.id">
                  {{ i18n.manageDerivate }}
                </b-dropdown-item>
                <b-dropdown-item v-b-modal.modal-2 v-if="current.delete">
                  {{ i18n.deleteBucket }}
                </b-dropdown-item>
              </b-dropdown>
            </div>
          </div>
        </div>
        <div class="card-body p-0">
          <file-browser-derivate v-if="current !== null" :base-url="baseUrl"
                                  :object-id="objectId"
                                  :derivate-id="current.id"
                                  :token="token"
                                  :title="getTitle(current)"
          />
        </div>
      </div>
    </div>
    <!-- creation dialog, without viewer -->
    <div v-else-if="derivateInfos.length == 0 && canCreate">
      <h3>{{ i18n.headline }}</h3>
      <div class="jumbotron text-center">
        <a v-b-modal.modal-1  v-if="canCreate">{{ i18n.addBucket }}</a>
      </div>
    </div>

    <b-modal v-if="current !== null" id="modal-3" :title="i18n.displayInfo"
      hide-footer hide-backdrop>
      <dl v-for="(value,name) in current.metadata" :key="name">
        <dt>{{ i18n[name] }}</dt>
        <dd>{{ value }}</dd>
      </dl>
    </b-modal>

    <b-modal id="modal-1" :title="i18n.addBucket" hide-footer hide-backdrop>
      <new-s3-file-system-form v-on:saveBucket="saveBucket">
        <b-alert v-model="showAddBucketError" dismissible variant="danger">
          {{ i18n.addBucketError }}
          {{ addBucketErrorMessage }}
        </b-alert>
      </new-s3-file-system-form>
    </b-modal>

    <b-modal id="modal-2" :title="i18n.deleteBucket" hide-footer hide-backdrop>
      <b-alert v-model="showDeleteBucketError" dismissible variant="danger">
        {{ i18n.deleteBucketError }}
        {{ deleteErrorMessage }}
      </b-alert>
      <p>{{ i18n.deleteBucketModal }}</p>
      <b-button v-if="current" variant="danger" v-on:click="removeBucket(current)">
        {{ i18n.deleteBucket }}
      </b-button>
    </b-modal>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator';
import FileBrowserDerivate from '@/components/FileBrowserDerivate.vue';
import NewS3FileSystemForm from '@/components/NewS3FileSystemForm.vue';
import {
  BNavItem,
  BSpinner,
  ModalPlugin,
  AlertPlugin,
  BDropdown,
  BDropdownItem,
  BButton,
} from 'bootstrap-vue';
import I18n from '@/i18n';
import {
  S3BucketSettings,
  Token,
  TokenResponse,
  DerivateInformations,
  DerivateInfo,
} from '@/model';
import {
  getInfo,
  saveS3Bucket,
  removeStore,
  getJWT,
} from '@/api/Client';

Vue.use(ModalPlugin);
Vue.use(AlertPlugin);

@Component({
  components: {
    NewS3FileSystemForm,
    FileBrowserDerivate,
    BSpinner,
    BNavItem,
    BDropdown,
    BDropdownItem,
    BButton,
  },
})
export default class FileBrowser extends Vue {
  @Prop({
    default: 'http://localhost:8291/mir/',
  })
  baseUrl!: string;

  @Prop({
    default: 'mir_mods_00000028',
  })
  objectId!: string;

  canCreate?: boolean = false;

  current: DerivateInfo | null = null;

  showAddBucketError = false;

  showDeleteBucketError = false;

  deleteErrorMessage = '';

  addBucketErrorMessage = '';

  derivateInfos: DerivateInfo[] = [];

  token?: Token;

  loaded = false;

  i18n: Record<string, string> = {
    addBucketError: '',
    addBucket: '',
    deleteBucket: '',
    deleteBucketModal: '',
    headline: '',
    displayInfo: '',
    endpoint: '',
    bucket: '',
    accessKey: '',
    scretKey: '',
    pathStyleAccess: '',
    directory: '',
    protocol: '',
  };

  async loadContents(): Promise<void> {
    this.loaded = false;
    const response = await getInfo(this.baseUrl, this.objectId, this.token);
    if (!response.ok) {
      console.error('Error while loading contents');
      return;
    }
    const newInfos: DerivateInformations = await response.json();

    while (this.derivateInfos.length > 0) {
      this.derivateInfos.pop();
    }

    this.canCreate = newInfos.create;

    newInfos.derivates.filter((info) => info.view).forEach((info) => {
      this.derivateInfos.push(info);
    });

    if (this.derivateInfos.length > 0) {
      const [derivateInfo] = this.derivateInfos;
      this.current = derivateInfo;
    }
    this.loaded = true;
  }

  async loadToken(): Promise<void> {
    const resp = await getJWT(this.baseUrl);
    if (resp.ok) {
      const tokenResponse: TokenResponse = await resp.json();
      if (tokenResponse.login_success) {
        this.token = {
          tokenType: tokenResponse.token_type,
          accessToken: tokenResponse.access_token,
        };
      } else {
        console.error('Error while fetching JWT');
      }
    } else {
      console.error('Error while fetching JWT');
    }
  }

  async created(): Promise<void> {
    await I18n.loadToObject(this.i18n);
    if (process.env.NODE_ENV === 'production') {
      await this.loadToken();
    } else {
      this.token = {
        tokenType: 'Basic',
        accessToken: 'YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=',
      };
    }
    await this.loadContents();
  }

  getTitle(info: DerivateInfo): string {
    if (info.titles.length > 0) {
      return info.titles[0].text;
    }
    return info.id;
  }

  async removeBucket(info: DerivateInfo): Promise<void> {
    this.showDeleteBucketError = false;
    try {
      const resp = await removeStore(this.baseUrl, this.objectId, info.id, this.token);
      if (resp.ok) {
        this.loadContents();
        this.$bvModal.hide('modal-2');
      } else {
        this.showDeleteBucketError = true;
        this.deleteErrorMessage = await resp.text();
      }
    } catch (e) {
      this.showDeleteBucketError = true;
      this.deleteErrorMessage = 'Error while removing bucket';
    }
  }

  async saveBucket(bucketSettings: S3BucketSettings): Promise<void> {
    this.showAddBucketError = false;
    try {
      const resp = await saveS3Bucket(this.baseUrl, this.objectId, bucketSettings, this.token);
      if (resp.ok) {
        this.$bvModal.hide('modal-1');
        this.loadContents();
      } else {
        this.showAddBucketError = true;
        this.addBucketErrorMessage = await resp.text();
      }
    } catch (e) {
      this.showAddBucketError = true;
      this.addBucketErrorMessage = 'Error while saving bucket';
    }
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

.card-header span.title {
  display: flex;
  align-items: center;
  justify-content: left;
  height: 100%;
}
</style>
