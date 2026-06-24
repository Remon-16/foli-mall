<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import service from '@/api'
import type { OrderVO } from '@/types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const order = ref<OrderVO | null>(null)
const loading = ref(false)
const actionLoading = ref(false)

const orderId = Number(route.params.id)

onMounted(() => {
  fetchOrder()
})

async function fetchOrder() {
  loading.value = true
  try {
    const res = await service.get(`/orders/${orderId}`)
    order.value = res.data.data as OrderVO
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    pending_pay: t('order.statusPendingPay'),
    paid: t('order.statusPaid'),
    shipped: t('order.statusShipped'),
    received: t('order.statusReceived'),
    completed: t('order.statusCompleted'),
    cancelled: t('order.statusCancelled'),
  }
  return map[status] || status
}

function getStatusColor(status: string): string {
  const map: Record<string, string> = {
    pending_pay: 'orange',
    paid: 'blue',
    shipped: 'cyan',
    received: 'purple',
    completed: 'green',
    cancelled: 'default',
  }
  return map[status] || 'default'
}

async function handlePay() {
  if (!order.value) return
  actionLoading.value = true
  try {
    await service.put(`/orders/${order.value.id}/pay`)
    message.success(t('common.success'))
    await fetchOrder()
  } catch {
    // handled by interceptor
  } finally {
    actionLoading.value = false
  }
}

function handleCancel() {
  if (!order.value) return
  Modal.confirm({
    title: t('order.cancel'),
    content: `${t('order.cancel')} ${t('order.orderNo')}: ${order.value.orderNo}?`,
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      try {
        await service.put(`/orders/${order.value!.id}/cancel`)
        message.success(t('common.success'))
        await fetchOrder()
      } catch {
        // handled by interceptor
      }
    },
  })
}

async function handleConfirmReceive() {
  if (!order.value) return
  Modal.confirm({
    title: t('order.confirmReceive'),
    content: `${t('order.confirmReceive')}?`,
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      actionLoading.value = true
      try {
        await service.put(`/orders/${order.value!.id}/receive`)
        message.success(t('common.success'))
        await fetchOrder()
      } catch {
        // handled by interceptor
      } finally {
        actionLoading.value = false
      }
    },
  })
}

function handleBack() {
  router.push('/orders')
}

const itemColumns = [
  {
    title: 'Image',
    dataIndex: 'productImage',
    key: 'productImage',
    width: 80,
  },
  {
    title: 'Product Name',
    dataIndex: 'productName',
    key: 'productName',
  },
  {
    title: 'Price',
    dataIndex: 'price',
    key: 'price',
  },
  {
    title: 'Quantity',
    dataIndex: 'quantity',
    key: 'quantity',
  },
  {
    title: 'Subtotal',
    key: 'subtotal',
  },
]
</script>

<template>
  <div class="order-detail-container">
    <a-button class="back-btn" @click="handleBack">{{ t('common.back') }}</a-button>

    <a-spin :spinning="loading">
      <a-result
        v-if="!loading && !order"
        status="404"
        title="404"
        sub-title="Order not found"
      />

      <template v-else-if="order">
        <!-- Order Status -->
        <a-card :bordered="false" class="status-card">
          <div class="status-header">
            <h2>{{ t('order.orderNo') }}: {{ order.orderNo }}</h2>
            <a-tag :color="getStatusColor(order.status)" class="status-tag">
              {{ getStatusText(order.status) }}
            </a-tag>
          </div>
        </a-card>

        <!-- Order Info -->
        <a-card title="Order Information" :bordered="false" class="info-card">
          <a-descriptions :column="{ xs: 1, sm: 2 }" bordered>
            <a-descriptions-item :label="t('order.orderNo')">
              {{ order.orderNo }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('order.status')">
              <a-tag :color="getStatusColor(order.status)">
                {{ getStatusText(order.status) }}
              </a-tag>
            </a-descriptions-item>
            <a-descriptions-item :label="t('store.storeName')">
              <router-link :to="`/stores/${order.storeId}`">
                {{ order.storeName }}
              </router-link>
            </a-descriptions-item>
            <a-descriptions-item :label="t('order.totalAmount')">
              <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
            </a-descriptions-item>
            <a-descriptions-item :label="t('order.createOrder')">
              {{ order.createTime }}
            </a-descriptions-item>
            <a-descriptions-item v-if="order.payTime" :label="t('order.payTime')">
              {{ order.payTime }}
            </a-descriptions-item>
            <a-descriptions-item v-if="order.shipTime" :label="t('order.shipTime')">
              {{ order.shipTime }}
            </a-descriptions-item>
            <a-descriptions-item v-if="order.receiveTime" :label="t('order.receiveTime')">
              {{ order.receiveTime }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- Receiver Info -->
        <a-card title="Receiver Information" :bordered="false" class="info-card">
          <a-descriptions :column="{ xs: 1, sm: 2 }" bordered>
            <a-descriptions-item :label="t('order.receiver')">
              {{ order.receiverName }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('order.receiverPhone')">
              {{ order.receiverPhone }}
            </a-descriptions-item>
            <a-descriptions-item :label="t('order.receiverAddress')" :span="2">
              {{ order.receiverAddress }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <!-- Order Items -->
        <a-card title="Order Items" :bordered="false" class="items-card">
          <a-table
            :columns="itemColumns"
            :data-source="order.items"
            :pagination="false"
            row-key="id"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'productImage'">
                <img
                  :src="record.productImage"
                  :alt="record.productName"
                  class="item-image"
                />
              </template>
              <template v-else-if="column.key === 'price'">
                <span>¥{{ record.price }}</span>
              </template>
              <template v-else-if="column.key === 'subtotal'">
                <span class="subtotal-text">¥{{ (record.price * record.quantity).toFixed(2) }}</span>
              </template>
            </template>
          </a-table>
        </a-card>

        <!-- Action Buttons -->
        <a-card :bordered="false" class="actions-card">
          <a-space :size="12">
            <a-button
              v-if="order.status === 'pending_pay'"
              type="primary"
              size="large"
              :loading="actionLoading"
              @click="handlePay"
            >
              {{ t('order.pay') }}
            </a-button>
            <a-button
              v-if="order.status === 'pending_pay'"
              size="large"
              @click="handleCancel"
            >
              {{ t('order.cancel') }}
            </a-button>
            <a-button
              v-if="order.status === 'shipped'"
              type="primary"
              size="large"
              :loading="actionLoading"
              @click="handleConfirmReceive"
            >
              {{ t('order.confirmReceive') }}
            </a-button>
          </a-space>
        </a-card>
      </template>
    </a-spin>
  </div>
</template>

<style scoped>
.order-detail-container {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.back-btn {
  margin-bottom: 16px;
}

.status-card {
  margin-bottom: 16px;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-header h2 {
  margin: 0;
  font-size: 18px;
}

.status-tag {
  font-size: 14px;
  padding: 4px 12px;
}

.info-card {
  margin-bottom: 16px;
}

.total-price {
  color: #cf1322;
  font-size: 18px;
  font-weight: 700;
}

.items-card {
  margin-bottom: 16px;
}

.item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.subtotal-text {
  color: #cf1322;
  font-weight: 600;
}

.actions-card {
  margin-bottom: 24px;
  text-align: center;
}
</style>
