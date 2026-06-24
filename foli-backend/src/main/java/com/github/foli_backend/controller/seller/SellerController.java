package com.github.foli_backend.controller.seller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.annotation.RequireRole;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.constant.RoleConstants;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.ProductPublishRequest;
import com.github.foli_backend.dto.request.ReturnDisputeRequest;
import com.github.foli_backend.dto.request.ReturnReviewRequest;
import com.github.foli_backend.dto.response.OrderVO;
import com.github.foli_backend.dto.response.ProductVO;
import com.github.foli_backend.dto.response.ReturnRefundVO;
import com.github.foli_backend.dto.response.StoreDetailVO;
import com.github.foli_backend.entity.FmProduct;
import com.github.foli_backend.entity.FmStore;
import com.github.foli_backend.mapper.FmStoreMapper;
import com.github.foli_backend.service.FmOrderService;
import com.github.foli_backend.service.FmProductService;
import com.github.foli_backend.service.FmReturnRefundService;
import com.github.foli_backend.service.FmStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 卖家中心控制器 Seller center controller
 * 所有接口要求登录且角色为卖家
 * All endpoints require login and SELLER role
 * 产品管理、订单管理等端点由其他模块补充
 * Product and order endpoints will be added by other agents
 */
@RestController
@RequestMapping("/api/seller")
@Tag(name = "卖家中心 Seller Center")
@RequireLogin
@RequireRole(RoleConstants.SELLER)
public class SellerController {

    private final FmStoreService storeService;
    private final FmStoreMapper storeMapper;
    private final FmProductService productService;
    private final FmOrderService orderService;
    private final FmReturnRefundService returnRefundService;

    /**
     * 构造器注入 Constructor injection
     */
    public SellerController(FmStoreService storeService,
                            FmStoreMapper storeMapper,
                            FmProductService productService,
                            FmOrderService orderService,
                            FmReturnRefundService returnRefundService) {
        this.storeService = storeService;
        this.storeMapper = storeMapper;
        this.productService = productService;
        this.orderService = orderService;
        this.returnRefundService = returnRefundService;
    }

    /**
     * 获取当前卖家的店铺信息
     * Get current seller's store information
     * 从 UserContext 获取当前用户ID，查询其所属店铺
     * Gets the current user ID from UserContext and queries their store
     */
    @GetMapping("/stores/my")
    @Operation(summary = "我的店铺", description = "获取当前登录卖家的店铺详情 Get current seller's store detail")
    public Result<StoreDetailVO> getMyStore() {
        Long userId = UserContext.getUserId();
        FmStore store = storeService.getMyStore(userId);
        if (store == null) {
            return Result.success("您还没有店铺 You don't have a store yet", null);
        }
        StoreDetailVO detail = storeService.getStoreDetail(store.getId());
        return Result.success(detail);
    }

    // ==================== 商品管理 Product Management ====================

    /**
     * 发布新商品 Publish new product
     */
    @PostMapping("/products")
    @Operation(summary = "发布商品 Publish product", description = "卖家发布新商品，发布后状态为待审核 Seller publishes a new product, status becomes PENDING_REVIEW")
    public Result<ProductVO> publishProduct(@Valid @RequestBody ProductPublishRequest req) {
        Long storeId = getCurrentStoreId();
        FmProduct product = productService.publishProduct(storeId, req);
        return Result.success("商品发布成功 Product published", ProductVO.fromEntity(product));
    }

    /**
     * 更新商品 Update product
     */
    @PutMapping("/products/{id}")
    @Operation(summary = "更新商品 Update product", description = "卖家更新商品信息，更新后需重新审核 Update product, requires re-review")
    public Result<ProductVO> updateProduct(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id,
            @Valid @RequestBody ProductPublishRequest req) {
        Long storeId = getCurrentStoreId();
        FmProduct product = productService.updateProduct(id, storeId, req);
        return Result.success("商品更新成功 Product updated", ProductVO.fromEntity(product));
    }

    /**
     * 删除商品 Delete product (逻辑删除 logical delete)
     */
    @DeleteMapping("/products/{id}")
    @Operation(summary = "删除商品 Delete product", description = "卖家逻辑删除商品 Seller logically deletes a product")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        productService.deleteProduct(id, storeId);
        return Result.success("商品删除成功 Product deleted", null);
    }

    /**
     * 卖家查询自己的商品列表 Seller lists own products
     */
    @GetMapping("/products")
    @Operation(summary = "卖家商品列表 Seller product list", description = "卖家查询自己的商品，可按状态筛选 Seller lists own products, filterable by status")
    public Result<PageResult<ProductVO>> listProducts(
            @Parameter(description = "页码 Page number", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,
            @Parameter(description = "商品状态(可选): 0=草稿 1=待审核 2=已通过 3=已拒绝 4=已下架 Product status (optional)")
            @RequestParam(required = false) Integer status) {
        Long storeId = getCurrentStoreId();
        PageResult<ProductVO> result = productService.listSellerProducts(storeId, page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 下架商品 Take product off shelf
     */
    @PutMapping("/products/{id}/off-shelf")
    @Operation(summary = "下架商品 Off shelf", description = "卖家下架商品 Seller takes a product off shelf")
    public Result<Void> offShelf(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        productService.offShelf(id, storeId);
        return Result.success("商品已下架 Product off-shelf", null);
    }

    /**
     * 重新上架商品 Put product back on shelf (need re-review)
     */
    @PutMapping("/products/{id}/on-shelf")
    @Operation(summary = "上架商品 On shelf", description = "卖家重新上架商品，需重新审核 Put product back on shelf, requires re-review")
    public Result<Void> onShelf(
            @Parameter(description = "商品ID Product ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        productService.onShelf(id, storeId);
        return Result.success("商品已重新上架，等待审核 Product on-shelf, pending review", null);
    }

    // ==================== 订单管理 Order Management ====================

    /**
     * 卖家订单列表 Seller order list
     */
    @GetMapping("/orders")
    @Operation(summary = "卖家订单列表 Seller order list", description = "卖家查看本店铺订单 Seller views own store orders")
    public Result<PageResult<OrderVO>> listOrders(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "订单状态筛选 Order status filter (optional)") @RequestParam(required = false) Integer status) {
        Long storeId = getCurrentStoreId();
        PageResult<OrderVO> result = orderService.listStoreOrders(storeId, page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 卖家订单详情 Seller order detail
     */
    @GetMapping("/orders/{id}")
    @Operation(summary = "卖家订单详情 Seller order detail", description = "卖家查看订单详情 Seller views order detail")
    public Result<OrderVO> getOrderDetail(@Parameter(description = "订单ID Order ID") @PathVariable Long id) {
        OrderVO vo = orderService.getOrderDetail(id);
        return Result.success(vo);
    }

    /**
     * 卖家发货 Ship order
     */
    @PutMapping("/orders/{id}/ship")
    @Operation(summary = "卖家发货 Ship order", description = "卖家对已支付订单进行发货 Seller ships a paid order")
    public Result<Void> shipOrder(@Parameter(description = "订单ID Order ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        orderService.shipOrder(id, storeId);
        return Result.success();
    }

    // ==================== 退货退款管理 Return/Refund Management ====================

    /**
     * 卖家退货列表 Seller return/refund list
     */
    @GetMapping("/returns")
    @Operation(summary = "卖家退货列表 Seller return list", description = "卖家查看本店铺退货退款申请 Seller views own store returns")
    public Result<PageResult<ReturnRefundVO>> listReturns(
            @Parameter(description = "页码 Page number") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小 Page size") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "状态筛选 Status filter (optional)") @RequestParam(required = false) Integer status) {
        Long storeId = getCurrentStoreId();
        PageResult<ReturnRefundVO> result = returnRefundService.listStoreReturns(storeId, page, pageSize, status);
        return Result.success(result);
    }

    /**
     * 卖家退货详情 Seller return detail
     */
    @GetMapping("/returns/{id}")
    @Operation(summary = "卖家退货详情 Seller return detail", description = "卖家查看退货退款详情 Seller views return detail")
    public Result<ReturnRefundVO> getReturnDetail(@Parameter(description = "退货ID Return ID") @PathVariable Long id) {
        ReturnRefundVO vo = returnRefundService.getReturnDetail(id);
        return Result.success(vo);
    }

    /**
     * 审核通过 Approve return request
     */
    @PutMapping("/returns/{id}/approve")
    @Operation(summary = "审核通过退货申请 Approve return", description = "卖家通过退货退款申请 Seller approves return request")
    public Result<Void> approveReturn(@Parameter(description = "退货ID Return ID") @PathVariable Long id,
                                       @RequestBody ReturnReviewRequest req) {
        Long storeId = getCurrentStoreId();
        req.setStatus(1); // APPROVED
        returnRefundService.reviewReturn(id, storeId, req);
        return Result.success();
    }

    /**
     * 审核拒绝 Reject return request
     */
    @PutMapping("/returns/{id}/reject")
    @Operation(summary = "审核拒绝退货申请 Reject return", description = "卖家拒绝退货退款申请 Seller rejects return request")
    public Result<Void> rejectReturn(@Parameter(description = "退货ID Return ID") @PathVariable Long id,
                                      @RequestBody ReturnReviewRequest req) {
        Long storeId = getCurrentStoreId();
        req.setStatus(2); // REJECTED
        returnRefundService.reviewReturn(id, storeId, req);
        return Result.success();
    }

    /**
     * 确认收到退货 Confirm receipt of returned goods
     */
    @PutMapping("/returns/{id}/confirm-receipt")
    @Operation(summary = "确认收到退货 Confirm receipt", description = "卖家确认收到买家退回的商品 Seller confirms receipt of returned goods")
    public Result<Void> confirmReceipt(@Parameter(description = "退货ID Return ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        returnRefundService.confirmReceipt(id, storeId);
        return Result.success();
    }

    /**
     * 验货通过 Inspect passed
     */
    @PutMapping("/returns/{id}/inspect-pass")
    @Operation(summary = "验货通过 Inspect passed", description = "卖家验货通过，系统自动退款给买家 Inspection passed, system refunds buyer")
    public Result<Void> inspectPass(@Parameter(description = "退货ID Return ID") @PathVariable Long id) {
        Long storeId = getCurrentStoreId();
        returnRefundService.inspectPass(id, storeId);
        return Result.success();
    }

    /**
     * 验货不通过 发起争议 Dispute return
     */
    @PutMapping("/returns/{id}/dispute")
    @Operation(summary = "发起争议 Dispute return", description = "卖家验货不通过，发起争议进入平台仲裁 Seller disputes return after failed inspection")
    public Result<Void> dispute(@Parameter(description = "退货ID Return ID") @PathVariable Long id,
                                 @RequestBody ReturnDisputeRequest req) {
        Long storeId = getCurrentStoreId();
        returnRefundService.dispute(id, storeId, req);
        return Result.success();
    }

    // ==================== 私有辅助方法 Private helper methods ====================

    /**
     * 获取当前登录卖家的店铺ID
     * Get current logged-in seller's store ID
     *
     * @return 店铺ID store ID
     * @throws BusinessException 如果卖家没有店铺 if seller has no store
     */
    private Long getCurrentStoreId() {
        Long userId = UserContext.getUserId();
        // 查询当前用户的店铺 find store by userId
        FmStore store = storeMapper.selectOne(
                new LambdaQueryWrapper<FmStore>().eq(FmStore::getUserId, userId)
        );
        if (store == null) {
            BizCodeEnum.STORE_NOT_CREATED.throwEx();
        }
        return store.getId();
    }
}
