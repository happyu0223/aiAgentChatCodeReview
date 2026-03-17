package com.easyiot.aiagent.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.easyiot.aiagent.mapper.PromptTemplateMapper;
import com.easyiot.aiagent.model.entity.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PromptTemplateService {
    
    private final PromptTemplateMapper promptTemplateMapper;
    private final Map<String, String> promptCache = new ConcurrentHashMap<>();
    
    public PromptTemplateService(PromptTemplateMapper promptTemplateMapper) {
        this.promptTemplateMapper = promptTemplateMapper;
    }
    
    public String getSystemPrompt(String promptKey) {
        if (promptCache.containsKey(promptKey)) {
            return promptCache.get(promptKey);
        }
        
        LambdaQueryWrapper<PromptTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PromptTemplate::getPromptKey, promptKey)
               .eq(PromptTemplate::getIsActive, 1);
        
        PromptTemplate template = promptTemplateMapper.selectOne(wrapper);
        
        if (template != null) {
            promptCache.put(promptKey, template.getSystemPrompt());
            return template.getSystemPrompt();
        }
        
        return null;
    }
    
    public List<PromptTemplate> getAllPrompts() {
        return promptTemplateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .orderByDesc(PromptTemplate::getCreatedAt)
        );
    }
    
    public PromptTemplate getById(Long id) {
        return promptTemplateMapper.selectById(id);
    }
    
    public boolean save(PromptTemplate template) {
        if (template.getId() == null) {
            return promptTemplateMapper.insert(template) > 0;
        } else {
            template.setVersion(template.getVersion() + 1);
            promptCache.remove(template.getPromptKey());
            return promptTemplateMapper.updateById(template) > 0;
        }
    }
    
    public boolean delete(Long id) {
        PromptTemplate template = promptTemplateMapper.selectById(id);
        if (template != null) {
            promptCache.remove(template.getPromptKey());
        }
        return promptTemplateMapper.deleteById(id) > 0;
    }
    
    public void clearCache() {
        promptCache.clear();
    }
}
