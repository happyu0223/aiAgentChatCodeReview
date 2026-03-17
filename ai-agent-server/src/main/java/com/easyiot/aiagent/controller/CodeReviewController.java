package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.dto.CodeReviewDTO;
import com.easyiot.aiagent.model.entity.CodeReview;
import com.easyiot.aiagent.mapper.CodeReviewMapper;
import com.easyiot.aiagent.service.agent.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/code-review")
public class CodeReviewController {
    
    private final CodeReviewAgent codeReviewAgent;
    private final CodeReviewMapper codeReviewMapper;
    
    public CodeReviewController(CodeReviewAgent codeReviewAgent, 
                               CodeReviewMapper codeReviewMapper) {
        this.codeReviewAgent = codeReviewAgent;
        this.codeReviewMapper = codeReviewMapper;
    }
    
    @PostMapping("/submit")
    public Result<?> submit(@RequestBody CodeReviewDTO dto, @RequestHeader("X-User-Id") Long userId) {
        AgentRequest request = new AgentRequest();
        request.setUserId(userId);
        request.setData(dto);
        
        AgentResponse response = codeReviewAgent.execute(request);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<CodeReview> getById(@PathVariable Long id) {
        CodeReview review = codeReviewMapper.selectById(id);
        return Result.success(review);
    }
    
    @GetMapping("/list")
    public Result<List<CodeReview>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        
        LambdaQueryWrapper<CodeReview> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(CodeReview::getStatus, status);
        }
        wrapper.orderByDesc(CodeReview::getCreatedAt);
        
        Page<CodeReview> pageResult = codeReviewMapper.selectPage(new Page<>(page, size), wrapper);
        
        return Result.success(pageResult.getRecords());
    }
}
