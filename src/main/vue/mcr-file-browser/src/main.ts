import Vue from 'vue';
import FileBrowser from './App.vue';

Vue.config.productionTip = false;

new Vue({
  render: (h) => h(FileBrowser),
}).$mount('#app');
