<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import service from '@/api'

const { t } = useI18n()
const router = useRouter()

const formRef = ref()
const loading = ref(false)

const formState = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
})

const rules = {
  receiverName: [
    { required: true, message: () => t('order.receiver'), trigger: 'blur' },
  ],
  receiverPhone: [
    { required: true, message: () => t('order.receiverPhone'), trigger: 'blur' },
  ],
  receiverAddress: [
    { required: true, message: () => t('order.receiverAddress'), trigger: 'blur' },
  ],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await service.post('/orders', {
      receiverName: formState.receiverName,
      receiverPhone: formState.receiverPhone,
      receiverAddress: formState.receiverAddress,
    })
    message.success(t('common.success'))
    router.push('/orders')
  } catch (err: any) {
    // Error already handled by axios interceptor
  } finally {
    loading.value = false
  }
}

function handleBack() {
  router.push('/cart')
}
</script>

<template>
  <div class="order-create-container">
    <h2 class="page-title">{{ t('order.createOrder') }}</h2>

    <a-card :bordered="false">
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        class="order-form"
        @finish="handleSubmit"
      >
        <a-form-item :label="t('order.receiver')" name="receiverName">
          <a-input
            v-model:value="formState.receiverName"
            size="large"
            placeholder="Please enter receiver name"
          />
        </a-form-item>

        <a-form-item :label="t('order.receiverPhone')" name="receiverPhone">
          <a-input
            v-model:value="formState.receiverPhone"
            size="large"
            placeholder="Please enter phone number"
          />
        </a-form-item>

        <a-form-item :label="t('order.receiverAddress')" name="receiverAddress">
          <a-input
            v-model:value="formState.receiverAddress"
            size="large"
            placeholder="Please enter shipping address"
          />
        </a-form-item>

        <div class="form-actions">
          <a-space :size="16">
            <a-button size="large" @click="handleBack">
              {{ t('common.back') }}
            </a-button>
            <a-button
              type="primary"
              html-type="submit"
              size="large"
              :loading="loading"
            >
              {{ t('common.submit') }}
            </a-button>
          </a-space>
        </div>
      </a-form>
    </a-card>
  </div>
</template>

<style scoped>
.order-create-container {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  font-size: 24px;
  margin-bottom: 24px;
}

.order-form {
  max-width: 600px;
}

.form-actions {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}
</style>
