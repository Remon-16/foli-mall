<template>
  <div class="become-seller">
    <h2>{{ t('nav.becomeSeller') }}</h2>
    <a-card :title="t('store.applyStore')" style="max-width: 600px; margin: 0 auto;">
      <a-alert
        type="info"
        show-icon
        :message="t('store.applyInfo')"
        style="margin-bottom: 16px"
      />
      <a-form :model="form" layout="vertical">
        <a-form-item :label="t('store.storeName')" required>
          <a-input v-model:value="form.storeName" :placeholder="t('store.storeName')" />
        </a-form-item>
        <a-form-item :label="t('store.storeLogo')">
          <a-input v-model:value="form.storeLogo" :placeholder="t('store.storeLogo')" />
        </a-form-item>
        <a-form-item :label="t('store.storeDescription')">
          <a-textarea v-model:value="form.description" :rows="4" :placeholder="t('store.storeDescription')" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" :loading="submitting" @click="handleSubmit">
            {{ t('common.submit') }}
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'
import service from '@/api'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const submitting = ref(false)
const form = reactive({
  storeName: '',
  storeLogo: '',
  description: '',
})

async function handleSubmit() {
  if (!form.storeName.trim()) {
    message.warning(t('store.storeName'))
    return
  }
  submitting.value = true
  try {
    await service.post('/stores', {
      storeName: form.storeName,
      storeLogo: form.storeLogo,
      description: form.description,
    })
    message.success(t('common.success'))
    await authStore.fetchUserInfo()
    router.push('/seller/store')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.become-seller {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}
.become-seller h2 {
  margin-bottom: 24px;
}
</style>
