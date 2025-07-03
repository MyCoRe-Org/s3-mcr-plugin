import { defineCustomElement } from 'vue';
import ExternalStorageViewer from './ExternalStorageViewer.vue';
if (import.meta.env.DEV) {
  import('bootstrap/dist/css/bootstrap.min.css');
  import('font-awesome/css/font-awesome.min.css');
}

const el = defineCustomElement(ExternalStorageViewer, {
  shadowRoot: false,
});
customElements.define('file-browser', el);
