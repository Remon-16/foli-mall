package com.github.foli_backend.service;

import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.response.ConversationVO;
import com.github.foli_backend.dto.response.MessageVO;

import java.util.List;

/**
 * 消息服务接口 / Message service interface
 */
public interface FmMessageService {

    /**
     * 发送消息 Send message
     *
     * @param senderId   发送者ID sender user ID
     * @param receiverId 接收者ID receiver user ID
     * @param content    消息内容 content
     * @return 消息视图 message view object
     */
    MessageVO sendMessage(Long senderId, Long receiverId, String content);

    /**
     * 获取用户的所有会话 Get all conversations for user
     *
     * @param userId 用户ID user ID
     * @return 会话列表 conversation list
     */
    List<ConversationVO> getConversations(Long userId);

    /**
     * 获取会话消息列表 (分页) Get paginated messages in a conversation
     *
     * @param conversationId 会话ID conversation ID
     * @param page           页码 page number
     * @param pageSize       每页大小 page size
     * @return 分页消息结果 paginated messages
     */
    PageResult<MessageVO> getConversationMessages(String conversationId, int page, int pageSize);

    /**
     * 标记单条消息为已读 Mark single message as read
     *
     * @param messageId 消息ID message ID
     * @param userId    用户ID user ID
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 标记会话中所有消息为已读 Mark all messages in conversation as read
     *
     * @param conversationId 会话ID conversation ID
     * @param userId         用户ID user ID
     */
    void markConversationRead(String conversationId, Long userId);
}
