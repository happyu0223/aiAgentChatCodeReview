package com.easyiot.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("approval_flow")
public class ApprovalFlow implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long reviewId;
    
    private String flowNo;
    
    private String title;
    
    private Long applicantId;
    
    private Long approverId;
    
    private String flowType;
    
    private String content;
    
    private String status;
    
    private Integer priority;
    
    private LocalDateTime dueDate;
    
    private String approvalComment;
    
    private String notifyChannels;
    
    private String notifyStatus;
    
    private LocalDateTime approvedAt;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}
