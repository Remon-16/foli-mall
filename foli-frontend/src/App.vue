<script setup lang="ts">
// 根组件 Root component
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// 恢复用户信息 Restore user info from storage
if (authStore.token && !authStore.userInfo) {
  authStore.fetchUserInfo().catch(() => {
    authStore.logout()
  })
}
</script>

<template>
  <router-view />
</template>
