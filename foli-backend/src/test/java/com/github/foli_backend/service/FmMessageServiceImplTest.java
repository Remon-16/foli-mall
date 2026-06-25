package com.github.foli_backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.foli_backend.common.BusinessException;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.constant.BizCodeEnum;
import com.github.foli_backend.dto.response.ConversationVO;
import com.github.foli_backend.dto.response.MessageVO;
import com.github.foli_backend.entity.FmMessage;
import com.github.foli_backend.entity.FmUser;
import com.github.foli_backend.helper.TestDataFactory;
import com.github.foli_backend.mapper.FmMessageMapper;
import com.github.foli_backend.mapper.FmUserMapper;
import com.github.foli_backend.service.impl.FmMessageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FmMessageServiceImpl 单元测试")
class FmMessageServiceImplTest {

    @Mock FmMessageMapper messageMapper;
    @Mock FmUserMapper userMapper;

    @InjectMocks
    FmMessageServiceImpl messageService;

    Long senderId = 1L;
    Long receiverId = 2L;
    String convId = "1_2";

    @Nested
    @DisplayName("sendMessage — 发送消息")
    class SendMessageTests {

        @Test
        @DisplayName("should_send_message_and_return_vo_when_valid_participants")
        void shouldSendMessageAndReturnVO_whenValidParticipants() {
            when(messageMapper.insert(any(FmMessage.class))).thenReturn(1);
            FmUser sender = TestDataFactory.createBuyer(senderId, "sender");
            FmUser receiver = TestDataFactory.createBuyer(receiverId, "receiver");
            when(userMapper.selectById(senderId)).thenReturn(sender);
            when(userMapper.selectById(receiverId)).thenReturn(receiver);

            MessageVO result = messageService.sendMessage(senderId, receiverId, "Hello");

            assertThat(result.getContent()).isEqualTo("Hello");
            assertThat(result.getSenderName()).isEqualTo("sender_nick");
            assertThat(result.getReceiverName()).isEqualTo("receiver_nick");
        }
    }

    @Nested
    @DisplayName("getConversations — 获取会话列表")
    class GetConversationsTests {

        @Test
        @DisplayName("should_return_empty_list_when_no_messages")
        void shouldReturnEmptyList_whenNoMessages() {
            when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

            List<ConversationVO> result = messageService.getConversations(senderId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markAsRead — 标记已读")
    class MarkAsReadTests {

        @Test
        @DisplayName("should_mark_as_read_when_user_is_receiver")
        void shouldMarkAsRead_whenUserIsReceiver() {
            FmMessage message = new FmMessage();
            message.setId(1L);
            message.setReceiverId(receiverId);
            when(messageMapper.selectById(1L)).thenReturn(message);
            when(messageMapper.updateById(any(FmMessage.class))).thenReturn(1);

            messageService.markAsRead(1L, receiverId);

            assertThat(message.getIsRead()).isEqualTo(1);
        }

        @Test
        @DisplayName("should_throw_forbidden_when_marking_read_as_non_receiver")
        void shouldThrowForbidden_whenMarkingReadAsNonReceiver() {
            FmMessage message = new FmMessage();
            message.setId(1L);
            message.setReceiverId(receiverId);
            when(messageMapper.selectById(1L)).thenReturn(message);

            assertThatThrownBy(() -> messageService.markAsRead(1L, senderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.FORBIDDEN.getCode());
        }

        @Test
        @DisplayName("should_throw_message_not_found_when_message_null")
        void shouldThrowMessageNotFound_whenMessageNull() {
            when(messageMapper.selectById(999L)).thenReturn(null);

            assertThatThrownBy(() -> messageService.markAsRead(999L, senderId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(BizCodeEnum.MESSAGE_NOT_FOUND.getCode());
        }
    }
}
