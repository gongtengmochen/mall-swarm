# mall-ai API 测试脚本 (PowerShell 版本)
# 注意：本脚本通过 Gateway 网关访问，符合微服务统一访问规范

$BASE_URL = "http://localhost:8201/mall-ai"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "mall-ai API 测试（通过 Gateway）" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 测试1：普通对话
Write-Host "1. 测试普通对话（非流式）" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/guide/chat" `
        -Method Post `
        -ContentType "application/json" `
        -Body @{
            userId = 1
            sessionId = "test_session_001"
            message = "我想买一部手机，预算3000元左右，有什么推荐吗？"
            stream = $false
        } | ConvertTo-Json -Depth 10
    
    Write-Host $response
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# 测试2：商品咨询
Write-Host "2. 测试商品咨询" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/guide/chat" `
        -Method Post `
        -ContentType "application/json" `
        -Body @{
            userId = 1
            sessionId = "test_session_002"
            message = "华为Mate60的价格是多少？"
            stream = $false
        } | ConvertTo-Json -Depth 10
    
    Write-Host $response
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# 测试3：多轮对话
Write-Host "3. 测试多轮对话（第一轮）" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/guide/chat" `
        -Method Post `
        -ContentType "application/json" `
        -Body @{
            userId = 1
            sessionId = "test_session_003"
            message = "我想看看笔记本电脑"
            stream = $false
        } | ConvertTo-Json -Depth 10
    
    Write-Host $response
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}
Write-Host ""

Start-Sleep -Seconds 1

Write-Host "多轮对话（第二轮）" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/guide/chat" `
        -Method Post `
        -ContentType "application/json" `
        -Body @{
            userId = 1
            sessionId = "test_session_003"
            message = "有没有性价比高的游戏本？"
            stream = $false
        } | ConvertTo-Json -Depth 10
    
    Write-Host $response
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# 测试4：获取热门商品
Write-Host "4. 测试获取热门商品" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
try {
    $response = Invoke-RestMethod -Uri "$BASE_URL/ai/guide/hot-products?limit=5" `
        -Method Get | ConvertTo-Json -Depth 10
    
    Write-Host $response
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}
Write-Host ""
Write-Host ""

# 测试5：流式对话（可选）
Write-Host "5. 测试流式对话（SSE）" -ForegroundColor Yellow
Write-Host "------------------------------------------" -ForegroundColor Gray
Write-Host "提示: 流式输出会持续显示，按 Ctrl+C 中断" -ForegroundColor Cyan
Write-Host ""

try {
    $webRequest = [System.Net.WebRequest]::Create("$BASE_URL/ai/guide/stream")
    $webRequest.Method = "POST"
    $webRequest.ContentType = "application/json"
    $webRequest.Accept = "text/event-stream"
    
    $body = @{
        userId = 1
        sessionId = "test_session_004"
        message = "推荐一款适合程序员的键盘"
        stream = $true
    } | ConvertTo-Json
    
    $requestStream = $webRequest.GetRequestStream()
    $requestWriter = New-Object System.IO.StreamWriter($requestStream)
    $requestWriter.Write($body)
    $requestWriter.Flush()
    $requestWriter.Close()
    
    $responseStream = $webRequest.GetResponse().GetResponseStream()
    $responseReader = New-Object System.IO.StreamReader($responseStream)
    
    while (-not $responseReader.EndOfStream) {
        $line = $responseReader.ReadLine()
        if ($line) {
            Write-Host $line
        }
    }
    
    $responseReader.Close()
    $responseStream.Close()
} catch {
    Write-Host "错误: 请求失败 - $_" -ForegroundColor Red
}

Write-Host ""
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "测试完成！" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "提示：" -ForegroundColor Green
Write-Host "- 通过 Gateway 访问: http://localhost:8201/mall-ai/ai/guide/*" -ForegroundColor White
Write-Host "- 直接访问（开发调试）: http://localhost:8094/ai/guide/*" -ForegroundColor White
Write-Host "- API 文档: http://localhost:8201/doc.html" -ForegroundColor White
