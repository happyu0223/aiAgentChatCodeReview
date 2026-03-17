#!/bin/bash

# 安装Git Hook脚本

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
HOOK_FILE="$SCRIPT_DIR/.git/hooks/pre-commit"

# 创建hooks目录
mkdir -p "$SCRIPT_DIR/.git/hooks"

# 复制脚本
cp "$SCRIPT_DIR/scripts/pre-commit-check.sh" "$HOOK_FILE"

# 添加执行权限
chmod +x "$HOOK_FILE"

echo "✅ Git pre-commit hook 已安装"
echo "   位置: $HOOK_FILE"
echo ""
echo "提交代码时会自动检查代码问题"
echo "严重问题会阻止提交，非严重问题会警告"
