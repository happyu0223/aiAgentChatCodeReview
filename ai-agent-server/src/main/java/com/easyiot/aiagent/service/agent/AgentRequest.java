package com.easyiot.aiagent.service.agent;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgentRequest implements Serializable {
    private String content;
    private Object data;
    private Long userId;
    private String sessionId;
    private String agentType;
}
