package com.github.foli_backend.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BalanceLogTypeEnum;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.ComplaintStatusEnum;
import com.github.foli_backend.constant.OrderStatusEnum;
import com.github.foli_backend.constant.ReturnRefundStatusEnum;
import com.github.foli_backend.dto.request.ReturnCreateRequest;
import com.github.foli_backend.dto.request.ReturnDisputeRequest;
import com.github.foli_backend.dto.request.ReturnReviewRequest;
import com.github.foli_backend.dto.response.ReturnRefundVO;
import com.github.foli_backend.entity.*;
import com.github.foli_backend.mapper.*;
import com.github.foli_backend.service.FmReturnRefundService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FmReturnRefundServiceImpl implements FmReturnRefundService {

    private final FmReturnRefundMapper returnRefundMapper;
    private final FmOrderMapper orderMapper;
    private final FmUserMapper userMapper;
    private final FmStoreMapper storeMapper;
    private final FmBalanceLogMapper balanceLogMapper;
    private final FmComplaintMapper complaintMapper;
    private final FmProductMapper productMapper;

    public FmReturnRefundServiceImpl(FmReturnRefundMapper returnRefundMapper,
                                     FmOrderMapper orderMapper,
                                     FmUserMapper userMapper,
                                     FmStoreMapper storeMapper,
                                     FmBalanceLogMapper balanceLogMapper,
                                     FmComplaintMapper complaintMapper,
                                     FmProductMapper productMapper) {
        this.returnRefundMapper = returnRefundMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.storeMapper = storeMapper;
        this.balanceLogMapper = balanceLogMapper;
        this.complaintMapper = complaintMapper;
        this.productMapper = productMapper;
    }

    private String generateReturnNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "RT" + date + RandomUtil.randomNumbers(6);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReturnRefundVO createReturn(Long userId, ReturnCreateRequest req) {
        FmOrder order = orderMapper.selectById(req.getOrderId());
        if (order == null) {
            BizCodeEnum.ORDER_NOT_FOUND.throwEx();
        }
        if (!order.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (order.getStatus() != OrderStatusEnum.COMPLETED.getCode()) {
            BizCodeEnum.ORDER_NOT_COMPLETED.throwEx();
        }

        FmReturnRefund returnRefund = new FmReturnRefund();
        returnRefund.setReturnNo(generateReturnNo());
        returnRefund.setOrderId(order.getId());
        returnRefund.setUserId(userId);
        returnRefund.setStoreId(order.getStoreId());
        returnRefund.setReturnReason(req.getReturnReason());
        returnRefund.setReturnType(req.getReturnType());
        returnRefund.setRefundAmount(order.getTotalAmount());
        returnRefund.setStatus(ReturnRefundStatusEnum.PENDING_REVIEW.getCode());
        returnRefund.setEvidenceImages(req.getEvidenceImages());
        returnRefundMapper.insert(returnRefund);

        ReturnRefundVO vo = ReturnRefundVO.fromEntity(returnRefund);
        vo.setOrderNo(order.getOrderNo());
        return vo;
    }

    @Override
    public PageResult<ReturnRefundVO> listBuyerReturns(Long userId, int page, int pageSize) {
        Page<FmReturnRefund> mpPage = returnRefundMapper.selectPage(Page.of(page, pageSize),
                new LambdaQueryWrapper<FmReturnRefund>()
                        .eq(FmReturnRefund::getUserId, userId)
                        .orderByDesc(FmReturnRefund::getCreateTime)
        );

        List<ReturnRefundVO> voList = enrichReturnList(mpPage.getRecords());
        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    public ReturnRefundVO getReturnDetail(Long returnId) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        return enrichReturn(returnRefund);
    }

    @Override
    public void shipBack(Long returnId, Long userId) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.APPROVED.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        returnRefund.setStatus(ReturnRefundStatusEnum.BUYER_SHIPPING.getCode());
        returnRefund.setShipBackTime(LocalDateTime.now());
        returnRefundMapper.updateById(returnRefund);
    }

    @Override
    public PageResult<ReturnRefundVO> listStoreReturns(Long storeId, int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmReturnRefund> wrapper = new LambdaQueryWrapper<FmReturnRefund>()
                .eq(FmReturnRefund::getStoreId, storeId)
                .orderByDesc(FmReturnRefund::getCreateTime);

        if (status != null) {
            wrapper.eq(FmReturnRefund::getStatus, status);
        }

        Page<FmReturnRefund> mpPage = returnRefundMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<ReturnRefundVO> voList = enrichReturnList(mpPage.getRecords());
        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewReturn(Long returnId, Long storeId, ReturnReviewRequest req) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.PENDING_REVIEW.getCode()) {
            BizCodeEnum.RETURN_ALREADY_PROCESSED.throwEx();
        }

        returnRefund.setSellerComment(req.getSellerComment());

        if (req.getStatus() == ReturnRefundStatusEnum.REJECTED.getCode()) {
            returnRefund.setStatus(ReturnRefundStatusEnum.REJECTED.getCode());
            returnRefundMapper.updateById(returnRefund);
        } else {
            if (returnRefund.getReturnType() != null && returnRefund.getReturnType() == 0) {
                doRefund(returnRefund);
                returnRefund.setStatus(ReturnRefundStatusEnum.REFUNDED.getCode());
                returnRefund.setRefundTime(LocalDateTime.now());
            } else {
                returnRefund.setStatus(ReturnRefundStatusEnum.APPROVED.getCode());
            }
            returnRefundMapper.updateById(returnRefund);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceipt(Long returnId, Long storeId) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.BUYER_SHIPPING.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        returnRefund.setStatus(ReturnRefundStatusEnum.SELLER_RECEIVED.getCode());
        returnRefund.setSellerReceiveTime(LocalDateTime.now());
        returnRefundMapper.updateById(returnRefund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void inspectPass(Long returnId, Long storeId) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.SELLER_RECEIVED.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        doRefund(returnRefund);

        returnRefund.setStatus(ReturnRefundStatusEnum.REFUNDED.getCode());
        returnRefund.setRefundTime(LocalDateTime.now());
        returnRefund.setInspectTime(LocalDateTime.now());
        returnRefundMapper.updateById(returnRefund);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dispute(Long returnId, Long storeId, ReturnDisputeRequest req) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getStoreId().equals(storeId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.SELLER_RECEIVED.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        returnRefund.setStatus(ReturnRefundStatusEnum.DISPUTED.getCode());
        returnRefund.setDisputeTime(LocalDateTime.now());
        returnRefund.setSellerComment(req.getSellerComment());
        returnRefundMapper.updateById(returnRefund);

        FmStore store = storeMapper.selectById(returnRefund.getStoreId());
        FmComplaint complaint = new FmComplaint();
        complaint.setUserId(store != null ? store.getUserId() : null);
        complaint.setStoreId(returnRefund.getStoreId());
        complaint.setReturnId(returnRefund.getId());
        complaint.setType("return_dispute");
        complaint.setTitle("退货争议 Return Dispute");
        complaint.setContent(req.getSellerComment());
        complaint.setStatus(ComplaintStatusEnum.PENDING.getCode());
        complaintMapper.insert(complaint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyerDispute(Long returnId, Long userId, String reason) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (!returnRefund.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.REJECTED.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        returnRefund.setStatus(ReturnRefundStatusEnum.DISPUTED.getCode());
        returnRefund.setDisputeTime(LocalDateTime.now());
        returnRefund.setSellerComment(reason);
        returnRefundMapper.updateById(returnRefund);

        FmComplaint complaint = new FmComplaint();
        complaint.setUserId(userId);
        complaint.setReportedUserId(null);
        complaint.setStoreId(returnRefund.getStoreId());
        complaint.setReturnId(returnRefund.getId());
        complaint.setType("return_dispute");
        complaint.setTitle("退货争议申诉 Return Dispute Appeal");
        complaint.setContent(reason);
        complaint.setStatus(ComplaintStatusEnum.PENDING.getCode());
        complaintMapper.insert(complaint);
    }

    @Override
    public PageResult<ReturnRefundVO> listAllReturns(int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmReturnRefund> wrapper = new LambdaQueryWrapper<FmReturnRefund>()
                .orderByDesc(FmReturnRefund::getCreateTime);

        if (status != null) {
            wrapper.eq(FmReturnRefund::getStatus, status);
        }

        Page<FmReturnRefund> mpPage = returnRefundMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<ReturnRefundVO> voList = enrichReturnList(mpPage.getRecords());
        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDispute(Long returnId, String decision, String result) {
        FmReturnRefund returnRefund = returnRefundMapper.selectById(returnId);
        if (returnRefund == null) {
            BizCodeEnum.RETURN_NOT_FOUND.throwEx();
        }
        if (returnRefund.getStatus() != ReturnRefundStatusEnum.DISPUTED.getCode()) {
            BizCodeEnum.WRONG_RETURN_STATUS.throwEx();
        }

        if ("refund".equals(decision)) {
            doRefund(returnRefund);
            returnRefund.setStatus(ReturnRefundStatusEnum.REFUNDED.getCode());
            returnRefund.setRefundTime(LocalDateTime.now());
        } else {
            returnRefund.setStatus(ReturnRefundStatusEnum.REJECTED.getCode());
        }
        returnRefund.setAdminHandleResult(result);
        returnRefundMapper.updateById(returnRefund);
    }

    // ========== 私有辅助方法 ==========

    private void doRefund(FmReturnRefund returnRefund) {
        FmUser buyer = userMapper.selectById(returnRefund.getUserId());
        if (buyer == null) {
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }

        BigDecimal refundAmount = returnRefund.getRefundAmount();
        BigDecimal beforeBalance = buyer.getBalance();

        userMapper.update(null,
                new LambdaUpdateWrapper<FmUser>()
                        .setSql("balance = balance + " + refundAmount)
                        .eq(FmUser::getId, returnRefund.getUserId())
        );

        FmBalanceLog balanceLog = new FmBalanceLog();
        balanceLog.setUserId(returnRefund.getUserId());
        balanceLog.setAmount(refundAmount);
        balanceLog.setType(BalanceLogTypeEnum.REFUND.name());
        balanceLog.setOrderNo(returnRefund.getReturnNo());
        balanceLog.setBeforeBalance(beforeBalance);
        balanceLog.setAfterBalance(beforeBalance.add(refundAmount));
        balanceLog.setRemark("退货退款 Return refund: " + returnRefund.getReturnNo());
        balanceLogMapper.insert(balanceLog);
    }

    private List<ReturnRefundVO> enrichReturnList(List<FmReturnRefund> records) {
        if (records == null) {
            return List.of();
        }
        return records.stream()
                .map(this::enrichReturn)
                .collect(Collectors.toList());
    }

    private ReturnRefundVO enrichReturn(FmReturnRefund rr) {
        ReturnRefundVO vo = ReturnRefundVO.fromEntity(rr);

        FmOrder order = orderMapper.selectById(rr.getOrderId());
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
        }

        FmUser buyer = userMapper.selectById(rr.getUserId());
        if (buyer != null) {
            vo.setBuyerNickname(buyer.getNickname());
        }

        FmStore store = storeMapper.selectById(rr.getStoreId());
        if (store != null) {
            vo.setStoreName(store.getStoreName());
        }

        return vo;
    }
}
