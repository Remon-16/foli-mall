<script setup lang="ts">
// 默认布局 Default layout — Header + Content + Footer
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

function goLogin() { router.push('/login') }
function goRegister() { router.push('/register') }
function goHome() { router.push('/') }
function goCart() { router.push('/cart') }
function goOrders() { router.push('/orders') }
function goAccount() { router.push('/account') }
function goReturns() { router.push('/returns') }
function goMessages() { router.push('/messages') }
function goComplaints() { router.push('/complaints') }
function goSeller() { router.push('/seller') }
function goAdmin() { router.push('/admin') }
function logout() { authStore.logout(); router.push('/') }
</script>

<template>
  <a-layout style="min-height:100vh">
    <a-layout-header style="background:#fff; display:flex; align-items:center; justify-content:space-between; padding:0 24px; border-bottom:1px solid #f0f0f0">
      <div style="display:flex; align-items:center; gap:24px">
        <a style="font-size:20px; font-weight:bold; color:#1890ff" @click="goHome">Foli Mall</a>
        <a-space>
          <a-button type="link" @click="goHome">{{ t('nav.home') }}</a-button>
        </a-space>
      </div>
      <div style="display:flex; align-items:center; gap:12px">
        <template v-if="authStore.isLoggedIn">
          <a-button type="link" @click="goCart">{{ t('nav.cart') }}</a-button>
          <a-button type="link" @click="goOrders">{{ t('nav.orders') }}</a-button>
          <a-button type="link" @click="goReturns">{{ t('nav.returns') }}</a-button>
          <a-button type="link" @click="goMessages">{{ t('nav.messages') }}</a-button>
          <a-button type="link" @click="goComplaints">{{ t('nav.complaints') }}</a-button>
          <a-button type="link" @click="goAccount">{{ t('nav.account') }}</a-button>
          <a-button v-if="authStore.isSeller" type="link" @click="goSeller">{{ t('nav.sellerCenter') }}</a-button>
          <a-button v-if="authStore.isAdmin" type="link" @click="goAdmin">{{ t('nav.adminPanel') }}</a-button>
          <a-dropdown>
            <a-button type="link">{{ authStore.userInfo?.nickname || 'User' }}</a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="goAccount">{{ t('nav.account') }}</a-menu-item>
                <a-menu-item @click="logout">{{ t('nav.logout') }}</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
        <template v-else>
          <a-button type="link" @click="goLogin">{{ t('nav.login') }}</a-button>
          <a-button type="primary" @click="goRegister">{{ t('nav.register') }}</a-button>
        </template>
        <a-button size="small" @click="toggleLocale">{{ locale === 'zh-CN' ? 'EN' : '中文' }}</a-button>
      </div>
    </a-layout-header>
    <a-layout-content style="padding: 24px; max-width: 1200px; margin: 0 auto; width: 100%">
      <router-view />
    </a-layout-content>
    <a-layout-footer style="text-align:center; color:#999">
      Foli Mall &copy; 2026 — Test Target for API Test Case Generation
    </a-layout-footer>
  </a-layout>
</template>
