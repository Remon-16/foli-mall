<template>
  <div class="seller-returns">
    <div class="page-header">
      <h2>{{ t('seller.returnManage') }}</h2>
      <a-select
        v-model:value="statusFilter"
        :placeholder="t('return.status')"
        style="width: 160px"
        allow-clear
        @change="fetchReturns"
      >
        <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
        <a-select-option :value="0">{{ t('return.statusPending') }}</a-select-option>
        <a-select-option :value="1">{{ t('return.statusApproved') }}</a-select-option>
        <a-select-option :value="2">{{ t('return.statusRejected') }}</a-select-option>
        <a-select-option :value="3">{{ t('return.statusShipping') }}</a-select-option>
        <a-select-option :value="4">{{ t('return.statusReceived') }}</a-select-option>
        <a-select-option :value="5">{{ t('return.statusInspecting') }}</a-select-option>
        <a-select-option :value="6">{{ t('return.statusRefunded') }}</a-select-option>
        <a-select-option :value="7">{{ t('return.statusDisputed') }}</a-select-option>
      </a-select>
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
          <a-tag :color="returnStatusColor(record.status)">
            {{ returnStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <!-- PENDING_REVIEW: Approve/Reject -->
          <template v-if="record.status === 0">
            <a-button type="primary" size="small" @click="approveReturn(record.id)">
              {{ t('seller.approve') }}
            </a-button>
            <a-button size="small" danger style="margin-left: 8px" @click="rejectReturn(record.id)">
              {{ t('seller.reject') }}
            </a-button>
          </template>
          <!-- BUYER_SHIPPING: Confirm Receipt -->
          <a-button
            v-if="record.status === 3"
            type="primary"
            size="small"
            @click="confirmReceipt(record.id)"
          >
            {{ t('seller.confirmReceipt') }}
          </a-button>
          <!-- SELLER_RECEIVED: Inspect Pass / Dispute -->
          <template v-if="record.status === 4">
            <a-button type="primary" size="small" @click="inspectPass(record.id)">
              {{ t('seller.inspectPass') }}
            </a-button>
            <a-button size="small" danger style="margin-left: 8px" @click="openDisputeModal(record.id)">
              {{ t('seller.dispute') }}
            </a-button>
          </template>
        </template>
      </template>
    </a-table>

    <!-- Dispute Modal -->
    <a-modal
      v-model:open="disputeModalOpen"
      :title="t('seller.dispute')"
      @ok="handleDispute"
      :confirm-loading="submitting"
    >
      <a-form layout="vertical">
        <a-form-item :label="t('return.sellerComment')" required>
          <a-textarea v-model:value="disputeComment" :rows="4" :placeholder="t('return.sellerComment')" />
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
import type { ReturnRefundVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const records = ref<ReturnRefundVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const statusFilter = ref<number | null>(null)

const columns = [
  { title: 'returnNo', key: 'returnNo', dataIndex: 'returnNo' },
  { title: 'buyer', key: 'buyerNickname', dataIndex: 'buyerNickname' },
  { title: 'refundAmount', key: 'refundAmount' },
  { title: 'returnType', key: 'returnType' },
  { title: 'status', key: 'status' },
  { title: 'time', key: 'createTime', dataIndex: 'createTime' },
  { title: 'actions', key: 'actions' },
]

const disputeModalOpen = ref(false)
const submitting = ref(false)
const disputeReturnId = ref<number | null>(null)
const disputeComment = ref('')

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

function returnStatusColor(status: number): string {
  const colors: Record<number, string> = {
    0: 'orange', 1: 'blue', 2: 'red', 3: 'cyan',
    4: 'purple', 5: 'geekblue', 6: 'green', 7: 'red',
  }
  return colors[status] ?? 'default'
}

async function fetchReturns() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await service.get('/seller/returns', { params })
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

async function approveReturn(id: string) {
  try {
    await service.put(`/seller/returns/${id}/approve`, {})
    message.success(t('common.success'))
    fetchReturns()
  } catch { /* ignore */ }
}

async function rejectReturn(id: string) {
  try {
    await service.put(`/seller/returns/${id}/reject`, {})
    message.success(t('common.success'))
    fetchReturns()
  } catch { /* ignore */ }
}

async function confirmReceipt(id: string) {
  try {
    await service.put(`/seller/returns/${id}/confirm-receipt`)
    message.success(t('common.success'))
    fetchReturns()
  } catch { /* ignore */ }
}

async function inspectPass(id: string) {
  try {
    await service.put(`/seller/returns/${id}/inspect-pass`)
    message.success(t('common.success'))
    fetchReturns()
  } catch { /* ignore */ }
}

function openDisputeModal(id: string) {
  disputeReturnId.value = id
  disputeComment.value = ''
  disputeModalOpen.value = true
}

async function handleDispute() {
  if (!disputeComment.value.trim()) {
    message.warning(t('return.sellerComment'))
    return
  }
  submitting.value = true
  try {
    await service.put(`/seller/returns/${disputeReturnId.value}/dispute`, {
      sellerComment: disputeComment.value,
    })
    message.success(t('common.success'))
    disputeModalOpen.value = false
    fetchReturns()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchReturns()
})
</script>

<style scoped>
.seller-returns {
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
