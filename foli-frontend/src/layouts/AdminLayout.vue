<script setup lang="ts">
// 管理员后台布局 Admin dashboard layout
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const { t, locale } = useI18n()
const authStore = useAuthStore()

function toggleLocale() {
  const next = locale.value === 'zh-CN' ? 'en-US' : 'zh-CN'
  locale.value = next
  localStorage.setItem('locale', next)
}

const menuItems = [
  { key: '/admin', label: t('nav.home'), icon: 'DashboardOutlined' },
  { key: '/admin/stores', label: t('admin.storeReview'), icon: 'ShopOutlined' },
  { key: '/admin/products', label: t('admin.productReview'), icon: 'AppstoreOutlined' },
  { key: '/admin/complaints', label: t('admin.complaintHandle'), icon: 'AlertOutlined' },
  { key: '/admin/returns', label: t('admin.returnDispute'), icon: 'SwapOutlined' },
  { key: '/admin/users', label: t('admin.userManagement'), icon: 'TeamOutlined' },
]

function goHome() { router.push('/') }
function logout() { authStore.logout(); router.push('/') }
</script>

<template>
  <a-layout style="min-height:100vh">
    <a-layout-sider>
      <div style="color:#fff; font-size:18px; font-weight:bold; padding:16px; text-align:center">Foli Mall Admin</div>
      <a-menu theme="dark" mode="inline" :default-selected-keys="['/admin']" @click="({key}) => router.push(key)">
        <a-menu-item v-for="item in menuItems" :key="item.key">{{ item.label }}</a-menu-item>
      </a-menu>
    </a-layout-sider>
    <a-layout>
      <a-layout-header style="background:#fff; display:flex; align-items:center; justify-content:flex-end; padding:0 24px; gap:12px">
        <a-button size="small" @click="toggleLocale">{{ locale === 'zh-CN' ? 'EN' : '中文' }}</a-button>
        <a-button type="link" @click="goHome">{{ t('nav.home') }}</a-button>
        <a-button type="link" @click="logout">{{ t('nav.logout') }}</a-button>
      </a-layout-header>
      <a-layout-content style="padding:24px">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>
