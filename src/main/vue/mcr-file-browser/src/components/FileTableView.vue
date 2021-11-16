<template>
  <div class="mcr-file-table-view">
    <table v-if="fs != null" class="table table-striped">
      <thead>
      <tr>
        <th scope="col">{{i18n.fileName}}</th>
        <th scope="col">{{i18n.fileDate}}</th>
        <th scope="col">{{i18n.fileSize}}</th>
      </tr>
      </thead>
      <tbody>
      <tr class="clickable" v-if="fs.type==='DIRECTORY'" v-on:click.prevent="backButtonClicked">
        <td colspan="3"><a href="#" v-on:click.prevent="">..</a></td>
      </tr>
      <tr v-for="child in fs.children" :key="child.path">
        <td class="clickable" v-on:click.prevent="childClicked(child)"><a href="#" v-on:click.prevent="">{{ child.name }}</a></td>
        <td v-if="child.lastModified!=null">{{ new Date(child.lastModified).toLocaleString() }}</td>
        <td v-else></td>
        <td v-if="child.size>0">{{ size(child.size) }}</td>
        <td v-else></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {FileBase} from "@/model/FileBase";
import {default as filesize} from "filesize.js"
import {I18n} from "@/i18n";

@Component
export default class FileTableView extends Vue {
  @Prop() private fs!: FileBase;

  private i18n: Record<string, string> = {
    fileSize:"",
    fileDate:"",
    fileName:""
  };

  async created() {
    await I18n.loadToObject(this.i18n);
  }

  public childClicked(file: FileBase): void {
    this.$emit("childClicked", file);
  }

  public backButtonClicked(){
    this.$emit("backButtonClicked");
  }

  public size(fileSizeBytes: number) {
    return filesize(fileSizeBytes);
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .clickable {
    cursor: pointer;
  }
</style>
