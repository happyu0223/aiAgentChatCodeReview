package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.dto.CodeReviewDTO;
import com.easyiot.aiagent.service.agent.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/code-review")
public class GitWebhookController {
    
    private final CodeReviewAgent codeReviewAgent;
    
    public GitWebhookController(CodeReviewAgent codeReviewAgent) {
        this.codeReviewAgent = codeReviewAgent;
    }
    
    @PostMapping("/webhook/gitlab")
    public Result<?> gitlabWebhook(@RequestBody Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> commit = (Map<String, Object>) payload.get("commit");
            if (commit == null) {
                return Result.badRequest("无效的GitLab webhook payload");
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> changes = (java.util.List<Map<String, Object>>) commit.get("modified");
            if (changes == null || changes.isEmpty()) {
                return Result.success("没有变更的文件");
            }
            
            StringBuilder report = new StringBuilder();
            report.append("## Git提交代码审查报告\n\n");
            
            for (Map<String, Object> file : changes) {
                String filename = (String) file.get("filename");
                String[] parts = filename.split("\\.");
                String ext = parts.length > 1 ? parts[parts.length - 1] : "";
                
                String language = getLanguage(ext);
                if (language == null) continue;
                
                String diff = (String) file.get("diff");
                if (diff == null || diff.isEmpty()) continue;
                
                CodeReviewDTO dto = new CodeReviewDTO();
                dto.setProjectName("gitlab-webhook");
                dto.setFileName(filename);
                dto.setCodeContent(diff);
                dto.setLanguage(language);
                dto.setReviewType("gitlab");
                
                AgentRequest request = new AgentRequest();
                request.setUserId(1L);
                request.setData(dto);
                
                AgentResponse response = codeReviewAgent.execute(request);
                
                if (response.isSuccess() && response.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) response.getData();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> reviewResult = (Map<String, Object>) result.get("result");
                    
                    if (reviewResult != null && reviewResult.containsKey("summary")) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> summary = (Map<String, Object>) reviewResult.get("summary");
                        int total = (Integer) summary.getOrDefault("totalIssues", 0);
                        int critical = (Integer) summary.getOrDefault("critical", 0);
                        
                        report.append("### ").append(filename).append("\n");
                        report.append("- 总问题数: ").append(total).append("\n");
                        report.append("- 严重问题: ").append(critical).append("\n\n");
                    }
                }
            }
            
            return Result.success(report.toString());
        } catch (Exception e) {
            return Result.error("处理失败: " + e.getMessage());
        }
    }
    
    private String getLanguage(String ext) {
        return switch (ext.toLowerCase()) {
            case "java" -> "java";
            case "py" -> "python";
            case "js", "jsx", "ts", "tsx", "vue" -> "javascript";
            case "go" -> "go";
            default -> null;
        };
    }
}
