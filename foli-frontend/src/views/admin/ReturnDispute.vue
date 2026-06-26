<template>
  <div class="return-dispute">
    <h2>{{ t('admin.returnDispute') }}</h2>

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
          <a-tag :color="'red'">{{ t('return.statusDisputed') }}</a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <a-button type="primary" size="small" @click="openDetailModal(record)">
            {{ t('admin.handleComplaint') }}
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- Detail & Handle Modal -->
    <a-modal
      v-model:open="detailModalOpen"
      :title="t('admin.returnDispute')"
      width="640px"
    >
      <a-descriptions bordered :column="1" v-if="currentReturn">
        <a-descriptions-item :label="t('return.returnNo')">{{ currentReturn.returnNo }}</a-descriptions-item>
        <a-descriptions-item :label="t('order.orderNo')">{{ currentReturn.orderNo }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.returnReason')">{{ currentReturn.returnReason }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.returnType')">
          {{ currentReturn.returnType === 0 ? t('return.refundOnly') : t('return.returnRefund') }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('return.refundAmount')">&yen;{{ currentReturn.refundAmount }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.sellerComment')">{{ currentReturn.sellerComment || '-' }}</a-descriptions-item>
        <a-descriptions-item :label="t('return.evidenceImages')">{{ currentReturn.evidenceImages || '-' }}</a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <a-form layout="vertical">
        <a-form-item :label="t('return.adminResult')">
          <a-textarea v-model:value="adminResult" :rows="4" :placeholder="t('return.adminResult')" />
        </a-form-item>
      </a-form>

      <template #footer>
        <a-button @click="detailModalOpen = false">{{ t('common.cancel') }}</a-button>
        <a-popconfirm
          :title="t('admin.rejectRefundConfirm')"
          @confirm="handleDispute('reject')"
        >
          <a-button type="primary" danger :loading="submitting">
            {{ t('admin.rejectRefund') }}
          </a-button>
        </a-popconfirm>
        <a-button type="primary" @click="handleDispute('refund')" :loading="submitting">
          {{ t('admin.approveRefund') }}
        </a-button>
      </template>
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

const columns = [
  { title: 'returnNo', key: 'returnNo', dataIndex: 'returnNo' },
  { title: 'orderNo', key: 'orderNo', dataIndex: 'orderNo' },
  { title: 'buyer', key: 'buyerNickname', dataIndex: 'buyerNickname' },
  { title: 'store', key: 'storeName', dataIndex: 'storeName' },
  { title: 'refundAmount', key: 'refundAmount' },
  { title: 'status', key: 'status' },
  { title: 'time', key: 'createTime', dataIndex: 'createTime' },
  { title: 'actions', key: 'actions' },
]

const detailModalOpen = ref(false)
const submitting = ref(false)
const currentReturn = ref<ReturnRefundVO | null>(null)
const adminResult = ref('')

async function fetchReturns() {
  loading.value = true
  try {
    const res = await service.get('/admin/returns', {
      params: { page: pagination.page, pageSize: pagination.pageSize, status: 7 },
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

function openDetailModal(record: ReturnRefundVO) {
  currentReturn.value = record
  adminResult.value = ''
  detailModalOpen.value = true
}

async function handleDispute(decision: string) {
  submitting.value = true
  try {
    await service.put(`/admin/returns/${currentReturn.value!.id}/handle-dispute`, {
      decision,
      result: adminResult.value,
    })
    message.success(t('common.success'))
    detailModalOpen.value = false
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
.return-dispute {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.return-dispute h2 {
  margin-bottom: 24px;
}
</style>
