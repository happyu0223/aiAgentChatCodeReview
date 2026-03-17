package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.dto.ApprovalDTO;
import com.easyiot.aiagent.model.entity.ApprovalFlow;
import com.easyiot.aiagent.mapper.ApprovalFlowMapper;
import com.easyiot.aiagent.service.agent.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/approval")
public class ApprovalController {
    
    private final ApprovalAgent approvalAgent;
    private final ApprovalFlowMapper approvalFlowMapper;
    
    public ApprovalController(ApprovalAgent approvalAgent, ApprovalFlowMapper approvalFlowMapper) {
        this.approvalAgent = approvalAgent;
        this.approvalFlowMapper = approvalFlowMapper;
    }
    
    @PostMapping("/create")
    public Result<?> create(@RequestBody ApprovalDTO dto, @RequestHeader("X-User-Id") Long userId) {
        AgentRequest request = new AgentRequest();
        request.setUserId(userId);
        request.setData(dto);
        
        AgentResponse response = approvalAgent.execute(request);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<ApprovalFlow> getById(@PathVariable Long id) {
        ApprovalFlow flow = approvalFlowMapper.selectById(id);
        return Result.success(flow);
    }
    
    @GetMapping("/list")
    public Result<List<ApprovalFlow>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        LambdaQueryWrapper<ApprovalFlow> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ApprovalFlow::getStatus, status);
        }
        wrapper.orderByDesc(ApprovalFlow::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApprovalFlow> pageResult = 
            approvalFlowMapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), wrapper);
        
        return Result.success(pageResult.getRecords());
    }
    
    @PostMapping("/{id}/approve")
    public Result<?> approve(@PathVariable Long id, @RequestBody(required = false) String comment) {
        AgentResponse response = approvalAgent.approve(id, comment);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    @PostMapping("/{id}/reject")
    public Result<?> reject(@PathVariable Long id, @RequestBody(required = false) String comment) {
        AgentResponse response = approvalAgent.reject(id, comment);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result<List<ApprovalFlow>> pendingList(@RequestHeader("X-User-Id") Long userId) {
        List<ApprovalFlow> flows = approvalFlowMapper.selectList(
            new LambdaQueryWrapper<ApprovalFlow>()
                .eq(ApprovalFlow::getApproverId, userId)
                .eq(ApprovalFlow::getStatus, "pending")
                .orderByDesc(ApprovalFlow::getCreatedAt)
        );
        
        return Result.success(flows);
    }
}
