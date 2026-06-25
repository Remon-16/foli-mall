<template>
  <div class="store-review">
    <h2>{{ t('admin.storeReview') }}</h2>

    <a-table
      :columns="columns"
      :data-source="stores"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="storeStatusColor(record.status)">
            {{ storeStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <template v-if="record.status === 0">
            <a-button type="primary" size="small" @click="openReviewModal(record.id, 1)">
              {{ t('admin.approve') }}
            </a-button>
            <a-button size="small" danger style="margin-left: 8px" @click="openReviewModal(record.id, 2)">
              {{ t('admin.reject') }}
            </a-button>
          </template>
        </template>
      </template>
    </a-table>

    <!-- Review Modal -->
    <a-modal
      v-model:open="reviewModalOpen"
      :title="reviewStatus === 1 ? t('admin.approve') : t('admin.reject')"
      @ok="handleReview"
      :confirm-loading="submitting"
    >
      <a-form layout="vertical">
        <a-form-item :label="t('admin.reviewComment')">
          <a-textarea v-model:value="reviewComment" :rows="3" :placeholder="t('admin.reviewComment')" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { StoreVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const stores = ref<StoreVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })

const columns = [
  { title: t('store.storeName'), key: 'storeName', dataIndex: 'storeName' },
  { title: t('store.ownerNickname'), key: 'ownerNickname', dataIndex: 'ownerNickname' },
  { title: t('store.storeDescription'), key: 'description', dataIndex: 'description' },
  { title: t('return.status'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

const reviewModalOpen = ref(false)
const submitting = ref(false)
const reviewStoreId = ref<number | null>(null)
const reviewStatus = ref(1)
const reviewComment = ref('')

function storeStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('store.statusPending'),
    1: t('store.statusApproved'),
    2: t('store.statusRejected'),
    3: t('store.statusClosed'),
  }
  return map[status] ?? ''
}

function storeStatusColor(status: number): string {
  const colors: Record<number, string> = { 0: 'orange', 1: 'green', 2: 'red', 3: 'default' }
  return colors[status] ?? 'default'
}

async function fetchStores() {
  loading.value = true
  try {
    const res = await service.get('/admin/stores/pending', {
      params: { page: pagination.page, pageSize: pagination.pageSize },
    })
    const data = res.data.data as PageResult<StoreVO>
    stores.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchStores()
}

function openReviewModal(id: string, status: number) {
  reviewStoreId.value = id
  reviewStatus.value = status
  reviewComment.value = ''
  reviewModalOpen.value = true
}

async function handleReview() {
  submitting.value = true
  try {
    await service.put(`/admin/stores/${reviewStoreId.value}/review`, {
      status: reviewStatus.value,
      reviewComment: reviewComment.value,
    })
    message.success(t('common.success'))
    reviewModalOpen.value = false
    fetchStores()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchStores()
})
</script>

<style scoped>
.store-review {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.store-review h2 {
  margin-bottom: 24px;
}
</style>
