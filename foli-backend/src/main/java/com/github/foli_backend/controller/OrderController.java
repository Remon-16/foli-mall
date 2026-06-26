package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.OrderCreateRequest;
import com.github.foli_backend.dto.response.OrderVO;
import com.github.foli_backend.service.FmOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器 / Order controller
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "订单管理 Orders")
@RequireLogin
public class OrderController {

    private final FmOrderService orderService;

    public OrderController(FmOrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建订单 Create order from selected cart items
     */
    @PostMapping
    @Operation(summary = "创建订单 Create order (split by store)")
    public Result<List<OrderVO>> createOrder(@Valid @RequestBody OrderCreateRequest req) {
        Long userId = UserContext.getUserId();
        List<OrderVO> voList = orderService.createOrder(userId, req);
        return Result.success(voList);
    }

    /**
     * 买家订单列表 Buyer order list
     */
    @GetMapping
    @Operation(summary = "买家订单列表 Buyer order list")
    public Result<PageResult<OrderVO>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status) {
        Long userId = UserContext.getUserId();
        PageResult<OrderVO> result = orderService.listBuyerOrders(userId, page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 获取订单详情 Get order detail with items
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情 Get order detail")
    public Result<OrderVO> getOrderDetail(@PathVariable Long id) {
        OrderVO vo = orderService.getOrderDetail(id);
        return Result.success(vo);
    }

    /**
     * 支付订单 Pay order
     */
    @PutMapping("/{id}/pay")
    @Operation(summary = "支付订单 Pay order")
    public Result<Void> payOrder(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        orderService.payOrder(id, userId);
        return Result.success();
    }

    /**
     * 取消订单 Cancel order
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单 Cancel order")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        orderService.cancelOrder(id, userId);
        return Result.success();
    }

    /**
     * 确认收货 Receive order
     */
    @PutMapping("/{id}/receive")
    @Operation(summary = "确认收货 Receive order")
    public Result<Void> receiveOrder(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        orderService.receiveOrder(id, userId);
        return Result.success();
    }
}
