package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.dto.OptimizeDTO;
import com.easyiot.aiagent.model.entity.CodeReview;
import com.easyiot.aiagent.mapper.CodeReviewMapper;
import com.easyiot.aiagent.service.agent.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/optimize")
public class OptimizeController {
    
    private final OptimizeAgent optimizeAgent;
    private final CodeReviewMapper codeReviewMapper;
    
    public OptimizeController(OptimizeAgent optimizeAgent, CodeReviewMapper codeReviewMapper) {
        this.optimizeAgent = optimizeAgent;
        this.codeReviewMapper = codeReviewMapper;
    }
    
    @PostMapping("/{reviewId}")
    public Result<?> optimize(@PathVariable Long reviewId, @RequestHeader("X-User-Id") Long userId) {
        CodeReview review = codeReviewMapper.selectById(reviewId);
        if (review == null) {
            return Result.notFound("审查记录不存在");
        }
        
        OptimizeDTO dto = new OptimizeDTO();
        dto.setReviewId(reviewId);
        dto.setCodeContent(review.getCodeContent());
        dto.setLanguage(review.getLanguage());
        dto.setReviewResult(review.getReviewResult());
        
        AgentRequest request = new AgentRequest();
        request.setUserId(userId);
        request.setData(dto);
        
        AgentResponse response = optimizeAgent.execute(request);
        
        if (response.isSuccess()) {
            return Result.success(response.getData());
        } else {
            return Result.error(response.getMessage());
        }
    }
}
