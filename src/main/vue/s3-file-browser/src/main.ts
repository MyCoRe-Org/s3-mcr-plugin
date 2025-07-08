import { defineCustomElement } from 'vue';
import { createBootstrap } from 'bootstrap-vue-next';
import ExternalStoreFileBrowser from './ExternalStoreFileBrowser.vue';
if (import.meta.env.DEV) {
  import('bootstrap/dist/css/bootstrap.min.css');
  import('font-awesome/css/font-awesome.min.css');
}

const el = defineCustomElement(ExternalStoreFileBrowser, {
  configureApp(app) {
    app.use(createBootstrap());
  },
  shadowRoot: false,
});
customElements.define('s3-file-browser', el);
