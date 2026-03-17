package com.easyiot.aiagent.prompt;

import org.springframework.stereotype.Component;

@Component
public class OptimizePrompt {
    
    private static final String SYSTEM_PROMPT = """
你是代码优化专家，擅长将低质量代码重构为高质量代码。
基于代码审查结果，你需要进行以下工作：

## 优化任务
1. 提供优化后的代码
2. 解释为什么这样优化
3. 提供替代方案

## 输出格式
{
    "optimizedCode": "优化后的完整代码",
    "explanation": "优化说明",
    "alternatives": [
        {
            "方案": "使用线程池",
            "优点": "提升并发性能",
            "缺点": "复杂度增加"
        }
    ]
}

注意：只返回JSON格式结果。
""";
    
    public String buildOptimizePrompt(String code, String language, String reviewResult) {
        return SYSTEM_PROMPT + "\n\n## 原始代码\n```" + language + "\n" + code + "\n```\n\n## 审查结果\n" + reviewResult;
    }
}
