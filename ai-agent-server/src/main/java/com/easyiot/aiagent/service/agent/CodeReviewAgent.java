package com.easyiot.aiagent.service.agent;

import com.easyiot.aiagent.mapper.CodeReviewMapper;
import com.easyiot.aiagent.model.dto.CodeReviewDTO;
import com.easyiot.aiagent.model.entity.CodeReview;
import com.easyiot.aiagent.service.OllamaService;
import com.easyiot.aiagent.service.PromptTemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CodeReviewAgent extends BaseAgent {
    
    @Value("${ollama.code-model}")
    private String codeReviewModel;
    
    private final OllamaService ollamaService;
    private final CodeReviewMapper codeReviewMapper;
    private final ObjectMapper objectMapper;
    private final PromptTemplateService promptTemplateService;
    
    public CodeReviewAgent(OllamaService ollamaService, 
                          CodeReviewMapper codeReviewMapper,
                          ObjectMapper objectMapper,
                          PromptTemplateService promptTemplateService) {
        this.ollamaService = ollamaService;
        this.codeReviewMapper = codeReviewMapper;
        this.objectMapper = objectMapper;
        this.promptTemplateService = promptTemplateService;
    }
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        CodeReviewDTO dto = (CodeReviewDTO) request.getData();
        
        long startTime = System.currentTimeMillis();
        
        String systemPrompt = promptTemplateService.getSystemPrompt("code_review");
        
        if (systemPrompt == null) {
            systemPrompt = "你是资深代码审查专家，请审查以下代码并给出JSON格式的审查结果。";
        }
        
        String userPrompt = "## 待审查代码\n```" + dto.getLanguage() + "\n" + dto.getCodeContent() + "\n```";
        String fullPrompt = systemPrompt + "\n\n" + userPrompt;
        
        String model = ollamaService.chat(fullPrompt, codeReviewModel);
        
        Map<String, Object> result = parseReviewResult(model);
        
        long duration = System.currentTimeMillis() - startTime;
        
        CodeReview review = new CodeReview();
        review.setUserId(request.getUserId());
        review.setProjectName(dto.getProjectName());
        review.setFileName(dto.getFileName());
        review.setFilePath(dto.getFilePath());
        review.setCodeContent(dto.getCodeContent());
        review.setLanguage(dto.getLanguage());
        review.setReviewType(dto.getReviewType());
        try{
            review.setReviewResult(objectMapper.writeValueAsString(result));
        }catch (Exception e){
            e.printStackTrace();
        }
        review.setModelUsed(codeReviewModel);
        review.setStatus("completed");
        review.setReviewTime((int) duration);
        
        if (result.containsKey("summary")) {
            Map<String, Object> summary = (Map<String, Object>) result.get("summary");
            review.setIssuesCount((Integer) summary.get("totalIssues"));
            review.setSeverityCritical((Integer) summary.get("critical"));
            review.setSeverityMajor((Integer) summary.get("major"));
            review.setSeverityMinor((Integer) summary.get("minor"));
        }
        
        codeReviewMapper.insert(review);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("reviewId", review.getId());
        responseData.put("result", result);
        responseData.put("duration", duration);
        
        return AgentResponse.success(responseData);
    }
    
    private Map<String, Object> parseReviewResult(String aiResponse) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            if (aiResponse != null && aiResponse.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(aiResponse);
                result = objectMapper.convertValue(jsonNode, Map.class);
            } else {
                result.put("rawResponse", aiResponse != null ? aiResponse : "");
                result.put("summary", Map.of(
                    "totalIssues", 0,
                    "critical", 0,
                    "major", 0,
                    "minor", 0
                ));
            }
        } catch (Exception e) {
            result.put("rawResponse", aiResponse != null ? aiResponse : "");
            result.put("parseError", e.getMessage());
            result.put("summary", Map.of(
                "totalIssues", 0,
                "critical", 0,
                "major", 0,
                "minor", 0
            ));
        }
        
        return result;
    }
}
