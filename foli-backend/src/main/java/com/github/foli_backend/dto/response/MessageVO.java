package com.github.foli_backend.dto.response;

import com.github.foli_backend.entity.FmMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 消息视图对象 / Message view object
 */
@Schema(description = "消息 Message")
public class MessageVO {

    @Schema(description = "消息ID Message ID")
    private Long id;

    @Schema(description = "会话ID Conversation ID")
    private String conversationId;

    @Schema(description = "发送者ID Sender ID")
    private Long senderId;

    @Schema(description = "发送者名称 Sender name")
    private String senderName;

    @Schema(description = "接收者ID Receiver ID")
    private Long receiverId;

    @Schema(description = "接收者名称 Receiver name")
    private String receiverName;

    @Schema(description = "消息内容 Content")
    private String content;

    @Schema(description = "是否已读 Is read: 0=未读 1=已读")
    private Integer isRead;

    @Schema(description = "创建时间 Create time")
    private LocalDateTime createTime;

    /**
     * 从实体构建视图对象 Build view object from entity
     *
     * @param msg 消息实体 message entity
     * @return MessageVO
     */
    public static MessageVO fromEntity(FmMessage msg) {
        MessageVO vo = new MessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setContent(msg.getContent());
        vo.setIsRead(msg.getIsRead());
        vo.setCreateTime(msg.getCreateTime());
        return vo;
    }

    // ========== Getters and Setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
