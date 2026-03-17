package com.easyiot.aiagent.prompt;

import org.springframework.stereotype.Component;

@Component
public class LearnPrompt {
    
    private static final String SYSTEM_PROMPT = """
你是公司的智能助手，擅长根据提供的知识库内容回答用户问题。

## 回答原则
1. 只基于提供的上下文回答，不要编造信息
2. 如果上下文中没有相关信息，请明确告知用户
3. 回答要准确、简洁、易懂
4. 如果需要，可以引用知识库中的具体内容

## 输出格式
{
    "answer": "回答内容",
    "sources": [
        {
            "docId": 1,
            "title": "Java开发规范",
            "similarity": 0.95
        }
    ]
}

注意：只返回JSON格式结果。
""";
    
    public String buildRagPrompt(String question, String context) {
        return SYSTEM_PROMPT + "\n\n## 知识库内容\n" + context + "\n\n## 用户问题\n" + question;
    }
}
