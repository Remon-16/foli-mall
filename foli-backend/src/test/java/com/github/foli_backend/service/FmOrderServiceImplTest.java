package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.OrderStatusEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.OrderCreateRequest;
import com.github.foli_backend.dto.response.OrderVO;
import com.github.foli_backend.entity.*;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.*;
import com.github.foli_backend.service.impl.FmOrderServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmOrderServiceImpl 单元测试")
class FmOrderServiceImplTest {

    @Mock FmOrderMapper orderMapper;
    @Mock FmOrderItemMapper orderItemMapper;
    @Mock FmCartItemMapper cartItemMapper;
    @Mock FmProductMapper productMapper;
    @Mock FmUserMapper userMapper;
    @Mock FmStoreMapper storeMapper;
    @Mock FmBalanceLogMapper balanceLogMapper;

    @InjectMocks
    FmOrderServiceImpl orderService;

    // Common test data
    Long buyerId = 1L;
    Long storeId = 10L;
    Long productId1 = 100L;
    Long productId2 = 101L;
    BigDecimal price1 = BigDecimal.valueOf(99.00);
    BigDecimal price2 = BigDecimal.valueOf(199.00);

    @BeforeEach
    void setUp() {
        // defaults if needed
    }

    @AfterEach
    void tearDown() {
        // cleanup if needed
    }

    // ==================== createOrder ====================

    @Nested
    @DisplayName("createOrder — 创建订单")
    class CreateOrderTests {

        @Test
        @DisplayName("should_create_order_successfully_when_cart_has_selected_items")
        void shouldCreateOrderSuccessfully_whenCartHasSelectedItems() {
            // given: cart with 2 selected items
            FmProduct product1 = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmProduct product2 = TestDataFactory.createApprovedProduct(productId2, storeId, 5, price2);
            FmCartItem cartItem1 = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 2);
            FmCartItem cartItem2 = TestDataFactory.createSelectedCartItem(2L, buyerId, productId2, 1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem1, cartItem2));
            when(productMapper.selectById(productId1)).thenReturn(product1);
            when(productMapper.selectById(productId2)).thenReturn(product2);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderMapper.insert(any(FmOrder.class))).thenReturn(1);
            when(orderItemMapper.insert(any(FmOrderItem.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(cartItemMapper.delete(any(LambdaUpdateWrapper.class))).thenReturn(2);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            OrderCreateRequest req = new OrderCreateRequest();
            req.setReceiverName("Zhang San");
            req.setReceiverPhone("13800138000");
            req.setReceiverAddress("Beijing");

            // when
            OrderVO result = orderService.createOrder(buyerId, req);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getOrderNo()).startsWith("FO");
            assertThat(result.getStatus()).isEqualTo(OrderStatusEnum.PAID.getCode());
            assertThat(result.getStoreName()).isEqualTo("Test Store 10");
            assertThat(result.getTotalAmount()).isEqualByComparingTo(
                    price1.multiply(BigDecimal.valueOf(2)).add(price2));

            // verify stock deducted (2 stock deductions + 2 sales_count increments)
            verify(productMapper, times(4)).update(isNull(), any(LambdaUpdateWrapper.class));
            // verify balance deducted
            verify(userMapper, times(1)).update(isNull(), any(LambdaUpdateWrapper.class));
            // verify order created
            verify(orderMapper).insert(any(FmOrder.class));
            // verify order items created
            verify(orderItemMapper, times(2)).insert(any(FmOrderItem.class));
            // verify balance log
            verify(balanceLogMapper).insert(any(FmBalanceLog.class));
            // verify cart cleared
            verify(cartItemMapper).delete(any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("should_throw_cart_empty_when_no_selected_items")
        void shouldThrowCartEmpty_whenNoSelectedItems() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CART_EMPTY.getCode());
        }

        @Test
        @DisplayName("should_throw_cart_empty_when_selected_items_is_null")
        void shouldThrowCartEmpty_whenSelectedItemsIsNull() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(null);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CART_EMPTY.getCode());
        }

        @Test
        @DisplayName("should_throw_product_not_found_when_product_deleted")
        void shouldThrowProductNotFound_whenProductDeleted() {
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(null);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_product_off_shelf_when_product_not_approved")
        void shouldThrowProductOffShelf_whenProductNotApproved() {
            FmProduct product = TestDataFactory.createProduct(productId1, storeId,
                    ProductStatusEnum.DRAFT.getCode(), 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_OFF_SHELF.getCode());
        }

        @Test
        @DisplayName("should_throw_product_off_shelf_when_product_status_null")
        void shouldThrowProductOffShelf_whenProductStatusNull() {
            FmProduct product = TestDataFactory.createProduct(productId1, storeId,
                    ProductStatusEnum.APPROVED.getCode(), 10, price1);
            product.setStatus(null);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_OFF_SHELF.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_stock_when_quantity_exceeds_stock")
        void shouldThrowInsufficientStock_whenQuantityExceedsStock() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 1, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 5);
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_STOCK.getCode());
        }

        @Test
        @DisplayName("should_throw_stock_deduction_failed_when_optimistic_lock_fails")
        void shouldThrowStockDeductionFailed_whenOptimisticLockFails() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 2);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            // simulate concurrent stock modification
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.STOCK_DEDUCTION_FAILED.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_balance_when_balance_below_total")
        void shouldThrowInsufficientBalance_whenBalanceBelowTotal() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10,
                    BigDecimal.valueOf(5000));
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 2);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(100)); // not enough

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_BALANCE.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_balance_when_balance_is_null")
        void shouldThrowInsufficientBalance_whenBalanceIsNull() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(null);

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_BALANCE.getCode());
        }

        @Test
        @DisplayName("should_throw_balance_deduction_failed_when_optimistic_lock_fails")
        void shouldThrowBalanceDeductionFailed_whenOptimisticLockFails() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            OrderCreateRequest req = new OrderCreateRequest();

            assertThatThrownBy(() -> orderService.createOrder(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.BALANCE_DEDUCTION_FAILED.getCode());
        }

        @Test
        @DisplayName("should_increment_sales_count_when_order_created")
        void shouldIncrementSalesCount_whenOrderCreated() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 2);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderMapper.insert(any(FmOrder.class))).thenReturn(1);
            when(orderItemMapper.insert(any(FmOrderItem.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(cartItemMapper.delete(any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            OrderCreateRequest req = new OrderCreateRequest();

            orderService.createOrder(buyerId, req);

            // 1 stock deduction + 1 sales_count increment = 2 calls
            verify(productMapper, times(2)).update(isNull(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("should_set_store_name_null_when_store_not_found")
        void shouldSetStoreNameNull_whenStoreNotFound() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId1, storeId, 10, price1);
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, buyerId, productId1, 1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId1)).thenReturn(product);
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderMapper.insert(any(FmOrder.class))).thenReturn(1);
            when(orderItemMapper.insert(any(FmOrderItem.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(cartItemMapper.delete(any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(storeMapper.selectById(storeId)).thenReturn(null);

            OrderCreateRequest req = new OrderCreateRequest();

            OrderVO result = orderService.createOrder(buyerId, req);

            assertThat(result.getStoreName()).isNull();
        }
    }

    // ==================== listBuyerOrders ====================

    @Nested
    @DisplayName("listBuyerOrders — 买家订单列表")
    class ListBuyerOrdersTests {

        @Test
        @DisplayName("should_return_paginated_orders_with_items_and_store_names")
        void shouldReturnPaginatedOrdersWithItemsAndStoreNames() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(List.of(order));
            mpPage.setTotal(1);

            FmOrderItem orderItem = TestDataFactory.createOrderItem(1L, 1L, productId1, 2, price1);
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(orderItem));
            when(storeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(store));

            PageResult<OrderVO> result = orderService.listBuyerOrders(buyerId, 1, 10, null);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
            OrderVO vo = result.getRecords().get(0);
            assertThat(vo.getItems()).hasSize(1);
            assertThat(vo.getStoreName()).isEqualTo("Test Store 10");
        }

        @Test
        @DisplayName("should_filter_by_status_when_status_provided")
        void shouldFilterByStatus_whenStatusProvided() {
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            orderService.listBuyerOrders(buyerId, 1, 10, OrderStatusEnum.PAID.getCode());

            verify(orderMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("should_return_empty_page_when_no_orders")
        void shouldReturnEmptyPage_whenNoOrders() {
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            PageResult<OrderVO> result = orderService.listBuyerOrders(buyerId, 1, 10, null);

            assertThat(result.getTotal()).isEqualTo(0);
            assertThat(result.getRecords()).isEmpty();
            // should not query order items when no orders
            verify(orderItemMapper, never()).selectList(any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("should_handle_store_name_when_store_not_found")
        void shouldHandleStoreName_whenStoreNotFound() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(List.of(order));
            mpPage.setTotal(1);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(storeMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            PageResult<OrderVO> result = orderService.listBuyerOrders(buyerId, 1, 10, null);

            assertThat(result.getRecords().get(0).getStoreName()).isNull();
        }
    }

    // ==================== getOrderDetail ====================

    @Nested
    @DisplayName("getOrderDetail — 订单详情")
    class GetOrderDetailTests {

        @Test
        @DisplayName("should_return_order_detail_with_items_and_store_name")
        void shouldReturnOrderDetailWithItemsAndStoreName() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            FmOrderItem orderItem = TestDataFactory.createOrderItem(1L, 1L, productId1, 2, price1);
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(orderItem));
            when(storeMapper.selectById(storeId)).thenReturn(store);

            OrderVO result = orderService.getOrderDetail(1L);

            assertThat(result).isNotNull();
            assertThat(result.getItems()).hasSize(1);
            assertThat(result.getStoreName()).isEqualTo("Test Store 10");
        }

        @Test
        @DisplayName("should_throw_order_not_found_when_order_does_not_exist")
        void shouldThrowOrderNotFound_whenOrderDoesNotExist() {
            when(orderMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.getOrderDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_set_store_name_null_when_store_not_found")
        void shouldSetStoreNameNull_whenStoreNotFound() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());
            when(storeMapper.selectById(storeId)).thenReturn(null);

            OrderVO result = orderService.getOrderDetail(1L);

            assertThat(result.getStoreName()).isNull();
        }
    }

    // ==================== payOrder ====================

    @Nested
    @DisplayName("payOrder — 支付订单")
    class PayOrderTests {

        @Test
        @DisplayName("should_pay_order_successfully_when_status_is_pending_pay")
        void shouldPayOrderSuccessfully_whenStatusIsPendingPay() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, buyerId, storeId, price1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderMapper.updateById(any(FmOrder.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);

            orderService.payOrder(1L, buyerId);

            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.PAID.getCode());
            assertThat(order.getPayTime()).isNotNull();
            verify(balanceLogMapper).insert(any(FmBalanceLog.class));
        }

        @Test
        @DisplayName("should_throw_order_not_found_when_order_does_not_exist")
        void shouldThrowOrderNotFound_whenPayNonExistentOrder() {
            when(orderMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> orderService.payOrder(999L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_forbidden_when_order_belongs_to_different_user")
        void shouldThrowForbidden_whenOrderBelongsToDifferentUser() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, 999L, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.payOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_wrong_order_status_when_not_pending_pay")
        void shouldThrowWrongOrderStatus_whenNotPendingPay() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.payOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_ORDER_STATUS.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_balance_when_balance_too_low")
        void shouldThrowInsufficientBalance_whenBalanceTooLow() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, buyerId, storeId,
                    BigDecimal.valueOf(10000));
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(50));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);

            assertThatThrownBy(() -> orderService.payOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_BALANCE.getCode());
        }

        @Test
        @DisplayName("should_throw_balance_deduction_failed_when_optimistic_lock_fails")
        void shouldThrowBalanceDeductionFailed_whenPayOptimisticLockFails() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, buyerId, storeId, price1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(0);

            assertThatThrownBy(() -> orderService.payOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.BALANCE_DEDUCTION_FAILED.getCode());
        }
    }

    // ==================== cancelOrder ====================

    @Nested
    @DisplayName("cancelOrder — 取消订单")
    class CancelOrderTests {

        @Test
        @DisplayName("should_cancel_order_successfully_when_status_is_pending_pay")
        void shouldCancelOrderSuccessfully_whenStatusIsPendingPay() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, buyerId, storeId, price1);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer1");
            buyer.setBalance(BigDecimal.valueOf(5000));
            FmOrderItem orderItem = TestDataFactory.createOrderItem(1L, 1L, productId1, 2, price1);

            when(orderMapper.selectById(1L)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderItemMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(orderItem));
            when(productMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(orderMapper.updateById(any(FmOrder.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);

            orderService.cancelOrder(1L, buyerId);

            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.CANCELLED.getCode());
            assertThat(order.getCancelTime()).isNotNull();
            assertThat(order.getCancelReason()).contains("Cancelled by user");
            // stock restored
            verify(productMapper).update(isNull(), any(LambdaUpdateWrapper.class));
            // refund log created
            verify(balanceLogMapper).insert(any(FmBalanceLog.class));
        }

        @Test
        @DisplayName("should_throw_wrong_order_status_when_cancelling_paid_order")
        void shouldThrowWrongOrderStatus_whenCancellingPaidOrder() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_ORDER_STATUS.getCode());
        }

        @Test
        @DisplayName("should_throw_forbidden_when_order_belongs_to_different_user")
        void shouldThrowForbidden_whenCancelOthersOrder() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, 999L, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.cancelOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }
    }

    // ==================== receiveOrder ====================

    @Nested
    @DisplayName("receiveOrder — 确认收货")
    class ReceiveOrderTests {

        @Test
        @DisplayName("should_receive_order_successfully_when_status_is_shipped")
        void shouldReceiveOrderSuccessfully_whenStatusIsShipped() {
            FmOrder order = TestDataFactory.createShippedOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(FmOrder.class))).thenReturn(1);

            orderService.receiveOrder(1L, buyerId);

            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.COMPLETED.getCode());
            assertThat(order.getReceiveTime()).isNotNull();
            assertThat(order.getCompleteTime()).isNotNull();
        }

        @Test
        @DisplayName("should_throw_wrong_order_status_when_order_is_not_shipped")
        void shouldThrowWrongOrderStatus_whenOrderIsNotShipped() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.receiveOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_ORDER_STATUS.getCode());
        }

        @Test
        @DisplayName("should_throw_forbidden_when_order_belongs_to_different_user")
        void shouldThrowForbidden_whenReceiveOthersOrder() {
            FmOrder order = TestDataFactory.createShippedOrder(1L, 999L, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.receiveOrder(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }
    }

    // ==================== listStoreOrders ====================

    @Nested
    @DisplayName("listStoreOrders — 卖家订单列表")
    class ListStoreOrdersTests {

        @Test
        @DisplayName("should_return_store_orders_with_store_name")
        void shouldReturnStoreOrdersWithStoreName() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(List.of(order));
            mpPage.setTotal(1);
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            PageResult<OrderVO> result = orderService.listStoreOrders(storeId, 1, 10, null);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords().get(0).getStoreName()).isEqualTo("Test Store 10");
        }

        @Test
        @DisplayName("should_filter_by_status_when_filtering_store_orders")
        void shouldFilterByStatus_whenFilteringStoreOrders() {
            Page<FmOrder> mpPage = new Page<>(1, 10);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            orderService.listStoreOrders(storeId, 1, 10, OrderStatusEnum.SHIPPED.getCode());

            verify(orderMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    // ==================== shipOrder ====================

    @Nested
    @DisplayName("shipOrder — 卖家发货")
    class ShipOrderTests {

        @Test
        @DisplayName("should_ship_order_successfully_when_status_is_paid")
        void shouldShipOrderSuccessfully_whenStatusIsPaid() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);
            when(orderMapper.updateById(any(FmOrder.class))).thenReturn(1);

            orderService.shipOrder(1L, storeId);

            assertThat(order.getStatus()).isEqualTo(OrderStatusEnum.SHIPPED.getCode());
            assertThat(order.getShipTime()).isNotNull();
        }

        @Test
        @DisplayName("should_throw_forbidden_when_order_belongs_to_different_store")
        void shouldThrowForbidden_whenOrderBelongsToDifferentStore() {
            FmOrder order = TestDataFactory.createPaidOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.shipOrder(1L, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_wrong_order_status_when_shipping_non_paid_order")
        void shouldThrowWrongOrderStatus_whenShippingNonPaidOrder() {
            FmOrder order = TestDataFactory.createPendingPayOrder(1L, buyerId, storeId, price1);
            when(orderMapper.selectById(1L)).thenReturn(order);

            assertThatThrownBy(() -> orderService.shipOrder(1L, storeId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_ORDER_STATUS.getCode());
        }
    }
}
