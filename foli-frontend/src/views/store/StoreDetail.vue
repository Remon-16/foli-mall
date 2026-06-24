<template>
  <div class="store-detail">
    <a-spin :spinning="loading">
      <!-- Store Info -->
      <div class="store-header" v-if="store">
        <a-descriptions :title="t('store.storeDetail')" bordered :column="{ xs: 1, sm: 2 }">
          <a-descriptions-item :label="t('store.storeName')">{{ store.storeName }}</a-descriptions-item>
          <a-descriptions-item :label="t('store.ownerNickname')">{{ store.ownerNickname }}</a-descriptions-item>
          <a-descriptions-item :label="t('store.storeDescription')" :span="2">{{ store.description }}</a-descriptions-item>
          <a-descriptions-item :label="t('store.storeStatus')">
            <a-tag :color="store.status === 1 ? 'green' : store.status === 0 ? 'orange' : 'red'">
              {{ storeStatusText(store.status) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item :label="t('store.productCount')">{{ store.productCount }}</a-descriptions-item>
        </a-descriptions>
        <div class="store-logo-wrap" v-if="store.storeLogo">
          <img :src="store.storeLogo" :alt="store.storeName" class="store-logo" />
        </div>
      </div>

      <!-- Products Grid -->
      <h3 class="section-title" v-if="store">{{ t('nav.products') }}</h3>
      <a-row :gutter="[16, 16]" v-if="products.length > 0">
        <a-col v-for="product in products" :key="product.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card hoverable @click="$router.push(`/products/${product.id}`)" class="product-card">
            <template #cover>
              <img :src="product.mainImage || '/placeholder-product.png'" :alt="product.name" class="product-img" />
            </template>
            <a-card-meta>
              <template #title>{{ product.name }}</template>
              <template #description>
                <p class="product-price">&yen;{{ product.price }}</p>
                <p class="product-sales">{{ t('product.sales') }}: {{ product.salesCount }}</p>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>

      <a-empty v-if="!loading && products.length === 0 && store" />

      <div class="pagination-wrap" v-if="productTotal > 0">
        <a-pagination
          v-model:current="productPagination.page"
          v-model:pageSize="productPagination.pageSize"
          :total="productTotal"
          :show-size-changer="false"
          @change="fetchProducts"
        />
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import service from '@/api'
import type { StoreVO, ProductVO, PageResult } from '@/types'

const { t } = useI18n()
const route = useRoute()

const loading = ref(false)
const store = ref<StoreVO | null>(null)
const products = ref<ProductVO[]>([])
const productTotal = ref(0)
const productPagination = reactive({ page: 1, pageSize: 12 })

function storeStatusText(status: number): string {
  const map: Record<number, string> = {
    0: t('store.statusPending'),
    1: t('store.statusApproved'),
    2: t('store.statusRejected'),
    3: t('store.statusClosed'),
  }
  return map[status] ?? ''
}

async function fetchStore() {
  loading.value = true
  try {
    const id = route.params.id as string
    const res = await service.get(`/stores/${id}`)
    store.value = res.data.data as StoreVO
  } finally {
    loading.value = false
  }
}

async function fetchProducts() {
  try {
    const id = route.params.id as string
    const res = await service.get(`/stores/${id}/products`, {
      params: { page: productPagination.page, pageSize: productPagination.pageSize },
    })
    const data = res.data.data as PageResult<ProductVO>
    products.value = data.records
    productTotal.value = data.total
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await fetchStore()
  await fetchProducts()
})
</script>

<style scoped>
.store-detail {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.store-header {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  margin-bottom: 32px;
}
.store-header > :first-child {
  flex: 1;
}
.store-logo-wrap {
  flex-shrink: 0;
}
.store-logo {
  width: 180px;
  height: 180px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}
.section-title {
  margin: 32px 0 16px;
  font-size: 20px;
}
.product-card {
  cursor: pointer;
  transition: transform 0.2s;
}
.product-card:hover {
  transform: translateY(-4px);
}
.product-img {
  height: 180px;
  object-fit: cover;
}
.product-price {
  color: #f5222d;
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}
.product-sales {
  color: #999;
  font-size: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
