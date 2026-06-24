<template>
  <div class="store-manage">
    <h2>{{ t('store.myStore') }}</h2>

    <a-spin :spinning="loading">
      <!-- Has Store: Show Info + Edit -->
      <template v-if="store && store.id">
        <a-alert
          v-if="store.status === 0"
          type="warning"
          show-icon
          :message="t('store.statusPending')"
          style="margin-bottom: 16px"
        />
        <a-alert
          v-if="store.status === 2"
          type="error"
          show-icon
          :message="t('store.statusRejected')"
          :description="store.reviewComment || ''"
          style="margin-bottom: 16px"
        />
        <a-alert
          v-if="store.status === 1"
          type="success"
          show-icon
          :message="t('store.statusApproved')"
          style="margin-bottom: 16px"
        />

        <a-card :title="store.storeName" style="margin-bottom: 24px">
          <a-descriptions bordered :column="{ xs: 1, sm: 2 }">
            <a-descriptions-item :label="t('store.storeName')">{{ store.storeName }}</a-descriptions-item>
            <a-descriptions-item :label="t('store.storeStatus')">
              <a-tag :color="store.status === 1 ? 'green' : store.status === 0 ? 'orange' : 'red'">
                {{ storeStatusText(store.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('store.storeDescription')" :span="2">
              {{ store.description }}
            </a-descriptions-item>
            <a-descriptions-item v-if="store.reviewComment" :label="t('store.reviewComment')" :span="2">
              {{ store.reviewComment }}
            </a-descriptions-item>
          </a-descriptions>
          <div v-if="store.storeLogo" style="margin-top: 16px">
            <img :src="store.storeLogo" :alt="store.storeName" class="store-logo" />
          </div>
        </a-card>

        <a-card :title="t('store.editStore')">
          <a-form :model="editForm" layout="vertical" style="max-width: 600px">
            <a-form-item :label="t('store.storeName')" required>
              <a-input v-model:value="editForm.storeName" :placeholder="t('store.storeName')" />
            </a-form-item>
            <a-form-item :label="t('store.storeLogo')">
              <a-input v-model:value="editForm.storeLogo" :placeholder="t('store.storeLogo')" />
            </a-form-item>
            <a-form-item :label="t('store.storeDescription')">
              <a-textarea v-model:value="editForm.description" :rows="4" :placeholder="t('store.storeDescription')" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="saving" @click="updateStore">
                {{ t('common.save') }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </template>

      <!-- No Store: Apply Form -->
      <template v-else-if="!loading && !store">
        <a-card :title="t('store.applyStore')">
          <a-form :model="applyForm" layout="vertical" style="max-width: 600px">
            <a-form-item :label="t('store.storeName')" required>
              <a-input v-model:value="applyForm.storeName" :placeholder="t('store.storeName')" />
            </a-form-item>
            <a-form-item :label="t('store.storeLogo')">
              <a-input v-model:value="applyForm.storeLogo" :placeholder="t('store.storeLogo')" />
            </a-form-item>
            <a-form-item :label="t('store.storeDescription')">
              <a-textarea v-model:value="applyForm.description" :rows="4" :placeholder="t('store.storeDescription')" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" :loading="saving" @click="applyStore">
                {{ t('common.submit') }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </template>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { StoreVO } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const store = ref<StoreVO | null>(null)

const applyForm = reactive({
  storeName: '',
  storeLogo: '',
  description: '',
})

const editForm = reactive({
  storeName: '',
  storeLogo: '',
  description: '',
})

function storeStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('store.statusPending'),
    1: t('store.statusApproved'),
    2: t('store.statusRejected'),
    3: t('store.statusClosed'),
  }
  return map[status] ?? ''
}

async function fetchStore() {
  loading.value = true
  try {
    const res = await service.get('/seller/stores/my')
    const data = res.data.data
    if (data) {
      store.value = data as StoreVO
      editForm.storeName = store.value.storeName
      editForm.storeLogo = store.value.storeLogo
      editForm.description = store.value.description
    }
  } catch {
    store.value = null
  } finally {
    loading.value = false
  }
}

async function applyStore() {
  if (!applyForm.storeName.trim()) {
    message.warning(t('store.storeName'))
    return
  }
  saving.value = true
  try {
    await service.post('/stores', {
      storeName: applyForm.storeName,
      storeLogo: applyForm.storeLogo,
      description: applyForm.description,
    })
    message.success(t('common.success'))
    fetchStore()
  } finally {
    saving.value = false
  }
}

async function updateStore() {
  if (!editForm.storeName.trim()) {
    message.warning(t('store.storeName'))
    return
  }
  saving.value = true
  try {
    await service.put(`/stores/${store.value!.id}`, {
      storeName: editForm.storeName,
      storeLogo: editForm.storeLogo,
      description: editForm.description,
    })
    message.success(t('common.success'))
    fetchStore()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchStore()
})
</script>

<style scoped>
.store-manage {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.store-manage h2 {
  margin-bottom: 24px;
}
.store-logo {
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
</style>
