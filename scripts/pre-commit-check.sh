#!/bin/bash

echo "正在检查提交代码..."

API_URL="http://localhost:8080/api/code-review/submit"

# 获取暂存区的代码变更
STAGED_FILES=$(git diff --cached --name-only --diff-filter=ACM)

if [ -z "$STAGED_FILES" ]; then
    echo "没有暂存的代码文件"
    exit 0
fi

for file in $STAGED_FILES; do
    echo "检查文件: $file"
    
    EXTENSION="${file##*.}"
    LANGUAGE=""
    
    case $EXTENSION in
        java) LANGUAGE="java" ;;
        py) LANGUAGE="python" ;;
        js) LANGUAGE="javascript" ;;
        vue) LANGUAGE="vue" ;;
        ts) LANGUAGE="javascript" ;;
        go) LANGUAGE="go" ;;
        *) continue ;;
    esac
    
    # 获取文件内容
    CONTENT=$(git show ":$file" 2>/dev/null)
    
    if [ -z "$CONTENT" ]; then
        continue
    fi
    
    # 调用API审查
    RESPONSE=$(curl -s -X POST "$API_URL" \
        -H "Content-Type: application/json" \
        -H "X-User-Id: 1" \
        -d "{
            \"projectName\": \"git-commit\",
            \"fileName\": \"$file\",
            \"language\": \"$LANGUAGE\",
            \"codeContent\": $(echo "$CONTENT" | jq -Rs .),
            \"reviewType\": \"git\"
        }")
    
    # 提取问题数量
    ISSUES=$(echo "$RESPONSE" | jq -r '.data.result.summary.totalIssues // 0')
    CRITICAL=$(echo "$RESPONSE" | jq -r '.data.result.summary.critical // 0')
    
    if [ "$ISSUES" -gt 0 ]; then
        echo "⚠️  发现 $ISSUES 个问题 (严重: $CRITICAL)"
        
        if [ "$CRITICAL" -gt 0 ]; then
            echo "❌ 存在严重问题，阻止提交"
            echo "$RESPONSE" | jq -r '.data.result.issues[]? | "  - [\( .severity)] \( .title): \( .message)"'
            exit 1
        fi
        
        echo "⚠️  存在非严重问题，警告但继续提交"
        echo "$RESPONSE" | jq -r '.data.result.issues[]? | "  - [\( .severity)] \( .title): \( .message)"'
    else
        echo "✅ 检查通过"
    fi
done

echo "代码检查完成"
exit 0
