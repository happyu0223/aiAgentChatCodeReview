package com.easyiot.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String promptKey;
    
    private String promptName;
    
    private String promptType;
    
    private String systemPrompt;
    
    private String userPromptTemplate;
    
    private String description;
    
    private Integer isActive;
    
    private Integer version;
    
    private Long createdBy;
    
    private Long updatedBy;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
