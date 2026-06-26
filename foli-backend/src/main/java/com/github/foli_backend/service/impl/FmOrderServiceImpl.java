package com.github.foli_backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BalanceLogTypeEnum;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.OrderStatusEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.OrderCreateRequest;
import com.github.foli_backend.dto.response.OrderItemVO;
import com.github.foli_backend.dto.response.OrderVO;
import com.github.foli_backend.entity.*;
import com.github.foli_backend.mapper.*;
import com.github.foli_backend.service.FmOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FmOrderServiceImpl implements FmOrderService {

    private final FmOrderMapper orderMapper;
    private final FmOrderItemMapper orderItemMapper;
    private final FmCartItemMapper cartItemMapper;
    private final FmProductMapper productMapper;
    private final FmUserMapper userMapper;
    private final FmStoreMapper storeMapper;
    private final FmBalanceLogMapper balanceLogMapper;

    public FmOrderServiceImpl(FmOrderMapper orderMapper,
                              FmOrderItemMapper orderItemMapper,
                              FmCartItemMapper cartItemMapper,
                              FmProductMapper productMapper,
                              FmUserMapper userMapper,
                              FmStoreMapper storeMapper,
                              FmBalanceLogMapper balanceLogMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
        this.userMapper = userMapper;
        this.storeMapper = storeMapper;
        this.balanceLogMapper = balanceLogMapper;
    }

    private String generateOrderNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "FO" + date + RandomUtil.randomNumbers(6);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<OrderVO> createOrder(Long userId, OrderCreateRequest req) {
        List<FmCartItem> selectedItems = cartItemMapper.selectList(
                new LambdaQueryWrapper<FmCartItem>()
                        .eq(FmCartItem::getUserId, userId)
                        .eq(FmCartItem::getSelected, 1)
        );

        if (selectedItems == null || selectedItems.isEmpty()) {
            BizCodeEnum.CART_EMPTY.throwEx();
        }

        // Phase 1: 验证商品并分组 / Validate products and group by store
        Map<Long, List<FmCartItem>> groupedByStore = new LinkedHashMap<>();
        Map<Long, FmProduct> productCache = new HashMap<>();
        BigDecimal grandTotal = BigDecimal.ZERO;

        for (FmCartItem cartItem : selectedItems) {
            FmProduct product = productMapper.selectById(cartItem.getProductId());
            if (product == null) {
                BizCodeEnum.PRODUCT_NOT_FOUND.throwEx(": " + cartItem.getProductId());
            }
            if (product.getStatus() == null || product.getStatus() != ProductStatusEnum.APPROVED.getCode()) {
                BizCodeEnum.PRODUCT_OFF_SHELF.throwEx(": " + product.getName());
            }
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                BizCodeEnum.INSUFFICIENT_STOCK.throwEx(": " + product.getName());
            }

            productCache.put(cartItem.getProductId(), product);
            groupedByStore.computeIfAbsent(product.getStoreId(), k -> new ArrayList<>()).add(cartItem);

            grandTotal = grandTotal.add(
                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }

        // Phase 2: 原子扣减库存 / Atomic stock deduction
        for (FmCartItem cartItem : selectedItems) {
            int affected = productMapper.update(null,
                    new LambdaUpdateWrapper<FmProduct>()
                            .setSql("stock = stock - " + cartItem.getQuantity())
                            .ge(FmProduct::getStock, cartItem.getQuantity())
                            .eq(FmProduct::getId, cartItem.getProductId())
            );
            if (affected == 0) {
                BizCodeEnum.STOCK_DEDUCTION_FAILED.throwEx();
            }
        }

        // Phase 3: 原子扣减余额（一次性扣总金额）/ Atomic balance deduction (once for grand total)
        FmUser buyer = userMapper.selectById(userId);
        if (buyer.getBalance() == null || buyer.getBalance().compareTo(grandTotal) < 0) {
            BizCodeEnum.INSUFFICIENT_BALANCE.throwEx();
        }

        BigDecimal beforeBalance = buyer.getBalance();
        int balanceAffected = userMapper.update(null,
                new LambdaUpdateWrapper<FmUser>()
                        .setSql("balance = balance - " + grandTotal)
                        .ge(FmUser::getBalance, grandTotal)
                        .eq(FmUser::getId, userId)
        );
        if (balanceAffected == 0) {
            BizCodeEnum.BALANCE_DEDUCTION_FAILED.throwEx();
        }

        // Phase 4: 按店铺创建订单 / Create orders per store
        BigDecimal runningBalance = beforeBalance;
        List<OrderVO> resultList = new ArrayList<>();

        for (Map.Entry<Long, List<FmCartItem>> entry : groupedByStore.entrySet()) {
            Long storeId = entry.getKey();
            List<FmCartItem> storeItems = entry.getValue();

            BigDecimal storeSubtotal = BigDecimal.ZERO;
            for (FmCartItem cartItem : storeItems) {
                FmProduct product = productCache.get(cartItem.getProductId());
                storeSubtotal = storeSubtotal.add(
                        product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                );
            }

            FmOrder order = new FmOrder();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setStoreId(storeId);
            order.setTotalAmount(storeSubtotal);
            order.setStatus(OrderStatusEnum.PAID.getCode());
            order.setReceiverName(req.getReceiverName());
            order.setReceiverPhone(req.getReceiverPhone());
            order.setReceiverAddress(req.getReceiverAddress());
            order.setPayTime(LocalDateTime.now());
            orderMapper.insert(order);

            for (FmCartItem cartItem : storeItems) {
                FmProduct product = productCache.get(cartItem.getProductId());

                FmOrderItem orderItem = new FmOrderItem();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductName(product.getName());
                orderItem.setProductImage(product.getMainImage());
                orderItem.setPrice(product.getPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItemMapper.insert(orderItem);

                productMapper.update(null,
                        new LambdaUpdateWrapper<FmProduct>()
                                .setSql("sales_count = sales_count + " + cartItem.getQuantity())
                                .eq(FmProduct::getId, cartItem.getProductId())
                );
            }

            BigDecimal afterBalance = runningBalance.subtract(storeSubtotal);
            FmBalanceLog balanceLog = new FmBalanceLog();
            balanceLog.setUserId(userId);
            balanceLog.setAmount(storeSubtotal.negate());
            balanceLog.setType(BalanceLogTypeEnum.PAY.name());
            balanceLog.setOrderNo(order.getOrderNo());
            balanceLog.setBeforeBalance(runningBalance);
            balanceLog.setAfterBalance(afterBalance);
            balanceLog.setRemark("订单支付 Order payment: " + order.getOrderNo());
            balanceLogMapper.insert(balanceLog);

            runningBalance = afterBalance;

            OrderVO vo = OrderVO.fromEntity(order);
            FmStore store = storeMapper.selectById(storeId);
            vo.setStoreName(store != null ? store.getStoreName() : null);
            resultList.add(vo);
        }

        // Phase 5: 清空已选中购物车项 / Clear selected cart items
        cartItemMapper.delete(
                new LambdaUpdateWrapper<FmCartItem>()
                        .eq(FmCartItem::getUserId, userId)
                        .eq(FmCartItem::getSelected, 1)
        );

        return resultList;
    }

    @Override
    public PageResult<OrderVO> listBuyerOrders(Long userId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmOrder> wrapper = new LambdaQueryWrapper<FmOrder>()
                .eq(FmOrder::getUserId, userId)
                .orderByDesc(FmOrder::getCreateTime);

        if (status != null) {
            wrapper.eq(FmOrder::getStatus, status);
        }

        Page<FmOrder> mpPage = orderMapper.selectPage(Page.of(page, pageSize), wrapper);

        // 批量查询所有订单项
        List<Long> orderIds = mpPage.getRecords().stream()
                .map(FmOrder::getId)
                .collect(Collectors.toList());

        Map<Long, List<OrderItemVO>> itemsMap = Map.of();
        if (!orderIds.isEmpty()) {
            List<FmOrderItem> allItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<FmOrderItem>()
                            .in(FmOrderItem::getOrderId, orderIds));
            itemsMap = allItems.stream()
                    .collect(Collectors.groupingBy(
                            FmOrderItem::getOrderId,
                            Collectors.mapping(OrderItemVO::fromEntity, Collectors.toList())
                    ));
        }

        // 批量查询所有店铺
        List<Long> storeIds = mpPage.getRecords().stream()
                .map(FmOrder::getStoreId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> storeNameMap = Map.of();
        if (!storeIds.isEmpty()) {
            storeNameMap = storeMapper.selectList(
                    new LambdaQueryWrapper<FmStore>()
                            .in(FmStore::getId, storeIds))
                    .stream()
                    .collect(Collectors.toMap(FmStore::getId, FmStore::getStoreName));
        }

        final Map<Long, List<OrderItemVO>> finalItemsMap = itemsMap;
        final Map<Long, String> finalStoreNameMap = storeNameMap;
        List<OrderVO> voList = mpPage.getRecords().stream()
                .map(order -> {
                    OrderVO vo = OrderVO.fromEntity(order);
                    vo.setItems(finalItemsMap.getOrDefault(order.getId(), List.of()));
                    vo.setStoreName(finalStoreNameMap.getOrDefault(order.getStoreId(), null));
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(),
                (int) mpPage.getSize(), voList);
    }

    @Override
    public OrderVO getOrderDetail(Long orderId) {
        FmOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }

        OrderVO vo = OrderVO.fromEntity(order);

        List<FmOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<FmOrderItem>()
                        .eq(FmOrderItem::getOrderId, orderId)
        );
        List<OrderItemVO> itemVOs = orderItems.stream()
                .map(OrderItemVO::fromEntity)
                .collect(Collectors.toList());
        vo.setItems(itemVOs);

        FmStore store = storeMapper.selectById(order.getStoreId());
        vo.setStoreName(store != null ? store.getStoreName() : null);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId) {
        FmOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }
        if (!order.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (order.getStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            BizCodeEnum.WRONG_ORDER_STATUS.throwEx();
        }

        FmUser buyer = userMapper.selectById(userId);
        if (buyer.getBalance() == null || buyer.getBalance().compareTo(order.getTotalAmount()) < 0) {
            BizCodeEnum.INSUFFICIENT_BALANCE.throwEx();
        }

        BigDecimal beforeBalance = buyer.getBalance();
        int balanceAffected = userMapper.update(null,
                new LambdaUpdateWrapper<FmUser>()
                        .setSql("balance = balance - " + order.getTotalAmount())
                        .apply("balance >= {0}", order.getTotalAmount())
                        .eq(FmUser::getId, userId)
        );
        if (balanceAffected == 0) {
            BizCodeEnum.BALANCE_DEDUCTION_FAILED.throwEx();
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        BigDecimal afterBalance = beforeBalance.subtract(order.getTotalAmount());
        FmBalanceLog balanceLog = new FmBalanceLog();
        balanceLog.setUserId(userId);
        balanceLog.setAmount(order.getTotalAmount().negate());
        balanceLog.setType(BalanceLogTypeEnum.PAY.name());
        balanceLog.setOrderNo(order.getOrderNo());
        balanceLog.setBeforeBalance(beforeBalance);
        balanceLog.setAfterBalance(afterBalance);
        balanceLog.setRemark("订单支付 Order payment: " + order.getOrderNo());
        balanceLogMapper.insert(balanceLog);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        FmOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }
        if (!order.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (order.getStatus() != OrderStatusEnum.PENDING_PAY.getCode()) {
            BizCodeEnum.WRONG_ORDER_STATUS.throwEx();
        }

        FmUser buyer = userMapper.selectById(userId);
        BigDecimal beforeBalance = buyer.getBalance();

        userMapper.update(null,
                new LambdaUpdateWrapper<FmUser>()
                        .setSql("balance = balance + " + order.getTotalAmount())
                        .eq(FmUser::getId, userId)
        );
        BigDecimal afterBalance = beforeBalance.add(order.getTotalAmount());

        // 恢复库存
        List<FmOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<FmOrderItem>()
                        .eq(FmOrderItem::getOrderId, orderId)
        );
        for (FmOrderItem item : orderItems) {
            productMapper.update(null,
                    new LambdaUpdateWrapper<FmProduct>()
                            .setSql("stock = stock + " + item.getQuantity())
                            .eq(FmProduct::getId, item.getProductId())
            );
        }

        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason("用户主动取消 Cancelled by user");
        orderMapper.updateById(order);

        FmBalanceLog balanceLog = new FmBalanceLog();
        balanceLog.setUserId(userId);
        balanceLog.setAmount(order.getTotalAmount());
        balanceLog.setType(BalanceLogTypeEnum.REFUND.name());
        balanceLog.setOrderNo(order.getOrderNo());
        balanceLog.setBeforeBalance(beforeBalance);
        balanceLog.setAfterBalance(afterBalance);
        balanceLog.setRemark("订单取消退款 Order cancelled refund: " + order.getOrderNo());
        balanceLogMapper.insert(balanceLog);
    }

    @Override
    public void receiveOrder(Long orderId, Long userId) {
        FmOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }
        if (!order.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (order.getStatus() != OrderStatusEnum.SHIPPED.getCode()) {
            BizCodeEnum.WRONG_ORDER_STATUS.throwEx();
        }

        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setReceiveTime(LocalDateTime.now());
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    @Override
    public PageResult<OrderVO> listStoreOrders(Long storeId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmOrder> wrapper = new LambdaQueryWrapper<FmOrder>()
                .eq(FmOrder::getStoreId, storeId)
                .orderByDesc(FmOrder::getCreateTime);

        if (status != null) {
            wrapper.eq(FmOrder::getStatus, status);
        }

        Page<FmOrder> mpPage = orderMapper.selectPage(Page.of(page, pageSize), wrapper);

        String storeName = null;
        FmStore store = storeMapper.selectById(storeId);
        if (store != null) {
            storeName = store.getStoreName();
        }

        final String finalStoreName = storeName;
        List<OrderVO> voList = mpPage.getRecords().stream()
                .map(order -> {
                    OrderVO vo = OrderVO.fromEntity(order);
                    vo.setStoreName(finalStoreName);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(),
                (int) mpPage.getSize(), voList);
    }

    @Override
    public void shipOrder(Long orderId, Long storeId) {
        FmOrder order = orderMapper.selectById(orderId);
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }
        if (!order.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (order.getStatus() != OrderStatusEnum.PAID.getCode()) {
            BizCodeEnum.WRONG_ORDER_STATUS.throwEx();
        }

        order.setStatus(OrderStatusEnum.SHIPPED.getCode());
        order.setShipTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }
}
