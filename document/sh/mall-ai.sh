#!/bin/bash
APP_NAME=mall-ai
JAR_FILE=$APP_NAME.jar

# 判断是否传入环境参数，默认为dev
ENV=${1:-dev}

echo "Starting $APP_NAME with profile: $ENV"

# 停止旧进程
PID=$(ps -ef | grep $JAR_FILE | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "Stopping existing $APP_NAME process (PID: $PID)"
    kill -9 $PID
    sleep 3
fi

# 启动新进程
nohup java -jar \
    -Dspring.profiles.active=$ENV \
    target/$JAR_FILE \
    > logs/$APP_NAME.log 2>&1 &

echo "$APP_NAME started successfully!"
echo "Log file: logs/$APP_NAME.log"
echo "Access Swagger UI: http://localhost:8402/swagger-ui.html"
