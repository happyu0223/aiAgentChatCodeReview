package com.easyiot.aiagent.service.agent;

import com.easyiot.aiagent.config.OllamaConfig;
import com.easyiot.aiagent.mapper.CodeReviewMapper;
import com.easyiot.aiagent.model.dto.OptimizeDTO;
import com.easyiot.aiagent.model.entity.CodeReview;
import com.easyiot.aiagent.service.OllamaService;
import com.easyiot.aiagent.service.PromptTemplateService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OptimizeAgent extends BaseAgent {
    
    private final OllamaService ollamaService;
    private final CodeReviewMapper codeReviewMapper;
    private final PromptTemplateService promptTemplateService;
    private final OllamaConfig ollamaConfig;
    
    public OptimizeAgent(OllamaService ollamaService, 
                        CodeReviewMapper codeReviewMapper,
                        PromptTemplateService promptTemplateService,
                        OllamaConfig ollamaConfig) {
        this.ollamaService = ollamaService;
        this.codeReviewMapper = codeReviewMapper;
        this.promptTemplateService = promptTemplateService;
        this.ollamaConfig = ollamaConfig;
    }
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        OptimizeDTO dto = (OptimizeDTO) request.getData();
        
        long startTime = System.currentTimeMillis();
        
        String systemPrompt = promptTemplateService.getSystemPrompt("optimize");
        
        if (systemPrompt == null) {
            systemPrompt = "你是代码优化专家，请优化以下代码并给出JSON格式的结果。";
        }
        
        String userPrompt = "## 原始代码\n```" + dto.getLanguage() + "\n" + dto.getCodeContent() + "\n```\n\n## 审查结果\n" + dto.getReviewResult();
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        
        String result = ollamaService.chat(fullPrompt, ollamaConfig.getCodeModel());
        
        long duration = System.currentTimeMillis() - startTime;
        
        CodeReview review = codeReviewMapper.selectById(dto.getReviewId());
        if (review != null) {
            review.setOptimizedCode(result);
            review.setOptimizeSuggestion(result);
            review.setStatus("optimized");
            codeReviewMapper.updateById(review);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviewId", dto.getReviewId());
        responseData.put("optimizedCode", result);
        responseData.put("duration", duration);
        
        return AgentResponse.success(responseData);
    }
}
