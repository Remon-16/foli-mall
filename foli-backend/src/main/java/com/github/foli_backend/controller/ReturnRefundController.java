package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.ReturnCreateRequest;
import com.github.foli_backend.dto.response.ReturnRefundVO;
import com.github.foli_backend.service.FmReturnRefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 退货退款控制器 / Return/refund controller
 */
@RestController
@RequestMapping("/api/returns")
@Tag(name = "退货退款 Returns")
@RequireLogin
public class ReturnRefundController {

    private final FmReturnRefundService returnRefundService;

    public ReturnRefundController(FmReturnRefundService returnRefundService) {
        this.returnRefundService = returnRefundService;
    }

    /**
     * 创建退货退款申请 Create return/refund request
     */
    @PostMapping
    @Operation(summary = "创建退货退款申请 Create return/refund request")
    public Result<ReturnRefundVO> createReturn(@Valid @RequestBody ReturnCreateRequest req) {
        Long userId = UserContext.getUserId();
        ReturnRefundVO vo = returnRefundService.createReturn(userId, req);
        return Result.success(vo);
    }

    /**
     * 买家退货列表 Buyer's return list
     */
    @GetMapping
    @Operation(summary = "买家退货列表 Buyer return list")
    public Result<PageResult<ReturnRefundVO>> listReturns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<ReturnRefundVO> result = returnRefundService.listBuyerReturns(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取退货详情 Get return detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取退货详情 Get return detail")
    public Result<ReturnRefundVO> getReturnDetail(@PathVariable Long id) {
        ReturnRefundVO vo = returnRefundService.getReturnDetail(id);
        return Result.success(vo);
    }

    /**
     * 买家退回商品 Ship back goods
     */
    @PutMapping("/{id}/ship-back")
    @Operation(summary = "买家退回商品 Ship back")
    public Result<Void> shipBack(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        returnRefundService.shipBack(id, userId);
        return Result.success();
    }
}
