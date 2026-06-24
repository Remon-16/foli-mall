<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import service from '@/api'
import type { CartItemVO } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const cartItems = ref<CartItemVO[]>([])

onMounted(() => {
  fetchCart()
})

async function fetchCart() {
  loading.value = true
  try {
    const res = await service.get('/cart')
    cartItems.value = res.data.data as CartItemVO[]
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

// Is an item selected? (selected === 1 means selected)
function isSelected(item: CartItemVO): boolean {
  return item.selected === 1
}

// Are all items selected?
const allSelected = computed(() => {
  if (cartItems.value.length === 0) return false
  return cartItems.value.every((item) => item.selected === 1)
})

// Total price of selected items
const totalPrice = computed(() => {
  return cartItems.value
    .filter((item) => item.selected === 1)
    .reduce((sum, item) => sum + item.price * item.quantity, 0)
})

// Toggle select all
async function toggleSelectAll() {
  const newSelected = allSelected.value ? 0 : 1
  loading.value = true
  try {
    // Update each item's selected status
    await Promise.all(
      cartItems.value.map((item) =>
        service.put(`/cart/${item.id}`, {
          quantity: item.quantity,
          selected: newSelected,
        })
      )
    )
    await fetchCart()
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

// Toggle single item selection
async function toggleItemSelect(item: CartItemVO) {
  const newSelected = item.selected === 1 ? 0 : 1
  try {
    await service.put(`/cart/${item.id}`, {
      quantity: item.quantity,
      selected: newSelected,
    })
    await fetchCart()
  } catch {
    // handled by interceptor
  }
}

// Update item quantity
async function updateQuantity(item: CartItemVO, quantity: number) {
  if (quantity < 1 || quantity > item.stock) return
  try {
    await service.put(`/cart/${item.id}`, {
      quantity,
      selected: item.selected,
    })
    await fetchCart()
  } catch {
    // handled by interceptor
  }
}

// Remove a single item
function removeItem(item: CartItemVO) {
  Modal.confirm({
    title: t('common.confirm'),
    content: `${t('cart.remove')} "${item.productName}"?`,
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      try {
        await service.delete(`/cart/${item.id}`)
        message.success(t('common.success'))
        await fetchCart()
      } catch {
        // handled by interceptor
      }
    },
  })
}

// Clear the entire cart
function clearCart() {
  Modal.confirm({
    title: t('common.confirm'),
    content: t('cart.clear') + '?',
    okText: t('common.yes'),
    cancelText: t('common.no'),
    onOk: async () => {
      try {
        await service.delete('/cart')
        message.success(t('common.success'))
        cartItems.value = []
      } catch {
        // handled by interceptor
      }
    },
  })
}

// Go to checkout
function goToCheckout() {
  const selectedCount = cartItems.value.filter((i) => i.selected === 1).length
  if (selectedCount === 0) {
    message.warning(t('cart.empty'))
    return
  }
  router.push('/orders/create')
}

// Subtotal for an item
function itemSubtotal(item: CartItemVO): number {
  return item.price * item.quantity
}
</script>

<template>
  <div class="cart-container">
    <a-card :title="t('cart.title')" :bordered="false">
      <template #extra>
        <a-button v-if="cartItems.length > 0" danger @click="clearCart">
          {{ t('cart.clear') }}
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <!-- Empty cart -->
        <a-empty
          v-if="!loading && cartItems.length === 0"
          :description="t('cart.empty')"
        />

        <!-- Cart items list -->
        <div v-else class="cart-list">
          <!-- Header -->
          <div class="cart-header">
            <a-row align="middle">
              <a-col :span="1">
                <a-checkbox
                  :checked="allSelected"
                  @change="toggleSelectAll"
                />
              </a-col>
              <a-col :span="1">
                <span class="header-text">{{ t('cart.selectAll') }}</span>
              </a-col>
              <a-col :span="6">
                <span class="header-text">{{ t('product.productDetail') }}</span>
              </a-col>
              <a-col :span="4">
                <span class="header-text">{{ t('product.price') }}</span>
              </a-col>
              <a-col :span="4">
                <span class="header-text">{{ t('cart.quantity') }}</span>
              </a-col>
              <a-col :span="4">
                <span class="header-text">{{ t('cart.total') }}</span>
              </a-col>
              <a-col :span="4">
                <span class="header-text">{{ t('common.actions') }}</span>
              </a-col>
            </a-row>
          </div>

          <!-- Items -->
          <div
            v-for="item in cartItems"
            :key="item.id"
            class="cart-item"
          >
            <a-row align="middle">
              <a-col :span="1">
                <a-checkbox
                  :checked="isSelected(item)"
                  @change="() => toggleItemSelect(item)"
                />
              </a-col>
              <a-col :span="6">
                <a-row align="middle" :gutter="12">
                  <a-col :span="10">
                    <img
                      :src="item.productImage"
                      :alt="item.productName"
                      class="cart-item-image"
                    />
                  </a-col>
                  <a-col :span="14">
                    <span class="cart-item-name">{{ item.productName }}</span>
                  </a-col>
                </a-row>
              </a-col>
              <a-col :span="4">
                <span class="price-text">¥{{ item.price }}</span>
              </a-col>
              <a-col :span="4">
                <a-input-number
                  :value="item.quantity"
                  :min="1"
                  :max="item.stock"
                  size="small"
                  style="width: 100%"
                  @change="(val: number | null) => val && updateQuantity(item, val)"
                />
              </a-col>
              <a-col :span="4">
                <span class="subtotal-text">¥{{ itemSubtotal(item) }}</span>
              </a-col>
              <a-col :span="4">
                <a-button
                  type="link"
                  danger
                  @click="removeItem(item)"
                >
                  {{ t('cart.remove') }}
                </a-button>
              </a-col>
            </a-row>
          </div>
        </div>
      </a-spin>
    </a-card>

    <!-- Bottom bar -->
    <div v-if="cartItems.length > 0" class="cart-bottom-bar">
      <a-row justify="end" align="middle">
        <a-col>
          <a-space :size="24">
            <span class="total-label">{{ t('cart.total') }}:</span>
            <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
            <a-button type="primary" size="large" @click="goToCheckout">
              {{ t('cart.checkout') }}
            </a-button>
          </a-space>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<style scoped>
.cart-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.cart-header {
  padding: 12px 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.header-text {
  font-weight: 600;
  color: #666;
}

.cart-item {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-item-name {
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-text {
  color: #cf1322;
  font-weight: 600;
}

.subtotal-text {
  color: #cf1322;
  font-weight: 600;
  font-size: 16px;
}

.cart-bottom-bar {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
  border-radius: 0 0 8px 8px;
}

.total-label {
  font-size: 16px;
  color: #666;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #cf1322;
}
</style>
