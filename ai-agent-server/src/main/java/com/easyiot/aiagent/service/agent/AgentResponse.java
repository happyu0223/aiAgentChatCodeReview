package com.easyiot.aiagent.service.agent;

import lombok.Data;

import java.io.Serializable;

@Data
public class AgentResponse implements Serializable {
    private boolean success;
    private String message;
    private Object data;
    private long duration;
    private String modelUsed;
    
    public static AgentResponse success(Object data) {
        AgentResponse response = new AgentResponse();
        response.setSuccess(true);
        response.setData(data);
        response.setMessage("success");
        return response;
    }
    
    public static AgentResponse success(String message, Object data) {
        AgentResponse response = new AgentResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    public static AgentResponse error(String message) {
        AgentResponse response = new AgentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
