# AI Gateway 配置更新和测试脚本
# 使用前请确保 Nacos 已启动

$ErrorActionPreference = "Continue"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  更新 Nacos Gateway 配置" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$NACOS_SERVER = "http://localhost:8848"
$DATA_ID = "mall-gateway-dev.yaml"
$GROUP = "DEFAULT_GROUP"
$CONFIG_FILE = "$PSScriptRoot\mall-gateway-dev.yaml"

Write-Host "[1/4] 读取配置文件..." -ForegroundColor Yellow
if (Test-Path $CONFIG_FILE) {
    $configContent = Get-Content $CONFIG_FILE -Raw -Encoding UTF8
    Write-Host "  ✓ 配置文件读取成功" -ForegroundColor Green
} else {
    Write-Host "   配置文件不存在: $CONFIG_FILE" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[2/4] 发布配置到 Nacos..." -ForegroundColor Yellow

# 准备 POST 数据
$postData = @{
    dataId = $DATA_ID
    group = $GROUP
    tenant = ""
    username = "nacos"
    password = "nacos"
    type = "yaml"
    content = $configContent
}

try {
    $response = Invoke-WebRequest -Uri "$NACOS_SERVER/nacos/v1/cs/configs" -Method POST -Body $postData -UseBasicParsing
    
    if ($response.StatusCode -eq 200) {
        Write-Host "  ✓ 配置发布成功" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 配置发布失败，状态码: $($response.StatusCode)" -ForegroundColor Red
        Write-Host $response.Content -ForegroundColor Red
    }
} catch {
    Write-Host "  ✗ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "[3/4] 刷新 Gateway 配置..." -ForegroundColor Yellow

try {
    $refreshResponse = Invoke-WebRequest -Uri "http://localhost:8201/actuator/refresh" -Method POST -UseBasicParsing
    Write-Host "  ✓ Gateway 配置已刷新" -ForegroundColor Green
} catch {
    Write-Host "  ⚠ Gateway 刷新失败，可能需要重启 Gateway 服务" -ForegroundColor Yellow
    Write-Host "    错误: $($_.Exception.Message)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "[4/4] 等待 5 秒后测试..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  测试 AI API" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 测试 1: 直接访问
Write-Host "测试 1: 直接访问 mall-ai 服务" -ForegroundColor Yellow
try {
    $directResponse = Invoke-WebRequest -Uri "http://localhost:8094/ai/guide/chat" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"userId":1,"sessionId":"test_direct","message":"你好","stream":false}' `
        -UseBasicParsing
    
    if ($directResponse.StatusCode -eq 200) {
        Write-Host "  ✓ 直接访问成功 (HTTP $($directResponse.StatusCode))" -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ 直接访问失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""

# 测试 2: 通过 Gateway 访问
Write-Host "测试 2: 通过 Gateway 访问 AI 服务" -ForegroundColor Yellow
try {
    $gatewayResponse = Invoke-WebRequest -Uri "http://localhost:8201/mall-ai/ai/guide/chat" `
        -Method POST `
        -ContentType "application/json" `
        -Body '{"userId":1,"sessionId":"test_gateway","message":"你好","stream":false}' `
        -UseBasicParsing
    
    if ($gatewayResponse.StatusCode -eq 200) {
        Write-Host "  ✓ Gateway 访问成功 (HTTP $($gatewayResponse.StatusCode))" -ForegroundColor Green
        $responseData = $gatewayResponse.Content | ConvertFrom-Json
        Write-Host "  回复: $($responseData.reply.Substring(0, [Math]::Min(50, $responseData.reply.Length)))..." -ForegroundColor Green
    }
} catch {
    Write-Host "  ✗ Gateway 访问失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if ($gatewayResponse -and $gatewayResponse.StatusCode -eq 200) {
    Write-Host "  ✓✓✓ 所有测试通过！配置更新成功！" -ForegroundColor Green
} else {
    Write-Host "  ⚠ 测试未完全通过，请检查配置" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  如果直接访问成功但 Gateway 访问失败，请：" -ForegroundColor Yellow
    Write-Host "  1. 重启 Gateway 服务" -ForegroundColor Yellow
    Write-Host "  2. 检查 Nacos 中的配置是否生效" -ForegroundColor Yellow
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
[void][System.Console]::ReadKey($true)
