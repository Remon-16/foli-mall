package com.github.foli_backend.helper;

import com.github.foli_backend.constant.OrderStatusEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.constant.ReturnRefundStatusEnum;
import com.github.foli_backend.constant.StoreStatusEnum;
import com.github.foli_backend.entity.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试数据工厂 — 为所有测试类提供预构建的实体对象
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // ==================== FmUser ====================

    public static FmUser createUser(Long id, String username, int role, int status) {
        FmUser user = new FmUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("$2a$10$hashedpasswordplaceholder");
        user.setNickname(username + "_nick");
        user.setRole(role);
        user.setStatus(status);
        user.setBalance(BigDecimal.valueOf(10000));
        user.setIsDelete(0);
        return user;
    }

    public static FmUser createBuyer(Long id, String username) {
        return createUser(id, username, RoleConstants.BUYER, 1);
    }

    public static FmUser createSeller(Long id, String username) {
        return createUser(id, username, RoleConstants.SELLER, 1);
    }

    // ==================== FmStore ====================

    public static FmStore createStore(Long id, Long userId, int status) {
        FmStore store = new FmStore();
        store.setId(id);
        store.setUserId(userId);
        store.setStoreName("Test Store " + id);
        store.setStoreLogo("/images/store-logo.png");
        store.setDescription("Test store description");
        store.setStatus(status);
        store.setIsDelete(0);
        return store;
    }

    public static FmStore createApprovedStore(Long id, Long userId) {
        return createStore(id, userId, StoreStatusEnum.APPROVED.getCode());
    }

    // ==================== FmProduct ====================

    public static FmProduct createProduct(Long id, Long storeId, int status, int stock, BigDecimal price) {
        FmProduct product = new FmProduct();
        product.setId(id);
        product.setStoreId(storeId);
        product.setCategoryId(1L);
        product.setName("Test Product " + id);
        product.setDescription("Test product description");
        product.setMainImage("/images/product-" + id + ".jpg");
        product.setPrice(price);
        product.setStock(stock);
        product.setStatus(status);
        product.setSalesCount(0);
        product.setIsDelete(0);
        return product;
    }

    public static FmProduct createApprovedProduct(Long id, Long storeId, int stock, BigDecimal price) {
        return createProduct(id, storeId, ProductStatusEnum.APPROVED.getCode(), stock, price);
    }

    // ==================== FmCartItem ====================

    public static FmCartItem createCartItem(Long id, Long userId, Long productId, int quantity, int selected) {
        FmCartItem item = new FmCartItem();
        item.setId(id);
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setSelected(selected);
        item.setIsDelete(0);
        return item;
    }

    public static FmCartItem createSelectedCartItem(Long id, Long userId, Long productId, int quantity) {
        return createCartItem(id, userId, productId, quantity, 1);
    }

    // ==================== FmOrder ====================

    public static FmOrder createOrder(Long id, Long userId, Long storeId, int status, BigDecimal totalAmount) {
        FmOrder order = new FmOrder();
        order.setId(id);
        order.setOrderNo("FO20260625" + String.format("%06d", id));
        order.setUserId(userId);
        order.setStoreId(storeId);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        order.setReceiverName("Test Receiver");
        order.setReceiverPhone("13800138000");
        order.setReceiverAddress("Test Address");
        order.setIsDelete(0);
        return order;
    }

    public static FmOrder createPendingPayOrder(Long id, Long userId, Long storeId, BigDecimal amount) {
        return createOrder(id, userId, storeId, OrderStatusEnum.PENDING_PAY.getCode(), amount);
    }

    public static FmOrder createPaidOrder(Long id, Long userId, Long storeId, BigDecimal amount) {
        FmOrder order = createOrder(id, userId, storeId, OrderStatusEnum.PAID.getCode(), amount);
        order.setPayTime(LocalDateTime.now().minusDays(1));
        return order;
    }

    public static FmOrder createShippedOrder(Long id, Long userId, Long storeId, BigDecimal amount) {
        FmOrder order = createOrder(id, userId, storeId, OrderStatusEnum.SHIPPED.getCode(), amount);
        order.setPayTime(LocalDateTime.now().minusDays(2));
        order.setShipTime(LocalDateTime.now().minusDays(1));
        return order;
    }

    public static FmOrder createCompletedOrder(Long id, Long userId, Long storeId, BigDecimal amount) {
        FmOrder order = createOrder(id, userId, storeId, OrderStatusEnum.COMPLETED.getCode(), amount);
        order.setPayTime(LocalDateTime.now().minusDays(5));
        order.setShipTime(LocalDateTime.now().minusDays(4));
        order.setReceiveTime(LocalDateTime.now().minusDays(3));
        order.setCompleteTime(LocalDateTime.now().minusDays(3));
        return order;
    }

    // ==================== FmOrderItem ====================

    public static FmOrderItem createOrderItem(Long id, Long orderId, Long productId, int quantity, BigDecimal price) {
        FmOrderItem item = new FmOrderItem();
        item.setId(id);
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setProductName("Test Product " + productId);
        item.setProductImage("/images/product-" + productId + ".jpg");
        item.setPrice(price);
        item.setQuantity(quantity);
        item.setIsDelete(0);
        return item;
    }

    // ==================== FmReturnRefund ====================

    public static FmReturnRefund createReturnRefund(Long id, Long orderId, Long userId, Long storeId,
                                                      int status, int returnType, BigDecimal refundAmount) {
        FmReturnRefund rr = new FmReturnRefund();
        rr.setId(id);
        rr.setReturnNo("RT20260625" + String.format("%06d", id));
        rr.setOrderId(orderId);
        rr.setUserId(userId);
        rr.setStoreId(storeId);
        rr.setReturnReason("Test return reason");
        rr.setReturnType(returnType);
        rr.setRefundAmount(refundAmount);
        rr.setStatus(status);
        rr.setIsDelete(0);
        return rr;
    }

    public static FmReturnRefund createPendingReturn(Long id, Long orderId, Long userId, Long storeId,
                                                      int returnType, BigDecimal refundAmount) {
        return createReturnRefund(id, orderId, userId, storeId,
                ReturnRefundStatusEnum.PENDING_REVIEW.getCode(), returnType, refundAmount);
    }

    public static FmReturnRefund createApprovedReturn(Long id, Long orderId, Long userId, Long storeId,
                                                       int returnType, BigDecimal refundAmount) {
        return createReturnRefund(id, orderId, userId, storeId,
                ReturnRefundStatusEnum.APPROVED.getCode(), returnType, refundAmount);
    }
}
