package com.easyiot.aiagent.service;

import com.easyiot.aiagent.config.OllamaConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OllamaService {
    
    private final OkHttpClient okHttpClient;
    private final OllamaConfig ollamaConfig;
    private final ObjectMapper objectMapper;
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public OllamaService(OkHttpClient okHttpClient, OllamaConfig ollamaConfig, ObjectMapper objectMapper) {
        this.okHttpClient = okHttpClient;
        this.ollamaConfig = ollamaConfig;
        this.objectMapper = objectMapper;
    }
    
    public String chat(String prompt) {
        return chat(prompt, ollamaConfig.getDefaultModel(), null);
    }
    
    public String chat(String prompt, String model) {
        return chat(prompt, model, null);
    }
    
    public String chat(String prompt, String model, List<Map<String, String>> history) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            
            if (history != null && !history.isEmpty()) {
                messages.addAll(history);
            }
            
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("stream", false);
            requestBody.put("options", Map.of(
                "temperature", 0.7,
                "top_p", 0.9,
                "num_ctx", 4096
            ));
            
            Request request = new Request.Builder()
                    .url(ollamaConfig.getBaseUrl() + "/api/chat")
                    .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON))
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Ollama API返回错误: {} - {}", response.code(), response.message());
                    throw new RuntimeException("Ollama调用失败: " + response.code() + " " + response.message());
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode messageNode = jsonNode.get("message");
                if (messageNode != null) {
                    return messageNode.get("content").asText();
                }
                return jsonNode.get("response").asText();
            }
        } catch (IOException e) {
            log.error("Ollama调用异常", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage(), e);
        }
    }
    
    public String chatCompletion(String prompt, List<Map<String, String>> messages) {
        return chatCompletion(prompt, ollamaConfig.getDefaultModel(), messages);
    }
    
    public String chatCompletion(String prompt, String model, List<Map<String, String>> messages) {
        return chat(prompt, model, messages);
    }
    
    public String generateCode(String code, String language) {
        String prompt = "请审查以下" + language + "代码，并给出审查结果:\n\n" + code;
        return chat(prompt, ollamaConfig.getCodeModel());
    }
    
    public List<Float> embed(String text) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "nomic-embed-text");
            requestBody.put("prompt", text);
            
            Request request = new Request.Builder()
                    .url(ollamaConfig.getBaseUrl() + "/api/embeddings")
                    .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), JSON))
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Ollama embedding调用失败: " + response);
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode embedding = jsonNode.get("embedding");
                
                List<Float> result = new ArrayList<>();
                if (embedding != null) {
                    for (JsonNode node : embedding) {
                        result.add(node.floatValue());
                    }
                }
                return result;
            }
        } catch (IOException e) {
            log.error("Ollama embedding调用异常", e);
            throw new RuntimeException("向量生成失败: " + e.getMessage(), e);
        }
    }
    
    public boolean checkHealth() {
        try {
            Request request = new Request.Builder()
                    .url(ollamaConfig.getBaseUrl() + "/api/tags")
                    .get()
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            log.warn("Ollama健康检查失败: {}", e.getMessage());
            return false;
        }
    }
    
    public List<String> listModels() {
        try {
            Request request = new Request.Builder()
                    .url(ollamaConfig.getBaseUrl() + "/api/tags")
                    .get()
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return Collections.emptyList();
                }
                
                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                JsonNode models = jsonNode.get("models");
                
                List<String> modelNames = new ArrayList<>();
                if (models != null) {
                    for (JsonNode model : models) {
                        modelNames.add(model.get("name").asText());
                    }
                }
                return modelNames;
            }
        } catch (IOException e) {
            log.error("获取模型列表失败", e);
            return Collections.emptyList();
        }
    }
}
