package com.github.foli_backend.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.annotation.RequireRole;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.dto.request.ComplaintHandleRequest;
import com.github.foli_backend.dto.request.ProductReviewRequest;
import com.github.foli_backend.dto.request.StoreReviewRequest;
import com.github.foli_backend.dto.response.ComplaintVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.dto.response.ReturnRefundVO;
import com.github.foli_backend.dto.response.StoreVO;
import com.github.foli_backend.dto.response.UserVO;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.FmComplaintService;
import com.github.foli_backend.service.FmProductService;
import com.github.foli_backend.service.FmReturnRefundService;
import com.github.foli_backend.service.FmStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员后台控制器 Admin backend controller
 * 所有接口要求登录且角色为管理员
 * All endpoints require login and ADMIN role
 * 其他管理端点（用户管理、订单管理等）由其他模块后续补充
 * Other admin endpoints (user management, orders, etc.) will be added later
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员后台 Admin")
@RequireLogin
@RequireRole(RoleConstants.ADMIN)
public class AdminController {

    private final FmStoreService storeService;
    private final FmProductService productService;
    private final FmReturnRefundService returnRefundService;
    private final FmComplaintService complaintService;
    private final FmUserMapper userMapper;

    /**
     * 构造器注入 Constructor injection
     */
    public AdminController(FmStoreService storeService,
                           FmProductService productService,
                           FmReturnRefundService returnRefundService,
                           FmComplaintService complaintService,
                           FmUserMapper userMapper) {
        this.storeService = storeService;
        this.productService = productService;
        this.returnRefundService = returnRefundService;
        this.complaintService = complaintService;
        this.userMapper = userMapper;
    }

    // ==================== 店铺管理 Store Management ====================

    /**
     * 分页查询所有店铺（可按状态筛选）
     * Paginated list of all stores with optional status filter
     */
    @GetMapping("/stores")
    @Operation(summary = "店铺列表", description = "分页查询所有店铺，可选按状态筛选 GetAll stores with optional status filter")
    public Result<PageResult<StoreVO>> listStores(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "15")
            @RequestParam(defaultValue = "15") int pageSize,
            @Parameter(description = "店铺状态筛选: 0=待审核 1=已通过 2=已拒绝 不传=全部 Optional status filter", example = "0")
            @RequestParam(required = false) Integer status) {
        PageResult<StoreVO> result = storeService.listAllStores(page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 查询待审核店铺列表
     * List pending stores awaiting review
     */
    @GetMapping("/stores/pending")
    @Operation(summary = "待审核店铺", description = "分页查询待审核的店铺列表 Get pending stores awaiting review")
    public Result<PageResult<StoreVO>> listPendingStores(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "15")
            @RequestParam(defaultValue = "15") int pageSize) {
        PageResult<StoreVO> result = storeService.listPendingStores(page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核店铺（通过或拒绝）
     * Review a store (approve or reject)
     */
    @PutMapping("/stores/{id}/review")
    @Operation(summary = "审核店铺", description = "管理员审核店铺：通过或拒绝 Admin reviews a store: approve or reject")
    public Result<Void> reviewStore(
            @Parameter(description = "店铺ID Store ID", example = "1")
            @PathVariable("id") Long storeId,
            @Valid @RequestBody StoreReviewRequest req) {
        storeService.reviewStore(storeId, req);
        return Result.success("审核操作已执行 Review completed", null);
    }

    // ==================== 商品审核 Product Review ====================

    /**
     * 查询待审核商品列表 List pending review products
     */
    @GetMapping("/products/pending")
    @Operation(summary = "待审核商品列表 Pending products", description = "管理员查询所有待审核商品 Admin lists all pending review products")
    public Result<PageResult<ProductVO>> listPendingProducts(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "20")
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<ProductVO> result = productService.listPendingProducts(page, pageSize);
        return Result.success(result);
    }

    /**
     * 审核商品（通过或拒绝）Review product (approve or reject)
     */
    @PutMapping("/products/{id}/review")
    @Operation(summary = "审核商品 Review product", description = "管理员审核商品，通过(status=2)或拒绝(status=3) Admin approves or rejects a product")
    public Result<Void> reviewProduct(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductReviewRequest req) {
        productService.reviewProduct(id, req);
        return Result.success("审核完成 Review completed", null);
    }

    /**
     * 查询所有商品列表（可按状态筛选）List all products (filterable by status)
     */
    @GetMapping("/products")
    @Operation(summary = "所有商品列表 All products", description = "管理员查看所有商品，可按状态筛选 Admin views all products, filterable by status")
    public Result<PageResult<ProductVO>> listAllProducts(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商品状态(可选): 0=草稿 1=待审核 2=已通过 3=已拒绝 4=已下架 Product status (optional)")
            @RequestParam(required = false) Integer status) {
        PageResult<ProductVO> result = productService.listAllProducts(page, pageSize, status);
        return Result.success(result);
    }

    // ==================== 退货退款管理 Return/Refund Management ====================

    /**
     * 平台所有退货列表 Admin return/refund list
     */
    @GetMapping("/returns")
    @Operation(summary = "平台退货列表 All returns", description = "管理员查看所有退货退款记录 Admin views all return/refund records")
    public Result<PageResult<ReturnRefundVO>> listReturns(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "状态筛选 Status filter (optional)") @RequestParam(required = false) Integer status) {
        PageResult<ReturnRefundVO> result = returnRefundService.listAllReturns(page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 平台仲裁争议 Admin handle dispute
     */
    @PutMapping("/returns/{id}/handle-dispute")
    @Operation(summary = "平台仲裁争议 Handle dispute", description = "管理员对争议中的退货进行仲裁，可选择同意退款或驳回 Admin arbitrates disputed returns, can approve refund or reject")
    public Result<Void> handleDispute(@Parameter(description = "退货ID Return ID") @PathVariable Long id,
                                       @RequestBody Map<String, String> body) {
        String decision = body.get("decision");
        if (decision == null || decision.isBlank()) {
            BizCodeEnum.BAD_REQUEST.throwEx("处理决策不能为空 Decision cannot be blank");
        }
        if (!"refund".equals(decision) && !"reject".equals(decision)) {
            BizCodeEnum.BAD_REQUEST.throwEx("处理决策必须为 refund 或 reject Decision must be refund or reject");
        }
        String result = body.getOrDefault("result", "");
        returnRefundService.handleDispute(id, decision, result);
        return Result.success();
    }

    // ==================== 投诉管理 Complaint Management ====================

    /**
     * 平台所有投诉列表 Admin complaint list
     */
    @GetMapping("/complaints")
    @Operation(summary = "平台投诉列表 All complaints", description = "管理员查看所有投诉记录 Admin views all complaint records")
    public Result<PageResult<ComplaintVO>> listComplaints(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "状态筛选 Status filter (optional)") @RequestParam(required = false) Integer status) {
        PageResult<ComplaintVO> result = complaintService.listAllComplaints(page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 平台处理投诉 Admin handle complaint
     */
    @PutMapping("/complaints/{id}/handle")
    @Operation(summary = "处理投诉 Handle complaint", description = "管理员处理投诉，标记为已解决或已驳回 Admin handles complaint, marks as resolved or rejected")
    public Result<Void> handleComplaint(@Parameter(description = "投诉ID Complaint ID") @PathVariable Long id,
                                         @Valid @RequestBody ComplaintHandleRequest req) {
        Long handlerId = com.github.foli_backend.context.UserContext.getUserId();
        complaintService.handleComplaint(id, handlerId, req);
        return Result.success();
    }

    // ==================== 用户管理 User Management ====================

    /**
     * 平台用户列表 Admin user list
     */
    @GetMapping("/users")
    @Operation(summary = "平台用户列表 User list", description = "管理员查看所有用户，可按角色和状态筛选 Admin views all users with role/status filter")
    public Result<PageResult<UserVO>> listUsers(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "角色筛选 Role filter (optional): 0=BUYER 1=SELLER 2=ADMIN") @RequestParam(required = false) Integer role,
            @Parameter(description = "状态筛选 Status filter (optional)") @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<FmUser> wrapper = new LambdaQueryWrapper<FmUser>()
                .orderByDesc(FmUser::getCreateTime);

        if (role != null) {
            wrapper.eq(FmUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(FmUser::getStatus, status);
        }

        Page<FmUser> mpPage = userMapper.selectPage(Page.of(page, pageSize), wrapper);
        List<UserVO> voList = mpPage.getRecords().stream()
                .map(UserVO::fromEntity)
                .collect(Collectors.toList());

        PageResult<UserVO> result = new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(),
                (int) mpPage.getSize(), voList);
        return Result.success(result);
    }

    /**
     * 修改用户状态 Update user status
     */
    @PutMapping("/users/{id}/status")
    @Operation(summary = "修改用户状态 Update user status", description = "管理员启用或禁用用户 Admin enables or disables a user")
    public Result<Void> updateUserStatus(@Parameter(description = "用户ID User ID") @PathVariable Long id,
                                          @Parameter(description = "状态 Status: 0=正常 1=禁用") @RequestParam Integer status) {
        FmUser user = userMapper.selectById(id);
        if (user == null) {
            BizCodeEnum.USER_NOT_FOUND.throwEx();
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success();
    }
}
