package com.github.foli_backend.service;

import com.github.foli_backend.dto.request.CartAddRequest;
import com.github.foli_backend.dto.request.CartUpdateRequest;
import com.github.foli_backend.dto.response.CartItemVO;

import java.util.List;

/**
 * 购物车服务接口 / Cart service interface
 */
public interface FmCartItemService {

    /**
     * 获取用户购物车列表 包含商品信息
     * Get all cart items for user, including product info
     *
     * @param userId 用户ID user ID
     * @return 购物车项列表 cart item list
     */
    List<CartItemVO> getCart(Long userId);

    /**
     * 添加商品到购物车 若同一userId+productId已存在则累加数量
     * Add product to cart, if same userId+productId exists, increment quantity
     *
     * @param userId 用户ID user ID
     * @param req    请求参数 request
     */
    void addToCart(Long userId, CartAddRequest req);

    /**
     * 更新购物车项 修改数量和/或选中状态 验证归属权
     * Update cart item quantity and/or selected, verify ownership
     *
     * @param cartItemId 购物车项ID cart item ID
     * @param userId     用户ID user ID
     * @param req        请求参数 request
     */
    void updateCartItem(Long cartItemId, Long userId, CartUpdateRequest req);

    /**
     * 移除购物车项 逻辑删除 验证归属权
     * Remove cart item (logical delete), verify ownership
     *
     * @param cartItemId 购物车项ID cart item ID
     * @param userId     用户ID user ID
     */
    void removeCartItem(Long cartItemId, Long userId);

    /**
     * 清空购物车 逻辑删除用户全部购物车项
     * Clear all cart items for user (logical delete)
     *
     * @param userId 用户ID user ID
     */
    void clearCart(Long userId);
}
