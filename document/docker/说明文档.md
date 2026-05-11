======================= 一、全部服务 命令 =======================
1. 一次性启动所有服务（后台运行）
docker-compose up -d

2. 一次性停止所有服务（不删除数据）
docker-compose down

3. 查看所有服务运行状态
docker-compose ps

4. 查看所有服务实时日志
docker-compose logs -f

======================= 二、单个服务 命令 =======================
# 启动单个服务
docker-compose start 服务名

# 停止单个服务
docker-compose stop 服务名

# 重启单个服务
docker-compose restart 服务名

# 查看单个服务日志（常用排错）
docker-compose logs -f 服务名

--- 你的服务名列表 ---
mysql
redis
nginx
rabbitmq
elasticsearch
logstash
kibana
mongo
nacos-registry
minio

======================= 三、常用示例（直接复制） =======================
1. 单独启动 MySQL
docker-compose start mysql

2. 单独停止 Nginx
docker-compose stop nginx

3. 重启 Elasticsearch
docker-compose restart elasticsearch

4. 查看 Nacos 日志
docker-compose logs -f nacos-registry

5. 查看 MySQL 日志
docker-compose logs -f mysql

======================= 四、删除 / 清理 命令 =======================
1. 停止并删除所有容器（数据还在）
docker-compose down

2. 彻底删除所有容器 + 所有数据（慎用！）
docker-compose down -v

3. 删除单个容器（不删数据）
docker rm -f 服务名

======================= 五、查看镜像 / 容器 =======================
1. 查看正在运行的容器
docker ps

2. 查看所有容器（包括停止的）
docker ps -a

3. 查看本地下载的镜像
docker images