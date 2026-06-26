package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.request.OrderCreateRequest;
import com.github.foli_backend.dto.response.OrderVO;

import java.util.List;

/**
 * 订单服务接口 / Order service interface
 */
public interface FmOrderService {

    /**
     * 创建订单 从用户选中的购物车项创建订单（按店铺拆分）, 扣减余额, 扣减库存, 记录余额日志
     * Create orders from selected cart items (split by store), deduct balance, deduct stock, record balance log
     *
     * @param userId 用户ID user ID
     * @param req    创建请求 create request
     * @return 订单视图列表 order view object list
     */
    List<OrderVO> createOrder(Long userId, OrderCreateRequest req);

    /**
     * 买家订单列表 Buyer's order list
     *
     * @param userId   用户ID user ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   订单状态筛选 order status filter (optional)
     * @return 分页结果 paginated result
     */
    PageResult<OrderVO> listBuyerOrders(Long userId, int page, int pageSize, Integer status);

    /**
     * 获取订单详情 包含订单项和店铺名称 Get order detail with items and store name
     *
     * @param orderId 订单ID order ID
     * @return 订单视图 order view object
     */
    OrderVO getOrderDetail(Long orderId);

    /**
     * 支付订单 仅PENDING_PAY状态可支付, 扣减余额, 更新状态为PAID
     * Pay order: only PENDING_PAY orders, deduct balance, update to PAID
     *
     * @param orderId 订单ID order ID
     * @param userId  用户ID user ID
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 取消订单 仅PENDING_PAY状态可取消, 退还余额, 恢复库存, 更新为CANCELLED
     * Cancel order: only PENDING_PAY, refund balance, restore stock, update to CANCELLED
     *
     * @param orderId 订单ID order ID
     * @param userId  用户ID user ID
     */
    void cancelOrder(Long orderId, Long userId);

    /**
     * 确认收货 仅SHIPPED状态可收货, 更新为COMPLETED
     * Receive order: only SHIPPED, update to COMPLETED
     *
     * @param orderId 订单ID order ID
     * @param userId  用户ID user ID
     */
    void receiveOrder(Long orderId, Long userId);

    /**
     * 卖家订单列表 Seller's order list
     *
     * @param storeId  店铺ID store ID
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   订单状态筛选 order status filter (optional)
     * @return 分页结果 paginated result
     */
    PageResult<OrderVO> listStoreOrders(Long storeId, int page, int pageSize, Integer status);

    /**
     * 卖家发\b货 PAID状态可发货, 更新为SHIPPED, 设置发货时间
     * Ship order: only PAID, update to SHIPPED, set shipTime
     *
     * @param orderId 订单ID order ID
     * @param storeId 店铺ID store ID
     */
    void shipOrder(Long orderId, Long storeId);
}
