package com.easyiot.aiagent.service.agent;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentDispatcher {
    
    private final Map<String, BaseAgent> agentMap = new HashMap<>();
    
    public AgentDispatcher(
            CodeReviewAgent codeReviewAgent,
            OptimizeAgent optimizeAgent,
            ChatAgent chatAgent,
            LearnAgent learnAgent,
            ApprovalAgent approvalAgent) {
        agentMap.put("CODE_REVIEW", codeReviewAgent);
        agentMap.put("OPTIMIZE", optimizeAgent);
        agentMap.put("CHAT", chatAgent);
        agentMap.put("LEARN", learnAgent);
        agentMap.put("APPROVAL", approvalAgent);
    }
    
    public AgentResponse dispatch(AgentRequest request) {
        String agentType = recognizeAgentType(request.getContent());
        
        if (request.getAgentType() != null) {
            agentType = request.getAgentType();
        }
        
        BaseAgent agent = agentMap.get(agentType);
        if (agent == null) {
            agent = agentMap.get("CHAT");
        }
        
        return agent.execute(request);
    }
    
    private String recognizeAgentType(String content) {
        if (content == null) {
            return "CHAT";
        }
        
        String lowerContent = content.toLowerCase();
        
        if (lowerContent.contains("审查") || lowerContent.contains("review") 
            || lowerContent.contains("代码") || lowerContent.contains("检查")) {
            return "CODE_REVIEW";
        }
        
        if (lowerContent.contains("优化") || lowerContent.contains("改进")
            || lowerContent.contains("重构")) {
            return "OPTIMIZE";
        }
        
        if (lowerContent.contains("审批") || lowerContent.contains("申请")
            || lowerContent.contains("流程")) {
            return "APPROVAL";
        }
        
        if (lowerContent.contains("知识") || lowerContent.contains("文档")
            || lowerContent.contains("学习") || lowerContent.contains("资料")) {
            return "LEARN";
        }
        
        return "CHAT";
    }
}
