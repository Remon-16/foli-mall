<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const { t } = useI18n()
const router = useRouter()
const authStore = useAuthStore()

const formRef = ref()
const loading = ref(false)
const formState = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [
    { required: true, message: () => t('auth.usernameRequired'), trigger: 'blur' },
  ],
  password: [
    { required: true, message: () => t('auth.passwordRequired'), trigger: 'blur' },
    { min: 6, message: () => t('auth.passwordMinLength'), trigger: 'blur' },
  ],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    loading.value = true
    await authStore.login(formState.username, formState.password)
    router.push('/')
  } catch (err: any) {
    // Error already handled by axios interceptor
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <a-card :title="t('auth.loginTitle')" class="login-card">
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

        <a-form-item name="password">
          <a-input-password
            v-model:value="formState.password"
            :placeholder="t('auth.password')"
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
            {{ t('auth.loginBtn') }}
          </a-button>
        </a-form-item>
      </a-form>

      <div class="login-footer">
        <router-link to="/register">
          {{ t('auth.noAccount') }}
        </router-link>
      </div>
    </a-card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 400px;
}

.login-footer {
  text-align: center;
  margin-top: 8px;
}
</style>
