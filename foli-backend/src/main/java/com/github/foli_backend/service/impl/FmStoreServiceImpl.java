package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.constant.StoreStatusEnum;
import com.github.foli_backend.dto.request.StoreApplyRequest;
import com.github.foli_backend.dto.request.StoreReviewRequest;
import com.github.foli_backend.dto.response.StoreDetailVO;
import com.github.foli_backend.dto.response.StoreVO;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.FmStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FmStoreServiceImpl implements FmStoreService {

    private static final Logger log = LoggerFactory.getLogger(FmStoreServiceImpl.class);
    private static final int MAX_STORE_PER_USER = 1;

    private final FmStoreMapper storeMapper;
    private final FmUserMapper userMapper;
    private final FmProductMapper productMapper;

    public FmStoreServiceImpl(FmStoreMapper storeMapper, FmUserMapper userMapper, FmProductMapper productMapper) {
        this.storeMapper = storeMapper;
        this.userMapper = userMapper;
        this.productMapper = productMapper;
    }

    // ==================== 用户端 卖家 ====================

    @Override
    @Transactional
    public FmStore applyStore(Long userId, StoreApplyRequest req) {
        FmUser user = userMapper.selectById(userId);
        if (user == null) {
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }
        if (user.getRole() == RoleConstants.BUYER) {
            user.setRole(RoleConstants.SELLER);
            userMapper.updateById(user);
            log.info("User role upgraded to SELLER: userId={}", userId);
        } else if (user.getRole() != RoleConstants.SELLER) {
            BizCodeEnum.NOT_SELLER_ROLE.throwEx();
        }

        LambdaQueryWrapper<FmStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmStore::getUserId, userId)
                    .eq(FmStore::getIsDelete, 0);
        Long count = storeMapper.selectCount(queryWrapper);
        if (count != null && count >= MAX_STORE_PER_USER) {
            BizCodeEnum.STORE_LIMIT_EXCEEDED.throwEx();
        }

        FmStore store = new FmStore();
        store.setUserId(userId);
        store.setStoreName(req.getStoreName());
        store.setStoreLogo(req.getStoreLogo());
        store.setDescription(req.getDescription());
        store.setStatus(StoreStatusEnum.PENDING.getCode());

        storeMapper.insert(store);
        log.info("店铺申请成功 Store application created: storeId={}, userId={}, storeName={}",
                store.getId(), userId, store.getStoreName());
        return store;
    }

    @Override
    public FmStore getMyStore(Long userId) {
        LambdaQueryWrapper<FmStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmStore::getUserId, userId)
                    .eq(FmStore::getIsDelete, 0)
                    .last("LIMIT 1");
        return storeMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public FmStore updateStore(Long storeId, Long userId, StoreApplyRequest req) {
        FmStore store = storeMapper.selectById(storeId);
        if (store == null || store.getIsDelete() == 1) {
            BizCodeEnum.STORE_NOT_FOUND.throwEx();
        }

        if (!store.getUserId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        store.setStoreName(req.getStoreName());
        store.setStoreLogo(req.getStoreLogo());
        store.setDescription(req.getDescription());
        store.setStatus(StoreStatusEnum.PENDING.getCode());
        store.setReviewComment(null);

        storeMapper.updateById(store);
        log.info("店铺信息已更新 Store updated: storeId={}, storeName={}", storeId, store.getStoreName());
        return store;
    }

    // ==================== 公开接口 Public ====================

    @Override
    public PageResult<StoreVO> listApprovedStores(int page, int pageSize) {
        Page<FmStore> mpPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<FmStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmStore::getStatus, StoreStatusEnum.APPROVED.getCode())
                    .eq(FmStore::getIsDelete, 0)
                    .orderByDesc(FmStore::getCreateTime);

        Page<FmStore> resultPage = storeMapper.selectPage(mpPage, queryWrapper);

        List<StoreVO> voList = resultPage.getRecords().stream().map(store -> {
            StoreVO vo = StoreVO.fromEntity(store);
            FmUser owner = userMapper.selectById(store.getUserId());
            vo.setOwnerNickname(owner != null ? owner.getNickname() : "未知 Unknown");
            LambdaQueryWrapper<FmProduct> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(FmProduct::getStoreId, store.getId())
                        .eq(FmProduct::getIsDelete, 0);
            vo.setProductCount(productMapper.selectCount(countWrapper).intValue());
            return vo;
        }).toList();

        return new PageResult<>(resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize(), voList);
    }

    @Override
    public StoreDetailVO getStoreDetail(Long storeId) {
        FmStore store = storeMapper.selectById(storeId);
        if (store == null || store.getIsDelete() == 1) {
            BizCodeEnum.STORE_NOT_FOUND.throwEx();
        }

        StoreDetailVO vo = new StoreDetailVO();
        vo.setId(store.getId());
        vo.setUserId(store.getUserId());
        vo.setStoreName(store.getStoreName());
        vo.setStoreLogo(store.getStoreLogo());
        vo.setDescription(store.getDescription());
        vo.setStatus(store.getStatus());
        vo.setReviewComment(store.getReviewComment());
        vo.setCreateTime(store.getCreateTime());

        FmUser owner = userMapper.selectById(store.getUserId());
        vo.setOwnerNickname(owner != null ? owner.getNickname() : "未知 Unknown");

        LambdaQueryWrapper<FmProduct> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FmProduct::getStoreId, store.getId())
                    .eq(FmProduct::getIsDelete, 0);
        vo.setProductCount(productMapper.selectCount(countWrapper).intValue());

        return vo;
    }

    // ==================== 管理员后台 Admin ====================

    @Override
    public PageResult<StoreVO> listAllStores(int page, int pageSize, Integer status) {
        Page<FmStore> mpPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<FmStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmStore::getIsDelete, 0);
        if (status != null) {
            queryWrapper.eq(FmStore::getStatus, status);
        }
        queryWrapper.orderByDesc(FmStore::getCreateTime);

        Page<FmStore> resultPage = storeMapper.selectPage(mpPage, queryWrapper);

        List<StoreVO> voList = resultPage.getRecords().stream().map(store -> {
            StoreVO vo = StoreVO.fromEntity(store);
            FmUser owner = userMapper.selectById(store.getUserId());
            vo.setOwnerNickname(owner != null ? owner.getNickname() : "未知 Unknown");
            LambdaQueryWrapper<FmProduct> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(FmProduct::getStoreId, store.getId())
                        .eq(FmProduct::getIsDelete, 0);
            vo.setProductCount(productMapper.selectCount(countWrapper).intValue());
            return vo;
        }).toList();

        return new PageResult<>(resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize(), voList);
    }

    @Override
    public PageResult<StoreVO> listPendingStores(int page, int pageSize) {
        Page<FmStore> mpPage = new Page<>(page, pageSize);

        LambdaQueryWrapper<FmStore> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmStore::getStatus, StoreStatusEnum.PENDING.getCode())
                    .eq(FmStore::getIsDelete, 0)
                    .orderByAsc(FmStore::getCreateTime);

        Page<FmStore> resultPage = storeMapper.selectPage(mpPage, queryWrapper);

        List<StoreVO> voList = resultPage.getRecords().stream().map(store -> {
            StoreVO vo = StoreVO.fromEntity(store);
            FmUser owner = userMapper.selectById(store.getUserId());
            vo.setOwnerNickname(owner != null ? owner.getNickname() : "未知 Unknown");
            LambdaQueryWrapper<FmProduct> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(FmProduct::getStoreId, store.getId())
                        .eq(FmProduct::getIsDelete, 0);
            vo.setProductCount(productMapper.selectCount(countWrapper).intValue());
            return vo;
        }).toList();

        return new PageResult<>(resultPage.getTotal(), (int) resultPage.getCurrent(), (int) resultPage.getSize(), voList);
    }

    @Override
    @Transactional
    public void reviewStore(Long storeId, StoreReviewRequest req) {
        FmStore store = storeMapper.selectById(storeId);
        if (store == null || store.getIsDelete() == 1) {
            BizCodeEnum.STORE_NOT_FOUND.throwEx();
        }
        if (StoreStatusEnum.PENDING.getCode() != store.getStatus()) {
            BizCodeEnum.STORE_ALREADY_REVIEWED.throwEx();
        }

        int status = req.getStatus();
        if (status != StoreStatusEnum.APPROVED.getCode() && status != StoreStatusEnum.REJECTED.getCode()) {
            BizCodeEnum.INVALID_REVIEW_STATUS.throwEx();
        }

        store.setStatus(status);
        store.setReviewComment(req.getReviewComment());

        storeMapper.updateById(store);
        log.info("店铺审核完成 Store reviewed: storeId={}, status={}, comment={}",
                storeId, status, req.getReviewComment());
    }
}
