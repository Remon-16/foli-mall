<template>
  <div class="product-review">
    <h2>{{ t('admin.productReview') }}</h2>

    <a-table
      :columns="columns"
      :data-source="products"
      :loading="loading"
      :pagination="{ total, current: pagination.page, pageSize: pagination.pageSize, showSizeChanger: false }"
      @change="handleTableChange"
      row-key="id"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'price'">
          &yen;{{ record.price }}
        </template>
        <template v-if="column.key === 'status'">
          <a-tag :color="productStatusColor(record.status)">
            {{ productStatusText(record.status) }}
          </a-tag>
        </template>
        <template v-if="column.key === 'actions'">
          <template v-if="record.status === 1">
            <a-button type="primary" size="small" @click="openReviewModal(record.id, 2)">
              {{ t('admin.approve') }}
            </a-button>
            <a-button size="small" danger style="margin-left: 8px" @click="openReviewModal(record.id, 3)">
              {{ t('admin.reject') }}
            </a-button>
          </template>
        </template>
      </template>
    </a-table>

    <!-- Review Modal -->
    <a-modal
      v-model:open="reviewModalOpen"
      :title="reviewStatus === 2 ? t('admin.approve') : t('admin.reject')"
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
import type { ProductVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const products = ref<ProductVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })

const columns = [
  { title: t('seller.productName'), key: 'name', dataIndex: 'name' },
  { title: t('nav.stores'), key: 'storeName', dataIndex: 'storeName' },
  { title: t('seller.productPrice'), key: 'price' },
  { title: t('return.status'), key: 'status' },
  { title: t('time'), key: 'createTime', dataIndex: 'createTime' },
  { title: t('common.actions'), key: 'actions' },
]

const reviewModalOpen = ref(false)
const submitting = ref(false)
const reviewProductId = ref<number | null>(null)
const reviewStatus = ref(2)
const reviewComment = ref('')

function productStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('productStatus.draft'),
    1: t('productStatus.pendingReview'),
    2: t('productStatus.approved'),
    3: t('productStatus.rejected'),
    4: t('productStatus.offShelf'),
  }
  return map[status] ?? ''
}

function productStatusColor(status: number): string {
  const colors: Record<number, string> = { 0: 'default', 1: 'orange', 2: 'green', 3: 'red', 4: 'default' }
  return colors[status] ?? 'default'
}

async function fetchProducts() {
  loading.value = true
  try {
    const res = await service.get('/admin/products/pending', {
      params: { page: pagination.page, pageSize: pagination.pageSize },
    })
    const data = res.data.data as PageResult<ProductVO>
    products.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: { current: number; pageSize: number }) {
  pagination.page = pag.current
  pagination.pageSize = pag.pageSize
  fetchProducts()
}

function openReviewModal(id: string, status: number) {
  reviewProductId.value = id
  reviewStatus.value = status
  reviewComment.value = ''
  reviewModalOpen.value = true
}

async function handleReview() {
  submitting.value = true
  try {
    await service.put(`/admin/products/${reviewProductId.value}/review`, {
      status: reviewStatus.value,
      reviewComment: reviewComment.value,
    })
    message.success(t('common.success'))
    reviewModalOpen.value = false
    fetchProducts()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProducts()
})
</script>

<style scoped>
.product-review {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.product-review h2 {
  margin-bottom: 24px;
}
</style>
