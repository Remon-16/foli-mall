<template>
  <div class="seller-dashboard">
    <h2>{{ t('nav.sellerCenter') }}</h2>
    <p class="welcome-msg">{{ t('common.loading') }}</p>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]" class="stats-row" v-if="store">
        <a-col :span="8">
          <a-card>
            <a-statistic :title="t('store.storeName')" :value="store.storeName" />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card>
            <a-statistic :title="t('store.storeStatus')" :value="storeStatusText(store.status)" />
          </a-card>
        </a-col>
        <a-col :span="8">
          <a-card>
            <a-statistic :title="t('store.productCount')" :value="store.productCount" />
          </a-card>
        </a-col>
      </a-row>

      <a-empty v-if="!loading && !store" :description="t('store.applyStore')">
        <template #children>
          <a-button type="primary" @click="$router.push('/seller/store')">
            {{ t('store.applyStore') }}
          </a-button>
        </template>
      </a-empty>

      <h3 class="section-title">{{ t('store.storeDetail') }}</h3>
      <a-row :gutter="[16, 16]">
        <a-col :span="6">
          <a-card hoverable @click="$router.push('/seller/store')">
            <a-statistic :title="t('nav.stores')" :value="store ? t('store.editStore') : t('store.applyStore')" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card hoverable @click="$router.push('/seller/products')">
            <a-statistic :title="t('seller.productManage')" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card hoverable @click="$router.push('/seller/orders')">
            <a-statistic :title="t('seller.orderManage')" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card hoverable @click="$router.push('/seller/returns')">
            <a-statistic :title="t('seller.returnManage')" />
          </a-card>
        </a-col>
      </a-row>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import service from '@/api'
import type { StoreVO } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const store = ref<StoreVO | null>(null)

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
    const res = await service.get('/seller/stores/my')
    store.value = res.data.data as StoreVO
  } catch {
    store.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStore()
})
</script>

<style scoped>
.seller-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.seller-dashboard h2 {
  margin-bottom: 8px;
}
.welcome-msg {
  color: #666;
  margin-bottom: 24px;
}
.stats-row {
  margin-bottom: 32px;
}
.section-title {
  margin: 24px 0 16px;
  font-size: 18px;
}
</style>
