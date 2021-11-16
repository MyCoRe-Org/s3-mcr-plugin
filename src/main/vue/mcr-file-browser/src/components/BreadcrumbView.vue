<template>
  <div class="mcr-breadcrumb-view">
    <nav aria-label="breadcrumb">
      <ol class="breadcrumb">
        <li v-for="crumb in crumbs"
            v-bind:key="crumb.id"
            class="breadcrumb-item"
            :class="{active: crumbs[crumbs.length-1]===crumb}"
            :aria-current="{page: crumbs[crumbs.length-1]===crumb}">
          <a href="#" v-if="crumbs[crumbs.length-1]!==crumb" v-on:click.prevent="crumbClicked(crumb)">{{ crumb.label }}</a>
          <template v-else>{{crumb.label}}</template>
        </li>

      </ol>
    </nav>
  </div>
</template>

<script lang="ts">
import {Component, Prop, Vue, Watch} from 'vue-property-decorator';

@Component
export default class BreadcrumbView extends Vue {
  @Prop() private path!: string;

  created() {
    this.calculateCrumbs(this.path);
  }

  @Watch("path")
  public pathChanged(_new: string, old: string) {
    this.calculateCrumbs(_new);
  }

  private calculateCrumbs(path: string) {

    while (this.crumbs.length > 0) {
      this.crumbs.pop();
    }

    let until = [];
    for (const crumbLabel of path.split("/")) {
      if (crumbLabel !== "") {
        until.push(crumbLabel);
        this.crumbs.push({
          id: until.join("/") + "/",
          label: crumbLabel
        })
      }
    }

  }

  private crumbClicked(crumb: Crumb) {
    this.$emit("crumbClicked", crumb);
  }

  private crumbs: Crumb[] = [];

}

export interface Crumb {
  id: string;
  label: string;
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
