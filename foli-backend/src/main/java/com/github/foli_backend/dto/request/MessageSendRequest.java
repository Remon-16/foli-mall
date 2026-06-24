package com.github.foli_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 发送消息请求 / Send message request
 */
@Schema(description = "发送消息请求 Send message request")
public class MessageSendRequest {

    @Schema(description = "接收者用户ID Receiver user ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "接收者ID不能为空 Receiver ID cannot be null")
    private Long receiverId;

    @Schema(description = "消息内容 Content", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "消息内容不能为空 Content cannot be blank")
    private String content;

    // ========== Getters and Setters ==========

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
