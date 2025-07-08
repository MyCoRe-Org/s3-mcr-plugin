<template>
  <!-- Loading bar -->
  <div v-if="!loaded" class="d-flex justify-content-center mb-3">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
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
              v-model="derivateInfo"
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
            v-if="derivateInfo?.write || derivateInfo?.delete"
            class="col-auto options"
          >
            <b-dropdown :aria-label="i18n.actions">
              <template #button-content>
                <i class="fa fa-cog"></i> {{ i18n.actions }}
              </template>
              <b-dropdown-item
                v-if="canCreate"
                @click="showAddS3ExternalStoreModal = true"
              >
                {{ i18n.addBucket }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="derivateInfo.write"
                @click="showInfoModal = true"
              >
                {{ i18n.displayInfo }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="derivateInfo.write"
                :href="
                  baseUrl +
                  'editor/editor-derivate.xed?derivateid=' +
                  derivateInfo.id
                "
              >
                {{ i18n.manageDerivate }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="derivateInfo.delete"
                @click="showDeleteExternalStoreModal = true"
              >
                {{ i18n.deleteBucket }}
              </b-dropdown-item>
            </b-dropdown>
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <file-browser-derivate
          v-if="derivateInfo !== undefined"
          :base-url="baseUrl"
          :client="client"
          :object-id="objectId"
          :derivate-id="derivateInfo.id"
          :title="getTitle(derivateInfo)"
        />
      </div>
    </div>
  </div>
  <!-- creation dialog, without viewer -->
  <div v-else-if="derivateInfos.length === 0 && canCreate">
    <h3>{{ i18n.headline }}</h3>
    <div class="jumbotron text-center">
      <a v-if="canCreate" @click="showAddS3ExternalStoreModal = true">
        {{ i18n.addBucket }}
      </a>
    </div>
  </div>

  <s3-external-store-info-modal
    v-if="derivateInfo"
    v-model="showInfoModal"
    :derivate-info="derivateInfo"
  />
  <new-s3-external-store-modal
    v-if="objectId"
    v-model="showAddS3ExternalStoreModal"
    :object-id="objectId"
    :client="client"
    @bucket-created="onBucketCreated"
  />
  <delete-external-store-modal
    v-if="objectId && derivateInfo"
    v-model="showDeleteExternalStoreModal"
    :object-id="objectId"
    :derivate-id="derivateInfo.id"
    :client="client"
    @store-deleted="onStoreDeleted"
  />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { DerivateInfo } from './types';
import { ApiClient } from './api/client';
import { AuthStrategy } from '@jsr/mycore__js-common/auth';
import NewS3ExternalStoreModal from './components/CreateS3ExternalStoreModal.vue';
import FileBrowserDerivate from './components/FileBrowserDerivate.vue';
import S3ExternalStoreInfoModal from './components/InfoS3ExternalStoreModal.vue';
import DeleteExternalStoreModal from './components/DeleteExternalStoreModal.vue';
import { BDropdown, BDropdownItem } from 'bootstrap-vue-next';
import I18n from './i18n';

interface Props {
  baseUrl: string;
  objectId: string;
}
const props = defineProps<Props>();

const loaded = ref(true);
const canCreate = ref(true);
const derivateInfos = ref<DerivateInfo[]>([]);
const derivateInfo = ref<DerivateInfo>();

const showDeleteExternalStoreModal = ref(false);
const showAddS3ExternalStoreModal = ref(false);
const showInfoModal = ref(false);

const i18n: Record<string, string> = {
  actions: '',
  addBucket: '',
  deleteBucket: '',
  headline: '',
  displayInfo: '',
};

const authStrategy: AuthStrategy | undefined = new (class
  implements AuthStrategy
{
  async getHeaders(): Promise<Record<string, string>> {
    return {
      Authorization: `Basic YWRtaW5pc3RyYXRvcjphbGxlc3dpcmRndXQ=`,
    };
  }
})();

const client = new ApiClient(props.baseUrl, authStrategy);

const loadContents = async (): Promise<void> => {
  loaded.value = false;
  const newInfos = await client.getDerivateInfo(props.objectId);
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
    const [d] = derivateInfos.value;
    derivateInfo.value = d;
  }
  loaded.value = true;
};

const onBucketCreated = async () => {
  showAddS3ExternalStoreModal.value = false;
  await loadContents();
};

onMounted(async () => {
  await I18n.loadToObject(i18n);
  await loadContents();
});

const onStoreDeleted = async () => {
  await loadContents();
};

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
