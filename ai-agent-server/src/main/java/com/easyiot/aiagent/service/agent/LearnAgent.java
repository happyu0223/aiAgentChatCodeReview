package com.easyiot.aiagent.service.agent;

import com.easyiot.aiagent.config.OllamaConfig;
import com.easyiot.aiagent.mapper.KnowledgeDocMapper;
import com.easyiot.aiagent.model.dto.LearnDTO;
import com.easyiot.aiagent.model.entity.KnowledgeDoc;
import com.easyiot.aiagent.service.OllamaService;
import com.easyiot.aiagent.service.PromptTemplateService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LearnAgent extends BaseAgent {
    
    private final OllamaService ollamaService;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final PromptTemplateService promptTemplateService;
    private final OllamaConfig ollamaConfig;
    
    public LearnAgent(OllamaService ollamaService,
                     KnowledgeDocMapper knowledgeDocMapper,
                     PromptTemplateService promptTemplateService,
                     OllamaConfig ollamaConfig) {
        this.ollamaService = ollamaService;
        this.knowledgeDocMapper = knowledgeDocMapper;
        this.promptTemplateService = promptTemplateService;
        this.ollamaConfig = ollamaConfig;
    }
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        LearnDTO dto = (LearnDTO) request.getData();
        
        List<KnowledgeDoc> docs = searchKnowledge(dto.getQuestion());
        
        String context = buildContext(docs);
        
        String systemPrompt = promptTemplateService.getSystemPrompt("learn");
        
        if (systemPrompt == null) {
            systemPrompt = "你是公司的智能助手，请根据提供的知识库内容回答用户问题。";
        }
        
        String userPrompt = "## 知识库内容\n" + context + "\n\n## 用户问题\n" + dto.getQuestion();
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        
        String answer = ollamaService.chat(fullPrompt, ollamaConfig.getDefaultModel());
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("answer", answer);
        responseData.put("sources", docs.stream().map(doc -> {
            Map<String, Object> source = new HashMap<>();
            source.put("id", doc.getId());
            source.put("title", doc.getTitle());
            return source;
        }).toList());
        
        return AgentResponse.success(responseData);
    }
    
    private List<KnowledgeDoc> searchKnowledge(String question) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getVectorStatus, "completed")
               .eq(KnowledgeDoc::getIsPublic, 1)
               .orderByDesc(KnowledgeDoc::getViewCount)
               .last("LIMIT 5");
        
        return knowledgeDocMapper.selectList(wrapper);
    }
    
    private String buildContext(List<KnowledgeDoc> docs) {
        StringBuilder context = new StringBuilder();
        for (KnowledgeDoc doc : docs) {
            context.append("【").append(doc.getTitle()).append("】\n");
            context.append(doc.getContent()).append("\n\n");
        }
        return context.toString();
    }
}
