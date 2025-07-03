<template>
  <!-- Loading bar -->
  <div v-if="!loaded" class="d-flex justify-content-center mb-3">
    <div class="spinner-border" role="status">
      <span class="visually-hidden">Loading...</span>
    </div>
  </div>
  <!-- creation dialog, without viewer -->
  <div v-else-if="derivateInfos.length === 0 && canCreate">
    <div class="jumbotron text-center">
      <a v-if="canCreate" @click="showLinkStorageModal = true">
        {{ tp('linkStorage') }}
      </a>
    </div>
  </div>
  <!-- Normal viewer-->
  <div v-else-if="derivateInfos.length > 0" class="mb-3">
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
                {{ resolveTitle(info) }}
              </option>
            </select>
            <span v-else class="title">
              {{ resolveTitle(derivateInfos[0]) }}
            </span>
          </div>
          <div
            v-if="derivateInfo?.write || derivateInfo?.delete"
            class="col-auto options"
          >
            <b-dropdown :aria-label="tp('actions')">
              <template #button-content>
                <i class="fa fa-cog"></i> {{ tp('actions') }}
              </template>
              <b-dropdown-item
                v-if="canCreate"
                @click="showLinkStorageModal = true"
              >
                {{ tp('linkStorage') }}
              </b-dropdown-item>
              <b-dropdown-divider />
              <b-dropdown-item
                v-if="derivateInfo.write"
                @click="showS3BucketInfoModal = true"
              >
                {{ tp('storageConfig') }}
              </b-dropdown-item>
              <b-dropdown-item
                v-if="derivateInfo.delete"
                @click="showUnlinkStorageModal = true"
              >
                {{ tp('unlinkStorage') }}
              </b-dropdown-item>
              <b-dropdown-divider />
              <b-dropdown-item
                v-if="derivateInfo.write"
                :href="
                  baseUrl +
                  'editor/editor-derivate.xed?derivateid=' +
                  derivateInfo.id
                "
              >
                {{ tp('manageDerivate') }}
              </b-dropdown-item>
            </b-dropdown>
          </div>
        </div>
      </div>
      <div class="card-body p-0">
        <file-browser
          v-if="derivateInfo"
          :client="client"
          :locale="locale"
          :object-id="objectId"
          :derivate-id="derivateInfo.id"
          :title="resolveTitle(derivateInfo)"
        />
      </div>
    </div>
  </div>

  <storage-config-modal
    v-if="derivateInfo"
    v-model="showS3BucketInfoModal"
    :derivate-info="derivateInfo"
  />
  <link-storage-modal
    v-if="objectId"
    v-model="showLinkStorageModal"
    :object-id="objectId"
    :client="client"
    @storage-linked="handleStorageLinked"
  />
  <unlink-storage-modal
    v-if="objectId && derivateInfo"
    v-model="showUnlinkStorageModal"
    :object-id="objectId"
    :derivate-id="derivateInfo.id"
    :client="client"
    @storage-unlinked="handleStorageUnlinked"
  />
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { DerivateInfo } from './types/storage';
import { StorageApiClient } from './api/StorageApiClient';
import { AuthStrategy, fetchJwt } from '@jsr/mycore__js-common/auth';
import LinkStorageModal from '@/components/storage/modals/LinkStorageModal.vue';
import FileBrowser from './components/file/FileBrowser.vue';
import StorageConfigModal from '@/components/storage/modals/StorageConfigModal.vue';
import UnlinkStorageModal from '@/components/storage/modals/UnlinkStorageModal.vue';
import { BDropdown, BDropdownItem, BDropdownDivider } from 'bootstrap-vue-next';
import { useI18n } from './composables/useI18n';
import { useI18nPrefix } from './composables/useI18nPrefix';
import { LangApiClient } from '@jsr/mycore__js-common/i18n';
import { resolveTitle } from './utils/storage';
import { I18N_PREFIX } from './constants/ui';

const { addMessages } = useI18n();
const { tp } = useI18nPrefix(I18N_PREFIX);

const props = withDefaults(
  defineProps<{
    baseUrl: string;
    objectId: string;
    locale?: string;
  }>(),
  {
    locale: 'de',
  }
);

const loaded = ref(true);
const canCreate = ref(true);
const derivateInfos = ref<DerivateInfo[]>([]);
const derivateInfo = ref<DerivateInfo>();

const showUnlinkStorageModal = ref(false);
const showLinkStorageModal = ref(false);
const showS3BucketInfoModal = ref(false);

const client = new StorageApiClient(
  props.baseUrl,
  new (class implements AuthStrategy {
    private accessToken = '';

    async getHeaders(): Promise<Record<string, string>> {
      if (import.meta.env.DEV) {
        return { Authorization: `Basic ${import.meta.env.VITE_APP_API_TOKEN}` };
      }
      if (!this.accessToken) {
        this.accessToken = await fetchJwt(props.baseUrl);
      }
      return { Authorization: `Bearer ${this.accessToken}` };
    }
  })()
);

const loadLinks = async (): Promise<void> => {
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
};

onMounted(async () => {
  loaded.value = false;
  await new LangApiClient(props.baseUrl)
    .getTranslations(I18N_PREFIX + '.*', props.locale)
    .then(messages => addMessages(messages));
  await loadLinks();
  loaded.value = true;
});

const handleStorageLinked = async () => {
  showLinkStorageModal.value = false;
  // TODO add proccessing info
  await loadLinks();
};

const handleStorageUnlinked = async () => {
  loaded.value = false;
  await loadLinks();
  loaded.value = true;
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
