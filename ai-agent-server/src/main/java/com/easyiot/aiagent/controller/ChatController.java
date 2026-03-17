package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.dto.ChatDTO;
import com.easyiot.aiagent.model.entity.ChatSession;
import com.easyiot.aiagent.mapper.ChatSessionMapper;
import com.easyiot.aiagent.service.agent.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
public class ChatController {
    
    private final ChatAgent chatAgent;
    private final ChatSessionMapper chatSessionMapper;
    
    public ChatController(ChatAgent chatAgent, ChatSessionMapper chatSessionMapper) {
        this.chatAgent = chatAgent;
        this.chatSessionMapper = chatSessionMapper;
    }
    
    @PostMapping("/session/create")
    public Result<ChatSession> createSession(@RequestHeader("X-User-Id") Long userId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionId(UUID.randomUUID().toString());
        session.setTitle("新对话");
        session.setAgentType("chat");
        session.setContextCount(0);
        
        chatSessionMapper.insert(session);
        
        return Result.success(session);
    }
    
    @GetMapping("/session/list")
    public Result<List<ChatSession>> listSessions(@RequestHeader("X-User-Id") Long userId) {
        List<ChatSession> sessions = chatSessionMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .orderByDesc(ChatSession::getLastMessageTime)
        );
        
        return Result.success(sessions);
    }
    
    @DeleteMapping("/session/{id}")
    public Result<?> deleteSession(@PathVariable Long id) {
        chatSessionMapper.deleteById(id);
        return Result.success();
    }
    
    @PostMapping("/message/send")
    public Result<?> sendMessage(@RequestBody ChatDTO dto, @RequestHeader("X-User-Id") Long userId) {
        AgentRequest request = new AgentRequest();
        request.setUserId(userId);
        request.setData(dto);
        
        AgentResponse response = chatAgent.execute(request);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
}
