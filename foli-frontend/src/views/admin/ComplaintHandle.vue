<template>
  <div class="complaint-handle">
    <div class="page-header">
      <h2>{{ t('admin.complaintHandle') }}</h2>
      <a-select
        v-model:value="statusFilter"
        :placeholder="t('return.status')"
        style="width: 160px"
        allow-clear
        @change="fetchComplaints"
      >
        <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
        <a-select-option :value="0">{{ t('complaint.statusPending') }}</a-select-option>
        <a-select-option :value="1">{{ t('complaint.statusInProgress') }}</a-select-option>
        <a-select-option :value="2">{{ t('complaint.statusResolved') }}</a-select-option>
        <a-select-option :value="3">{{ t('complaint.statusRejected') }}</a-select-option>
      </a-select>
    </div>

    <a-table
      :columns="columns"
      :data-source="complaints"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'type'">
          {{ complaintTypeText(record.type) }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="complaintStatusColor(record.status)">
            {{ complaintStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-button type="link" size="small" @click="openDetailModal(record)">
            {{ t('common.edit') }}
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- Detail & Handle Modal -->
    <a-modal
      v-model:open="detailModalOpen"
      :title="t('admin.complaintHandle')"
      @ok="handleComplaint"
      :confirm-loading="submitting"
      width="640px"
    >
      <a-descriptions bordered :column="1" v-if="currentComplaint">
        <a-descriptions-item :label="t('complaint.complaintTitle')">{{ currentComplaint.title }}</a-descriptions-item>
        <a-descriptions-item :label="t('complaint.complaintType')">{{ complaintTypeText(currentComplaint.type) }}</a-descriptions-item>
        <a-descriptions-item :label="t('complaint.targetStore')">{{ currentComplaint.storeName }}</a-descriptions-item>
        <a-descriptions-item :label="t('complaint.complaintContent')">{{ currentComplaint.content }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.evidenceImages')">{{ currentComplaint.evidenceImages || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.status')">
          <a-tag :color="complaintStatusColor(currentComplaint.status)">
            {{ complaintStatusText(currentComplaint.status) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-form layout="vertical">
        <a-form-item :label="t('return.status')" required>
          <a-select v-model:value="handleStatus" :placeholder="t('return.status')">
            <a-select-option :value="2">{{ t('complaint.statusResolved') }}</a-select-option>
            <a-select-option :value="3">{{ t('complaint.statusRejected') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('complaint.handleResult')" required>
          <a-textarea v-model:value="handleResult" :rows="3" :placeholder="t('complaint.handleResult')" />
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
import type { ComplaintVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const complaints = ref<ComplaintVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const statusFilter = ref<number | null>(null)

const columns = [
  { title: t('complaint.complaintTitle'), key: 'title', dataIndex: 'title' },
  { title: t('complaint.complaintType'), key: 'type', dataIndex: 'type' },
  { title: t('complaint.targetStore'), key: 'storeName', dataIndex: 'storeName' },
  { title: t('return.status'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

const detailModalOpen = ref(false)
const submitting = ref(false)
const currentComplaint = ref<ComplaintVO | null>(null)
const handleStatus = ref(2)
const handleResult = ref('')

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

async function fetchComplaints() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await service.get('/admin/complaints', { params })
    const data = res.data.data as PageResult<ComplaintVO>
    complaints.value = data.records
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

function openDetailModal(record: ComplaintVO) {
  currentComplaint.value = record
  handleStatus.value = 2
  handleResult.value = ''
  detailModalOpen.value = true
}

async function handleComplaint() {
  if (!handleResult.value.trim()) {
    message.warning(t('complaint.handleResult'))
    return
  }
  submitting.value = true
  try {
    await service.put(`/admin/complaints/${currentComplaint.value!.id}/handle`, {
      status: handleStatus.value,
      handleResult: handleResult.value,
    })
    message.success(t('common.success'))
    detailModalOpen.value = false
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
.complaint-handle {
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
