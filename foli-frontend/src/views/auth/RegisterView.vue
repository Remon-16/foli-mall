<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)
const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
})

const validateConfirmPassword = (_rule: any, value: string) => {
  if (value !== formState.password) {
    return Promise.reject(new Error(t('auth.confirmPassword')))
  }
  return Promise.resolve()
}

const rules = {
  username: [
    { required: true, message: () => t('auth.usernameRequired'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordMinLength'), trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: () => t('auth.confirmPassword'), trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: () => t('auth.confirmPassword'), trigger: 'blur' },
  ],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await authStore.register(formState.username, formState.password, formState.nickname)
    message.success(t('auth.registerSuccess'))
    router.push('/login')
  } catch (err: any) {
    // Error already handled by axios interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <a-card :title="t('auth.registerTitle')" class="register-card">
      <a-form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
      >
        <a-form-item name="username">
          <a-input
            v-model:value="formState.username"
            :placeholder="t('auth.username')"
            size="large"
          />
        </a-form-item>

        <a-form-item name="nickname">
          <a-input
            v-model:value="formState.nickname"
            :placeholder="t('auth.nickname')"
            size="large"
          />
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            :placeholder="t('auth.password')"
            size="large"
          />
        </a-form-item>

        <a-form-item name="confirmPassword">
          <a-input-password
            v-model:value="formState.confirmPassword"
            :placeholder="t('auth.confirmPassword')"
            size="large"
          />
        </a-form-item>

        <a-form-item>
          <a-button
            type="primary"
            html-type="submit"
            :loading="loading"
            block
            size="large"
          >
            {{ t('auth.registerBtn') }}
          </a-button>
        </a-form-item>
      </a-form>

      <div class="register-footer">
        <router-link to="/login">
          {{ t('auth.hasAccount') }}
        </router-link>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 24px;
}

.register-card {
  width: 100%;
  max-width: 400px;
}

.register-footer {
  text-align: center;
  margin-top: 8px;
}
</style>
