package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.ComplaintStatusEnum;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.dto.request.ComplaintCreateRequest;
import com.github.foli_backend.dto.request.ComplaintHandleRequest;
import com.github.foli_backend.dto.response.ComplaintVO;
import com.github.foli_backend.entity.FmComplaint;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmComplaintMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.FmComplaintService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投诉服务实现 / Complaint service implementation
 */
@Service
public class FmComplaintServiceImpl implements FmComplaintService {

    private final FmComplaintMapper complaintMapper;
    private final FmUserMapper userMapper;
    private final FmStoreMapper storeMapper;

    public FmComplaintServiceImpl(FmComplaintMapper complaintMapper,
                                  FmUserMapper userMapper,
                                  FmStoreMapper storeMapper) {
        this.complaintMapper = complaintMapper;
        this.userMapper = userMapper;
        this.storeMapper = storeMapper;
    }

    @Override
    public ComplaintVO createComplaint(Long userId, ComplaintCreateRequest req) {
        if (req.getStoreId() == null && req.getReportedUserId() == null) {
            BizCodeEnum.BAD_REQUEST.throwEx("storeId or reportedUserId is required");
        }
        FmComplaint complaint = new FmComplaint();
        complaint.setUserId(userId);
        complaint.setStoreId(req.getStoreId());
        complaint.setReportedUserId(req.getReportedUserId());
        complaint.setOrderId(req.getOrderId());
        complaint.setProductId(req.getProductId());
        complaint.setReturnId(req.getReturnId());
        complaint.setType(req.getType());
        complaint.setTitle(req.getTitle());
        complaint.setContent(req.getContent());
        complaint.setEvidenceImages(req.getEvidenceImages());
        complaint.setStatus(ComplaintStatusEnum.PENDING.getCode());
        complaintMapper.insert(complaint);

        return enrichComplaint(complaint);
    }

    @Override
    public PageResult<ComplaintVO> listUserComplaints(Long userId, int page, int pageSize) {
        Page<FmComplaint> mpPage = complaintMapper.selectPage(Page.of(page, pageSize),
                new LambdaQueryWrapper<FmComplaint>()
                        .eq(FmComplaint::getUserId, userId)
                        .orderByDesc(FmComplaint::getCreateTime)
        );

        List<ComplaintVO> voList = mpPage.getRecords().stream()
                .map(this::enrichComplaint)
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    public ComplaintVO getComplaintDetail(Long complaintId) {
        FmComplaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            BizCodeEnum.COMPLAINT_NOT_FOUND.throwEx();
        }
        return enrichComplaint(complaint);
    }

    @Override
    public PageResult<ComplaintVO> listAllComplaints(int page, int pageSize, Integer status) {
        LambdaQueryWrapper<FmComplaint> wrapper = new LambdaQueryWrapper<FmComplaint>()
                .orderByDesc(FmComplaint::getCreateTime);

        if (status != null) {
            wrapper.eq(FmComplaint::getStatus, status);
        }

        Page<FmComplaint> mpPage = complaintMapper.selectPage(Page.of(page, pageSize), wrapper);

        List<ComplaintVO> voList = mpPage.getRecords().stream()
                .map(this::enrichComplaint)
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    public void handleComplaint(Long complaintId, Long handlerId, ComplaintHandleRequest req) {
        FmComplaint complaint = complaintMapper.selectById(complaintId);
        if (complaint == null) {
            BizCodeEnum.COMPLAINT_NOT_FOUND.throwEx();
        }

        // 状态机校验：已解决/已驳回的投诉不可再处理
        int cur = complaint.getStatus();
        if (cur == ComplaintStatusEnum.RESOLVED.getCode()
                || cur == ComplaintStatusEnum.REJECTED.getCode()) {
            BizCodeEnum.COMPLAINT_ALREADY_HANDLED.throwEx();
        }

        complaint.setStatus(req.getStatus());
        complaint.setHandlerId(handlerId);
        complaint.setHandleResult(req.getHandleResult());
        complaint.setHandleTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
    }

    /**
     * 丰富投诉信息 (添加用户名称和店铺名称)
     * Enrich complaint with user name and store name
     */
    private ComplaintVO enrichComplaint(FmComplaint c) {
        ComplaintVO vo = ComplaintVO.fromEntity(c);

        FmUser user = userMapper.selectById(c.getUserId());
        if (user != null) {
            vo.setUserName(user.getNickname());
        }

        if (c.getStoreId() != null) {
            FmStore store = storeMapper.selectById(c.getStoreId());
            if (store != null) {
                vo.setStoreName(store.getStoreName());
            }
        }

        if (c.getReportedUserId() != null) {
            FmUser reportedUser = userMapper.selectById(c.getReportedUserId());
            if (reportedUser != null) {
                vo.setReportedUserName(reportedUser.getNickname());
            }
        }

        return vo;
    }
}
