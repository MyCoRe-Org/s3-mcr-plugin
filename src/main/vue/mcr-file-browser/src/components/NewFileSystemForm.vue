<template>
  <div class="mcr-file-system-tab">
    <form v-on:submit.prevent.stop="">
      <div class="form-group">
        <label for="protocol">Protocol</label>
        <select class="form-control" v-model="bucketSettings.protocol" id="protocol">
          <option disabled value="">{{i18n.chooseProtocol}}</option>
          <option>http</option>
          <option>https</option>
        </select>
      </div>
      <div class="form-group">
        <label for="endpoint">{{ i18n.s3Endpoint }}</label>
        <input type="text" class="form-control" id="endpoint" v-model="bucketSettings.endpoint">
      </div>
      <div class="form-group">
        <label for="bucket">{{i18n.s3BucketName}}</label>
        <input type="text" class="form-control" id="bucket" v-model="bucketSettings.bucket">
      </div>
      <div class="form-group">
        <label for="accessKey">{{i18n.s3AccessKey}}</label>
        <input type="text" class="form-control" id="accessKey" v-model="bucketSettings.accessKey">
      </div>
      <div class="form-group">
        <label for="secretKey">{{i18n.s3SecretKey}}</label>
        <input type="password" class="form-control" id="secretKey" v-model="bucketSettings.secretKey">
      </div>
      <div class="form-check">
        <input type="checkbox" class="form-check-input" id="pathStyleAccess" v-model="bucketSettings.pathStyleAccess">
        <label class="form-check-label" for="pathStyleAccess">{{i18n.s3PathStyleAccess}}</label>
      </div>

      <button type="submit" class="btn btn-primary" v-on:click="save()">{{i18n.save}}</button>
    </form>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {S3BucketSettings} from "@/model/S3BucketSettings";
import {I18n} from "@/i18n";


@Component
export default class NewFileSystemForm extends Vue {

  private i18n: Record<string, string> = {
    save: "",
    chooseProtocol: "",
    s3Endpoint: "",
    s3BucketName: "",
    s3AccessKey: "",
    s3SecretKey: "",
    s3PathStyleAccess: ""
  };

  async created() {
    await I18n.loadToObject(this.i18n);
  }

  private bucketSettings: S3BucketSettings = {
    endpoint: "",
    bucket: "",
    protocol: "",
    secretKey: "",
    accessKey: "",
    pathStyleAccess: false
  }

  private save() {
    this.$emit("saveBucket", this.bucketSettings);

  }

  private resetBucketSettings() {
    this.bucketSettings = {
      endpoint: "",
      bucket: "",
      protocol: "https",
      secretKey: "",
      accessKey: "",
      pathStyleAccess: false
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
