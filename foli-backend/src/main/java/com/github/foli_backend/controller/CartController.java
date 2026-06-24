package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.CartAddRequest;
import com.github.foli_backend.dto.request.CartUpdateRequest;
import com.github.foli_backend.dto.response.CartItemVO;
import com.github.foli_backend.service.FmCartItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车控制器 / Cart controller
 */
@RestController
@RequestMapping("/api/cart")
@Tag(name = "购物车 Cart")
@RequireLogin
public class CartController {

    private final FmCartItemService cartItemService;

    public CartController(FmCartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    /**
     * 获取购物车列表 Get cart list
     */
    @GetMapping
    @Operation(summary = "获取购物车列表 Get cart list")
    public Result<List<CartItemVO>> getCart() {
        Long userId = UserContext.getUserId();
        List<CartItemVO> cart = cartItemService.getCart(userId);
        return Result.success(cart);
    }

    /**
     * 添加商品到购物车 Add to cart
     */
    @PostMapping
    @Operation(summary = "添加商品到购物车 Add to cart")
    public Result<Void> addToCart(@Valid @RequestBody CartAddRequest req) {
        Long userId = UserContext.getUserId();
        cartItemService.addToCart(userId, req);
        return Result.success();
    }

    /**
     * 更新购物车项 Update cart item
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新购物车项 Update cart item")
    public Result<Void> updateCartItem(@PathVariable Long id, @RequestBody CartUpdateRequest req) {
        Long userId = UserContext.getUserId();
        cartItemService.updateCartItem(id, userId, req);
        return Result.success();
    }

    /**
     * 删除购物车项 Remove cart item
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除购物车项 Remove cart item")
    public Result<Void> removeCartItem(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        cartItemService.removeCartItem(id, userId);
        return Result.success();
    }

    /**
     * 清空购物车 Clear all cart items
     */
    @DeleteMapping
    @Operation(summary = "清空购物车 Clear cart")
    public Result<Void> clearCart() {
        Long userId = UserContext.getUserId();
        cartItemService.clearCart(userId);
        return Result.success();
    }
}
