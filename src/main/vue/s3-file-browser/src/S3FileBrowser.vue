<template>
  <!-- Loading bar -->
  <div v-if="!loaded" class="mb-3">
    <!-- <b-spinner label="Spinning"></b-spinner> -->
  </div>
  <!-- Normal viewer-->
  <div v-else-if="derivateInfos.length > 0" class="mb-3">
    <h3>{{ i18n.headline }}</h3>
    <div class="card">
      <div class="card-header">
        <div class="row">
          <div class="col">
            <select
              v-if="derivateInfos.length > 1"
              v-model="current"
              class="form-control"
            >
              <option
                v-for="info in derivateInfos"
                :key="info.id"
                :value="info"
              >
                {{ getTitle(info) }}
              </option>
            </select>
            <span v-else class="title">{{ getTitle(derivateInfos[0]) }}</span>
          </div>
          <div
            v-if="current?.write || current?.delete"
            class="col-auto options"
          >
            <b-dropdown variant="link" toggle-class="text-decoration-none">
              <template #button-content>
                <span class="fas fa-cog"></span><span> Aktionen</span>
              </template>
              <b-dropdown-item
                v-if="canCreate"
                @click="showAddBucketModal = true"
              >
                {{ i18n.addBucket }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="current.write"
                @click="showInfoModal = true"
              >
                {{ i18n.displayInfo }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="current.write"
                :href="
                  baseUrl +
                  'editor/editor-derivate.xed?derivateid=' +
                  current.id
                "
              >
                {{ i18n.manageDerivate }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="current.delete"
                @click="showDeleteBucketModal = true"
              >
                {{ i18n.deleteBucket }}
              </b-dropdown-item>
            </b-dropdown>
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <file-browser-derivate
          v-if="current !== undefined"
          :base-url="baseUrl"
          :client="client"
          :object-id="objectId"
          :derivate-id="current.id"
          :title="getTitle(current)"
        />
      </div>
    </div>
  </div>
  <!-- creation dialog, without viewer -->
  <div v-else-if="derivateInfos.length == 0 && canCreate">
    <h3>{{ i18n.headline }}</h3>
    <div class="jumbotron text-center">
      <a v-if="canCreate" @click="showAddBucketModal = true">
        {{ i18n.addBucket }}
      </a>
    </div>
  </div>

  <base-modal
    v-if="current"
    id="modal-3"
    :visible="showInfoModal"
    :title="i18n.displayInfo"
    hide-footer
    hide-backdrop
    @cancel="showInfoModal = false"
  >
    <dl v-for="(value, name) in current.metadata" :key="name">
      <dt>{{ i18n[name] }}</dt>
      <dd>{{ value }}</dd>
    </dl>
  </base-modal>

  <base-modal
    id="modal-1"
    :title="i18n.addBucket"
    :visible="showAddBucketModal"
    @cancel="showAddBucketModal = false"
  >
    <new-s3-file-system-form @save-bucket="saveBucket">
      <div v-if="showAddBucketError" class="alert alert-danger" role="alert">
        {{ i18n.addBucketError }}
        {{ addBucketErrorMessage }}
      </div>
    </new-s3-file-system-form>
  </base-modal>

  <base-modal
    id="modal-2"
    :title="i18n.deleteBucket"
    :visible="showDeleteBucketModal"
    @cancel="showDeleteBucketModal = false"
  >
    <div v-if="showDeleteBucketError" class="alert alert-danger" role="alert">
      {{ i18n.deleteBucketError }}
      {{ deleteErrorMessage }}
    </div>
    <p>{{ i18n.deleteBucketModal }}</p>
    <button v-if="current" variant="danger" @click="removeBucket(current)">
      {{ i18n.deleteBucket }}
    </button>
  </base-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, provide } from 'vue';
import { DerivateInfo, S3BucketSettings } from './types';
import { S3ApiClient } from './api/client';
import { AuthStrategy } from '@jsr/mycore__js-common/auth';
import { BaseModal } from '@mycore-org/vue-components';
import NewS3FileSystemForm from './components/NewS3FileSystemForm.vue';
import FileBrowserDerivate from './components/FileBrowserDerivate.vue';

interface Props {
  baseUrl: string;
  objectId: string;
}
const props = defineProps<Props>();

provide('baseUrl', props.baseUrl);

const loaded = ref(true);
const canCreate = ref(true);
const derivateInfos = ref<DerivateInfo[]>([]);
const showDeleteBucketError = ref(false);
const deleteErrorMessage = ref('');
const showAddBucketError = ref(false);
const addBucketErrorMessage = ref('');
const current = ref<DerivateInfo>();

const showDeleteBucketModal = ref(false);
const showAddBucketModal = ref(false);
const showInfoModal = ref(false);

const i18n: Record<string, string> = {
  addBucketError: 'addBucketError',
  addBucket: 'addBucket',
  deleteBucket: 'deleteBucket',
  deleteBucketModal: 'deleteBucketModal',
  headline: 'headline',
  displayInfo: 'displayInfo',
  endpoint: 'endpoint',
  bucket: 'bucket',
  accessKey: 'accessKey',
  scretKey: 'scretKey',
  pathStyleAccess: 'pathStyleAccess',
  directory: 'directory',
  protocol: 'protocol',
  useDownloadProxy: 'useDownloadProxy',
  customDownloadProxyUrl: 'customDownloadProxyUrl',
};

const loadContents = async (): Promise<void> => {
  loaded.value = false;
  const newInfos = await client.getInfo(props.objectId);
  while (derivateInfos.value.length > 0) {
    derivateInfos.value.pop();
  }
  canCreate.value = newInfos.create;
  newInfos.derivates
    .filter(info => info.view)
    .forEach(info => {
      derivateInfos.value.push(info);
    });

  if (derivateInfos.value.length > 0) {
    const [derivateInfo] = derivateInfos.value;
    current.value = derivateInfo;
  }
  loaded.value = true;
};

const removeBucket = async (info: DerivateInfo): Promise<void> => {
  showDeleteBucketError.value = false;
  try {
    await client.removeStore(props.objectId, info.id);
    await loadContents();
    showDeleteBucketModal.value = false;
  } catch {
    showDeleteBucketError.value = true;
    deleteErrorMessage.value = 'Error while removing bucket';
  }
};

const saveBucket = async (bucketSettings: S3BucketSettings): Promise<void> => {
  showAddBucketError.value = false;
  try {
    client.saveS3Bucket(props.objectId, bucketSettings);
    showAddBucketModal.value = false;
    await loadContents();
  } catch {
    showAddBucketError.value = true;
    addBucketErrorMessage.value = 'Error while saving bucket';
  }
};

const authStrategy: AuthStrategy | undefined = import.meta.env.DEV
  ? new (class implements AuthStrategy {
      async getHeaders(): Promise<Record<string, string>> {
        return {
          Authorization: `Basic ${import.meta.env.VITE_APP_API_TOKEN}`,
        };
      }
    })()
  : undefined;

const client = new S3ApiClient(props.baseUrl, authStrategy);

onMounted(async () => {
  // TODO await I18n.loadToObject(this.i18n);
  await loadContents();
});

const getTitle = (info: DerivateInfo): string => {
  if (info.titles.length > 0) {
    return info.titles[0].text;
  }
  return info.id;
};
</script>
<style scoped>
.options button * {
  color: #2c3e50;
  font-size: 12px;
}

.jumbotron {
  padding: 1.25rem;
  border: 1px solid rgba(0, 0, 0, 0.125);
}
.card-header span.title {
  display: flex;
  align-items: center;
  justify-content: left;
  height: 100%;
}
</style>
