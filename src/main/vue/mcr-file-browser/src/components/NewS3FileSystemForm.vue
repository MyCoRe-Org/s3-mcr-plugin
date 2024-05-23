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
  <div class="mcr-file-system-tab">
    <form v-on:submit.prevent.stop="">
      <slot></slot>
      <div class="form-group">
        <label for="protocol">Protocol</label>
        <select id="protocol" v-model="bucketSettings.protocol" :class="{
                'is-valid' : !isValid.protocol.clean && isValid.protocol.valid,
                'is-invalid' :  !isValid.protocol.clean && !isValid.protocol.valid
        }" class="form-control">
          <option disabled value="">
            {{ i18n.chooseProtocol }}
          </option>
          <option>http</option>
          <option>https</option>
        </select>
        <b-tooltip target="protocol" placement="left" triggers="hover focus">
          {{ i18n.helpProtocol }}
        </b-tooltip>
        <div v-if="!isValid.protocol.clean && !isValid.protocol.valid" class="invalid-feedback">
          {{ i18n.validationProtocolFail }}
        </div>
      </div>
      <div class="form-group">
        <label for="endpoint">
          {{ i18n.endpoint }}
        </label>
        <input id="endpoint" v-model="bucketSettings.endpoint" :class="{
                'is-valid' : !isValid.endpoint.clean && isValid.endpoint.valid,
                'is-invalid' :  !isValid.endpoint.clean && !isValid.endpoint.valid
        }" class="form-control" type="text">
        <b-tooltip target="endpoint" placement="left" triggers="hover focus">
          {{ i18n.helpEndpoint }}
        </b-tooltip>
        <div v-if="!isValid.endpoint.clean && !isValid.endpoint.valid" class="invalid-feedback">
          {{ i18n.validationEndpointFail }}
        </div>
      </div>
      <div class="form-group">
        <label for="bucket">
          {{ i18n.bucket }}
        </label>
        <input id="bucket" v-model="bucketSettings.bucket" :class="{
                'is-valid' : !isValid.bucket.clean && isValid.bucket.valid,
                'is-invalid' :  !isValid.bucket.clean && !isValid.bucket.valid
        }" class="form-control" type="text">
        <b-tooltip target="bucket" placement="left" triggers="hover focus">
          {{ i18n.helpBucket }}
        </b-tooltip>
        <div v-if="!isValid.bucket.clean && !isValid.bucket.valid" class="invalid-feedback">
          {{ i18n.validationBucketFail }}
        </div>
      </div>
      <div class="form-group">
        <label for="accessKey">
          {{ i18n.accessKey }}
        </label>
        <input id="accessKey" v-model="bucketSettings.accessKey" :class="{
                'is-valid' : !isValid.accessKey.clean && isValid.accessKey.valid,
                'is-invalid' :  !isValid.accessKey.clean && !isValid.accessKey.valid
        }" class="form-control" type="text">
        <b-tooltip target="accessKey" placement="left" triggers="hover focus">
          {{ i18n.helpAccessKey }}
        </b-tooltip>
        <div v-if="!isValid.accessKey.clean && !isValid.accessKey.valid" class="invalid-feedback">
          {{ i18n.validationAccesKeyFail }}
        </div>
      </div>
      <div class="form-group">
        <label for="secretKey">{{ i18n.secretKey }}</label>
        <input id="secretKey" v-model="bucketSettings.secretKey" :class="{
                'is-valid' : !isValid.secretKey.clean && isValid.secretKey.valid,
                'is-invalid' :  !isValid.secretKey.clean && !isValid.secretKey.valid
        }" class="form-control" type="password">
        <b-tooltip target="secretKey" placement="left" triggers="hover focus">
          {{ i18n.helpSecretKey }}
        </b-tooltip>
        <div v-if="!isValid.secretKey.clean && !isValid.secretKey.valid" class="invalid-feedback">
          {{ i18n.validationSecretKeyFail }}
        </div>
      </div>
      <div class="form-group">
        <label for="directory">
          {{ i18n.directory }}
        </label>
        <input id="directory" v-model="bucketSettings.directory"  class="form-control" type="text">
        <b-tooltip target="directory" placement="left" triggers="hover focus">
          {{ i18n.helpDirectory }}
        </b-tooltip>
      </div>
      <div id="pta" class="form-check form-group">
        <input id="pathStyleAccess" v-model="bucketSettings.pathStyleAccess"
          class="form-check-input" type="checkbox">
        <label class="form-check-label" for="pathStyleAccess">
          {{ i18n.pathStyleAccess }}
        </label>
        <b-tooltip target="pta" placement="left" triggers="focus hover">
          {{ i18n.helpPathStyleAccess }}
        </b-tooltip>
      </div>
      <hr>
      <div id="useDownloadProxy" class="form-check form-group">
        <input id="useDownloadProxy" v-model="bucketSettings.useDownloadProxy"
          class="form-check-input" type="checkbox">
        <label class="form-check-label" for="useDownloadProxy">
          {{ i18n.useDownloadProxy }}
        </label>
        <b-tooltip target="useDownloadProxy" placement="left" triggers="focus hover">
          {{ i18n.helpUseDownloadProxy }}
        </b-tooltip>
      </div>
      <div class="form-group">
        <label for="customDownloadProxyUrl">
          {{ i18n.customDownloadProxyUrl }}
        </label>
        <input id="customDownloadProxyUrl" v-model="bucketSettings.customDownloadProxyUrl"
          class="form-control" :disabled="!bucketSettings.useDownloadProxy" type="text">
        <b-tooltip target="customDownloadProxyUrl" placement="left" triggers="hover focus">
          {{ i18n.helpCustomDownloadProxyUrl }}
        </b-tooltip>
      </div>
      <button class="btn btn-primary" type="submit" v-on:click="save()">
        {{ i18n.save }}
      </button>
    </form>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import { S3BucketSettings } from '@/model';
import I18n from '@/i18n';
import { BTooltip } from 'bootstrap-vue';

@Component({
  components: {
    BTooltip,
  },
})
export default class NewS3FileSystemForm extends Vue {
  i18n: Record<string, string> = {
    save: '',
    chooseProtocol: '',
    endpoint: '',
    bucket: '',
    accessKey: '',
    scretKey: '',
    pathStyleAccess: '',
    directory: '',
    useDownloadProxy: '',
    customDownloadProxyUrl: '',
    validationOk: '',
    validationEndpointFail: '',
    validationBucketFail: '',
    validationAccesKeyFail: '',
    validationSecretKeyFail: '',
    validationProtocolFail: '',
    helpEndpoint: '',
    helpBucket: '',
    helpAccessKey: '',
    helpSecretKey: '',
    helpProtocol: '',
    helpDirectory: '',
    helpPathStyleAccess: '',
    helpUseDownloadProxy: '',
    helpCustomDownloadProxyUrl: '',
  };

  bucketSettings: S3BucketSettings = {
    endpoint: '',
    bucket: '',
    protocol: '',
    secretKey: '',
    accessKey: '',
    pathStyleAccess: true,
    directory: undefined,
    useDownloadProxy: false,
    customDownloadProxyUrl: undefined,
  }

  isValid = {
    endpoint: { clean: true, valid: false },
    bucket: { clean: true, valid: false },
    protocol: { clean: true, valid: false },
    secretKey: { clean: true, valid: false },
    accessKey: { clean: true, valid: false },
  }

  async created(): Promise<void> {
    await I18n.loadToObject(this.i18n);
  }

  @Watch('bucketSettings.useDownloadProxy')
  useDownloadProxyChanged(): void {
    this.bucketSettings.customDownloadProxyUrl = undefined;
  }

  save(): void {
    this.isValid.endpoint.clean = false;
    this.isValid.bucket.clean = false;
    this.isValid.protocol.clean = false;
    this.isValid.secretKey.clean = false;
    this.isValid.accessKey.clean = false;

    this.isValid.endpoint.valid = this.bucketSettings.endpoint.trim().length > 0;
    this.isValid.bucket.valid = this.bucketSettings.bucket.trim().length > 0;
    this.isValid.protocol.valid = this.bucketSettings.protocol.trim().length > 0
      && (this.bucketSettings.protocol === 'http' || this.bucketSettings.protocol === 'https');
    this.isValid.secretKey.valid = this.bucketSettings.secretKey.trim().length > 0;
    this.isValid.accessKey.valid = this.bucketSettings.accessKey.trim().length > 0;

    if (this.isValid.endpoint.valid
        && this.isValid.bucket.valid
        && this.isValid.protocol.valid
        && this.isValid.secretKey.valid
        && this.isValid.accessKey.valid) {
      this.$emit('saveBucket', this.bucketSettings);
    }
  }

  resetBucketSettings(): void {
    this.bucketSettings = {
      endpoint: '',
      bucket: '',
      protocol: 'https',
      secretKey: '',
      accessKey: '',
      pathStyleAccess: true,
      directory: undefined,
      useDownloadProxy: false,
      customDownloadProxyUrl: undefined,
    };
  }
}
</script>
<style scoped>
.clickable {
  cursor: pointer;
}

</style>
<style>
.b-tooltip{
  opacity: 1 !important;
}
</style>
