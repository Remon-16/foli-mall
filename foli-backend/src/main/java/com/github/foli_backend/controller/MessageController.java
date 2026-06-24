package com.github.foli_backend.controller;

import com.github.foli_backend.annotation.RequireLogin;
import com.github.foli_backend.common.PageResult;
import com.github.foli_backend.common.Result;
import com.github.foli_backend.context.UserContext;
import com.github.foli_backend.dto.request.MessageSendRequest;
import com.github.foli_backend.dto.response.ConversationVO;
import com.github.foli_backend.dto.response.MessageVO;
import com.github.foli_backend.service.FmMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息控制器 / Message controller
 */
@RestController
@RequestMapping("/api/messages")
@Tag(name = "消息沟通 Messages")
@RequireLogin
public class MessageController {

    private final FmMessageService messageService;

    public MessageController(FmMessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 发送消息 Send message
     */
    @PostMapping
    @Operation(summary = "发送消息 Send message")
    public Result<MessageVO> sendMessage(@Valid @RequestBody MessageSendRequest req) {
        Long userId = UserContext.getUserId();
        MessageVO vo = messageService.sendMessage(userId, req.getReceiverId(), req.getContent());
        return Result.success(vo);
    }

    /**
     * 获取会话列表 Get conversations list
     */
    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表 Get conversations")
    public Result<List<ConversationVO>> getConversations() {
        Long userId = UserContext.getUserId();
        List<ConversationVO> conversations = messageService.getConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 获取会话消息列表 Get conversation messages
     */
    @GetMapping("/conversation/{conversationId}")
    @Operation(summary = "获取会话消息 Get conversation messages")
    public Result<PageResult<MessageVO>> getConversationMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<MessageVO> result = messageService.getConversationMessages(conversationId, page, pageSize);
        return Result.success(result);
    }

    /**
     * 标记消息为已读 Mark message as read
     */
    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读 Mark as read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        messageService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 标记整个会话为已读 Mark all messages in conversation as read
     */
    @PutMapping("/conversation/{conversationId}/read-all")
    @Operation(summary = "标记会话全部已读 Mark conversation read")
    public Result<Void> markConversationRead(@PathVariable String conversationId) {
        Long userId = UserContext.getUserId();
        messageService.markConversationRead(conversationId, userId);
        return Result.success();
    }
}
