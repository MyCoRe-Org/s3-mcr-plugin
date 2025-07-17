import { defineCustomElement } from 'vue';
import { createBootstrap } from 'bootstrap-vue-next';
import ExternalStorageViewer from './ExternalStorageViewer.vue';
if (import.meta.env.DEV) {
  import('bootstrap/dist/css/bootstrap.min.css');
  import('font-awesome/css/font-awesome.min.css');
}

const el = defineCustomElement(ExternalStorageViewer, {
  configureApp(app) {
    app.use(createBootstrap());
  },
  shadowRoot: false,
});
customElements.define('file-browser', el);
