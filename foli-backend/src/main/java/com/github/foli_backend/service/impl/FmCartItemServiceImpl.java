package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.CartAddRequest;
import com.github.foli_backend.dto.request.CartUpdateRequest;
import com.github.foli_backend.dto.response.CartItemVO;
import com.github.foli_backend.entity.FmCartItem;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.mapper.FmCartItemMapper;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.service.FmCartItemService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务实现 / Cart service implementation
 */
@Service
public class FmCartItemServiceImpl implements FmCartItemService {

    private final FmCartItemMapper cartItemMapper;
    private final FmProductMapper productMapper;

    public FmCartItemServiceImpl(FmCartItemMapper cartItemMapper, FmProductMapper productMapper) {
        this.cartItemMapper = cartItemMapper;
        this.productMapper = productMapper;
    }

    @Override
    public List<CartItemVO> getCart(Long userId) {
        List<FmCartItem> items = cartItemMapper.selectList(
                new LambdaQueryWrapper<FmCartItem>()
                        .eq(FmCartItem::getUserId, userId)
                        .orderByDesc(FmCartItem::getCreateTime)
        );

        List<CartItemVO> result = new ArrayList<>();
        for (FmCartItem item : items) {
            FmProduct product = productMapper.selectById(item.getProductId());
            result.add(CartItemVO.fromEntity(item, product));
        }
        return result;
    }

    @Override
    public void addToCart(Long userId, CartAddRequest req) {
        // 验证商品是否存在且状态为已审核通过且有库存 validate product exists, status=APPROVED and stock>0
        FmProduct product = productMapper.selectById(req.getProductId());
        if (product == null) {
            BizCodeEnum.PRODUCT_NOT_FOUND.throwEx();
        }
        if (product.getStatus() == null || product.getStatus() != ProductStatusEnum.APPROVED.getCode()) {
            BizCodeEnum.PRODUCT_OFF_SHELF.throwEx();
        }
        if (product.getStock() == null || product.getStock() <= 0) {
            BizCodeEnum.INSUFFICIENT_STOCK.throwEx();
        }

        // 查询是否已有相同商品在购物车中 check if same product already in cart
        FmCartItem existing = cartItemMapper.selectOne(
                new LambdaQueryWrapper<FmCartItem>()
                        .eq(FmCartItem::getUserId, userId)
                        .eq(FmCartItem::getProductId, req.getProductId())
        );

        if (existing != null) {
            // 累加数量 increment quantity
            int quantity = req.getQuantity() != null ? req.getQuantity() : 1;
            existing.setQuantity(existing.getQuantity() + quantity);
            cartItemMapper.updateById(existing);
        } else {
            // 新增购物车项 insert new cart item
            FmCartItem cartItem = new FmCartItem();
            cartItem.setUserId(userId);
            cartItem.setProductId(req.getProductId());
            cartItem.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1);
            cartItem.setSelected(1);
            cartItemMapper.insert(cartItem);
        }
    }

    @Override
    public void updateCartItem(Long cartItemId, Long userId, CartUpdateRequest req) {
        FmCartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            BizCodeEnum.CART_ITEM_NOT_FOUND.throwEx();
        }

        if (req.getQuantity() != null) {
            item.setQuantity(req.getQuantity());
        }
        if (req.getSelected() != null) {
            item.setSelected(req.getSelected());
        }
        cartItemMapper.updateById(item);
    }

    @Override
    public void removeCartItem(Long cartItemId, Long userId) {
        FmCartItem item = cartItemMapper.selectById(cartItemId);
        if (item == null || !item.getUserId().equals(userId)) {
            BizCodeEnum.CART_ITEM_NOT_FOUND.throwEx();
        }
        cartItemMapper.deleteById(cartItemId);
    }

    @Override
    public void clearCart(Long userId) {
        // 逻辑删除用户的全部购物车项 logical delete all cart items for user
        cartItemMapper.delete(
                new LambdaUpdateWrapper<FmCartItem>()
                        .eq(FmCartItem::getUserId, userId)
        );
    }
}
