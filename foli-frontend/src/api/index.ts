// Axios 实例 带JWT拦截器 Axios instance with JWT interceptor
import axios from 'axios'
import type { ApiResult } from '@/types'
import { message } from 'ant-design-vue'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器 附加JWT Token Request interceptor — attach JWT token
service.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器 处理401和统一错误 Response interceptor — handle 401 and errors
service.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResult
    if (res.code !== 200) {
      message.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      router.push('/login')
      message.error('Login expired, please login again')
    } else if (error.response?.data?.message) {
      message.error(error.response.data.message)
    } else {
      message.error('Network error')
    }
    return Promise.reject(error)
  }
)

export default service
