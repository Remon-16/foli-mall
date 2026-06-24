// TypeScript 类型定义 TypeScript type definitions

/** 通用API响应 Unified API response */
export interface ApiResult<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页结果 Paginated result */
export interface PageResult<T> {
  total: number
  page: number
  pageSize: number
  records: T[]
}

/** 用户信息 User info */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  email: string
  avatar: string
  balance: number
  role: number
  status: number
  createTime: string
}

/** 登录响应 Login response */
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  role: number
}

/** 商品 Product */
export interface ProductVO {
  id: number
  storeId: number
  storeName: string
  categoryId: number
  categoryName: string
  name: string
  description: string
  mainImage: string
  price: number
  stock: number
  status: number
  salesCount: number
  images: string[]
  createTime: string
}

/** 商品详情 Product detail */
export interface ProductDetailVO extends ProductVO {}

/** 分类 Category */
export interface CategoryVO {
  id: number
  parentId: number
  name: string
  icon: string
  sortOrder: number
  children: CategoryVO[]
}

/** 店铺 Store */
export interface StoreVO {
  id: number
  userId: number
  storeName: string
  storeLogo: string
  description: string
  status: number
  reviewComment: string
  ownerNickname: string
  productCount: number
  createTime: string
}

/** 购物车项 Cart item */
export interface CartItemVO {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  stock: number
  quantity: number
  selected: number
}

/** 订单 Order */
export interface OrderVO {
  id: number
  orderNo: string
  storeId: number
  storeName: string
  totalAmount: number
  status: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  createTime: string
  payTime: string
  shipTime: string
  receiveTime: string
  items: OrderItemVO[]
}

/** 订单项 Order item */
export interface OrderItemVO {
  id: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
}

/** 消息 Message */
export interface MessageVO {
  id: number
  conversationId: string
  senderId: number
  senderName: string
  receiverId: number
  receiverName: string
  content: string
  isRead: number
  createTime: string
}

/** 会话 Conversation */
export interface ConversationVO {
  conversationId: string
  otherUserId: number
  otherUserName: string
  lastMessage: string
  unreadCount: number
  lastMessageTime: string
}

/** 退货退款 Return/Refund */
export interface ReturnRefundVO {
  id: number
  returnNo: string
  orderId: number
  orderNo: string
  userId: number
  buyerNickname: string
  storeId: number
  storeName: string
  returnReason: string
  returnType: number
  refundAmount: number
  status: number
  sellerComment: string
  evidenceImages: string
  adminHandleResult: string
  createTime: string
  shipBackTime: string
  refundTime: string
}

/** 投诉 Complaint */
export interface ComplaintVO {
  id: number
  userId: number
  userName: string
  orderId: number | null
  productId: number | null
  storeId: number
  storeName: string
  returnId: number | null
  type: string
  title: string
  content: string
  evidenceImages: string
  status: number
  handlerId: number | null
  handleResult: string | null
  handleTime: string | null
  createTime: string
}

/** 余额流水 Balance log */
export interface BalanceLogVO {
  id: number
  amount: number
  type: string
  orderNo: string
  beforeBalance: number
  afterBalance: number
  remark: string
  createTime: string
}
