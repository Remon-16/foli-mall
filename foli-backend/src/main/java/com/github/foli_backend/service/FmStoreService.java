package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.request.StoreApplyRequest;
import com.github.foli_backend.dto.request.StoreReviewRequest;
import com.github.foli_backend.dto.response.StoreDetailVO;
import com.github.foli_backend.dto.response.StoreVO;
import com.github.foli_backend.entity.FmStore;

/**
 * 店铺服务接口 Store service interface
 * 提供店铺相关的业务逻辑
 * Provides store-related business logic
 */
public interface FmStoreService {

    /**
     * 申请开店
     * Apply to open a store
     * 创建店铺，初始状态为 PENDING(0) 待审核
     * Creates a store with initial status PENDING(0)
     *
     * @param userId 申请用户ID applicant user ID
     * @param req    开店申请表单 application form
     * @return 新创建的店铺实体 newly created store entity
     * @throws com.github.foli_backend.common.BusinessException 用户已有店铺时抛出 thrown when user already has a store
     */
    FmStore applyStore(Long userId, StoreApplyRequest req);

    /**
     * 获取当前用户的店铺
     * Get the current user's store
     *
     * @param userId 用户ID user ID
     * @return 店铺实体 store entity, or null if not found
     */
    FmStore getMyStore(Long userId);

    /**
     * 更新店铺信息
     * Update store information
     * 验证店铺所有权，仅店主可修改
     * Verifies store ownership, only the owner can update
     *
     * @param storeId 店铺ID store ID
     * @param userId  当前用户ID current user ID (for ownership check)
     * @param req     更新表单 update form
     * @return 更新后的店铺实体 updated store entity
     * @throws com.github.foli_backend.common.BusinessException 店铺不存在或无权操作时抛出
     */
    FmStore updateStore(Long storeId, Long userId, StoreApplyRequest req);

    /**
     * 分页查询已通过审核的店铺列表 (公开接口)
     * Paginated list of approved stores (public endpoint)
     * 包含店主昵称和商品数量统计
     * Includes owner nickname and product count
     *
     * @param page     页码 page number (1-based)
     * @param pageSize 每页大小 page size
     * @return 分页结果 paginated result wrapping StoreVO list
     */
    PageResult<StoreVO> listApprovedStores(int page, int pageSize);

    /**
     * 获取店铺详情 (公开接口)
     * Get store detail (public endpoint)
     * 包含店主昵称和商品数量
     * Includes owner nickname and product count
     *
     * @param storeId 店铺ID store ID
     * @return 店铺详情VO store detail VO
     * @throws com.github.foli_backend.common.BusinessException 店铺不存在时抛出
     */
    StoreDetailVO getStoreDetail(Long storeId);

    /**
     * 管理员 - 分页查询所有店铺 (可筛选状态)
     * Admin - paginated list of all stores (optional status filter)
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @param status   店铺状态 (null=全部) store status (null=all)
     * @return 分页结果 paginated result
     */
    PageResult<StoreVO> listAllStores(int page, int pageSize, Integer status);

    /**
     * 管理员 - 分页查询待审核店铺
     * Admin - paginated list of pending stores
     *
     * @param page     页码 page number
     * @param pageSize 每页大小 page size
     * @return 分页结果 paginated result (status=PENDING only)
     */
    PageResult<StoreVO> listPendingStores(int page, int pageSize);

    /**
     * 管理员 - 审核店铺
     * Admin - review a store
     * 只能审核状态为 PENDING 的店铺
     * Only stores with status PENDING can be reviewed
     *
     * @param storeId 店铺ID store ID
     * @param req     审核请求 review request (status: 1=APPROVED, 2=REJECTED)
     * @throws com.github.foli_backend.common.BusinessException 店铺不存在或状态不允许审核时抛出
     */
    void reviewStore(Long storeId, StoreReviewRequest req);
}
