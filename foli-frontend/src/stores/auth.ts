// 认证状态管理 Auth state management (Pinia)
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo, LoginResult } from '@/types'
import service from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role ?? -1)
  const isAdmin = computed(() => role.value === 2)
  const isSeller = computed(() => role.value === 1)
  const isBuyer = computed(() => role.value === 0)

  /** 登录 Login */
  async function login(username: string, password: string) {
    const res = await service.post('/auth/login', { username, password })
    const data = res.data.data as LoginResult
    token.value = data.token
    localStorage.setItem('token', data.token)
    await fetchUserInfo()
  }

  /** 注册 Register */
  async function register(username: string, password: string, nickname: string) {
    await service.post('/auth/register', { username, password, nickname })
  }

  /** 获取当前用户信息 Fetch current user info */
  async function fetchUserInfo() {
    const res = await service.get('/auth/me')
    userInfo.value = res.data.data as UserInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  /** 退出登录 Logout */
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, role, isAdmin, isSeller, isBuyer, login, register, fetchUserInfo, logout }
})
