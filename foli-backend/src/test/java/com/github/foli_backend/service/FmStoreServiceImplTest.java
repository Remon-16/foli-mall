package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.*;
import com.github.foli_backend.dto.request.StoreApplyRequest;
import com.github.foli_backend.dto.request.StoreReviewRequest;
import com.github.foli_backend.dto.response.StoreDetailVO;
import com.github.foli_backend.dto.response.StoreVO;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.impl.FmStoreServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmStoreServiceImpl 单元测试")
class FmStoreServiceImplTest {

    @Mock FmStoreMapper storeMapper;
    @Mock FmUserMapper userMapper;
    @Mock FmProductMapper productMapper;

    @InjectMocks
    FmStoreServiceImpl storeService;

    Long userId = 1L;
    Long storeId = 10L;

    // ==================== applyStore ====================

    @Nested
    @DisplayName("applyStore — 申请开店")
    class ApplyStoreTests {

        @Test
        @DisplayName("should_apply_store_successfully_when_user_is_seller_and_has_no_store")
        void shouldApplyStoreSuccessfully_whenUserIsSellerAndHasNoStore() {
            FmUser seller = TestDataFactory.createSeller(userId, "seller1");
            when(userMapper.selectById(userId)).thenReturn(seller);
            when(storeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(storeMapper.insert(any(FmStore.class))).thenReturn(1);

            StoreApplyRequest req = new StoreApplyRequest();
            req.setStoreName("My Store");
            req.setDescription("A great store");

            FmStore result = storeService.applyStore(userId, req);

            assertThat(result.getStoreName()).isEqualTo("My Store");
            assertThat(result.getStatus()).isEqualTo(StoreStatusEnum.PENDING.getCode());
        }

        @Test
        @DisplayName("should_throw_user_not_found_when_apply_store_with_invalid_user")
        void shouldThrowUserNotFound_whenApplyStoreWithInvalidUser() {
            when(userMapper.selectById(999L)).thenReturn(null);

            StoreApplyRequest req = new StoreApplyRequest();

            assertThatThrownBy(() -> storeService.applyStore(999L, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_not_seller_role_when_user_is_buyer")
        void shouldThrowNotSellerRole_whenUserIsBuyer() {
            FmUser buyer = TestDataFactory.createBuyer(userId, "buyer1");
            when(userMapper.selectById(userId)).thenReturn(buyer);

            StoreApplyRequest req = new StoreApplyRequest();

            assertThatThrownBy(() -> storeService.applyStore(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.NOT_SELLER_ROLE.getCode());
        }

        @Test
        @DisplayName("should_throw_store_limit_exceeded_when_seller_already_has_store")
        void shouldThrowStoreLimitExceeded_whenSellerAlreadyHasStore() {
            FmUser seller = TestDataFactory.createSeller(userId, "seller1");
            when(userMapper.selectById(userId)).thenReturn(seller);
            when(storeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            StoreApplyRequest req = new StoreApplyRequest();

            assertThatThrownBy(() -> storeService.applyStore(userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.STORE_LIMIT_EXCEEDED.getCode());
        }
    }

    // ==================== updateStore ====================

    @Nested
    @DisplayName("updateStore — 更新店铺")
    class UpdateStoreTests {

        @Test
        @DisplayName("should_update_store_successfully_when_owner_updates")
        void shouldUpdateStoreSuccessfully_whenOwnerUpdates() {
            FmStore store = TestDataFactory.createApprovedStore(storeId, userId);
            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(storeMapper.updateById(any(FmStore.class))).thenReturn(1);

            StoreApplyRequest req = new StoreApplyRequest();
            req.setStoreName("Updated Store");

            FmStore result = storeService.updateStore(storeId, userId, req);

            assertThat(result.getStoreName()).isEqualTo("Updated Store");
            assertThat(result.getStatus()).isEqualTo(StoreStatusEnum.PENDING.getCode());
            assertThat(result.getReviewComment()).isNull();
        }

        @Test
        @DisplayName("should_throw_forbidden_when_non_owner_updates_store")
        void shouldThrowForbidden_whenNonOwnerUpdatesStore() {
            FmStore store = TestDataFactory.createApprovedStore(storeId, userId);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            StoreApplyRequest req = new StoreApplyRequest();

            assertThatThrownBy(() -> storeService.updateStore(storeId, 999L, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_store_not_found_when_store_deleted")
        void shouldThrowStoreNotFound_whenStoreDeleted() {
            when(storeMapper.selectById(storeId)).thenReturn(null);

            StoreApplyRequest req = new StoreApplyRequest();

            assertThatThrownBy(() -> storeService.updateStore(storeId, userId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.STORE_NOT_FOUND.getCode());
        }
    }

    // ==================== listApprovedStores ====================

    @Nested
    @DisplayName("listApprovedStores — 已审核店铺列表")
    class ListApprovedStoresTests {

        @Test
        @DisplayName("should_list_approved_stores_with_owner_name_and_product_count")
        void shouldListApprovedStoresWithOwnerNameAndProductCount() {
            FmStore store = TestDataFactory.createApprovedStore(storeId, userId);
            Page<FmStore> mpPage = new Page<>(1, 12);
            mpPage.setRecords(List.of(store));
            mpPage.setTotal(1);
            FmUser owner = TestDataFactory.createSeller(userId, "seller1");

            when(storeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(mpPage);
            when(userMapper.selectById(userId)).thenReturn(owner);
            when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            PageResult<StoreVO> result = storeService.listApprovedStores(1, 12);

            assertThat(result.getTotal()).isEqualTo(1);
            StoreVO vo = result.getRecords().get(0);
            assertThat(vo.getOwnerNickname()).isEqualTo("seller1_nick");
            assertThat(vo.getProductCount()).isEqualTo(5L);
        }
    }

    // ==================== getStoreDetail ====================

    @Nested
    @DisplayName("getStoreDetail — 店铺详情")
    class GetStoreDetailTests {

        @Test
        @DisplayName("should_get_store_detail_with_product_count")
        void shouldGetStoreDetailWithProductCount() {
            FmStore store = TestDataFactory.createApprovedStore(storeId, userId);
            FmUser owner = TestDataFactory.createSeller(userId, "seller1");

            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(userMapper.selectById(userId)).thenReturn(owner);
            when(productMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

            StoreDetailVO result = storeService.getStoreDetail(storeId);

            assertThat(result.getStoreName()).isEqualTo("Test Store 10");
            assertThat(result.getOwnerNickname()).isEqualTo("seller1_nick");
            assertThat(result.getProductCount()).isEqualTo(10L);
        }

        @Test
        @DisplayName("should_throw_store_not_found_when_get_deleted_store")
        void shouldThrowStoreNotFound_whenGetDeletedStore() {
            when(storeMapper.selectById(storeId)).thenReturn(null);

            assertThatThrownBy(() -> storeService.getStoreDetail(storeId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.STORE_NOT_FOUND.getCode());
        }
    }

    // ==================== reviewStore ====================

    @Nested
    @DisplayName("reviewStore — 审核店铺")
    class ReviewStoreTests {

        @Test
        @DisplayName("should_approve_store_when_review_with_approved_status")
        void shouldApproveStore_whenReviewWithApprovedStatus() {
            FmStore store = TestDataFactory.createStore(storeId, userId, StoreStatusEnum.PENDING.getCode());
            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(storeMapper.updateById(any(FmStore.class))).thenReturn(1);

            StoreReviewRequest req = new StoreReviewRequest();
            req.setStatus(StoreStatusEnum.APPROVED.getCode());

            storeService.reviewStore(storeId, req);

            assertThat(store.getStatus()).isEqualTo(StoreStatusEnum.APPROVED.getCode());
        }

        @Test
        @DisplayName("should_reject_store_when_review_with_rejected_status")
        void shouldRejectStore_whenReviewWithRejectedStatus() {
            FmStore store = TestDataFactory.createStore(storeId, userId, StoreStatusEnum.PENDING.getCode());
            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(storeMapper.updateById(any(FmStore.class))).thenReturn(1);

            StoreReviewRequest req = new StoreReviewRequest();
            req.setStatus(StoreStatusEnum.REJECTED.getCode());
            req.setReviewComment("Not qualified");

            storeService.reviewStore(storeId, req);

            assertThat(store.getStatus()).isEqualTo(StoreStatusEnum.REJECTED.getCode());
            assertThat(store.getReviewComment()).isEqualTo("Not qualified");
        }

        @Test
        @DisplayName("should_throw_already_reviewed_when_review_non_pending_store")
        void shouldThrowAlreadyReviewed_whenReviewNonPendingStore() {
            FmStore store = TestDataFactory.createApprovedStore(storeId, userId);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            StoreReviewRequest req = new StoreReviewRequest();
            req.setStatus(StoreStatusEnum.APPROVED.getCode());

            assertThatThrownBy(() -> storeService.reviewStore(storeId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.STORE_ALREADY_REVIEWED.getCode());
        }

        @Test
        @DisplayName("should_throw_invalid_review_status_when_status_not_approved_or_rejected")
        void shouldThrowInvalidReviewStatus_whenStatusNotApprovedOrRejected() {
            FmStore store = TestDataFactory.createStore(storeId, userId, StoreStatusEnum.PENDING.getCode());
            when(storeMapper.selectById(storeId)).thenReturn(store);

            StoreReviewRequest req = new StoreReviewRequest();
            req.setStatus(999); // invalid

            assertThatThrownBy(() -> storeService.reviewStore(storeId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.INVALID_REVIEW_STATUS.getCode());
        }
    }
}
