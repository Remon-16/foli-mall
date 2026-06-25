<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import service from '@/api'
import type { ProductVO, CategoryVO, PageResult } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const products = ref<ProductVO[]>([])
const categories = ref<CategoryVO[]>([])
const total = ref(0)

const searchParams = reactive({
  keyword: '',
  categoryId: undefined as number | undefined,
  minPrice: undefined as number | undefined,
  maxPrice: undefined as number | undefined,
  sortBy: undefined as string | undefined,
  page: 1,
  pageSize: 12,
})

const sortOptions = [
  { value: 'newest', label: () => t('product.sortNewest') },
  { value: 'price_asc', label: () => t('product.sortPriceAsc') },
  { value: 'price_desc', label: () => t('product.sortPriceDesc') },
  { value: 'sales', label: () => t('product.sortSales') },
]

onMounted(() => {
  fetchCategories()
  fetchProducts()
})

async function fetchCategories() {
  try {
    const res = await service.get('/categories')
    categories.value = res.data.data as CategoryVO[]
  } catch {
    // handled by interceptor
  }
}

async function fetchProducts() {
  loading.value = true
  try {
    const params: Record<string, any> = {
      page: searchParams.page,
      pageSize: searchParams.pageSize,
    }
    if (searchParams.keyword) params.keyword = searchParams.keyword
    if (searchParams.categoryId) params.categoryId = searchParams.categoryId
    if (searchParams.minPrice !== undefined) params.minPrice = searchParams.minPrice
    if (searchParams.maxPrice !== undefined) params.maxPrice = searchParams.maxPrice
    if (searchParams.sortBy) params.sortBy = searchParams.sortBy

    const res = await service.get('/products', { params })
    const data = res.data.data as PageResult<ProductVO>
    products.value = data.records
    total.value = data.total
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  searchParams.page = 1
  fetchProducts()
}

function handlePageChange(page: number) {
  searchParams.page = page
  fetchProducts()
}

function goToProduct(id: string) {
  router.push(`/products/${id}`)
}
</script>

<template>
  <div class="home-container">
    <!-- Search & Filter Bar -->
    <a-card class="filter-card" :bordered="false">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="12" :md="6">
          <a-input
            v-model:value="searchParams.keyword"
            :placeholder="t('product.searchPlaceholder')"
            size="large"
            @press-enter="handleSearch"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="6">
          <a-select
            v-model:value="searchParams.categoryId"
            :placeholder="t('product.category')"
            allow-clear
            size="large"
            style="width: 100%"
          >
            <a-select-option
              v-for="cat in categories"
              :key="cat.id"
              :value="cat.id"
            >
              {{ cat.name }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="12" :sm="6" :md="3">
          <a-input-number
            v-model:value="searchParams.minPrice"
            :placeholder="t('product.minPrice')"
            size="large"
            style="width: 100%"
            :min="0"
          />
        </a-col>
        <a-col :xs="12" :sm="6" :md="3">
          <a-input-number
            v-model:value="searchParams.maxPrice"
            :placeholder="t('product.maxPrice')"
            size="large"
            style="width: 100%"
            :min="0"
          />
        </a-col>
        <a-col :xs="24" :sm="12" :md="4">
          <a-select
            v-model:value="searchParams.sortBy"
            :placeholder="t('product.sortBy')"
            allow-clear
            size="large"
            style="width: 100%"
          >
            <a-select-option
              v-for="opt in sortOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label() }}
            </a-select-option>
          </a-select>
        </a-col>
        <a-col :xs="24" :sm="12" :md="2">
          <a-button type="primary" size="large" block @click="handleSearch">
            {{ t('common.search') }}
          </a-button>
        </a-col>
      </a-row>
    </a-card>

    <!-- Product Grid -->
    <a-spin :spinning="loading" tip="Loading...">
      <div class="product-grid-wrapper">
        <a-empty
          v-if="!loading && products.length === 0"
          :description="t('common.noData')"
        />
        <a-row v-else :gutter="[16, 16]">
          <a-col
            v-for="product in products"
            :key="product.id"
            :xs="12"
            :sm="8"
            :md="6"
            :lg="4"
          >
            <a-card
              hoverable
              class="product-card"
              @click="goToProduct(product.id)"
            >
              <template #cover>
                <img
                  :src="product.mainImage"
                  :alt="product.name"
                  class="product-image"
                />
              </template>
              <a-card-meta :title="product.name">
                <template #description>
                  <div class="product-info">
                    <span class="product-price">¥{{ product.price }}</span>
                    <span class="product-sales">{{ t('product.sales') }}: {{ product.salesCount }}</span>
                  </div>
                </template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>
    </a-spin>

    <!-- Pagination -->
    <div v-if="total > 0" class="pagination-wrapper">
      <a-pagination
        :current="searchParams.page"
        :total="total"
        :page-size="searchParams.pageSize"
        show-size-changer
        show-quick-jumper
        :show-total="(total: number) => t('common.total', { total })"
        @change="handlePageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.home-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.filter-card {
  margin-bottom: 24px;
}

.product-grid-wrapper {
  min-height: 400px;
}

.product-card {
  border-radius: 8px;
  overflow: hidden;
}

.product-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.product-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.product-price {
  color: #cf1322;
  font-size: 16px;
  font-weight: 600;
}

.product-sales {
  color: #999;
  font-size: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding-bottom: 24px;
}
</style>
