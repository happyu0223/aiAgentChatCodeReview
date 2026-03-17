package com.easyiot.aiagent.service.agent;

import com.easyiot.aiagent.mapper.ApprovalFlowMapper;
import com.easyiot.aiagent.model.dto.ApprovalDTO;
import com.easyiot.aiagent.model.entity.ApprovalFlow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ApprovalAgent extends BaseAgent {
    
    private final ApprovalFlowMapper approvalFlowMapper;
    
    public ApprovalAgent(ApprovalFlowMapper approvalFlowMapper) {
        this.approvalFlowMapper = approvalFlowMapper;
    }
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        ApprovalDTO dto = (ApprovalDTO) request.getData();
        
        ApprovalFlow flow = new ApprovalFlow();
        flow.setFlowNo(generateFlowNo());
        flow.setTitle(dto.getTitle());
        flow.setApplicantId(request.getUserId());
        flow.setApproverId(dto.getApproverId());
        flow.setFlowType(dto.getFlowType());
        flow.setContent(dto.getContent());
        flow.setStatus("pending");
        flow.setPriority(dto.getPriority() != null ? dto.getPriority() : 1);
        flow.setNotifyChannels("webhook");
        flow.setNotifyStatus("not_sent");
        
        approvalFlowMapper.insert(flow);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("flowId", flow.getId());
        responseData.put("flowNo", flow.getFlowNo());
        responseData.put("status", flow.getStatus());
        
        return AgentResponse.success(responseData);
    }
    
    public AgentResponse approve(Long flowId, String comment) {
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
        if (flow == null) {
            return AgentResponse.error("审批流程不存在");
        }
        
        flow.setStatus("approved");
        flow.setApprovalComment(comment);
        flow.setApprovedAt(LocalDateTime.now());
        approvalFlowMapper.updateById(flow);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("flowId", flow.getId());
        responseData.put("status", flow.getStatus());
        
        return AgentResponse.success(responseData);
    }
    
    public AgentResponse reject(Long flowId, String comment) {
        ApprovalFlow flow = approvalFlowMapper.selectById(flowId);
        if (flow == null) {
            return AgentResponse.error("审批流程不存在");
        }
        
        flow.setStatus("rejected");
        flow.setApprovalComment(comment);
        approvalFlowMapper.updateById(flow);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("flowId", flow.getId());
        responseData.put("status", flow.getStatus());
        
        return AgentResponse.success(responseData);
    }
    
    private String generateFlowNo() {
        return "APV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
