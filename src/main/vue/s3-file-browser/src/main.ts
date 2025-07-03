import { defineCustomElement } from 'vue';
import S3FileBrowser from './S3FileBrowser.vue';

const S3FileBrowserElement = defineCustomElement(S3FileBrowser, {
  shadowRoot: false,
});

customElements.define('s3-file-browser', S3FileBrowserElement);
