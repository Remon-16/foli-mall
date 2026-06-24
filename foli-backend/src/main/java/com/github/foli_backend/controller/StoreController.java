package com.github.foli_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.annotation.RequireRole;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.constant.ProductStatusEnum;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.StoreApplyRequest;
import com.github.foli_backend.dto.response.StoreDetailVO;
import com.github.foli_backend.dto.response.StoreVO;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.mapper.FmProductMapper;
import com.github.foli_backend.service.FmStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


/**
 * 店铺控制器 Store controller
 * 提供店铺公开查询接口和卖家店铺管理接口
 * Provides public store query endpoints and seller store management endpoints
 */
@RestController
@RequestMapping("/api/stores")
@Tag(name = "店铺管理 Stores")
public class StoreController {

    private final FmStoreService storeService;
    private final FmProductMapper productMapper;

    /**
     * 构造器注入 Constructor injection
     */
    public StoreController(FmStoreService storeService, FmProductMapper productMapper) {
        this.storeService = storeService;
        this.productMapper = productMapper;
    }

    // ==================== 公开接口 Public Endpoints ====================

    /**
     * 分页查询已通过审核的店铺列表
     * Paginated list of approved stores
     */
    @GetMapping
    @Operation(summary = "获取店铺列表", description = "分页查询已通过审核的店铺 Get paginated list of approved stores")
    public Result<PageResult<StoreVO>> listStores(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "12")
            @RequestParam(defaultValue = "12") int pageSize) {
        PageResult<StoreVO> result = storeService.listApprovedStores(page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取店铺详情
     * Get store detail
     * 公开接口，返回店主昵称和商品数量
     * Public endpoint, returns owner nickname and product count
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取店铺详情", description = "获取店铺详细信息，含店主昵称和商品数量 Get store detail with owner nickname and product count")
    public Result<StoreDetailVO> getStoreDetail(
            @Parameter(description = "店铺ID Store ID", example = "1")
            @PathVariable("id") Long id) {
        StoreDetailVO detail = storeService.getStoreDetail(id);
        return Result.success(detail);
    }

    /**
     * 查询该店铺的商品列表
     * Get products for this store
     * 公开接口，不依赖 productService，直接使用 FmProductMapper
     * Public endpoint, uses FmProductMapper directly instead of productService
     */
    @GetMapping("/{id}/products")
    @Operation(summary = "获取店铺商品列表", description = "查询指定店铺下已上架的商品 Get approved products for this store")
    public Result<PageResult<FmProduct>> listStoreProducts(
            @Parameter(description = "店铺ID Store ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "12")
            @RequestParam(defaultValue = "12") int pageSize) {
        // 使用 MyBatis-Plus 分页查询该店铺已上架的商品
        // Use MyBatis-Plus paginated query for approved products in this store
        Page<FmProduct> mpPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<FmProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FmProduct::getStoreId, id)
                    .eq(FmProduct::getStatus, ProductStatusEnum.APPROVED.getCode())
                    .eq(FmProduct::getIsDelete, 0)
                    .orderByDesc(FmProduct::getCreateTime);

        Page<FmProduct> resultPage = productMapper.selectPage(mpPage, queryWrapper);
        return Result.success(PageResult.of(resultPage));
    }

    // ==================== 卖家接口 Seller Endpoints ====================

    /**
     * 申请开店
     * Apply to open a store
     * 仅卖家角色可申请
     * Only users with SELLER role can apply
     */
    @PostMapping
    @RequireLogin
    @RequireRole(RoleConstants.SELLER)
    @Operation(summary = "申请开店", description = "卖家申请创建新店铺 Seller applies to create a new store")
    public Result<StoreVO> applyStore(
            @Valid @RequestBody StoreApplyRequest req) {
        Long userId = UserContext.getUserId();
        FmStore store = storeService.applyStore(userId, req);
        StoreVO vo = StoreVO.fromEntity(store);
        return Result.success("店铺申请已提交，等待审核 Store application submitted, pending review", vo);
    }

    /**
     * 更新店铺信息
     * Update store information
     * 仅店铺所属卖家可修改
     * Only the owning seller can update
     */
    @PutMapping("/{id}")
    @RequireLogin
    @RequireRole(RoleConstants.SELLER)
    @Operation(summary = "更新店铺信息", description = "卖家更新自己店铺的信息 Seller updates their own store info")
    public Result<StoreVO> updateStore(
            @Parameter(description = "店铺ID Store ID", example = "1")
            @PathVariable("id") Long storeId,
            @Valid @RequestBody StoreApplyRequest req) {
        Long userId = UserContext.getUserId();
        FmStore store = storeService.updateStore(storeId, userId, req);
        StoreVO vo = StoreVO.fromEntity(store);
        return Result.success("店铺信息已更新 Store info updated", vo);
    }
}
