<template>
  <div class="return-list">
    <div class="page-header">
      <h2>{{ t('return.title') }}</h2>
      <a-button type="primary" @click="openApplyModal">{{ t('return.applyReturn') }}</a-button>
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
        <template v-if="column.key === 'returnType'">
          {{ record.returnType === 0 ? t('return.refundOnly') : t('return.returnRefund') }}
        </template>
        <template v-if="column.key === 'refundAmount'">
          &yen;{{ record.refundAmount }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">
            {{ returnStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-button
            v-if="record.status === 1 && record.returnType === 1"
            type="primary"
            size="small"
            @click="shipBack(record.id)"
          >
            {{ t('return.shipBack') }}
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- Apply Return Modal -->
    <a-modal
      v-model:open="applyModalOpen"
      :title="t('return.applyReturn')"
      @ok="handleApply"
      :confirm-loading="submitting"
    >
      <a-form :model="applyForm" layout="vertical">
        <a-form-item :label="t('order.title')" required>
          <a-select
            v-model:value="applyForm.orderId"
            :placeholder="t('order.title')"
            show-search
            :filter-option="filterOrderOption"
          >
            <a-select-option v-for="o in completedOrders" :key="o.id" :value="o.id">
              {{ o.orderNo }} - {{ o.storeName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('return.returnType')" required>
          <a-radio-group v-model:value="applyForm.returnType">
            <a-radio :value="0">{{ t('return.refundOnly') }}</a-radio>
            <a-radio :value="1">{{ t('return.returnRefund') }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item :label="t('return.returnReason')" required>
          <a-textarea v-model:value="applyForm.returnReason" :rows="3" :placeholder="t('return.returnReason')" />
        </a-form-item>
        <a-form-item :label="t('return.evidenceImages')">
          <a-input v-model:value="applyForm.evidenceImages" :placeholder="t('return.evidenceImages')" />
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
import type { ReturnRefundVO, OrderVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const records = ref<ReturnRefundVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })

const columns = [
  { title: 'returnNo', key: 'returnNo', dataIndex: 'returnNo' },
  { title: 'orderNo', key: 'orderNo', dataIndex: 'orderNo' },
  { title: 'storeName', key: 'storeName', dataIndex: 'storeName' },
  { title: 'returnType', key: 'returnType' },
  { title: 'refundAmount', key: 'refundAmount' },
  { title: 'status', key: 'status' },
  { title: 'time', key: 'createTime', dataIndex: 'createTime' },
  { title: 'actions', key: 'actions' },
]
columns.forEach((col) => {
  if (['returnNo', 'orderNo', 'storeName', 'returnType', 'refundAmount', 'status', 'time'].includes(col.key)) {
    col.title = t(`return.${col.key === 'time' ? 'status' : col.key}`)
    if (col.key === 'time') col.title = 'time'
  }
})

const applyModalOpen = ref(false)
const submitting = ref(false)
const completedOrders = ref<OrderVO[]>([])
const applyForm = reactive({
  orderId: null as number | null,
  returnReason: '',
  returnType: 0,
  evidenceImages: '',
})

function statusColor(status: number): string {
  const colors: Record<number, string> = {
    0: 'orange',
    1: 'blue',
    2: 'red',
    3: 'cyan',
    4: 'purple',
    5: 'green',
    6: 'red',
    7: 'red',
  }
  return colors[status] ?? 'default'
}

function returnStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('return.statusPending'),
    1: t('return.statusApproved'),
    2: t('return.statusRejected'),
    3: t('return.statusShipping'),
    4: t('return.statusReceived'),
    5: t('return.statusInspecting'),
    6: t('return.statusRefunded'),
    7: t('return.statusDisputed'),
  }
  return map[status] ?? ''
}

function filterOrderOption(input: string, option: { label: string }) {
  return option.label.toLowerCase().includes(input.toLowerCase())
}

async function fetchReturns() {
  loading.value = true
  try {
    const res = await service.get('/returns', {
      params: { page: pagination.page, pageSize: pagination.pageSize },
    })
    const data = res.data.data as PageResult<ReturnRefundVO>
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchReturns()
}

async function openApplyModal() {
  applyForm.orderId = null
  applyForm.returnReason = ''
  applyForm.returnType = 0
  applyForm.evidenceImages = ''
  try {
    const res = await service.get('/orders', { params: { status: 4 } })
    const data = res.data.data as PageResult<OrderVO>
    completedOrders.value = data.records
  } catch {
    completedOrders.value = []
  }
  applyModalOpen.value = true
}

async function handleApply() {
  if (!applyForm.orderId) {
    message.warning(t('order.title'))
    return
  }
  if (!applyForm.returnReason.trim()) {
    message.warning(t('return.returnReason'))
    return
  }
  submitting.value = true
  try {
    await service.post('/returns', {
      orderId: applyForm.orderId,
      returnReason: applyForm.returnReason,
      returnType: applyForm.returnType,
      evidenceImages: applyForm.evidenceImages,
    })
    message.success(t('common.success'))
    applyModalOpen.value = false
    fetchReturns()
  } finally {
    submitting.value = false
  }
}

async function shipBack(id: number) {
  try {
    await service.put(`/returns/${id}/ship-back`)
    message.success(t('common.success'))
    fetchReturns()
  } catch {
    // interceptor handles error
  }
}

onMounted(() => {
  fetchReturns()
})
</script>

<style scoped>
.return-list {
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
