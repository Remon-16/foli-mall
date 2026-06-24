package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 投诉处理请求 / Complaint handle request (admin)
 */
@Schema(description = "投诉处理请求 Handle complaint request")
public class ComplaintHandleRequest {

    @Schema(description = "处理状态 Handle status: 2=已解决 3=已驳回", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "处理状态不能为空 Status cannot be null")
    private Integer status;

    @Schema(description = "处理结果 Handle result description")
    private String handleResult;

    // ========== Getters and Setters ==========

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }
}
