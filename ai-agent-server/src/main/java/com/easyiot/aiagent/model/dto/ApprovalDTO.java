package com.easyiot.aiagent.model.dto;

import lombok.Data;

@Data
public class ApprovalDTO {
    private String title;
    private String content;
    private Long approverId;
    private String flowType;
    private Integer priority;
    private Long reviewId;
}
