<template>
  <div class="mcr-file-table-view">
    <!--
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
    </table> -->
    <b-table id="my-table" :current-page="currentPage" :fields="fields"
             :sort-compare-options="{ numeric: true }"
             :items="fs.children"
             :per-page="perPage" hover striped>
      <template #head(download)="">
        <span> </span>
      </template>

      <template #head(valid)="">
        <span> </span>
      </template>

      <template #head(name)="">
        <span class="text-info clickable">{{ i18n.fileName }} </span>
      </template>

      <template #head(lastModified)="">
        <span class="text-info clickable">{{ i18n.fileDate }} </span>
      </template>

      <template #top-row="" v-if="fs.type==='DIRECTORY'">
        <td role="cell"></td>
        <td role="cell" colspan="4"><a href="#" v-on:click.prevent="backButtonClicked">..</a></td>
      </template>

      <template #head(size)="">
        <span class="text-info clickable">{{ i18n.fileSize }} </span>
      </template>

      <template #cell(download)="data">
        <span>
          <a v-if="data.item.capabilities.indexOf('DOWNLOAD')!==-1" href="#"
             v-on:click.prevent="childDownloadClicke(data.item)">
            <span class="fa fa-download"> </span>
          </a>
        </span>
      </template>

      <template #cell(valid)="data">
        <span>
          <span v-if="data.item.etag !== null && (data.item.type === 'BROWSABLE_FILE' || data.item.type === 'FILE')">
            <span :id="generateId(data.item)" v-if="data.item.storedEtag == data.item.etag"
                  class="fa fa-check clickable text-success"> </span>
            <span :id="generateId(data.item)" v-else class="fa fa-exclamation-triangle clickable text-danger"> </span>
            <b-tooltip :target="generateId(data.item)" placement="left" triggers="click">
              <template v-if="data.item.storedEtag != data.item.etag">
                <p>{{ i18n.etagMissmatch }}</p>
                <dl>
                  <dt>{{ i18n.etagS3 }}</dt>
                  <dd>{{ data.item.etag }}</dd>
                  <dt>{{ i18n.etagMCR }}</dt>
                  <dd>{{ data.item.storedEtag }}</dd>
                </dl>
              </template>
              <template v-else>
                <p>{{ i18n.etagMatch }}</p>
                <dl>
                  <dt>{{ i18n.etagS3 }}</dt>
                  <dd>{{ data.item.storedEtag }}</dd>
                </dl>
              </template>
            </b-tooltip>
          </span>
        </span>
      </template>

      <template #cell(name)="data">
        <a v-if="data.item.type === 'DIRECTORY' || data.item.type === 'BROWSABLE_FILE'" href="#"
           v-on:click.prevent="childClicked(data.item)"> {{ data.item.name }}</a>
        <template v-else>
          {{ data.item.name }}
        </template>
      </template>

      <template #cell(lastModified)="data">
        <span v-if="data.item.lastModified!=null" :title="new Date(data.item.lastModified).toLocaleTimeString()">
          {{ new Date(data.item.lastModified).toLocaleDateString() }}
        </span>
        <template v-else></template>
      </template>

      <template #cell(size)="data">
        <template v-if="data.item.size>0">
          {{ size(data.item.size) }}
        </template>
        <template v-else>
        </template>
      </template>

    </b-table>

    <b-pagination
        v-model="currentPage"
        :fields="fields"
        :per-page="perPage"
        :total-rows="fs.children.length"
        aria-controls="my-table"
        align="center"
        v-show="Math.ceil(fs.children.length/perPage)>1"
    ></b-pagination>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue} from 'vue-property-decorator';
import {FileBase} from "@/model/FileBase";
import {I18n} from "@/i18n";
import {BPagination, BTable, BTooltip} from "bootstrap-vue";

@Component({
  components: {
    BPagination,
    BTable,
    BTooltip
  }
})
export default class FileTableView extends Vue {
  @Prop() private fs!: FileBase;

  private perPage = 10;
  private currentPage = 1;

  private i18n: Record<string, string> = {
    fileSize: "",
    fileDate: "",
    fileName: "",
    etagMCR: "",
    etagS3: "",
    etagMissmatch: "",
    etagMatch: ""
  };

  private fields = [
    {
      key: 'download',
      sortable: false
    },
    {
      key: 'name',
      sortable: true
    },
    {
      key: 'valid',
      sortable: false
    },
    {
      key: 'lastModified',
      sortable: true
    },
    {
      key: 'size',
      sortable: true
    }]


  async created() {
    await I18n.loadToObject(this.i18n);
  }

  public childClicked(file: FileBase): void {
    this.$emit("childClicked", file);
  }

  public childDownloadClicke(file: FileBase): void {
    this.$emit("childDownloadClicked", file);
  }

  public backButtonClicked() {
    this.$emit("backButtonClicked");
  }

  public generateId(file: FileBase) {
    return 'validation' + btoa(file.path);
  }

  public size(bytes: number) {
    // Values used in MIR
    const {radix, unit} = {radix: 1e3, unit: ['bytes', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']};
    bytes = Math.abs(bytes);


    let loop = 0;

    // calculate
    while (bytes >= radix) {
      bytes /= radix;
      ++loop;
    }
    return `${bytes.toFixed(1)} ${unit[loop]}`;
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.clickable {
  cursor: pointer;
}

[aria-sort=ascending] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0de';
  font-weight: 900;
}

[aria-sort=descending] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0dd';
  font-weight: 900;
}

[aria-sort=none] span.text-info:after {
  font-family: "Font Awesome 5 Free";
  content: '\f0dc';
  font-weight: 900;
}

.tooltip {
  opacity: 1 !important;
}
</style>
