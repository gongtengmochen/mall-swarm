# mall-swarm 后端镜像配置教程（Docker Desktop 初学者版）

本文档提供一套可以直接用于 Docker Desktop 的后端部署方案，目标是把 `mall-admin`、`mall-auth`、`mall-gateway`、`mall-monitor`、`mall-portal` 以及它们依赖的 MySQL、Redis、MongoDB、RabbitMQ、Nacos 一次性配置好，并让你后续可以在 Docker Desktop 里通过点击按钮启动或停止整套后端。

## 1. 这套方案会做什么

仓库已经补齐以下文件：

| 文件 | 作用 |
| --- | --- |
| `compose.yaml` | Docker Desktop/Compose 的主编排文件 |
| `docker\backend\Dockerfile` | 统一构建后端服务镜像 |
| `docker\backend\init-nacos.sh` | 自动把 `config` 目录下的 `*-prod.yaml` 导入到 Nacos |
| `docker\mysql\init\00-create-users.sql` | 初始化数据库账号，并创建 `reader` 用户 |
| `document\sql\mall.sql` | 初始化 `mall` 数据库结构和演示数据 |

本方案默认实现以下关键目标：

1. MySQL 宿主机端口固定为 **3307**
2. 首次部署后，Docker Desktop 会出现一个名为 **mall-backend** 的应用组
3. 后续可直接在 Docker Desktop 的 **Containers** 页面点击启动/停止整套后端
4. Nacos 配置和数据库初始化自动完成，避免手工导入

## 1.1 为什么你在 Docker Desktop 里没看到镜像

你给的截图停留在 **Containers** 页面，这一页显示的是“容器”和“应用组”，**不是镜像列表**。

要看后端镜像，请按下面路径：

1. 打开 Docker Desktop 左侧边栏
2. 点击 **Images** 图标
3. 切换到 **Local**
4. 在搜索框输入 `mall/`

正常情况下会看到这些镜像：

- `mall/mall-admin:docker-desktop`
- `mall/mall-auth:docker-desktop`
- `mall/mall-gateway:docker-desktop`
- `mall/mall-monitor:docker-desktop`
- `mall/mall-portal:docker-desktop`

如果镜像已经存在，但页面没刷新出来，点击 Docker Desktop 顶部的刷新按钮，或者关闭后重新打开 Docker Desktop。

## 2. 部署前先了解端口

| 服务 | 容器端口 | 主机访问地址 |
| --- | --- | --- |
| MySQL | 3306 | `127.0.0.1:3307` |
| Redis | 6379 | `127.0.0.1:6379` |
| MongoDB | 27017 | `127.0.0.1:27017` |
| RabbitMQ | 5672 / 15672 | `127.0.0.1:5672` / `http://localhost:15672` |
| Nacos | 8848 | `http://localhost:8848/nacos` |
| mall-admin | 8080 | `http://localhost:8080` |
| mall-auth | 8401 | `http://localhost:8401` |
| mall-gateway | 8201 | `http://localhost:8201` |
| mall-monitor | 8101 | `http://localhost:8101` |
| mall-portal | 8085 | `http://localhost:8085` |

## 3. 环境准备

### 3.1 必装软件

| 软件 | 建议版本 | 说明 |
| --- | --- | --- |
| Docker Desktop | 最新稳定版 | 必须启用 Docker Engine |
| Git | 最新稳定版 | 用于获取源码 |

### 3.2 Docker Desktop 建议检查项

打开 Docker Desktop 后，请先确认：

1. 左下角 Docker Engine 状态为 **Running**
2. `Settings -> Resources` 至少分配 **4 GB 内存**
3. 没有其他本地 MySQL 占用 **3307** 端口

### 3.3 首次部署前的镜像说明

本仓库的后端镜像会通过 `compose.yaml` 自动构建；基础镜像会在首次执行时自动拉取。为了提高国内网络环境下的可用性，默认使用了 `m.daocloud.io` 镜像加速地址。你也可以提前手工拉取，减少等待时间：

```powershell
docker pull m.daocloud.io/docker.io/library/mysql:5.7
docker pull m.daocloud.io/docker.io/library/redis:7
docker pull m.daocloud.io/docker.io/library/mongo:4
docker pull m.daocloud.io/docker.io/library/rabbitmq:3.9.11-management
docker pull m.daocloud.io/docker.io/nacos/nacos-server:v2.3.0
docker pull m.daocloud.io/docker.io/curlimages/curl:8.7.1
docker pull m.daocloud.io/docker.io/library/maven:3.9.9-eclipse-temurin-17
docker pull m.daocloud.io/docker.io/library/eclipse-temurin:17-jre
```

## 4. 关键配置说明

### 4.1 MySQL 为什么是 3307

`compose.yaml` 中的配置如下：

```yaml
mysql:
  ports:
    - "${MYSQL_HOST_PORT:-3307}:3306"
```

含义是：

- 容器内部仍然监听 `3306`
- Windows 主机通过 `3307` 访问数据库

所以：

- 应用容器之间仍然使用 `db:3306` 或 `mysql:3306` 这类容器网络地址通信
- 你在宿主机上连接数据库时，要使用 `127.0.0.1:3307`

### 4.2 Nacos 配置如何自动导入

仓库中的 `config` 目录本来就是各服务的配置中心文件来源。本次配置新增了 `nacos-import` 容器，会在 Nacos 就绪后自动把以下文件导入：

- `config\admin\mall-admin-prod.yaml`
- `config\gateway\mall-gateway-prod.yaml`
- `config\portal\mall-portal-prod.yaml`
- `config\search\mall-search-prod.yaml`
- `config\demo\mall-demo-prod.yaml`

因此你不再需要手工打开 Nacos 控制台逐个新增配置。

### 4.3 数据库如何自动初始化

MySQL 第一次启动时会自动执行：

1. `docker\mysql\init\00-create-users.sql`
2. `document\sql\mall.sql`

这会完成：

- 创建 `mall` 数据库
- 创建 `reader / 123456` 账号
- 导入演示数据和后台用户数据

## 5. 第一次部署：命令行创建应用组

> 第一次建议在项目根目录执行一次命令行部署。执行完成后，Docker Desktop 就会识别到 `mall-backend` 应用组，之后可以直接点按钮启动。

### 5.1 进入项目根目录

```powershell
cd D:\code\git\mall-swarm\mall-swarm.worktrees\copilot-docker-backend-image-setup-guide
```

### 5.2 构建并启动后端

```powershell
docker compose up -d --build
```

这条命令会自动完成：

1. 拉取基础镜像
2. 构建 `mall-admin`、`mall-auth`、`mall-gateway`、`mall-monitor`、`mall-portal` 镜像
3. 启动 MySQL、Redis、MongoDB、RabbitMQ、Nacos
4. 自动导入数据库和 Nacos 配置
5. 启动后端服务

### 5.2.1 Windows 一键部署脚本

仓库还提供了一个适合 Windows 初学者的脚本：

- PowerShell 脚本：`docker\backend\deploy-docker-desktop.ps1`
- 批处理入口：`docker\backend\deploy-docker-desktop.bat`

如果你的本机已经有 MySQL 占用了 `3307`，请使用**管理员身份**打开 PowerShell，再执行：

```powershell
cd D:\code\git\mall-swarm\mall-swarm.worktrees\copilot-docker-backend-image-setup-guide
powershell.exe -ExecutionPolicy Bypass -NoProfile -File .\docker\backend\deploy-docker-desktop.ps1 -ForceStopLocalMySql
```

这个脚本会自动完成：

1. 检查 `3307` 是否被本地 MySQL 占用
2. 在你显式传入 `-ForceStopLocalMySql` 时停止本地 `mysqld.exe`
3. 构建后端镜像
4. 启动 Docker Compose
5. 检查健康接口
6. 验证认证登录链路
7. 验证 `mall-portal` 首页和分类接口
7. 验证 `mall-portal` 首页和分类接口

如果你更习惯双击，也可以右键 `docker\backend\deploy-docker-desktop.bat`，选择**以管理员身份运行**。

### 5.3 查看启动状态

```powershell
docker compose ps
```

### 5.4 查看实时日志

```powershell
docker compose logs -f mall-admin
docker compose logs -f mall-auth
docker compose logs -f mall-gateway
docker compose logs -f mall-portal
```

## 6. 之后如何在 Docker Desktop 里通过点击启动

首次 `docker compose up -d --build` 完成后，Docker Desktop 的 **Containers** 页面会出现一个名为 **mall-backend** 的应用组。

结合你当前看到的 Docker Desktop 界面，可以按下面操作：

1. 打开左侧边栏第一个 **Containers** 图标
2. 在列表中找到 **mall-backend**
3. 如果应用组已停止，点击应用组右侧的 **播放按钮（Start）**
4. 如果只想启动单个服务，就展开应用组，再点击对应服务右侧的播放按钮

### 截图指引

你提供的截图里已经能看到 Docker Desktop 的典型操作区域：

- 中间列表区域：容器/应用列表
- 右侧栏：Docker Desktop 的操作说明面板
- 每一行 **Actions** 列中的三角形按钮：就是启动按钮
- 左侧边栏 **Images** 图标：查看本地镜像列表

首次创建好 `mall-backend` 后，后续直接在这里点启动即可，不需要每次重新构建。

## 7. 常用操作命令

### 启动

```powershell
docker compose up -d
```

### 停止

```powershell
docker compose stop
```

### 停止并删除容器

```powershell
docker compose down
```

### 停止并删除容器与数据卷

> 只有在你想彻底重置数据库和 Redis 数据时再使用。

```powershell
docker compose down -v
```

### 单独重建某个后端镜像

```powershell
docker compose build mall-admin
docker compose up -d mall-admin
```

## 8. 验证方法

### 8.1 验证数据库端口 3307

如果你本机安装了 MySQL 客户端，可直接连接：

```powershell
mysql -h 127.0.0.1 -P 3307 -uroot -proot
```

或者用 Navicat / DataGrip / DBeaver 新建连接：

- Host: `127.0.0.1`
- Port: `3307`
- Username: `root`
- Password: `root`

### 8.2 验证 Nacos 是否正常

浏览器访问：

```text
http://localhost:8848/nacos
```

如果能打开控制台，就说明注册中心/配置中心容器已启动。

### 8.3 验证后台服务健康状态

浏览器或命令行访问：

```text
http://localhost:8080/actuator/health
http://localhost:8401/actuator/health
http://localhost:8201/actuator/health
http://localhost:8101/actuator/health
http://localhost:8085/actuator/health
```

只要返回 `UP`，说明对应服务已经启动成功。

### 8.4 验证认证链路

通过网关调用认证服务登录接口：

```powershell
curl.exe -X POST "http://localhost:8201/mall-auth/auth/login?clientId=admin-app&username=admin&password=macro123"
```

返回结果中只要包含 token 信息，就说明：

1. 网关可用
2. 认证服务可用
3. mall-auth 能正常调用 mall-admin
4. mall-admin 可以正常访问数据库

### 8.5 验证 mall-app-web 依赖的前台接口

浏览器或命令行访问：

```text
http://localhost:8201/mall-portal/home/content
http://localhost:8201/mall-portal/home/productCateList/0
```

只要不再返回 `503 Service Unavailable`，说明 `mall-app-web` 最核心的首页和分类链路已经恢复。

### 8.6 验证监控中心

打开：

```text
http://localhost:8101
```

登录账号密码：

- 用户名：`macro`
- 密码：`root`

如果能看到 `mall-admin`、`mall-auth`、`mall-gateway` 等实例，说明服务注册正常。

## 9. 常见问题与优先排障方法

### 问题 1：3307 端口被占用

**现象**

MySQL 容器启动失败，日志中出现端口冲突。

**处理**

1. 关闭本机正在运行的 MySQL
2. 最稳妥的做法是以管理员身份运行一键部署脚本：

```powershell
powershell.exe -ExecutionPolicy Bypass -NoProfile -File .\docker\backend\deploy-docker-desktop.ps1 -ForceStopLocalMySql
```

3. 或在当前 PowerShell 会话里临时指定别的端口，例如：

```powershell
$env:MYSQL_HOST_PORT=3310
docker compose up -d
```

4. 如果你希望恢复默认端口 `3307`，关闭当前终端窗口后重新打开即可
5. 如果你更喜欢直接改文件，也可以把 `${MYSQL_HOST_PORT:-3307}:3306` 改成固定值，例如 `3310:3306`
6. 修改后重新执行：

```powershell
docker compose up -d
```

### 问题 2：改了 SQL 或 Nacos 配置，但服务没有生效

**原因**

MySQL 初始化脚本只会在数据库卷首次创建时执行一次。

**处理**

```powershell
docker compose down -v
docker compose up -d --build
```

### 问题 3：Nacos 打开了，但服务没有注册

**优先检查**

1. `nacos-import` 是否执行成功
2. `mall-admin` / `mall-auth` / `mall-gateway` 日志里是否有连接 Nacos 失败信息
3. 是否误把 `SPRING_PROFILES_ACTIVE` 改回了 `dev`

查看日志：

```powershell
docker compose logs nacos-import
docker compose logs mall-admin
docker compose logs mall-auth
docker compose logs mall-gateway
docker compose logs mall-portal
```

### 问题 4：登录接口返回失败

**优先检查**

1. MySQL 是否正常启动
2. `mall-admin` 的 `/actuator/health` 是否为 `UP`
3. 登录请求是否使用了 `clientId=admin-app`

推荐先直接访问：

```text
http://localhost:8080/actuator/health
```

如果这里不是 `UP`，先修复 `mall-admin`，不要直接排查前端。

### 问题 5：mall-app-web 调用 `mall-portal` 接口返回 503

**现象**

前端调用下面这些接口返回 `503`：

- `/mall-portal/home/content`
- `/mall-portal/home/productCateList/0`
- `/mall-portal/sso/login`

**根因**

通常是 `mall-portal`、MongoDB 或 RabbitMQ 没启动，导致网关虽然有路由，但后端没有可用实例。

**处理**

1. 先看 `mall-portal` 是否已经运行
2. 再看 `mongo` 和 `rabbitmq` 是否已启动
3. 然后查看 `mall-portal` 日志

```powershell
docker compose ps
docker compose logs --tail=200 mall-portal
docker compose logs --tail=100 mongo
docker compose logs --tail=100 rabbitmq
```

### 问题 6：Docker Desktop 里能看到容器，但点击启动后马上退出

**处理顺序**

1. 先看该容器的日志
2. 再看它依赖的基础服务是否已经启动
3. 最后看端口是否冲突、配置是否导入成功

最常用日志命令：

```powershell
docker compose logs --tail=200 mysql
docker compose logs --tail=200 nacos-import
docker compose logs --tail=200 mall-admin
docker compose logs --tail=200 mall-auth
docker compose logs --tail=200 mall-gateway
```

## 10. 调试建议

如果你想自己优先定位问题，建议按这个顺序检查：

1. **先看基础服务**：MySQL、Redis、Nacos
2. **再看配置导入**：`nacos-import`
3. **再看业务服务**：`mall-admin -> mall-auth -> mall-gateway`
4. **最后再看图形界面或前端请求**

这是因为大多数启动失败都不是前端问题，而是数据库、注册中心、配置中心没有准备好。

## 11. 推荐的启动/停止习惯

### 每天开始工作

1. 打开 Docker Desktop
2. 进入 **Containers**
3. 找到 **mall-backend**
4. 点击播放按钮启动整组服务

### 每天结束工作

1. 回到 **Containers**
2. 找到 **mall-backend**
3. 点击停止按钮停止整组服务

这样最适合初学者，也最不容易误停单个依赖服务。
