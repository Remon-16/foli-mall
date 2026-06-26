<template>
  <div class="complaint-list">
    <div class="page-header">
      <h2>{{ t('complaint.title') }}</h2>
      <a-button type="primary" @click="openFileModal">{{ t('complaint.fileComplaint') }}</a-button>
    </div>

    <a-table
      :columns="columns"
      :data-source="records"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          {{ complaintTypeText(record.type) }}
        </template>
        <template v-if="column.key === 'target'">
          <span v-if="record.storeName">{{ record.storeName }}</span>
          <span v-else-if="record.reportedUserName">{{ record.reportedUserName }}</span>
          <span v-else>-</span>
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="complaintStatusColor(record.status)">
            {{ complaintStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-button type="link" size="small" v-if="record.handlerId" @click="contactAdmin(record)">
            {{ t('message.contactAdmin') }}
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- File Complaint Modal -->
    <a-modal
      v-model:open="fileModalOpen"
      :title="t('complaint.fileComplaint')"
      @ok="handleFile"
      :confirm-loading="submitting"
    >
      <a-form :model="fileForm" layout="vertical">
        <a-form-item :label="t('complaint.targetStore')" required>
          <a-select
            v-model:value="fileForm.storeId"
            :placeholder="t('complaint.targetStore')"
            show-search
            :filter-option="filterStoreOption"
          >
            <a-select-option v-for="s in stores" :key="s.id" :value="s.id">
              {{ s.storeName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('complaint.complaintType')" required>
          <a-select v-model:value="fileForm.type" :placeholder="t('complaint.complaintType')">
            <a-select-option value="product_quality">{{ t('complaint.typeProductQuality') }}</a-select-option>
            <a-select-option value="service">{{ t('complaint.typeService') }}</a-select-option>
            <a-select-option value="delivery">{{ t('complaint.typeDelivery') }}</a-select-option>
            <a-select-option value="fraud">{{ t('complaint.typeFraud') }}</a-select-option>
            <a-select-option value="return_dispute">{{ t('complaint.typeReturnDispute') }}</a-select-option>
            <a-select-option value="other">{{ t('complaint.typeOther') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('complaint.complaintTitle')" required>
          <a-input v-model:value="fileForm.title" :placeholder="t('complaint.complaintTitle')" />
        </a-form-item>
        <a-form-item :label="t('complaint.complaintContent')" required>
          <a-textarea v-model:value="fileForm.content" :rows="4" :placeholder="t('complaint.complaintContent')" />
        </a-form-item>
        <a-form-item :label="t('return.evidenceImages')">
          <a-input v-model:value="fileForm.evidenceImages" :placeholder="t('return.evidenceImages')" />
        </a-form-item>
        <a-form-item :label="t('complaint.relatedOrder')">
          <a-input v-model:value="fileForm.orderId" :placeholder="t('complaint.relatedOrder')" />
        </a-form-item>
        <a-form-item :label="t('complaint.relatedProduct')">
          <a-input v-model:value="fileForm.productId" :placeholder="t('complaint.relatedProduct')" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { ComplaintVO, StoreVO, PageResult } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const records = ref<ComplaintVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })

const columns = [
  { title: t('complaint.complaintTitle'), key: 'title', dataIndex: 'title' },
  { title: t('complaint.complaintType'), key: 'type', dataIndex: 'type' },
  { title: t('complaint.targetStore'), key: 'target' },
  { title: t('return.status'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

const fileModalOpen = ref(false)
const submitting = ref(false)
const stores = ref<StoreVO[]>([])
const fileForm = reactive({
  storeId: null as number | null,
  type: 'product_quality',
  title: '',
  content: '',
  evidenceImages: '',
  orderId: '' as string | number,
  productId: '' as string | number,
})

function contactAdmin(record: ComplaintVO) {
  router.push(`/messages/new?receiverId=${record.handlerId}&receiverName=${encodeURIComponent(t('admin.roleAdmin'))}`)
}

function complaintTypeText(type: string): string {
  const map: Record<string, string> = {
    product_quality: t('complaint.typeProductQuality'),
    service: t('complaint.typeService'),
    delivery: t('complaint.typeDelivery'),
    fraud: t('complaint.typeFraud'),
    return_dispute: t('complaint.typeReturnDispute'),
    other: t('complaint.typeOther'),
  }
  return map[type] ?? type
}

function complaintStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('complaint.statusPending'),
    1: t('complaint.statusInProgress'),
    2: t('complaint.statusResolved'),
    3: t('complaint.statusRejected'),
  }
  return map[status] ?? ''
}

function complaintStatusColor(status: number): string {
  const colors: Record<number, string> = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'red' }
  return colors[status] ?? 'default'
}

function filterStoreOption(input: string, option: { label: string }) {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

async function fetchComplaints() {
  loading.value = true
  try {
    const res = await service.get('/complaints', {
      params: { page: pagination.page, pageSize: pagination.pageSize },
    })
    const data = res.data.data as PageResult<ComplaintVO>
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchComplaints()
}

async function openFileModal() {
  fileForm.storeId = null
  fileForm.type = 'product_quality'
  fileForm.title = ''
  fileForm.content = ''
  fileForm.evidenceImages = ''
  fileForm.orderId = ''
  fileForm.productId = ''
  try {
    const res = await service.get('/stores', { params: { page: 1, pageSize: 100 } })
    const data = res.data.data as PageResult<StoreVO>
    stores.value = data.records
  } catch {
    stores.value = []
  }
  fileModalOpen.value = true
}

async function handleFile() {
  if (!fileForm.storeId) { message.warning(t('complaint.targetStore')); return }
  if (!fileForm.title.trim()) { message.warning(t('complaint.complaintTitle')); return }
  if (!fileForm.content.trim()) { message.warning(t('complaint.complaintContent')); return }

  submitting.value = true
  try {
    const body: Record<string, unknown> = {
      storeId: fileForm.storeId,
      type: fileForm.type,
      title: fileForm.title,
      content: fileForm.content,
      evidenceImages: fileForm.evidenceImages,
    }
    if (fileForm.orderId) body.orderId = Number(fileForm.orderId)
    if (fileForm.productId) body.productId = Number(fileForm.productId)

    await service.post('/complaints', body)
    message.success(t('common.success'))
    fileModalOpen.value = false
    fetchComplaints()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchComplaints()
})
</script>

<style scoped>
.complaint-list {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.page-header h2 {
  margin: 0;
}
</style>
