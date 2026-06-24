package com.github.foli_backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 会话视图对象 / Conversation view object
 */
@Schema(description = "会话 Conversation")
public class ConversationVO {

    @Schema(description = "会话ID Conversation ID")
    private String conversationId;

    @Schema(description = "对方用户ID Other user ID")
    private Long otherUserId;

    @Schema(description = "对方用户名称 Other user name")
    private String otherUserName;

    @Schema(description = "最后一条消息 Last message")
    private String lastMessage;

    @Schema(description = "未读消息数 Unread count")
    private Integer unreadCount;

    @Schema(description = "最后消息时间 Last message time")
    private LocalDateTime lastMessageTime;

    public ConversationVO() {}

    public ConversationVO(String conversationId, Long otherUserId, String otherUserName,
                          String lastMessage, Integer unreadCount, LocalDateTime lastMessageTime) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
        this.lastMessageTime = lastMessageTime;
    }

    // ========== Getters and Setters ==========

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Integer getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
}
