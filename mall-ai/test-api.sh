#!/bin/bash

# mall-ai API 测试脚本
# 注意：本脚本通过 Gateway 网关访问，符合微服务统一访问规范

BASE_URL="http://localhost:8201/mall-ai"

echo "=========================================="
echo "mall-ai API 测试（通过 Gateway）"
echo "=========================================="
echo ""

# 检查 jq 是否安装
if ! command -v jq &> /dev/null; then
    echo "警告: jq 未安装，JSON 输出将不会格式化"
    echo "安装方法: brew install jq (Mac) 或 apt-get install jq (Linux)"
    echo ""
    JQ_CMD="cat"
else
    JQ_CMD="jq '.'"
fi

# 测试1：普通对话
echo "1. 测试普通对话（非流式）"
echo "------------------------------------------"
RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/guide/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sessionId": "test_session_001",
    "message": "我想买一部手机，预算3000元左右，有什么推荐吗？",
    "stream": false
  }')

if [ $? -eq 0 ]; then
    echo "$RESPONSE" | eval $JQ_CMD
else
    echo "错误: 请求失败"
fi
echo ""
echo ""

# 测试2：商品咨询
echo "2. 测试商品咨询"
echo "------------------------------------------"
RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/guide/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sessionId": "test_session_002",
    "message": "华为Mate60的价格是多少？",
    "stream": false
  }')

if [ $? -eq 0 ]; then
    echo "$RESPONSE" | eval $JQ_CMD
else
    echo "错误: 请求失败"
fi
echo ""
echo ""

# 测试3：多轮对话
echo "3. 测试多轮对话（第一轮）"
echo "------------------------------------------"
RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/guide/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sessionId": "test_session_003",
    "message": "我想看看笔记本电脑",
    "stream": false
  }')

if [ $? -eq 0 ]; then
    echo "$RESPONSE" | eval $JQ_CMD
else
    echo "错误: 请求失败"
fi
echo ""

sleep 1

echo "多轮对话（第二轮）"
echo "------------------------------------------"
RESPONSE=$(curl -s -X POST "${BASE_URL}/ai/guide/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sessionId": "test_session_003",
    "message": "有没有性价比高的游戏本？",
    "stream": false
  }')

if [ $? -eq 0 ]; then
    echo "$RESPONSE" | eval $JQ_CMD
else
    echo "错误: 请求失败"
fi
echo ""
echo ""

# 测试4：获取热门商品
echo "4. 测试获取热门商品"
echo "------------------------------------------"
RESPONSE=$(curl -s -X GET "${BASE_URL}/ai/guide/hot-products?limit=5")

if [ $? -eq 0 ]; then
    echo "$RESPONSE" | eval $JQ_CMD
else
    echo "错误: 请求失败"
fi
echo ""
echo ""

# 测试5：流式对话（可选，需要手动中断）
echo "5. 测试流式对话（SSE）"
echo "------------------------------------------"
echo "提示: 流式输出会持续显示，按 Ctrl+C 中断"
echo ""
curl -X POST "${BASE_URL}/ai/guide/stream" \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{
    "userId": 1,
    "sessionId": "test_session_004",
    "message": "推荐一款适合程序员的键盘",
    "stream": true
  }'
echo ""
echo ""

echo "=========================================="
echo "测试完成！"
echo "=========================================="
echo ""
echo "提示："
echo "- 通过 Gateway 访问: http://localhost:8201/mall-ai/ai/guide/*"
echo "- 直接访问（开发调试）: http://localhost:8094/ai/guide/*"
echo "- API 文档: http://localhost:8201/doc.html"
