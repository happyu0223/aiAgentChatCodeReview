package com.easyiot.aiagent.model.dto;

import lombok.Data;

@Data
public class OptimizeDTO {
    private Long reviewId;
    private String codeContent;
    private String language;
    private String reviewResult;
}
