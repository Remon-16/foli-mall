<template>
  <div class="admin-dashboard">
    <h2>{{ t('nav.adminPanel') }}</h2>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]" class="stats-row">
        <a-col :xs="12" :sm="6">
          <a-card>
            <a-statistic :title="t('admin.userManagement')" :value="userCount" />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card>
            <a-statistic :title="t('nav.stores')" :value="storeCount" />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card>
            <a-statistic :title="t('nav.products')" :value="productCount" />
          </a-card>
        </a-col>
        <a-col :xs="12" :sm="6">
          <a-card>
            <a-statistic :title="t('store.statusPending')" :value="pendingCount" />
          </a-card>
        </a-col>
      </a-row>

      <h3 class="section-title">{{ t('common.actions') }}</h3>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :sm="8">
          <a-card hoverable @click="$router.push('/admin/stores')">
            <a-statistic :title="t('admin.storeReview')" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card hoverable @click="$router.push('/admin/products')">
            <a-statistic :title="t('admin.productReview')" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card hoverable @click="$router.push('/admin/complaints')">
            <a-statistic :title="t('admin.complaintHandle')" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card hoverable @click="$router.push('/admin/returns')">
            <a-statistic :title="t('admin.returnDispute')" />
          </a-card>
        </a-col>
        <a-col :xs="24" :sm="8">
          <a-card hoverable @click="$router.push('/admin/users')">
            <a-statistic :title="t('admin.userManagement')" />
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
import type { PageResult } from '@/types'

const { t } = useI18n()

const loading = ref(false)
const userCount = ref(0)
const storeCount = ref(0)
const productCount = ref(0)
const pendingCount = ref(0)

async function fetchStatistics() {
  loading.value = true
  try {
    const [userRes, storeRes, productRes, pendingRes] = await Promise.all([
      service.get('/admin/users', { params: { page: 1, pageSize: 1 } }),
      service.get('/admin/stores', { params: { page: 1, pageSize: 1 } }),
      service.get('/admin/products', { params: { page: 1, pageSize: 1 } }),
      service.get('/admin/stores/pending', { params: { page: 1, pageSize: 1 } }),
    ])
    userCount.value = (userRes.data.data as PageResult<unknown>).total
    storeCount.value = (storeRes.data.data as PageResult<unknown>).total
    productCount.value = (productRes.data.data as PageResult<unknown>).total
    pendingCount.value = (pendingRes.data.data as PageResult<unknown>).total
  } catch {
    // ignore
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.admin-dashboard h2 {
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
