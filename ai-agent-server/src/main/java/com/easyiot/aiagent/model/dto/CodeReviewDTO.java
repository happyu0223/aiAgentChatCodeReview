package com.easyiot.aiagent.model.dto;

import lombok.Data;

@Data
public class CodeReviewDTO {
    private String projectName;
    private String fileName;
    private String filePath;
    private String codeContent;
    private String language;
    private String reviewType;
}
