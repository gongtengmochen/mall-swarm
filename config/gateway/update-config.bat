@echo off
chcp 65001 >nul
echo ========================================
echo  更新 Nacos Gateway 配置
echo ========================================
echo.

set NACOS_SERVER=http://localhost:8848
set NACOS_USER=nacos
set NACOS_PASSWORD=nacos
set DATA_ID=mall-gateway-dev.yaml
set GROUP=DEFAULT_GROUP

echo [1/3] 读取配置文件...
set CONFIG_FILE=%~dp0mall-gateway-dev.yaml

echo [2/3] 发布配置到 Nacos...
echo.

curl -X POST "%NACOS_SERVER%/nacos/v1/cs/configs" ^
  -d "dataId=%DATA_ID%" ^
  -d "group=%GROUP%" ^
  -d "tenant=" ^
  -d "username=%NACOS_USER%" ^
  -d "password=%NACOS_PASSWORD%" ^
  -d "type=yaml" ^
  --data-urlencode "content@%CONFIG_FILE%"

echo.
echo.

echo [3/3] 刷新 Gateway 配置...
curl -X POST "http://localhost:8201/actuator/refresh"

echo.
echo.
echo ========================================
echo  配置更新完成！
echo ========================================
echo.
echo 请等待 5 秒后测试...
timeout /t 5 >nul

echo.
echo 测试 AI API（通过 Gateway）...
curl -X POST "http://localhost:8201/mall-ai/guide/chat" ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":1,\"sessionId\":\"test\",\"message\":\"你好\",\"stream\":false}"

echo.
echo.
echo 如果返回 200 和 JSON 数据，说明配置成功！
echo.
pause
