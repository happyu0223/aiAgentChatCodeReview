package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import com.easyiot.aiagent.model.entity.PromptTemplate;
import com.easyiot.aiagent.service.PromptTemplateService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/prompt")
public class PromptController {
    
    private final PromptTemplateService promptTemplateService;
    
    public PromptController(PromptTemplateService promptTemplateService) {
        this.promptTemplateService = promptTemplateService;
    }
    
    @GetMapping("/list")
    public Result<List<PromptTemplate>> list() {
        List<PromptTemplate> list = promptTemplateService.getAllPrompts();
        return Result.success(list);
    }
    
    @GetMapping("/{id}")
    public Result<PromptTemplate> getById(@PathVariable Long id) {
        PromptTemplate template = promptTemplateService.getById(id);
        return Result.success(template);
    }
    
    @PostMapping
    public Result<?> save(@RequestBody PromptTemplate template) {
        boolean success = promptTemplateService.save(template);
        if (success) {
            return Result.success();
        } else {
            return Result.error("保存失败");
        }
    }
    
    @PutMapping
    public Result<?> update(@RequestBody PromptTemplate template) {
        boolean success = promptTemplateService.save(template);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新失败");
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = promptTemplateService.delete(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
    
    @PostMapping("/clear-cache")
    public Result<?> clearCache() {
        promptTemplateService.clearCache();
        return Result.success();
    }
}
