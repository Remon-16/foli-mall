package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.CartAddRequest;
import com.github.foli_backend.dto.request.CartUpdateRequest;
import com.github.foli_backend.dto.response.CartItemVO;
import com.github.foli_backend.entity.FmCartItem;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmCartItemMapper;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.service.impl.FmCartItemServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmCartItemServiceImpl 单元测试")
class FmCartItemServiceImplTest {

    @Mock FmCartItemMapper cartItemMapper;
    @Mock FmProductMapper productMapper;

    @InjectMocks
    FmCartItemServiceImpl cartService;

    Long userId = 1L;
    Long productId = 100L;
    Long storeId = 10L;

    // ==================== getCart ====================

    @Nested
    @DisplayName("getCart — 获取购物车")
    class GetCartTests {

        @Test
        @DisplayName("should_get_cart_with_product_info_when_user_has_items")
        void shouldGetCartWithProductInfo_whenUserHasItems() {
            FmCartItem cartItem = TestDataFactory.createSelectedCartItem(1L, userId, productId, 2);
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.valueOf(99));

            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(cartItem));
            when(productMapper.selectById(productId)).thenReturn(product);

            List<CartItemVO> result = cartService.getCart(userId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getQuantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("should_return_empty_list_when_cart_is_empty")
        void shouldReturnEmptyList_whenCartIsEmpty() {
            when(cartItemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<CartItemVO> result = cartService.getCart(userId);

            assertThat(result).isEmpty();
        }
    }

    // ==================== addToCart ====================

    @Nested
    @DisplayName("addToCart — 添加到购物车")
    class AddToCartTests {

        @Test
        @DisplayName("should_add_new_item_when_product_not_in_cart")
        void shouldAddNewItem_whenProductNotInCart() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.valueOf(99));
            when(productMapper.selectById(productId)).thenReturn(product);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            doReturn(1).when(cartItemMapper).insert(any(FmCartItem.class));

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);
            req.setQuantity(1);

            cartService.addToCart(userId, req);

            verify(cartItemMapper).insert(any(FmCartItem.class));
        }

        @Test
        @DisplayName("should_increment_quantity_when_product_already_in_cart")
        void shouldIncrementQuantity_whenProductAlreadyInCart() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.valueOf(99));
            FmCartItem existing = TestDataFactory.createSelectedCartItem(1L, userId, productId, 3);

            when(productMapper.selectById(productId)).thenReturn(product);
            when(cartItemMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);
            when(cartItemMapper.updateById(any(FmCartItem.class))).thenReturn(1);

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);
            req.setQuantity(2);

            cartService.addToCart(userId, req);

            assertThat(existing.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("should_throw_product_not_found_when_adding_invalid_product")
        void shouldThrowProductNotFound_whenAddingInvalidProduct() {
            when(productMapper.selectById(productId)).thenReturn(null);

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);

            assertThatThrownBy(() -> cartService.addToCart(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_product_off_shelf_when_product_not_approved")
        void shouldThrowProductOffShelf_whenProductNotApproved() {
            FmProduct product = TestDataFactory.createProduct(productId, storeId,
                    ProductStatusEnum.DRAFT.getCode(), 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);

            assertThatThrownBy(() -> cartService.addToCart(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_OFF_SHELF.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_stock_when_stock_is_zero")
        void shouldThrowInsufficientStock_whenStockIsZero() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 0, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);

            assertThatThrownBy(() -> cartService.addToCart(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_STOCK.getCode());
        }

        @Test
        @DisplayName("should_throw_insufficient_stock_when_stock_is_null")
        void shouldThrowInsufficientStock_whenStockIsNull() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            product.setStock(null);
            when(productMapper.selectById(productId)).thenReturn(product);

            CartAddRequest req = new CartAddRequest();
            req.setProductId(productId);

            assertThatThrownBy(() -> cartService.addToCart(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INSUFFICIENT_STOCK.getCode());
        }
    }

    // ==================== updateCartItem ====================

    @Nested
    @DisplayName("updateCartItem — 更新购物车")
    class UpdateCartItemTests {

        @Test
        @DisplayName("should_update_quantity_and_selected_when_user_owns_item")
        void shouldUpdateQuantityAndSelected_whenUserOwnsItem() {
            FmCartItem item = TestDataFactory.createSelectedCartItem(1L, userId, productId, 1);
            when(cartItemMapper.selectById(1L)).thenReturn(item);
            when(cartItemMapper.updateById(any(FmCartItem.class))).thenReturn(1);

            CartUpdateRequest req = new CartUpdateRequest();
            req.setQuantity(5);
            req.setSelected(0);

            cartService.updateCartItem(1L, userId, req);

            assertThat(item.getQuantity()).isEqualTo(5);
            assertThat(item.getSelected()).isEqualTo(0);
        }

        @Test
        @DisplayName("should_throw_cart_item_not_found_when_item_does_not_belong_to_user")
        void shouldThrowCartItemNotFound_whenItemDoesNotBelongToUser() {
            FmCartItem item = TestDataFactory.createSelectedCartItem(1L, 999L, productId, 1);
            when(cartItemMapper.selectById(1L)).thenReturn(item);

            CartUpdateRequest req = new CartUpdateRequest();

            assertThatThrownBy(() -> cartService.updateCartItem(1L, userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CART_ITEM_NOT_FOUND.getCode());
        }
    }

    // ==================== removeCartItem ====================

    @Nested
    @DisplayName("removeCartItem — 删除购物车项")
    class RemoveCartItemTests {

        @Test
        @DisplayName("should_remove_cart_item_when_user_owns_item")
        void shouldRemoveCartItem_whenUserOwnsItem() {
            FmCartItem item = TestDataFactory.createSelectedCartItem(1L, userId, productId, 1);
            when(cartItemMapper.selectById(1L)).thenReturn(item);
            when(cartItemMapper.deleteById(1L)).thenReturn(1);

            cartService.removeCartItem(1L, userId);

            verify(cartItemMapper).deleteById(1L);
        }

        @Test
        @DisplayName("should_throw_cart_item_not_found_when_deleting_others_item")
        void shouldThrowCartItemNotFound_whenDeletingOthersItem() {
            FmCartItem item = TestDataFactory.createSelectedCartItem(1L, 999L, productId, 1);
            when(cartItemMapper.selectById(1L)).thenReturn(item);

            assertThatThrownBy(() -> cartService.removeCartItem(1L, userId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.CART_ITEM_NOT_FOUND.getCode());
        }
    }

    // ==================== clearCart ====================

    @Nested
    @DisplayName("clearCart — 清空购物车")
    class ClearCartTests {

        @Test
        @DisplayName("should_clear_cart_when_user_has_items")
        void shouldClearCart_whenUserHasItems() {
            doReturn(3).when(cartItemMapper).delete(any());

            cartService.clearCart(userId);
        }
    }
}
