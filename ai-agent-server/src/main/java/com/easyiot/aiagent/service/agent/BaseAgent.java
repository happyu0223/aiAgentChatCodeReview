package com.easyiot.aiagent.service.agent;

public abstract class BaseAgent {
    
    public abstract AgentResponse execute(AgentRequest request);
    
    protected String getAgentType() {
        return this.getClass().getSimpleName();
    }
}
