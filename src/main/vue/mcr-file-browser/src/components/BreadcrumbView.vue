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
import {Component, Prop, Vue} from 'vue-property-decorator';

@Component
export default class BreadcrumbView extends Vue {
  @Prop() private crumbs!: Crumb[];

  private crumbClicked(crumb: Crumb) {
    this.$emit("crumbClicked", crumb);
  }
}

export interface Crumb {
  id: string;
  label: string;
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
