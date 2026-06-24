<template>
  <div class="seller-orders">
    <div class="page-header">
      <h2>{{ t('seller.orderManage') }}</h2>
      <a-select
        v-model:value="statusFilter"
        :placeholder="t('order.status')"
        style="width: 160px"
        allow-clear
        @change="fetchOrders"
      >
        <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
        <a-select-option :value="0">{{ t('order.statusPendingPay') }}</a-select-option>
        <a-select-option :value="1">{{ t('order.statusPaid') }}</a-select-option>
        <a-select-option :value="2">{{ t('order.statusShipped') }}</a-select-option>
        <a-select-option :value="3">{{ t('order.statusReceived') }}</a-select-option>
        <a-select-option :value="4">{{ t('order.statusCompleted') }}</a-select-option>
        <a-select-option :value="5">{{ t('order.statusCancelled') }}</a-select-option>
      </a-select>
    </div>

    <a-table
      :columns="columns"
      :data-source="orders"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'totalAmount'">
          &yen;{{ record.totalAmount }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="orderStatusColor(record.status)">
            {{ orderStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'buyer'">
          {{ record.receiverName }}
        </template>
        <template v-if="column.key === 'actions'">
          <a-button
            v-if="record.status === 1"
            type="primary"
            size="small"
            @click="shipOrder(record.id)"
          >
            {{ t('seller.ship') }}
          </a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { OrderVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const orders = ref<OrderVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const statusFilter = ref<number | null>(null)

const columns = [
  { title: t('order.orderNo'), key: 'orderNo', dataIndex: 'orderNo' },
  { title: t('nav.orders'), key: 'buyer', dataIndex: 'receiverName' },
  { title: t('order.totalAmount'), key: 'totalAmount' },
  { title: t('order.status'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

function orderStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('order.statusPendingPay'),
    1: t('order.statusPaid'),
    2: t('order.statusShipped'),
    3: t('order.statusReceived'),
    4: t('order.statusCompleted'),
    5: t('order.statusCancelled'),
  }
  return map[status] ?? ''
}

function orderStatusColor(status: number): string {
  const colors: Record<number, string> = {
    0: 'orange',
    1: 'blue',
    2: 'cyan',
    3: 'purple',
    4: 'green',
    5: 'default',
  }
  return colors[status] ?? 'default'
}

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await service.get('/seller/orders', { params })
    const data = res.data.data as PageResult<OrderVO>
    orders.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchOrders()
}

async function shipOrder(id: number) {
  try {
    await service.put(`/seller/orders/${id}/ship`)
    message.success(t('common.success'))
    fetchOrders()
  } catch {
    // interceptor handles error
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.seller-orders {
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
