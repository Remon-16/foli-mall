<template>
  <div class="store-list">
    <h2 class="page-title">{{ t('store.title') }}</h2>

    <a-spin :spinning="loading">
      <a-row :gutter="[16, 16]">
        <a-col v-for="store in stores" :key="store.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card hoverable @click="goToStore(store.id)" class="store-card">
            <template #cover>
              <img :src="store.storeLogo || '/placeholder-store.png'" :alt="store.storeName" class="store-logo" />
            </template>
            <a-card-meta>
              <template #title>{{ store.storeName }}</template>
              <template #description>
                <p class="store-desc">{{ store.description }}</p>
                <p class="store-count">{{ t('store.productCount') }}: {{ store.productCount }}</p>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>

      <a-empty v-if="!loading && stores.length === 0" />

      <div class="pagination-wrap" v-if="total > 0">
        <a-pagination
          v-model:current="pagination.page"
          v-model:pageSize="pagination.pageSize"
          :total="total"
          :show-size-changer="false"
          @change="fetchStores"
        />
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import service from '@/api'
import type { StoreVO, PageResult } from '@/types'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const stores = ref<StoreVO[]>([])
const total = ref(0)
const pagination = reactive({ page: 1, pageSize: 12 })

async function fetchStores() {
  loading.value = true
  try {
    const res = await service.get('/stores', {
      params: { page: pagination.page, pageSize: pagination.pageSize },
    })
    const data = res.data.data as PageResult<StoreVO>
    stores.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function goToStore(id: number) {
  router.push(`/stores/${id}`)
}

onMounted(() => {
  fetchStores()
})
</script>

<style scoped>
.store-list {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}
.page-title {
  margin-bottom: 24px;
  font-size: 24px;
}
.store-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.store-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.store-logo {
  height: 180px;
  object-fit: cover;
}
.store-desc {
  color: #666;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}
.store-count {
  color: #999;
  font-size: 12px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
