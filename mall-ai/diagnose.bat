@echo off
echo ========================================
echo  mall-ai 服务诊断工具
echo ========================================
echo.

echo [1/4] 检查 mall-ai 服务端口 8094...
netstat -ano | findstr :8094
if %errorlevel% equ 0 (
    echo ✅ 端口 8094 已被占用，服务可能正在运行
) else (
    echo ❌ 端口 8094 未被占用，服务可能未启动
)
echo.

echo [2/4] 测试直接访问 mall-ai 服务...
curl -s -o NUL -w "HTTP 状态码: %%{http_code}\n" http://localhost:8094/ai/guide/chat -X POST -H "Content-Type: application/json" -d "{\"userId\":1,\"sessionId\":\"test\",\"message\":\"测试\",\"stream\":false}"
echo.

echo [3/4] 测试 Gateway 健康状态...
curl -s http://localhost:8201/actuator/health
echo.
echo.

echo [4/4] 测试通过 Gateway 访问 AI 服务...
curl -s -o NUL -w "HTTP 状态码: %%{http_code}\n" http://localhost:8201/mall-ai/guide/chat -X POST -H "Content-Type: application/json" -d "{\"userId\":1,\"sessionId\":\"test\",\"message\":\"测试\",\"stream\":false}"
echo.

echo ========================================
echo  诊断完成
echo ========================================
echo.
echo 如果所有测试都失败，请检查：
echo 1. mall-ai 服务是否已启动
echo 2. 查看 mall-ai 日志是否有错误
echo 3. 检查 Nacos 服务注册情况
echo.
pause
