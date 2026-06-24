package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.ComplaintCreateRequest;
import com.github.foli_backend.dto.response.ComplaintVO;
import com.github.foli_backend.service.FmComplaintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 投诉控制器 / Complaint controller
 */
@RestController
@RequestMapping("/api/complaints")
@Tag(name = "投诉管理 Complaints")
@RequireLogin
public class ComplaintController {

    private final FmComplaintService complaintService;

    public ComplaintController(FmComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    /**
     * 创建投诉 Create complaint
     */
    @PostMapping
    @Operation(summary = "创建投诉 Create complaint")
    public Result<ComplaintVO> createComplaint(@Valid @RequestBody ComplaintCreateRequest req) {
        Long userId = UserContext.getUserId();
        ComplaintVO vo = complaintService.createComplaint(userId, req);
        return Result.success(vo);
    }

    /**
     * 用户投诉列表 User complaint list
     */
    @GetMapping
    @Operation(summary = "我的投诉列表 My complaint list")
    public Result<PageResult<ComplaintVO>> listComplaints(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        Long userId = UserContext.getUserId();
        PageResult<ComplaintVO> result = complaintService.listUserComplaints(userId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 获取投诉详情 Get complaint detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取投诉详情 Get complaint detail")
    public Result<ComplaintVO> getComplaintDetail(@PathVariable Long id) {
        ComplaintVO vo = complaintService.getComplaintDetail(id);
        return Result.success(vo);
    }
}
