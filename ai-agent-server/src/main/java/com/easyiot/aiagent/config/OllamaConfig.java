package com.easyiot.aiagent.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "ollama")
public class OllamaConfig {
    @Value("${ollama.base-url}")
    private String baseUrl;
    @Value("${ollama.default-model}")
    private String defaultModel;
    @Value("${ollama.code-model}")
    private String codeModel;
    private long timeout = 120000;
    private long streamTimeout = 60000;
    
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getDefaultModel() {
        return defaultModel;
    }
    
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }
    
    public String getCodeModel() {
        return codeModel;
    }
    
    public void setCodeModel(String codeModel) {
        this.codeModel = codeModel;
    }
    
    public long getTimeout() {
        return timeout;
    }
    
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    public long getStreamTimeout() {
        return streamTimeout;
    }
    
    public void setStreamTimeout(long streamTimeout) {
        this.streamTimeout = streamTimeout;
    }
}
