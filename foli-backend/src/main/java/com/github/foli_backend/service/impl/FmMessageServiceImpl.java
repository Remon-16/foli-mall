package com.github.foli_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.dto.response.ConversationVO;
import com.github.foli_backend.dto.response.MessageVO;
import com.github.foli_backend.entity.FmMessage;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.mapper.FmMessageMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.FmMessageService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现 / Message service implementation
 */
@Service
public class FmMessageServiceImpl implements FmMessageService {

    private final FmMessageMapper messageMapper;
    private final FmUserMapper userMapper;

    public FmMessageServiceImpl(FmMessageMapper messageMapper, FmUserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.userMapper = userMapper;
    }

    /**
     * 生成会话ID Generate conversation ID
     * 格式: min(userId)_max(userId)
     */
    private String buildConversationId(Long userId1, Long userId2) {
        if (userId1 < userId2) {
            return userId1 + "_" + userId2;
        }
        return userId2 + "_" + userId1;
    }

    @Override
    public MessageVO sendMessage(Long senderId, Long receiverId, String content) {
        // 生成会话ID generate conversation ID
        String conversationId = buildConversationId(senderId, receiverId);

        // 创建消息 create message
        FmMessage message = new FmMessage();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        messageMapper.insert(message);

        // 构建返回结果 build response
        MessageVO vo = MessageVO.fromEntity(message);

        FmUser sender = userMapper.selectById(senderId);
        vo.setSenderName(sender != null ? sender.getNickname() : null);

        FmUser receiver = userMapper.selectById(receiverId);
        vo.setReceiverName(receiver != null ? receiver.getNickname() : null);

        return vo;
    }

    @Override
    public List<ConversationVO> getConversations(Long userId) {
        // 查询用户参与的所有消息 (作为发送者或接收者) query all messages user participated in
        List<FmMessage> allMessages = messageMapper.selectList(
                new LambdaQueryWrapper<FmMessage>()
                        .and(wrapper -> wrapper
                                .eq(FmMessage::getSenderId, userId)
                                .or()
                                .eq(FmMessage::getReceiverId, userId)
                        )
                        .orderByDesc(FmMessage::getCreateTime)
        );

        if (allMessages == null || allMessages.isEmpty()) {
            return Collections.emptyList();
        }

        // 按会话ID分组 group by conversation ID
        Map<String, List<FmMessage>> groupedByConversation = allMessages.stream()
                .collect(Collectors.groupingBy(FmMessage::getConversationId, LinkedHashMap::new, Collectors.toList()));

        List<ConversationVO> conversations = new ArrayList<>();
        for (Map.Entry<String, List<FmMessage>> entry : groupedByConversation.entrySet()) {
            String convId = entry.getKey();
            List<FmMessage> messages = entry.getValue();

            // 最后一条消息 the latest message
            FmMessage latestMsg = messages.get(0);

            // 未读消息数 (接收者是当前用户且未读) unread count (receiver is current user and unread)
            long unreadCount = messages.stream()
                    .filter(m -> m.getReceiverId().equals(userId) && (m.getIsRead() == null || m.getIsRead() == 0))
                    .count();

            // 对方用户ID other user ID
            Long otherUserId = latestMsg.getSenderId().equals(userId)
                    ? latestMsg.getReceiverId()
                    : latestMsg.getSenderId();

            FmUser otherUser = userMapper.selectById(otherUserId);

            ConversationVO conv = new ConversationVO();
            conv.setConversationId(convId);
            conv.setOtherUserId(otherUserId);
            conv.setOtherUserName(otherUser != null ? otherUser.getNickname() : null);
            conv.setLastMessage(latestMsg.getContent());
            conv.setUnreadCount((int) unreadCount);
            conv.setLastMessageTime(latestMsg.getCreateTime());
            conversations.add(conv);
        }

        // 按最后消息时间降序排列 sort by last message time descending
        conversations.sort((a, b) -> {
            if (a.getLastMessageTime() == null) return 1;
            if (b.getLastMessageTime() == null) return -1;
            return b.getLastMessageTime().compareTo(a.getLastMessageTime());
        });

        return conversations;
    }

    @Override
    public PageResult<MessageVO> getConversationMessages(String conversationId, int page, int pageSize) {
        Page<FmMessage> mpPage = messageMapper.selectPage(Page.of(page, pageSize),
                new LambdaQueryWrapper<FmMessage>()
                        .eq(FmMessage::getConversationId, conversationId)
                        .orderByAsc(FmMessage::getCreateTime)
        );

        List<MessageVO> voList = mpPage.getRecords().stream()
                .map(msg -> {
                    MessageVO vo = MessageVO.fromEntity(msg);
                    FmUser sender = userMapper.selectById(msg.getSenderId());
                    vo.setSenderName(sender != null ? sender.getNickname() : null);
                    FmUser receiver = userMapper.selectById(msg.getReceiverId());
                    vo.setReceiverName(receiver != null ? receiver.getNickname() : null);
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(mpPage.getTotal(), (int) mpPage.getCurrent(), (int) mpPage.getSize(), voList);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        FmMessage message = messageMapper.selectById(messageId);
        if (message == null) {
            BizCodeEnum.MESSAGE_NOT_FOUND.throwEx();
        }
        if (!message.getReceiverId().equals(userId)) {
            BizCodeEnum.FORBIDDEN.throwEx();
        }

        message.setIsRead(1);
        messageMapper.updateById(message);
    }

    @Override
    public void markConversationRead(String conversationId, Long userId) {
        // 标记该会话中接收者是当前用户的所有未读消息为已读
        // Mark all unread messages in conversation where receiver is current user as read
        messageMapper.update(null,
                new LambdaUpdateWrapper<FmMessage>()
                        .set(FmMessage::getIsRead, 1)
                        .eq(FmMessage::getConversationId, conversationId)
                        .eq(FmMessage::getReceiverId, userId)
                        .eq(FmMessage::getIsRead, 0)
        );
    }
}
