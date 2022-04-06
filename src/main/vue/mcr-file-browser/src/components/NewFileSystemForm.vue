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
          <option disabled value="">{{ i18n.chooseProtocol }}</option>
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
        <label for="endpoint">{{ i18n.endpoint }}</label>
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
        <label for="bucket">{{ i18n.bucket }}</label>
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
        <label for="accessKey">{{ i18n.accessKey }}</label>
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
        <label for="directory">{{ i18n.directory }}</label>
        <input id="directory" v-model="bucketSettings.directory"  class="form-control" type="text">
        <b-tooltip target="directory" placement="left" triggers="hover focus">
          {{ i18n.helpDirectory }}
        </b-tooltip>
      </div>
      <div id="pta" class="form-check">
        <input id="pathStyleAccess" v-model="bucketSettings.pathStyleAccess" class="form-check-input" type="checkbox">
        <label class="form-check-label" for="pathStyleAccess">{{ i18n.pathStyleAccess }}</label>
        <b-tooltip target="pta" placement="left" triggers="focus hover">
          {{ i18n.helpPathStyleAccess }}
        </b-tooltip>
      </div>
      <button class="btn btn-primary" type="submit" v-on:click="save()">{{ i18n.save }}</button>
    </form>
  </div>
</template>

<script lang="ts">
import {Component, Vue} from 'vue-property-decorator';
import {S3BucketSettings} from "@/model/S3BucketSettings";
import {I18n} from "@/i18n";
import {BTooltip} from "bootstrap-vue";


@Component({
  components: {
    BTooltip
  }
})
export default class NewFileSystemForm extends Vue {

  private i18n: Record<string, string> = {
    save: "",
    chooseProtocol: "",
    endpoint: "",
    bucket: "",
    accessKey: "",
    scretKey: "",
    pathStyleAccess: "",
    directory: "",
    validationOk: "",
    validationEndpointFail: "",
    validationBucketFail: "",
    validationAccesKeyFail: "",
    validationSecretKeyFail: "",
    validationProtocolFail: "",
    helpEndpoint: "",
    helpBucket: "",
    helpAccessKey: "",
    helpSecretKey: "",
    helpProtocol: "",
    helpDirectory: "",
    helpPathStyleAccess: ""
  };
  private bucketSettings: S3BucketSettings = {
    endpoint: "",
    bucket: "",
    protocol: "",
    secretKey: "",
    accessKey: "",
    pathStyleAccess: true,
    directory: ""
  }
  private isValid = {
    endpoint: {clean: true, valid: false},
    bucket: {clean: true, valid: false},
    protocol: {clean: true, valid: false},
    secretKey: {clean: true, valid: false},
    accessKey: {clean: true, valid: false}
  }

  async created() {
    await I18n.loadToObject(this.i18n);
  }

  private save() {
    this.isValid.endpoint.clean = false;
    this.isValid.bucket.clean = false;
    this.isValid.protocol.clean = false;
    this.isValid.secretKey.clean = false;
    this.isValid.accessKey.clean = false;

    this.isValid.endpoint.valid = this.bucketSettings.endpoint.trim().length > 0;
    this.isValid.bucket.valid = this.bucketSettings.bucket.trim().length > 0;
    this.isValid.protocol.valid = this.bucketSettings.protocol.trim().length > 0 && ("http" === this.bucketSettings.protocol || "https" === this.bucketSettings.protocol);
    this.isValid.secretKey.valid = this.bucketSettings.secretKey.trim().length > 0;
    this.isValid.accessKey.valid = this.bucketSettings.accessKey.trim().length > 0;

    if (this.isValid.endpoint.valid &&
        this.isValid.bucket.valid &&
        this.isValid.protocol.valid &&
        this.isValid.secretKey.valid &&
        this.isValid.accessKey.valid) {
      this.$emit("saveBucket", this.bucketSettings);
    }
  }

  private resetBucketSettings() {
    this.bucketSettings = {
      endpoint: "",
      bucket: "",
      protocol: "https",
      secretKey: "",
      accessKey: "",
      pathStyleAccess: true,
      directory: ""
    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
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
