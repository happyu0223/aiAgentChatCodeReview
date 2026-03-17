package com.easyiot.aiagent.service.agent;

import com.easyiot.aiagent.config.OllamaConfig;
import com.easyiot.aiagent.mapper.ChatMessageMapper;
import com.easyiot.aiagent.mapper.ChatSessionMapper;
import com.easyiot.aiagent.model.dto.ChatDTO;
import com.easyiot.aiagent.model.entity.ChatMessage;
import com.easyiot.aiagent.model.entity.ChatSession;
import com.easyiot.aiagent.service.OllamaService;
import com.easyiot.aiagent.service.PromptTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatAgent extends BaseAgent {
    
    private final OllamaService ollamaService;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final PromptTemplateService promptTemplateService;
    private final OllamaConfig ollamaConfig;
    
    public ChatAgent(OllamaService ollamaService,
                    ChatMessageMapper chatMessageMapper,
                    ChatSessionMapper chatSessionMapper,
                    PromptTemplateService promptTemplateService,
                    OllamaConfig ollamaConfig) {
        this.ollamaService = ollamaService;
        this.chatMessageMapper = chatMessageMapper;
        this.chatSessionMapper = chatSessionMapper;
        this.promptTemplateService = promptTemplateService;
        this.ollamaConfig = ollamaConfig;
    }
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        ChatDTO dto = (ChatDTO) request.getData();
        
        List<Map<String, String>> history = getHistory(dto.getSessionId());
        
        String systemPrompt = promptTemplateService.getSystemPrompt("chat");
        
        if (systemPrompt == null) {
            systemPrompt = "你是公司的AI智能助手，可以帮助用户解答技术问题、代码问题等。请用友好、专业的方式回答用户的问题。";
        }
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        history.add(0, systemMsg);
        
        String answer = ollamaService.chatCompletion(dto.getMessage(), history);
        
        saveMessage(request.getUserId(), dto.getSessionId(), "user", dto.getMessage());
        saveMessage(request.getUserId(), dto.getSessionId(), "assistant", answer);
        
        updateSessionTime(dto.getSessionId());
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", answer);
        responseData.put("sessionId", dto.getSessionId());
        
        return AgentResponse.success(responseData);
    }
    
    private List<Map<String, String>> getHistory(String sessionId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
               .orderByAsc(ChatMessage::getCreatedAt)
               .last("LIMIT 20");
        
        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        
        List<Map<String, String>> history = new ArrayList<>();
        for (ChatMessage msg : messages) {
            Map<String, String> map = new HashMap<>();
            map.put("role", msg.getRole());
            map.put("content", msg.getContent());
            history.add(map);
        }
        
        return history;
    }
    
    private void saveMessage(Long userId, String sessionId, String role, String content) {
        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setSessionId(sessionId);
        message.setRole(role);
        message.setContent(content);
        message.setModelUsed(ollamaConfig.getDefaultModel());
        chatMessageMapper.insert(message);
    }
    
    private void updateSessionTime(String sessionId) {
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getSessionId, sessionId);
        ChatSession session = chatSessionMapper.selectOne(wrapper);
        
        if (session != null) {
            session.setLastMessageTime(LocalDateTime.now());
            session.setContextCount(session.getContextCount() + 2);
            chatSessionMapper.updateById(session);
        }
    }
}
