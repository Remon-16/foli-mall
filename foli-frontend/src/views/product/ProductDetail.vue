<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'
import type { ProductDetailVO } from '@/types'

const { t } = useI18n()
const route = useRoute()

const product = ref<ProductDetailVO | null>(null)
const loading = ref(false)
const quantity = ref(1)
const addingToCart = ref(false)

const productId = route.params.id as string

onMounted(() => {
  fetchProduct()
})

async function fetchProduct() {
  loading.value = true
  try {
    const res = await service.get(`/products/${productId}`)
    product.value = res.data.data as ProductDetailVO
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function addToCart() {
  if (!product.value) return
  addingToCart.value = true
  try {
    await service.post('/cart', {
      productId: product.value.id,
      quantity: quantity.value,
    })
    message.success(t('common.success'))
  } catch {
    // handled by interceptor
  } finally {
    addingToCart.value = false
  }
}

function getStockText(stock: number): string {
  if (stock <= 0) return t('product.outOfStock')
  if (stock < 10) return t('product.lowStock')
  return t('product.inStock')
}

function getStockColor(stock: number): string {
  if (stock <= 0) return '#cf1322'
  if (stock < 10) return '#faad14'
  return '#52c41a'
}
</script>

<template>
  <div class="product-detail-container">
    <a-spin :spinning="loading" tip="Loading...">
      <a-result
        v-if="!loading && !product"
        status="404"
        title="404"
        sub-title="Product not found"
      />

      <template v-else-if="product">
        <a-row :gutter="[32, 24]">
          <!-- Main Image -->
          <a-col :xs="24" :md="10" :lg="8">
            <a-image
              :src="product.mainImage"
              :alt="product.name"
              :preview="true"
              class="main-image"
              fallback="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII="
            />
          </a-col>

          <!-- Product Info -->
          <a-col :xs="24" :md="14" :lg="16">
            <a-descriptions
              :title="product.name"
              :column="{ xs: 1, sm: 2 }"
              bordered
            >
              <a-descriptions-item :label="t('product.price')">
                <span class="price-text">¥{{ product.price }}</span>
              </a-descriptions-item>
              <a-descriptions-item :label="t('product.stock')">
                <a-tag :color="getStockColor(product.stock)">
                  {{ getStockText(product.stock) }}
                </a-tag>
                <span class="stock-num">({{ product.stock }})</span>
              </a-descriptions-item>
              <a-descriptions-item :label="t('product.sales')">
                {{ product.salesCount }}
              </a-descriptions-item>
              <a-descriptions-item :label="t('store.storeName')">
                <router-link :to="`/stores/${product.storeId}`">
                  {{ product.storeName }}
                </router-link>
              </a-descriptions-item>
              <a-descriptions-item :label="t('product.category')">
                {{ product.categoryName }}
              </a-descriptions-item>
            </a-descriptions>

            <!-- Quantity & Add to Cart -->
            <div class="action-bar">
              <a-space :size="16">
                <span>{{ t('cart.quantity') }}:</span>
                <a-input-number
                  v-model:value="quantity"
                  :min="1"
                  :max="product.stock"
                  :disabled="product.stock <= 0"
                  size="large"
                />
                <a-button
                  type="primary"
                  size="large"
                  :loading="addingToCart"
                  :disabled="product.stock <= 0"
                  @click="addToCart"
                >
                  {{ t('product.addToCart') }}
                </a-button>
              </a-space>
            </div>
          </a-col>
        </a-row>

        <!-- Description -->
        <a-card :title="t('product.productDescription')" class="desc-card">
          <div v-if="product.description" v-html="product.description" />
          <a-empty v-else :description="t('common.noData')" />
        </a-card>

        <!-- Additional Images -->
        <a-card v-if="product.images && product.images.length > 0" title="Product Images" class="images-card">
          <a-row :gutter="[16, 16]">
            <a-col v-for="(img, idx) in product.images" :key="idx" :xs="12" :sm="8" :md="6" :lg="4">
              <a-image :src="img" :preview="true" class="extra-image" />
            </a-col>
          </a-row>
        </a-card>
      </template>
    </a-spin>
  </div>
</template>

<style scoped>
.product-detail-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.main-image {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.main-image :deep(img) {
  width: 100%;
  object-fit: cover;
  max-height: 400px;
}

.price-text {
  color: #cf1322;
  font-size: 20px;
  font-weight: 700;
}

.stock-num {
  margin-left: 4px;
  color: #999;
}

.action-bar {
  margin-top: 24px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}

.desc-card {
  margin-top: 24px;
}

.images-card {
  margin-top: 24px;
}

.extra-image {
  width: 100%;
  border-radius: 4px;
  overflow: hidden;
}

.extra-image :deep(img) {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
</style>
