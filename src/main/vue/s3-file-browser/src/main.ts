import { defineCustomElement } from 'vue';
import { createBootstrap } from 'bootstrap-vue-next';
import S3FileBrowser from './S3FileBrowser.vue';
if (import.meta.env.DEV) {
  import('bootstrap/dist/css/bootstrap.min.css');
  import('font-awesome/css/font-awesome.min.css');
}

const S3FileBrowserElement = defineCustomElement(S3FileBrowser, {
  configureApp(app) {
    app.use(createBootstrap());
  },
  shadowRoot: false,
});
customElements.define('s3-file-browser', S3FileBrowserElement);
