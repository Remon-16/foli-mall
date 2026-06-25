package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.dto.request.ProductPublishRequest;
import com.github.foli_backend.dto.request.ProductReviewRequest;
import com.github.foli_backend.dto.request.ProductSearchRequest;
import com.github.foli_backend.dto.response.ProductDetailVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmProductCategory;
import com.github.foli_backend.entity.FmProductImage;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.*;
import com.github.foli_backend.service.impl.FmProductServiceImpl;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmProductServiceImpl 单元测试")
class FmProductServiceImplTest {

    @Mock FmProductMapper productMapper;
    @Mock FmProductImageMapper productImageMapper;
    @Mock FmStoreMapper storeMapper;
    @Mock FmProductCategoryMapper categoryMapper;

    @InjectMocks
    FmProductServiceImpl productService;

    Long storeId = 10L;
    Long productId = 100L;

    // ==================== searchProducts ====================

    @Nested
    @DisplayName("searchProducts — 搜索商品")
    class SearchProductsTests {

        @Test
        @DisplayName("should_search_by_keyword")
        void shouldSearchByKeyword() {
            Page<FmProduct> mpPage = new Page<>(1, 20);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);

            ProductSearchRequest req = new ProductSearchRequest();
            req.setKeyword("phone");
            req.setPage(1);
            req.setPageSize(20);

            PageResult<ProductVO> result = productService.searchProducts(req);

            assertThat(result.getRecords()).isEmpty();
            verify(productMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("should_search_by_category_and_sort_by_price_asc")
        void shouldSearchByCategoryAndSortByPriceAsc() {
            Page<FmProduct> mpPage = new Page<>(1, 20);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(categoryMapper.selectList(any())).thenReturn(Collections.emptyList());
            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);

            ProductSearchRequest req = new ProductSearchRequest();
            req.setCategoryId(1L);
            req.setSortBy("price_asc");
            req.setPage(1);
            req.setPageSize(20);

            PageResult<ProductVO> result = productService.searchProducts(req);

            assertThat(result.getRecords()).isEmpty();
        }

        @Test
        @DisplayName("should_search_with_price_range")
        void shouldSearchWithPriceRange() {
            Page<FmProduct> mpPage = new Page<>(1, 20);
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.valueOf(99));
            mpPage.setRecords(List.of(product));
            mpPage.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);
            when(storeMapper.selectBatchIds(anyCollection())).thenReturn(Collections.emptyList());

            ProductSearchRequest req = new ProductSearchRequest();
            req.setMinPrice(BigDecimal.valueOf(50));
            req.setMaxPrice(BigDecimal.valueOf(150));

            PageResult<ProductVO> result = productService.searchProducts(req);

            assertThat(result.getTotal()).isEqualTo(1);
        }
    }

    // ==================== getProductDetail ====================

    @Nested
    @DisplayName("getProductDetail — 商品详情")
    class GetProductDetailTests {

        @Test
        @DisplayName("should_return_product_detail_with_images")
        void shouldReturnProductDetailWithImages() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.valueOf(99));
            FmStore store = TestDataFactory.createApprovedStore(storeId, 1L);
            FmProductCategory category = new FmProductCategory();
            category.setId(1L);
            category.setName("Electronics");
            FmProductImage image = new FmProductImage();
            image.setId(1L);
            image.setProductId(productId);
            image.setImageUrl("/images/test.jpg");

            when(productMapper.selectById(productId)).thenReturn(product);
            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(categoryMapper.selectById(1L)).thenReturn(category);
            when(productImageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(image));

            ProductDetailVO result = productService.getProductDetail(productId);

            assertThat(result.getName()).isEqualTo("Test Product 100");
            assertThat(result.getStoreName()).isEqualTo("Test Store 10");
            assertThat(result.getCategoryName()).isEqualTo("Electronics");
            assertThat(result.getImages()).hasSize(1).contains("/images/test.jpg");
        }

        @Test
        @DisplayName("should_throw_product_not_found_when_get_detail_with_invalid_id")
        void shouldThrowProductNotFound_whenGetDetailWithInvalidId() {
            when(productMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> productService.getProductDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_NOT_FOUND.getCode());
        }
    }

    // ==================== publishProduct ====================

    @Nested
    @DisplayName("publishProduct — 发布商品")
    class PublishProductTests {

        @Test
        @DisplayName("should_publish_product_successfully_with_images")
        void shouldPublishProductSuccessfullyWithImages() {
            when(productMapper.insert(any(FmProduct.class))).thenReturn(1);
            when(productImageMapper.insert(any(FmProductImage.class))).thenReturn(1);

            ProductPublishRequest req = new ProductPublishRequest();
            req.setName("New Product");
            req.setCategoryId(1L);
            req.setPrice(BigDecimal.valueOf(99));
            req.setStock(100);
            req.setMainImage("/images/main.jpg");
            req.setImages(List.of("/images/1.jpg", "/images/2.jpg"));

            FmProduct result = productService.publishProduct(storeId, req);

            assertThat(result.getName()).isEqualTo("New Product");
            assertThat(result.getStatus()).isEqualTo(ProductStatusEnum.PENDING_REVIEW.getCode());
            verify(productImageMapper, times(2)).insert(any(FmProductImage.class));
        }

        @Test
        @DisplayName("should_publish_product_with_default_stock_zero_when_null")
        void shouldPublishProductWithDefaultStockZero_whenNull() {
            when(productMapper.insert(any(FmProduct.class))).thenReturn(1);

            ProductPublishRequest req = new ProductPublishRequest();
            req.setName("Product");
            req.setCategoryId(1L);
            req.setPrice(BigDecimal.valueOf(50));
            req.setStock(null);

            FmProduct result = productService.publishProduct(storeId, req);

            assertThat(result.getStock()).isEqualTo(0);
        }
    }

    // ==================== updateProduct ====================

    @Nested
    @DisplayName("updateProduct — 更新商品")
    class UpdateProductTests {

        @Test
        @DisplayName("should_throw_forbidden_when_non_owner_updates_product")
        void shouldThrowForbidden_whenNonOwnerUpdatesProduct() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);

            ProductPublishRequest req = new ProductPublishRequest();

            assertThatThrownBy(() -> productService.updateProduct(productId, 999L, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }
    }

    // ==================== deleteProduct ====================

    @Nested
    @DisplayName("deleteProduct — 删除商品")
    class DeleteProductTests {

        @Test
        @DisplayName("should_delete_product_successfully_when_owner_deletes")
        void shouldDeleteProductSuccessfully_whenOwnerDeletes() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productMapper.deleteById(productId)).thenReturn(1);

            productService.deleteProduct(productId, storeId);

            verify(productMapper).deleteById(productId);
        }

        @Test
        @DisplayName("should_throw_forbidden_when_non_owner_deletes_product")
        void shouldThrowForbidden_whenNonOwnerDeletesProduct() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);

            assertThatThrownBy(() -> productService.deleteProduct(productId, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }
    }

    // ==================== offShelf / onShelf ====================

    @Nested
    @DisplayName("offShelf — 下架商品")
    class OffShelfTests {

        @Test
        @DisplayName("should_off_shelf_product_successfully")
        void shouldOffShelfProductSuccessfully() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productMapper.updateById(any(FmProduct.class))).thenReturn(1);

            productService.offShelf(productId, storeId);

            assertThat(product.getStatus()).isEqualTo(ProductStatusEnum.OFF_SHELF.getCode());
        }
    }

    @Nested
    @DisplayName("onShelf — 上架商品")
    class OnShelfTests {

        @Test
        @DisplayName("should_on_shelf_product_successfully")
        void shouldOnShelfProductSuccessfully() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productMapper.updateById(any(FmProduct.class))).thenReturn(1);

            productService.onShelf(productId, storeId);

            assertThat(product.getStatus()).isEqualTo(ProductStatusEnum.PENDING_REVIEW.getCode());
            assertThat(product.getReviewComment()).isNull();
        }
    }

    // ==================== reviewProduct ====================

    @Nested
    @DisplayName("reviewProduct — 审核商品")
    class ReviewProductTests {

        @Test
        @DisplayName("should_review_product_to_approved")
        void shouldReviewProductToApproved() {
            FmProduct product = TestDataFactory.createProduct(productId, storeId,
                    ProductStatusEnum.PENDING_REVIEW.getCode(), 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);
            when(productMapper.updateById(any(FmProduct.class))).thenReturn(1);

            ProductReviewRequest req = new ProductReviewRequest();
            req.setStatus(ProductStatusEnum.APPROVED.getCode());

            productService.reviewProduct(productId, req);

            assertThat(product.getStatus()).isEqualTo(ProductStatusEnum.APPROVED.getCode());
        }

        @Test
        @DisplayName("should_throw_already_reviewed_when_review_non_pending")
        void shouldThrowAlreadyReviewed_whenReviewNonPending() {
            FmProduct product = TestDataFactory.createApprovedProduct(productId, storeId, 10, BigDecimal.TEN);
            when(productMapper.selectById(productId)).thenReturn(product);

            ProductReviewRequest req = new ProductReviewRequest();
            req.setStatus(ProductStatusEnum.APPROVED.getCode());

            assertThatThrownBy(() -> productService.reviewProduct(productId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.PRODUCT_ALREADY_REVIEWED.getCode());
        }
    }

    // ==================== listPendingProducts ====================

    @Nested
    @DisplayName("listPendingProducts — 待审核商品列表")
    class ListPendingProductsTests {

        @Test
        @DisplayName("should_return_pending_products")
        void shouldReturnPendingProducts() {
            FmProduct product = TestDataFactory.createProduct(productId, storeId,
                    ProductStatusEnum.PENDING_REVIEW.getCode(), 10, BigDecimal.TEN);
            Page<FmProduct> mpPage = new Page<>(1, 20);
            mpPage.setRecords(List.of(product));
            mpPage.setTotal(1);

            when(productMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);
            when(storeMapper.selectBatchIds(anyCollection())).thenReturn(Collections.emptyList());

            PageResult<ProductVO> result = productService.listPendingProducts(1, 20);

            assertThat(result.getTotal()).isEqualTo(1);
        }
    }
}
