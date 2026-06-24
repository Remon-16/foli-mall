<template>
  <div class="product-manage">
    <div class="page-header">
      <h2>{{ t('seller.productManage') }}</h2>
      <div class="header-actions">
        <a-select
          v-model:value="statusFilter"
          :placeholder="t('return.status')"
          style="width: 140px; margin-right: 12px"
          allow-clear
          @change="fetchProducts"
        >
          <a-select-option :value="null">{{ t('common.all') }}</a-select-option>
          <a-select-option :value="0">{{ t('productStatus.draft') }}</a-select-option>
          <a-select-option :value="1">{{ t('productStatus.pendingReview') }}</a-select-option>
          <a-select-option :value="2">{{ t('productStatus.approved') }}</a-select-option>
          <a-select-option :value="3">{{ t('productStatus.rejected') }}</a-select-option>
          <a-select-option :value="4">{{ t('productStatus.offShelf') }}</a-select-option>
        </a-select>
        <a-button type="primary" @click="openPublishModal">{{ t('seller.publishProduct') }}</a-button>
      </div>
    </div>

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
          <a-button type="link" size="small" @click="openEditModal(record)">
            {{ t('common.edit') }}
          </a-button>
          <a-popconfirm
            :title="t('common.delete') + '?'"
            @confirm="deleteProduct(record.id)"
          >
            <a-button type="link" size="small" danger>{{ t('common.delete') }}</a-button>
          </a-popconfirm>
          <a-button
            v-if="record.status === 2"
            type="link"
            size="small"
            @click="offShelf(record.id)"
          >
            {{ t('seller.offShelf') }}
          </a-button>
          <a-button
            v-if="record.status === 4"
            type="link"
            size="small"
            @click="onShelf(record.id)"
          >
            {{ t('seller.onShelf') }}
          </a-button>
        </template>
      </template>
    </a-table>

    <!-- Publish/Edit Modal -->
    <a-modal
      v-model:open="modalOpen"
      :title="editingId ? t('seller.editProduct') : t('seller.publishProduct')"
      @ok="handleSubmit"
      :confirm-loading="submitting"
    >
      <a-form :model="form" layout="vertical">
        <a-form-item :label="t('seller.productName')" required>
          <a-input v-model:value="form.name" :placeholder="t('seller.productName')" />
        </a-form-item>
        <a-form-item :label="t('product.category')" required>
          <a-select v-model:value="form.categoryId" :placeholder="t('product.category')">
            <a-select-option v-for="c in categories" :key="c.id" :value="c.id">
              {{ c.name }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item :label="t('seller.productPrice')" required>
          <a-input-number v-model:value="form.price" :min="0" :precision="2" style="width: 100%" :placeholder="t('seller.productPrice')" />
        </a-form-item>
        <a-form-item :label="t('seller.productStock')" required>
          <a-input-number v-model:value="form.stock" :min="0" style="width: 100%" :placeholder="t('seller.productStock')" />
        </a-form-item>
        <a-form-item :label="t('seller.mainImage')">
          <a-input v-model:value="form.mainImage" :placeholder="t('seller.mainImage')" />
        </a-form-item>
        <a-form-item :label="t('product.productDescription')">
          <a-textarea v-model:value="form.description" :rows="4" :placeholder="t('product.productDescription')" />
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
import type { ProductVO, CategoryVO, PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const products = ref<ProductVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 10 })
const statusFilter = ref<number | null>(null)

const columns = [
  { title: t('seller.productName'), key: 'name', dataIndex: 'name' },
  { title: t('seller.productPrice'), key: 'price' },
  { title: t('seller.productStock'), key: 'stock', dataIndex: 'stock' },
  { title: t('return.status'), key: 'status' },
  { title: t('product.sales'), key: 'salesCount', dataIndex: 'salesCount' },
  { title: t('common.actions'), key: 'actions' },
]

const modalOpen = ref(false)
const submitting = ref(false)
const editingId = ref<number | null>(null)
const categories = ref<CategoryVO[]>([])
const form = reactive({
  name: '',
  categoryId: null as number | null,
  price: 0,
  stock: 0,
  mainImage: '',
  description: '',
})

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
    const params: Record<string, unknown> = {
      page: pagination.page,
      pageSize: pagination.pageSize,
    }
    if (statusFilter.value !== null) params.status = statusFilter.value
    const res = await service.get('/seller/products', { params })
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

async function loadCategories() {
  try {
    const res = await service.get('/categories')
    categories.value = res.data.data as CategoryVO[]
  } catch {
    // ignore
  }
}

function openPublishModal() {
  editingId.value = null
  form.name = ''
  form.categoryId = null
  form.price = 0
  form.stock = 0
  form.mainImage = ''
  form.description = ''
  modalOpen.value = true
}

function openEditModal(record: ProductVO) {
  editingId.value = record.id
  form.name = record.name
  form.categoryId = record.categoryId
  form.price = record.price
  form.stock = record.stock
  form.mainImage = record.mainImage
  form.description = record.description
  modalOpen.value = true
}

async function handleSubmit() {
  if (!form.name.trim()) { message.warning(t('seller.productName')); return }
  if (!form.categoryId) { message.warning(t('product.category')); return }

  submitting.value = true
  try {
    const body = {
      name: form.name,
      categoryId: form.categoryId,
      price: form.price,
      stock: form.stock,
      mainImage: form.mainImage,
      description: form.description,
    }
    if (editingId.value) {
      await service.put(`/seller/products/${editingId.value}`, body)
    } else {
      await service.post('/seller/products', body)
    }
    message.success(t('common.success'))
    modalOpen.value = false
    fetchProducts()
  } finally {
    submitting.value = false
  }
}

async function deleteProduct(id: number) {
  try {
    await service.delete(`/seller/products/${id}`)
    message.success(t('common.success'))
    fetchProducts()
  } catch {
    // interceptor handles error
  }
}

async function offShelf(id: number) {
  try {
    await service.put(`/seller/products/${id}/off-shelf`)
    message.success(t('common.success'))
    fetchProducts()
  } catch {
    // ignore
  }
}

async function onShelf(id: number) {
  try {
    await service.put(`/seller/products/${id}/on-shelf`)
    message.success(t('common.success'))
    fetchProducts()
  } catch {
    // ignore
  }
}

onMounted(() => {
  fetchProducts()
  loadCategories()
})
</script>

<style scoped>
.product-manage {
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
.header-actions {
  display: flex;
  align-items: center;
}
</style>
