// Vue Router 路由配置 Route configuration with auth guards
import { createRouter, createWebHashHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录 Login' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { title: '注册 Register' },
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/home/HomeView.vue'), meta: { title: '首页 Home' } },
      { path: 'products/:id', name: 'ProductDetail', component: () => import('@/views/product/ProductDetail.vue'), meta: { title: '商品详情 Product Detail' } },
      { path: 'stores', name: 'StoreList', component: () => import('@/views/store/StoreList.vue'), meta: { title: '店铺 Stores' } },
      { path: 'stores/:id', name: 'StoreDetail', component: () => import('@/views/store/StoreDetail.vue'), meta: { title: '店铺详情 Store Detail' } },
    ],
  },
  {
    path: '/',
    component: () => import('@/layouts/DefaultLayout.vue'),
    meta: { requireAuth: true },
    children: [
      { path: 'cart', name: 'Cart', component: () => import('@/views/cart/CartView.vue'), meta: { title: '购物车 Cart' } },
      { path: 'orders', name: 'OrderList', component: () => import('@/views/order/OrderList.vue'), meta: { title: '我的订单 Orders' } },
      { path: 'orders/create', name: 'OrderCreate', component: () => import('@/views/order/OrderCreate.vue'), meta: { title: '确认订单 Create Order' } },
      { path: 'orders/:id', name: 'OrderDetail', component: () => import('@/views/order/OrderDetail.vue'), meta: { title: '订单详情 Order Detail' } },
      { path: 'account', name: 'Account', component: () => import('@/views/account/AccountView.vue'), meta: { title: '账户 Account' } },
      { path: 'returns', name: 'ReturnList', component: () => import('@/views/return/ReturnList.vue'), meta: { title: '退货退款 Returns' } },
      { path: 'messages', name: 'MessageList', component: () => import('@/views/message/MessageList.vue'), meta: { title: '消息 Messages' } },
      { path: 'messages/:conversationId', name: 'Conversation', component: () => import('@/views/message/ConversationView.vue'), meta: { title: '会话 Chat' } },
      { path: 'complaints', name: 'ComplaintList', component: () => import('@/views/complaint/ComplaintList.vue'), meta: { title: '投诉 Complaints' } },
    ],
  },
  {
    path: '/seller',
    component: () => import('@/layouts/SellerLayout.vue'),
    meta: { requireAuth: true, requireRole: [1] },
    children: [
      { path: '', name: 'SellerDashboard', component: () => import('@/views/seller/SellerDashboard.vue'), meta: { title: '卖家中心 Seller Center' } },
      { path: 'store', name: 'StoreManage', component: () => import('@/views/seller/StoreManage.vue'), meta: { title: '店铺管理 Store Manage' } },
      { path: 'products', name: 'ProductManage', component: () => import('@/views/seller/ProductManage.vue'), meta: { title: '商品管理 Products' } },
      { path: 'orders', name: 'SellerOrders', component: () => import('@/views/seller/SellerOrders.vue'), meta: { title: '店铺订单 Orders' } },
      { path: 'returns', name: 'SellerReturns', component: () => import('@/views/seller/SellerReturns.vue'), meta: { title: '退货管理 Returns' } },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { requireAuth: true, requireRole: [2] },
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { title: '管理后台 Admin Panel' } },
      { path: 'stores', name: 'StoreReview', component: () => import('@/views/admin/StoreReview.vue'), meta: { title: '店铺审核 Store Review' } },
      { path: 'products', name: 'ProductReview', component: () => import('@/views/admin/ProductReview.vue'), meta: { title: '商品审核 Product Review' } },
      { path: 'complaints', name: 'ComplaintHandle', component: () => import('@/views/admin/ComplaintHandle.vue'), meta: { title: '投诉处理 Complaints' } },
      { path: 'returns', name: 'ReturnDispute', component: () => import('@/views/admin/ReturnDispute.vue'), meta: { title: '退货争议 Returns' } },
      { path: 'users', name: 'UserManage', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理 Users' } },
    ],
  },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

// 导航守卫 检查认证状态 Navigation guard — check auth status
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const requiresAuth = to.matched.some(r => r.meta.requireAuth)
  const requiredRoles = to.meta.requireRole as number[] | undefined

  if (requiresAuth && !token) {
    next('/login')
    return
  }

  if (requiredRoles && token) {
    try {
      const userInfoStr = localStorage.getItem('userInfo')
      if (userInfoStr) {
        const userInfo = JSON.parse(userInfoStr)
        if (!requiredRoles.includes(userInfo.role)) {
          next('/')
          return
        }
      }
    } catch {
      next('/login')
      return
    }
  }

  if (to.path === '/login' && token) {
    next('/')
    return
  }

  next()
})

export default router
