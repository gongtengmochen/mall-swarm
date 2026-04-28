# mall-ai 快速启动指南

## 一、环境准备

### 1. 必需软件

确保以下软件已安装并运行：

- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ MySQL 5.7+（mall 数据库）
- ✅ Redis 7.0+
- ✅ Nacos 2.x

### 2. 获取阿里云百炼 API Key

1. 访问 [阿里云百炼平台](https://bailian.console.aliyun.com/)
2. 注册/登录阿里云账号
3. 开通百炼服务
4. 创建 API Key
5. 复制 API Key 备用

**注意：** 新用户有免费额度可用于测试。

## 二、配置 API Key

### 方式一：环境变量（推荐）

在 IDEA 中配置：
1. Run → Edit Configurations
2. 找到 MallAiApplication
3. 在 Environment variables 中添加：
   ```
   AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
   ```
4. 点击 Apply → OK

或在命令行中配置：
```powershell
# Windows PowerShell
$env:AI_DASHSCOPE_API_KEY="sk-your-actual-api-key-here"
```

```cmd
# Windows CMD
set AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

```bash
# Linux/Mac
export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**验证配置：**
```bash
# Windows PowerShell
echo $env:AI_DASHSCOPE_API_KEY

# Windows CMD
echo %AI_DASHSCOPE_API_KEY%

# Linux/Mac
echo $AI_DASHSCOPE_API_KEY
```

## 三、启动依赖服务

### 1. 启动 Nacos

```bash
# 启动 Nacos（单机模式）
cd nacos/bin
startup.cmd -m standalone  # Windows
# 或
./startup.sh -m standalone # Linux/Mac

# 访问 Nacos 控制台
# http://localhost:8848/nacos
# 默认账号密码：nacos/nacos
```

### 2. 确认 MySQL 和 Redis 运行

```bash
# 检查 MySQL
mysql -u root -p
SHOW DATABASES;  # 应该看到 mall 数据库

# 检查 Redis
redis-cli ping   # 应该返回 PONG
```

## 四、导入 Nacos 配置（重要）

### 将配置文件导入 Nacos 配置中心

**重要说明：** 本项目使用 Spring AI Alibaba 框架，API Key 必须使用标准配置属性名 `spring.ai.dashscope.api-key`。

1. 访问 Nacos 控制台：http://localhost:8848/nacos
2. 进入 **配置管理 → 配置列表**
3. 点击 **"+" 创建配置**

**创建 mall-ai-dev.yaml 配置：**

```
Data ID: mall-ai-dev.yaml
Group: DEFAULT_GROUP
配置格式: YAML
配置内容: 复制 config/ai/mall-ai-dev.yaml 的内容
```

**正确的配置内容：**
```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:sk-9d5c65f97a184e17a08e88a6de17c097}
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
          max-completion-tokens: 2000
  datasource:
    url: jdbc:mysql://localhost:3306/mall?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
    username: root
    password: root
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password:
      timeout: 3000ms

mall:
  ai:
    enable-stream: true
    max-history-size: 10
    rag-top-k: 5
    api-timeout: 30000
    max-retries: 3
    rate-limit-qps: 10
    system-prompt: |
      你是一个专业的电商导购助手，请根据用户的提问，结合商城的商品信息，给出专业、详细的购买建议。
      注意：
      1. 回答要简洁明了
      2. 基于真实商品信息回答
      3. 如果不能确定具体商品，可以提供一般性建议

logging:
  level:
    root: info
    com.macro.mall: debug

logstash:
  host: localhost
```

**配置说明：**
- `spring.ai.dashscope.api-key` - Spring AI Alibaba 标准配置属性名（从环境变量读取）
- `spring.ai.dashscope.chat.options.model` - 使用的模型名称
- `spring.ai.dashscope.chat.options.temperature` - 温度参数（0-1，越高越有创造性）
- `spring.ai.dashscope.chat.options.max-completion-tokens` - 最大生成 token 数（1.0.0-M3.2+ 版本使用此配置）
- `mall.ai.*` - AI 导购业务配置（对应 [AiGuideConfig.java](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\config\AiGuideConfig.java)）

## 五、编译并启动 mall-ai

### 启动顺序（重要）

必须按以下顺序启动服务：

1. **Nacos**（注册中心/配置中心）
2. **mall-gateway**（网关服务，端口 8201）
3. **mall-ai**（AI 服务，端口 8094）

### 方式一：IDEA 直接运行（推荐）

1. 用 IDEA 打开 mall-swarm 项目
2. 等待 Maven 依赖下载完成
3. 启动服务（按顺序）：
   - 先启动 Nacos
   - 然后启动 `MallGatewayApplication`（mall-gateway 模块）
   - 最后启动 [`MallAiApplication`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\MallAiApplication.java)（mall-ai 模块）

### 方式二：Maven 命令行

```powershell
# 进入项目根目录（PowerShell）
cd "e:\大学课程\软件工程\xiangmu\mall-swarm"

# 编译整个项目（跳过测试）
mvn clean install "-DskipTests" "-Ddocker.skip=true"

# 启动 mall-ai 服务（需要先启动 Nacos 和 Gateway）
cd mall-ai
mvn spring-boot:run
```

**注意：** 在 PowerShell 中，Maven 的 `-D` 参数必须用双引号包裹。

## 六、验证服务

### 1. 检查启动日志

看到以下日志表示启动成功：

```
Started MallAiApplication in X.XXX seconds
```

同时可以在 Nacos 控制台看到 mall-ai 服务已注册。

### 2. 检查 Nacos 服务列表

访问：http://localhost:8848/nacos

进入 **服务管理 → 服务列表**，应该能看到：
- mall-ai（健康状态）
- mall-gateway（健康状态）
- 其他已启动的服务

### 3. 访问 API 文档

**通过 Gateway 网关访问（推荐）：**

浏览器打开：http://localhost:8201/doc.html

在左上角下拉框中选择 **"mall-ai"**，即可查看 AI 相关接口。

**可用接口：**
- `POST /ai/guide/chat` - AI对话（非流式）
- `POST /ai/guide/stream` - AI对话（流式输出）
- `GET /ai/guide/hot-products` - 获取热门商品推荐

### 4. 手动测试 API

使用 Postman 或 curl 测试（通过 Gateway）：

```bash
# 测试普通对话
curl -X POST http://localhost:8201/mall-ai/ai/guide/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "sessionId": "test_001",
    "message": "我想买一部手机，预算3000元左右",
    "stream": false
  }'
```

**预期响应：**

```json
{
  "sessionId": "test_001",
  "reply": "根据您的预算，我为您推荐以下几款手机...",
  "recommendedProducts": [...],
  "intent": "recommend",
  "success": true,
  "timestamp": 1234567890
}
```

## 七、常见问题排查

### 问题1：无法连接 Nacos

**错误信息：**
```
com.alibaba.nacos.api.exception.NacosException: failed to req API
```

**解决方案：**
1. 确认 Nacos 已启动：访问 http://localhost:8848/nacos
2. 检查 [`bootstrap.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\bootstrap.yml) 中的 Nacos 地址必须是 `http://localhost:8848`（带 http:// 前缀）
3. 确认防火墙未阻止 8848 端口

### 问题2：Nacos 服务列表中没有 mall-ai

**解决方案：**
1. 确认 mall-ai 已成功启动（查看 IDEA 控制台日志）
2. 检查 Nacos 地址配置：必须使用 `http://localhost:8848` 格式
3. 重启 mall-ai 服务
4. 刷新 Nacos 控制台页面

### 问题3：Gateway 文档中没有 mall-ai 分组

**解决方案：**
1. 确认 mall-ai 已在 Nacos 服务列表中
2. 确认已添加 [`SpringDocConfig`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\config\SpringDocConfig.java) 配置类
3. 重启 mall-gateway 服务
4. 按 Ctrl+F5 强制刷新浏览器

### 问题4：无法连接 MySQL

**错误信息：**
```
com.mysql.cj.jdbc.exceptions.CommunicationsException
```

**解决方案：**
1. 确认 MySQL 服务已启动
2. 检查 [`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml) 中的数据库配置
3. 确认 mall 数据库存在且可访问

### 问题5：无法连接 Redis

**错误信息：**
```
io.lettuce.core.RedisConnectionException
```

**解决方案：**
1. 确认 Redis 服务已启动：`redis-cli ping`
2. 检查 [`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml) 中的 Redis 配置
3. 确认 Redis 密码配置正确（如果有）

### 问题6：API 调用失败

**错误信息：**
```
AI对话失败
```

**解决方案：**
1. 检查 API Key 是否正确配置（通过环境变量）
   ```bash
   echo $env:AI_DASHSCOPE_API_KEY  # PowerShell
   echo %AI_DASHSCOPE_API_KEY%     # CMD
   echo $AI_DASHSCOPE_API_KEY      # Linux/Mac
   ```
2. 确认网络连接正常（能访问阿里云）
3. 查看日志获取详细错误信息
4. 确认 API Key 余额充足
5. 参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md) 了解详细的安全配置

### 问题7：Maven 依赖下载失败

**错误信息：**
```
Could not find artifact org.springframework.ai:spring-ai-core
```

**解决方案：**
已在根 `pom.xml` 中添加 Spring Milestones 仓库，如果仍有问题：

1. 清理 Maven 缓存：
   ```powershell
   mvn dependency:purge-local-repository
   ```

2. 重新下载依赖：
   ```powershell
   mvn clean install -U
   ```

### 问题8：端口被占用

**错误信息：**
```
Web server failed to start. Port 8094 was already in use.
```

**解决方案：**
1. 查找占用端口的进程并关闭
2. 或修改 [`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml) 中的端口配置：
   ```yaml
   server:
     port: 8095  # 改为其他可用端口
   ```

### 问题9：配置属性名错误

**错误信息：**
```
IllegalStateException: No setter found for property 'api-key'
```

**解决方案：**
1. 确认使用的是 Spring AI Alibaba 标准配置属性名 `spring.ai.dashscope.api-key`
2. 不要使用自定义属性名如 `mall.ai.api-key`
3. 参考 [`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml) 和 [`mall-ai-dev.yaml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\config\ai\mall-ai-dev.yaml) 的正确配置

### 问题10：max-tokens 配置错误

**错误信息：**
```
IllegalStateException: No setter found for property 'max-tokens'
```

**解决方案：**
1. Spring AI Alibaba 1.0.0-M3.2+ 版本应使用 `max-completion-tokens` 而非 `max-tokens`
2. 检查配置文件中是否使用了正确的属性名

## 八、配置说明

### 配置文件位置

- **[`bootstrap.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\bootstrap.yml)** - 服务启动配置（Nacos 地址）
- **[`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml)** - 本地业务配置（数据库、Redis、AI 参数）
- **Nacos 配置中心** - 生产环境配置（[`mall-ai-dev.yaml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\config\ai\mall-ai-dev.yaml)）

**配置优先级：** Nacos 配置 > 本地 application.yml

### 关键配置项

#### Spring AI Alibaba 模型配置

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:sk-xxx}  # 从环境变量读取（标准配置属性名）
      chat:
        options:
          model: qwen-plus              # 使用的模型
          temperature: 0.7              # 创造性参数（0-1）
          max-completion-tokens: 2000   # 最大生成 token 数（1.0.0-M3.2+ 版本）
```

#### AI 导购业务配置

```yaml
mall:
  ai:
    enable-stream: true         # 启用流式输出
    max-history-size: 10        # 最大对话历史条数
    rag-top-k: 5                # RAG 检索 Top K 商品数
    api-timeout: 30000          # API 超时时间（毫秒）
    max-retries: 3              # 最大重试次数
    rate-limit-qps: 10          # 限流 QPS
```

### 环境变量

```bash
# 必须设置的环境变量
AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**安全提醒：**
- ⚠️ 不要将真实的 API Key 提交到 Git 仓库
- ✅ 使用环境变量或 Nacos 配置中心管理敏感信息
- ✅ 参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md) 了解详细的安全配置指南

## 九、下一步

服务启动成功后，你可以：

1. 📖 阅读 [`README.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\README.md) 了解完整功能
2. 📝 查看 [`FUNCTION_IMPLEMENTATION.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\FUNCTION_IMPLEMENTATION.md) 了解技术实现细节
3. 🔧 根据需求调整配置参数
4. 🚀 集成到前端应用
5. 📊 接入监控系统（Spring Boot Admin）
6. 🧪 运行测试脚本验证 API

## 十、技术支持

如遇到问题，可以：

1. 查看日志文件：`logs/mall-ai.log`
2. 检查 Nacos 配置中心：http://localhost:8848/nacos
3. 参考 [Spring AI Alibaba 官方文档](https://sca.aliyun.com/docs/ai/)
4. 查看 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md) 了解 API Key 配置
5. 查看项目 Issues：https://github.com/macrozheng/mall-swarm/issues

---

**祝你使用愉快！** 🎉
