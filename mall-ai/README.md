# mall-ai AI智能导购服务

## 项目简介

基于 Spring AI Alibaba 和通义千问大模型实现的 AI 智能导购与商品问答助手，集成 RAG（检索增强生成）技术，为用户提供智能化的购物体验和商品推荐服务。

## 核心功能

### 1. AI 商品问答
- 基于真实商品信息回答用户问题
- 支持多轮对话，保持上下文连贯性
- RAG 技术确保回答准确性

### 2. 智能导购
- 根据用户需求自动推荐商品
- 生成个性化推荐理由
- 支持价格、品牌、分类等多维度筛选

### 3. 智能客服
- 用户意图识别（咨询/推荐/投诉等）
- 自然语言理解和回复
- 7x24 小时在线服务

### 4. 高可用保障
- Resilience4j 限流保护
- API 调用失败自动重试
- Redis 缓存会话和热点数据
- 降级策略应对服务异常

### 5. 流式输出
- 支持 SSE 流式响应
- 提升用户交互体验
- 实时显示 AI 回复内容

## 技术栈

- **Spring Boot 3.2.2** - 基础框架
- **Spring Cloud 2023** - 微服务架构
- **Spring Cloud Alibaba 2023.0.1.0** - 微服务组件
- **Spring AI Alibaba 1.0.0-M3.2** - AI 能力集成
- **通义千问 qwen-plus** - 大语言模型
- **RAG** - 检索增强生成技术
- **Resilience4j** - 限流和熔断
- **Redis** - 会话管理和缓存
- **MyBatis** - 数据持久化
- **MySQL** - 商品数据存储
- **Nacos** - 服务注册与配置中心
- **Knife4j** - API 文档

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+（mall 数据库）
- Redis 7.0+
- Nacos 2.x

### 2. 配置 API Key

**重要说明：** 本项目使用 Spring AI Alibaba 框架，API Key 必须使用标准配置属性名 `spring.ai.dashscope.api-key`。

#### 方式一：环境变量（推荐）

```bash
# Windows PowerShell
$env:AI_DASHSCOPE_API_KEY="sk-your-actual-api-key-here"

# Windows CMD
set AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here

# Linux/Mac
export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

#### 方式二：IDEA 运行配置

1. Run → Edit Configurations
2. 找到 MallAiApplication
3. 在 Environment variables 中添加：
   ```
   AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
   ```

#### 方式三：Nacos 配置中心（生产环境推荐）

将 [`config/ai/mall-ai-dev.yaml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\config\ai\mall-ai-dev.yaml) 导入到 Nacos 配置中心。

**安全提醒：**
- ⚠️ 不要将真实的 API Key 提交到 Git 仓库
- ✅ 使用环境变量或配置中心管理敏感信息
- 📖 参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md) 了解详细的安全配置指南

### 3. 启动服务

**启动顺序（重要）：**

1. **Nacos**（注册中心/配置中心）
2. **mall-gateway**（网关服务，端口 8201）
3. **mall-ai**（AI 服务，端口 8094）

#### 方式一：IDEA 直接运行（推荐）

1. 用 IDEA 打开 mall-swarm 项目
2. 等待 Maven 依赖下载完成
3. 按顺序启动服务：
   - 先启动 Nacos
   - 然后启动 `MallGatewayApplication`
   - 最后启动 [`MallAiApplication`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\MallAiApplication.java)

#### 方式二：Maven 命令行

```powershell
# 进入项目根目录
cd "e:\大学课程\软件工程\xiangmu\mall-swarm"

# 编译整个项目
mvn clean install "-DskipTests" "-Ddocker.skip=true"

# 启动 mall-ai 服务（需先启动 Nacos 和 Gateway）
cd mall-ai
mvn spring-boot:run
```

**注意：** 在 PowerShell 中，Maven 的 `-D` 参数必须用双引号包裹。

### 4. 访问 API 文档

**通过 Gateway 网关访问（推荐）：**

浏览器打开：http://localhost:8201/doc.html

在左上角下拉框中选择 **"mall-ai"**，即可查看 AI 相关接口。

**直接访问（开发调试用）：**

http://localhost:8094/doc.html

## API 接口说明

### 1. AI 对话（非流式）

**接口：** `POST /ai/guide/chat`

**请求示例：**
```json
{
  "userId": 1,
  "sessionId": "session_123",
  "message": "我想买一部手机，预算3000元左右",
  "stream": false
}
```

**响应示例：**
```json
{
  "sessionId": "session_123",
  "reply": "根据您的预算，我为您推荐以下几款手机...",
  "recommendedProducts": [...],
  "intent": "recommend",
  "success": true,
  "timestamp": 1234567890
}
```

### 2. AI 对话（流式输出）

**接口：** `POST /ai/guide/stream`

**请求示例：** 同上

**响应：** Server-Sent Events (SSE) 流式输出

### 3. 获取热门商品

**接口：** `GET /ai/guide/hot-products?limit=10`

## 架构设计

### RAG 工作流程

```
用户提问 → 关键词提取 → 数据库检索 → 商品上下文构建 
       → Prompt 组装 → 调用大模型 → 生成回复 → 返回结果
```

### 对话历史管理

- 使用 Redis 存储会话历史
- 每个会话独立存储，支持多用户并发
- 自动清理过期会话（24小时）
- 限制历史记录长度（默认10轮）

### 限流保护

- 基于 Resilience4j 实现 QPS 限流
- 默认限流：10 请求/秒（可通过配置调整）
- 超过限制返回友好提示

## 配置说明

### Spring AI Alibaba 模型配置

在 [`application.yml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\application.yml) 或 Nacos 配置中心配置：

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:sk-xxx}  # 从环境变量读取（标准配置属性名）
      chat:
        options:
          model: qwen-plus              # 使用的模型
          temperature: 0.7              # 温度参数（创造性，0-1）
          max-completion-tokens: 2000   # 最大生成 token 数（1.0.0-M3.2+ 版本）
```

**重要说明：**
- 必须使用 `spring.ai.dashscope.api-key` 作为配置属性名
- 使用 1.0.0-M3.2+ 版本时，应使用 `max-completion-tokens` 而非 `max-tokens`
- 配置选项参数名称必须严格匹配 Java Bean 的 Setter 方法名

### AI 导购业务配置

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

对应配置类：[`AiGuideConfig.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\config\AiGuideConfig.java)

### Nacos 配置

生产环境建议使用 Nacos 配置中心管理配置：

- Data ID: `mall-ai-dev.yaml`
- Group: `DEFAULT_GROUP`
- 配置内容：参考 [`config/ai/mall-ai-dev.yaml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\config\ai\mall-ai-dev.yaml)

**配置优先级：** Nacos 配置 > 本地 application.yml

## 开发指南

### 扩展商品检索逻辑

修改 [`ProductKnowledgeMapper.xml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\mapper\ProductKnowledgeMapper.xml) 中的 SQL 查询，可以优化商品检索算法。

### 自定义系统提示词

修改 [`AiGuideServiceImpl`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java) 中的 `SYSTEM_PROMPT` 常量，调整 AI 助手的角色定位。

### 添加新的意图类型

在 [`detectIntent()`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java#L178-L197) 方法中添加新的意图识别规则。

## 性能优化建议

1. **缓存优化**：对热门商品和常见问题建立缓存（[`ProductKnowledgeService.searchWithCache()`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\ProductKnowledgeService.java#L177-L189) 已预留）
2. **向量检索**：引入向量数据库（如 Milvus）提升检索精度
3. **异步处理**：对耗时操作使用异步处理
4. **连接池**：优化数据库和 Redis 连接池配置

## 注意事项

1. **API Key 安全**
   - ✅ 不要将 API Key 提交到代码仓库
   - ✅ 使用环境变量或配置中心管理
   - ✅ 定期轮换 API Key
   - 📖 参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md)

2. **微服务访问规范**
   - ✅ 所有外部请求应统一通过 Gateway 网关访问
   - ✅ API 文档应通过 Gateway 统一入口访问
   - 📖 参考项目规范：微服务统一访问规范

3. **生产环境建议**
   - 配置 HTTPS
   - 根据实际情况调整限流参数
   - 定期监控 API 调用量和响应时间
   - 使用 Nacos 配置中心管理配置

4. **配置属性名规范**
   - ✅ 必须使用 `spring.ai.dashscope.api-key`（Spring AI Alibaba 标准配置）
   - ❌ 不要使用 `mall.ai.api-key`（这不是标准配置）
   - ✅ 使用 `max-completion-tokens`（1.0.0-M3.2+ 版本）
   - ❌ 不要使用 `max-tokens`（旧版本配置）

## 许可证

MIT License

## 作者

macrozheng

## 相关链接

- [Spring AI Alibaba 官方文档](https://sca.aliyun.com/docs/ai/)
- [阿里云百炼平台](https://bailian.console.aliyun.com/)
- [mall-swarm 项目](https://github.com/macrozheng/mall-swarm)
- [API Key 安全配置指南](API_KEY_SECURITY.md)
- [功能实现详解](FUNCTION_IMPLEMENTATION.md)
- [快速启动指南](QUICK_START.md)
- [项目总结](PROJECT_SUMMARY.md)
