package com.easyiot.aiagent.controller;

import com.easyiot.aiagent.common.result.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/code-format")
public class CodeFormatController {
    
    @PostMapping("/format")
    public Result<String> format(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        String language = params.get("language");
        
        if (code == null || code.isEmpty()) {
            return Result.badRequest("代码不能为空");
        }
        
        String formatted = formatCode(code, language);
        return Result.success(formatted);
    }
    
    private String formatCode(String code, String language) {
        if (code == null) return "";
        
        String[] lines = code.split("\n");
        StringBuilder result = new StringBuilder();
        int indent = 0;
        
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                result.append("\n");
                continue;
            }
            
            if (trimmed.startsWith("}") || trimmed.startsWith("};") || 
                trimmed.startsWith("]") || trimmed.startsWith("];")) {
                indent = Math.max(0, indent - 1);
            }
            
            for (int i = 0; i < indent; i++) {
                result.append("    ");
            }
            result.append(trimmed).append("\n");
            
            if (trimmed.endsWith("{") || trimmed.endsWith("{")) {
                indent++;
            }
        }
        
        return result.toString().trim();
    }
}
