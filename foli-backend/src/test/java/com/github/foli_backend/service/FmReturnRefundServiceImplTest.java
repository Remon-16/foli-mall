package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ReturnRefundStatusEnum;
import com.github.foli_backend.dto.request.ReturnCreateRequest;
import com.github.foli_backend.dto.request.ReturnDisputeRequest;
import com.github.foli_backend.dto.request.ReturnReviewRequest;
import com.github.foli_backend.dto.response.ReturnRefundVO;
import com.github.foli_backend.entity.*;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.*;
import com.github.foli_backend.service.impl.FmReturnRefundServiceImpl;
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
@DisplayName("FmReturnRefundServiceImpl 单元测试")
class FmReturnRefundServiceImplTest {

    @Mock FmReturnRefundMapper returnRefundMapper;
    @Mock FmOrderMapper orderMapper;
    @Mock FmUserMapper userMapper;
    @Mock FmStoreMapper storeMapper;
    @Mock FmBalanceLogMapper balanceLogMapper;
    @Mock FmComplaintMapper complaintMapper;
    @Mock FmProductMapper productMapper;

    @InjectMocks
    FmReturnRefundServiceImpl returnService;

    Long buyerId = 1L;
    Long storeId = 10L;
    Long orderId = 100L;
    BigDecimal refundAmount = BigDecimal.valueOf(99.00);

    // ==================== createReturn ====================

    @Nested
    @DisplayName("createReturn — 创建退货申请")
    class CreateReturnTests {

        @Test
        @DisplayName("should_create_return_successfully_when_order_is_completed")
        void shouldCreateReturnSuccessfully_whenOrderIsCompleted() {
            FmOrder order = TestDataFactory.createCompletedOrder(orderId, buyerId, storeId, refundAmount);
            when(orderMapper.selectById(orderId)).thenReturn(order);
            when(returnRefundMapper.insert(any(FmReturnRefund.class))).thenReturn(1);

            ReturnCreateRequest req = new ReturnCreateRequest();
            req.setOrderId(orderId);
            req.setReturnReason("Defective");
            req.setReturnType(1);

            ReturnRefundVO result = returnService.createReturn(buyerId, req);

            assertThat(result).isNotNull();
            assertThat(result.getReturnNo()).startsWith("RT");
            assertThat(result.getStatus()).isEqualTo(ReturnRefundStatusEnum.PENDING_REVIEW.getCode());
            assertThat(result.getOrderNo()).isEqualTo(order.getOrderNo());
        }

        @Test
        @DisplayName("should_throw_order_not_found_when_order_does_not_exist")
        void shouldThrowOrderNotFound_whenOrderDoesNotExist() {
            when(orderMapper.selectById(orderId)).thenReturn(null);

            ReturnCreateRequest req = new ReturnCreateRequest();
            req.setOrderId(orderId);

            assertThatThrownBy(() -> returnService.createReturn(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.ORDER_NOT_FOUND.getCode());
        }

        @Test
        @DisplayName("should_throw_forbidden_when_order_belongs_to_different_user")
        void shouldThrowForbidden_whenOrderBelongsToDifferentUser() {
            FmOrder order = TestDataFactory.createCompletedOrder(orderId, 999L, storeId, refundAmount);
            when(orderMapper.selectById(orderId)).thenReturn(order);

            ReturnCreateRequest req = new ReturnCreateRequest();
            req.setOrderId(orderId);

            assertThatThrownBy(() -> returnService.createReturn(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_order_not_completed_when_order_status_is_not_completed")
        void shouldThrowOrderNotCompleted_whenOrderStatusIsNotCompleted() {
            FmOrder order = TestDataFactory.createPaidOrder(orderId, buyerId, storeId, refundAmount);
            when(orderMapper.selectById(orderId)).thenReturn(order);

            ReturnCreateRequest req = new ReturnCreateRequest();
            req.setOrderId(orderId);

            assertThatThrownBy(() -> returnService.createReturn(buyerId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.ORDER_NOT_COMPLETED.getCode());
        }
    }

    // ==================== reviewReturn ====================

    @Nested
    @DisplayName("reviewReturn — 审核退货申请")
    class ReviewReturnTests {

        @Test
        @DisplayName("should_approve_and_auto_refund_when_return_type_is_refund_only")
        void shouldApproveAndAutoRefund_whenReturnTypeIsRefundOnly() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer");
            buyer.setBalance(BigDecimal.valueOf(100));

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            ReturnReviewRequest req = new ReturnReviewRequest();
            req.setStatus(ReturnRefundStatusEnum.APPROVED.getCode());

            returnService.reviewReturn(1L, storeId, req);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.REFUNDED.getCode());
            assertThat(rr.getRefundTime()).isNotNull();
        }

        @Test
        @DisplayName("should_approve_without_refund_when_return_type_is_return_and_refund")
        void shouldApproveWithoutRefund_whenReturnTypeIsReturnAndRefund() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 1, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            ReturnReviewRequest req = new ReturnReviewRequest();
            req.setStatus(ReturnRefundStatusEnum.APPROVED.getCode());

            returnService.reviewReturn(1L, storeId, req);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.APPROVED.getCode());
            // refund should NOT be called for return+refund type
            verify(userMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
        }

        @Test
        @DisplayName("should_reject_return_when_seller_rejects")
        void shouldRejectReturn_whenSellerRejects() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            ReturnReviewRequest req = new ReturnReviewRequest();
            req.setStatus(ReturnRefundStatusEnum.REJECTED.getCode());
            req.setSellerComment("Not eligible");

            returnService.reviewReturn(1L, storeId, req);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.REJECTED.getCode());
            assertThat(rr.getSellerComment()).isEqualTo("Not eligible");
        }

        @Test
        @DisplayName("should_throw_forbidden_when_review_by_non_owning_store")
        void shouldThrowForbidden_whenReviewByNonOwningStore() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            ReturnReviewRequest req = new ReturnReviewRequest();

            assertThatThrownBy(() -> returnService.reviewReturn(1L, 999L, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_already_processed_when_review_non_pending_return")
        void shouldThrowAlreadyProcessed_whenReviewNonPendingReturn() {
            FmReturnRefund rr = TestDataFactory.createApprovedReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            ReturnReviewRequest req = new ReturnReviewRequest();

            assertThatThrownBy(() -> returnService.reviewReturn(1L, storeId, req))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.RETURN_ALREADY_PROCESSED.getCode());
        }
    }

    // ==================== shipBack ====================

    @Nested
    @DisplayName("shipBack — 买家退回商品")
    class ShipBackTests {

        @Test
        @DisplayName("should_ship_back_successfully_when_status_is_approved")
        void shouldShipBackSuccessfully_whenStatusIsApproved() {
            FmReturnRefund rr = TestDataFactory.createApprovedReturn(1L, orderId, buyerId, storeId, 1, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            returnService.shipBack(1L, buyerId);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.BUYER_SHIPPING.getCode());
            assertThat(rr.getShipBackTime()).isNotNull();
        }

        @Test
        @DisplayName("should_throw_wrong_return_status_when_ship_back_on_non_approved")
        void shouldThrowWrongReturnStatus_whenShipBackOnNonApproved() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            assertThatThrownBy(() -> returnService.shipBack(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_RETURN_STATUS.getCode());
        }

        @Test
        @DisplayName("should_throw_forbidden_when_ship_back_others_return")
        void shouldThrowForbidden_whenShipBackOthersReturn() {
            FmReturnRefund rr = TestDataFactory.createApprovedReturn(1L, orderId, 999L, storeId, 1, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            assertThatThrownBy(() -> returnService.shipBack(1L, buyerId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }
    }

    // ==================== confirmReceipt ====================

    @Nested
    @DisplayName("confirmReceipt — 确认收到退货")
    class ConfirmReceiptTests {

        @Test
        @DisplayName("should_confirm_receipt_successfully_when_status_is_buyer_shipping")
        void shouldConfirmReceiptSuccessfully_whenStatusIsBuyerShipping() {
            FmReturnRefund rr = TestDataFactory.createReturnRefund(1L, orderId, buyerId, storeId,
                    ReturnRefundStatusEnum.BUYER_SHIPPING.getCode(), 1, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            returnService.confirmReceipt(1L, storeId);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.SELLER_RECEIVED.getCode());
            assertThat(rr.getSellerReceiveTime()).isNotNull();
        }

        @Test
        @DisplayName("should_throw_wrong_return_status_when_confirm_receipt_in_wrong_state")
        void shouldThrowWrongReturnStatus_whenConfirmReceiptInWrongState() {
            FmReturnRefund rr = TestDataFactory.createApprovedReturn(1L, orderId, buyerId, storeId, 1, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            assertThatThrownBy(() -> returnService.confirmReceipt(1L, storeId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_RETURN_STATUS.getCode());
        }
    }

    // ==================== inspectPass ====================

    @Nested
    @DisplayName("inspectPass — 验货通过并退款")
    class InspectPassTests {

        @Test
        @DisplayName("should_inspect_pass_and_refund_when_status_is_seller_received")
        void shouldInspectPassAndRefund_whenStatusIsSellerReceived() {
            FmReturnRefund rr = TestDataFactory.createReturnRefund(1L, orderId, buyerId, storeId,
                    ReturnRefundStatusEnum.SELLER_RECEIVED.getCode(), 1, refundAmount);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer");
            buyer.setBalance(BigDecimal.valueOf(100));

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            returnService.inspectPass(1L, storeId);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.REFUNDED.getCode());
            assertThat(rr.getInspectTime()).isNotNull();
            assertThat(rr.getRefundTime()).isNotNull();
        }

        @Test
        @DisplayName("should_throw_user_not_found_when_refunding_to_deleted_user")
        void shouldThrowUserNotFound_whenRefundingToDeletedUser() {
            FmReturnRefund rr = TestDataFactory.createReturnRefund(1L, orderId, buyerId, storeId,
                    ReturnRefundStatusEnum.SELLER_RECEIVED.getCode(), 1, refundAmount);

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(userMapper.selectById(buyerId)).thenReturn(null);

            assertThatThrownBy(() -> returnService.inspectPass(1L, storeId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.USER_NOT_FOUND.getCode());
        }
    }

    // ==================== dispute ====================

    @Nested
    @DisplayName("dispute — 发起争议")
    class DisputeTests {

        @Test
        @DisplayName("should_dispute_and_create_complaint_when_inspection_failed")
        void shouldDisputeAndCreateComplaint_whenInspectionFailed() {
            FmReturnRefund rr = TestDataFactory.createReturnRefund(1L, orderId, buyerId, storeId,
                    ReturnRefundStatusEnum.SELLER_RECEIVED.getCode(), 1, refundAmount);
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(storeMapper.selectById(storeId)).thenReturn(store);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);
            when(complaintMapper.insert(any(FmComplaint.class))).thenReturn(1);

            ReturnDisputeRequest req = new ReturnDisputeRequest();
            req.setSellerComment("Product is fine, buyer damaged it");

            returnService.dispute(1L, storeId, req);

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.DISPUTED.getCode());
            assertThat(rr.getDisputeTime()).isNotNull();
            // complaint auto-created
            verify(complaintMapper).insert(any(FmComplaint.class));
        }
    }

    // ==================== handleDispute ====================

    @Nested
    @DisplayName("handleDispute — 平台仲裁争议")
    class HandleDisputeTests {

        @Test
        @DisplayName("should_handle_dispute_and_refund_when_status_is_disputed")
        void shouldHandleDisputeAndRefund_whenStatusIsDisputed() {
            FmReturnRefund rr = TestDataFactory.createReturnRefund(1L, orderId, buyerId, storeId,
                    ReturnRefundStatusEnum.DISPUTED.getCode(), 1, refundAmount);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer");
            buyer.setBalance(BigDecimal.valueOf(100));

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(userMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);
            when(balanceLogMapper.insert(any(FmBalanceLog.class))).thenReturn(1);
            when(returnRefundMapper.updateById(any(FmReturnRefund.class))).thenReturn(1);

            returnService.handleDispute(1L, "Refund approved by admin");

            assertThat(rr.getStatus()).isEqualTo(ReturnRefundStatusEnum.REFUNDED.getCode());
            assertThat(rr.getAdminHandleResult()).isEqualTo("Refund approved by admin");
        }

        @Test
        @DisplayName("should_throw_wrong_return_status_when_handle_dispute_on_non_disputed")
        void shouldThrowWrongReturnStatus_whenHandleDisputeOnNonDisputed() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            when(returnRefundMapper.selectById(1L)).thenReturn(rr);

            assertThatThrownBy(() -> returnService.handleDispute(1L, "result"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.WRONG_RETURN_STATUS.getCode());
        }
    }

    // ==================== listBuyerReturns ====================

    @Nested
    @DisplayName("listBuyerReturns — 买家退货列表")
    class ListBuyerReturnsTests {

        @Test
        @DisplayName("should_return_buyer_returns_with_enriched_data")
        void shouldReturnBuyerReturnsWithEnrichedData() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            Page<FmReturnRefund> mpPage = new Page<>(1, 10);
            mpPage.setRecords(List.of(rr));
            mpPage.setTotal(1);
            FmOrder order = TestDataFactory.createCompletedOrder(orderId, buyerId, storeId, refundAmount);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer");
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(returnRefundMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);
            when(orderMapper.selectById(orderId)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            PageResult<ReturnRefundVO> result = returnService.listBuyerReturns(buyerId, 1, 10);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
            ReturnRefundVO vo = result.getRecords().get(0);
            assertThat(vo.getOrderNo()).isEqualTo(order.getOrderNo());
            assertThat(vo.getBuyerNickname()).isEqualTo("buyer_nick");
            assertThat(vo.getStoreName()).isEqualTo("Test Store 10");
        }

        @Test
        @DisplayName("should_return_empty_page_when_no_returns")
        void shouldReturnEmptyPage_whenNoReturns() {
            Page<FmReturnRefund> mpPage = new Page<>(1, 10);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(returnRefundMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            PageResult<ReturnRefundVO> result = returnService.listBuyerReturns(buyerId, 1, 10);

            assertThat(result.getRecords()).isEmpty();
        }
    }

    // ==================== getReturnDetail ====================

    @Nested
    @DisplayName("getReturnDetail — 退货详情")
    class GetReturnDetailTests {

        @Test
        @DisplayName("should_return_detail_with_enriched_data")
        void shouldReturnDetailWithEnrichedData() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            FmOrder order = TestDataFactory.createCompletedOrder(orderId, buyerId, storeId, refundAmount);
            FmUser buyer = TestDataFactory.createBuyer(buyerId, "buyer");
            FmStore store = TestDataFactory.createApprovedStore(storeId, 2L);

            when(returnRefundMapper.selectById(1L)).thenReturn(rr);
            when(orderMapper.selectById(orderId)).thenReturn(order);
            when(userMapper.selectById(buyerId)).thenReturn(buyer);
            when(storeMapper.selectById(storeId)).thenReturn(store);

            ReturnRefundVO result = returnService.getReturnDetail(1L);

            assertThat(result).isNotNull();
            assertThat(result.getOrderNo()).isEqualTo(order.getOrderNo());
        }

        @Test
        @DisplayName("should_throw_return_not_found_when_not_exist")
        void shouldThrowReturnNotFound_whenNotExist() {
            when(returnRefundMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> returnService.getReturnDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.RETURN_NOT_FOUND.getCode());
        }
    }

    // ==================== listStoreReturns ====================

    @Nested
    @DisplayName("listStoreReturns — 卖家退货列表")
    class ListStoreReturnsTests {

        @Test
        @DisplayName("should_return_store_returns_with_status_filter")
        void shouldReturnStoreReturnsWithStatusFilter() {
            Page<FmReturnRefund> mpPage = new Page<>(1, 10);
            mpPage.setRecords(Collections.emptyList());
            mpPage.setTotal(0);

            when(returnRefundMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);

            PageResult<ReturnRefundVO> result = returnService.listStoreReturns(storeId, 1, 10,
                    ReturnRefundStatusEnum.PENDING_REVIEW.getCode());

            assertThat(result.getRecords()).isEmpty();
        }
    }

    // ==================== listAllReturns ====================

    @Nested
    @DisplayName("listAllReturns — 管理员查看所有退货")
    class ListAllReturnsTests {

        @Test
        @DisplayName("should_return_all_returns_with_optional_status")
        void shouldReturnAllReturnsWithOptionalStatus() {
            FmReturnRefund rr = TestDataFactory.createPendingReturn(1L, orderId, buyerId, storeId, 0, refundAmount);
            Page<FmReturnRefund> mpPage = new Page<>(1, 10);
            mpPage.setRecords(List.of(rr));
            mpPage.setTotal(1);

            when(returnRefundMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                    .thenReturn(mpPage);
            when(orderMapper.selectById(orderId)).thenReturn(null); // order might not exist
            when(userMapper.selectById(buyerId)).thenReturn(null);
            when(storeMapper.selectById(storeId)).thenReturn(null);

            PageResult<ReturnRefundVO> result = returnService.listAllReturns(1, 10, null);

            assertThat(result.getTotal()).isEqualTo(1);
            assertThat(result.getRecords()).hasSize(1);
        }
    }
}
