<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import service from '@/api'
import type { OrderVO, PageResult } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const orders = ref<OrderVO[]>([])
const total = ref(0)
const activeTab = ref('all')

const searchParams = reactive({
  page: 1,
  pageSize: 10,
  status: undefined as string | undefined,
})

const statusTabs = [
  { key: 'all', label: () => t('common.all') },
  { key: 'pending_pay', label: () => t('order.statusPendingPay') },
  { key: 'paid', label: () => t('order.statusPaid') },
  { key: 'shipped', label: () => t('order.statusShipped') },
  { key: 'completed', label: () => t('order.statusCompleted') },
  { key: 'cancelled', label: () => t('order.statusCancelled') },
]

const statusColorMap: Record<string, string> = {
  pending_pay: 'orange',
  paid: 'blue',
  shipped: 'cyan',
  completed: 'green',
  cancelled: 'default',
}

onMounted(() => {
  fetchOrders()
})

async function fetchOrders() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: searchParams.page,
      pageSize: searchParams.pageSize,
    }
    if (searchParams.status && searchParams.status !== 'all') {
      params.status = searchParams.status
    }
    const res = await service.get('/orders', { params })
    const data = res.data.data as PageResult<OrderVO>
    orders.value = data.records
    total.value = data.total
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleTabChange(key: string) {
  activeTab.value = key
  searchParams.status = key
  searchParams.page = 1
  fetchOrders()
}

function handlePageChange(page: number) {
  searchParams.page = page
  fetchOrders()
}

function goToDetail(id: string) {
  router.push(`/orders/${id}`)
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    pending_pay: t('order.statusPendingPay'),
    paid: t('order.statusPaid'),
    shipped: t('order.statusShipped'),
    completed: t('order.statusCompleted'),
    cancelled: t('order.statusCancelled'),
  }
  return map[status] || status
}

async function handlePay(order: OrderVO) {
  try {
    // Assume the API expects just the order id or a pay endpoint
    await service.put(`/orders/${order.id}/pay`)
    message.success(t('common.success'))
    await fetchOrders()
  } catch {
    // handled by interceptor
  }
}

function handleCancel(order: OrderVO) {
  Modal.confirm({
    title: t('order.cancel'),
    content: `${t('order.cancel')} ${t('order.orderNo')}: ${order.orderNo}?`,
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      try {
        await service.put(`/orders/${order.id}/cancel`)
        message.success(t('common.success'))
        await fetchOrders()
      } catch {
        // handled by interceptor
      }
    },
  })
}

async function handleConfirmReceive(order: OrderVO) {
  Modal.confirm({
    title: t('order.confirmReceive'),
    content: `${t('order.confirmReceive')}?`,
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      try {
        await service.put(`/orders/${order.id}/receive`)
        message.success(t('common.success'))
        await fetchOrders()
      } catch {
        // handled by interceptor
      }
    },
  })
}
</script>

<template>
  <div class="order-list-container">
    <h2 class="page-title">{{ t('order.title') }}</h2>

    <!-- Status filter tabs -->
    <a-card :bordered="false" class="tab-card">
      <a-tabs :active-key="activeTab" @change="handleTabChange">
        <a-tab-pane
          v-for="tab in statusTabs"
          :key="tab.key"
          :tab="tab.label()"
        />
      </a-tabs>
    </a-card>

    <!-- Order list -->
    <a-spin :spinning="loading">
      <a-empty
        v-if="!loading && orders.length === 0"
        :description="t('common.noData')"
        class="empty-wrapper"
      />

      <div v-else class="order-list">
        <a-card
          v-for="order in orders"
          :key="order.id"
          :bordered="false"
          class="order-card"
          hoverable
          @click="goToDetail(order.id)"
        >
          <!-- Order header -->
          <div class="order-header">
            <a-space :size="16">
              <span class="order-store">{{ order.storeName }}</span>
              <a-tag :color="statusColorMap[order.status] || 'default'">
                {{ getStatusText(order.status) }}
              </a-tag>
            </a-space>
            <span class="order-no">{{ t('order.orderNo') }}: {{ order.orderNo }}</span>
          </div>

          <!-- Order items preview -->
          <div class="order-items">
            <div
              v-for="item in order.items.slice(0, 3)"
              :key="item.id"
              class="order-item"
            >
              <img
                :src="item.productImage"
                :alt="item.productName"
                class="order-item-image"
              />
              <div class="order-item-info">
                <span class="order-item-name">{{ item.productName }}</span>
                <span class="order-item-qty">x{{ item.quantity }}</span>
              </div>
              <span class="order-item-price">¥{{ item.price }}</span>
            </div>
            <div v-if="order.items.length > 3" class="more-items">
              ... and {{ order.items.length - 3 }} more items
            </div>
          </div>

          <!-- Order footer -->
          <div class="order-footer">
            <span class="order-time">{{ order.createTime }}</span>
            <div class="order-footer-right">
              <span class="order-total">
                {{ t('order.totalAmount') }}:
                <span class="total-price">¥{{ order.totalAmount.toFixed(2) }}</span>
              </span>
              <a-space :size="8">
                <a-button
                  v-if="order.status === 'pending_pay'"
                  type="primary"
                  size="small"
                  @click.stop="handlePay(order)"
                >
                  {{ t('order.pay') }}
                </a-button>
                <a-button
                  v-if="order.status === 'pending_pay'"
                  size="small"
                  @click.stop="handleCancel(order)"
                >
                  {{ t('order.cancel') }}
                </a-button>
                <a-button
                  v-if="order.status === 'shipped'"
                  type="primary"
                  size="small"
                  @click.stop="handleConfirmReceive(order)"
                >
                  {{ t('order.confirmReceive') }}
                </a-button>
              </a-space>
            </div>
          </div>
        </a-card>
      </div>
    </a-spin>

    <!-- Pagination -->
    <div v-if="total > 0" class="pagination-wrapper">
      <a-pagination
        :current="searchParams.page"
        :total="total"
        :page-size="searchParams.pageSize"
        :show-total="(total: number) => t('common.total', { total })"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.order-list-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 16px;
}

.tab-card {
  margin-bottom: 16px;
}

.empty-wrapper {
  margin-top: 80px;
}

.order-card {
  margin-bottom: 16px;
  cursor: pointer;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.order-store {
  font-weight: 600;
  font-size: 14px;
}

.order-no {
  color: #999;
  font-size: 13px;
}

.order-items {
  margin-bottom: 12px;
}

.order-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.order-item-image {
  width: 60px;
  height: 60px;
  object-fit: cover;
  border-radius: 4px;
}

.order-item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-item-name {
  font-size: 14px;
}

.order-item-qty {
  font-size: 12px;
  color: #999;
}

.order-item-price {
  color: #666;
  font-size: 14px;
}

.more-items {
  text-align: center;
  color: #999;
  font-size: 12px;
  padding: 4px 0;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-time {
  color: #999;
  font-size: 13px;
}

.order-footer-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.order-total {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 18px;
  font-weight: 600;
  color: #cf1322;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 24px;
}
</style>
