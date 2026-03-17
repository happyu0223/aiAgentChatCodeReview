package com.easyiot.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("code_review")
public class CodeReview implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String projectName;
    
    private String fileName;
    
    private String filePath;
    
    @TableField("`code_content`")
    private String codeContent;
    
    private String language;
    
    private String reviewType;
    
    private String reviewResult;
    
    private Integer issuesCount;
    
    private Integer severityCritical;
    
    private Integer severityMajor;
    
    private Integer severityMinor;
    
    private String status;
    
    @TableField("`optimized_code`")
    private String optimizedCode;
    
    @TableField("`optimize_suggestion`")
    private String optimizeSuggestion;
    
    private Integer reviewTime;
    
    private String modelUsed;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
