# AI智能导购与商品问答助手 - 功能实现说明

## 一、项目概述

本项目在 mall-swarm 微服务电商系统基础上，集成 Spring AI Alibaba 和通义千问大模型，实现了基于 RAG（检索增强生成）技术的 AI 智能导购与商品问答助手。

## 二、核心功能实现

### 1. 商品信息结构化处理与知识库构建

**实现位置：** [`ProductKnowledgeService.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\ProductKnowledgeService.java) + [`ProductKnowledgeMapper.xml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\mapper\ProductKnowledgeMapper.xml)

**功能特点：**
- ✅ 从 MySQL 数据库实时检索商品信息
- ✅ 支持关键词搜索、分类筛选、价格区间查询
- ✅ 商品数据结构化：名称、品牌、价格、销量、库存、描述等
- ✅ RAG 上下文格式化，为大模型提供准确的商品信息
- ✅ Redis 缓存优化（预留接口）

**关键代码：**
```java
// 根据用户问题检索相关商品
List<ProductInfoDTO> retrieveRelevantProducts(String question, Integer limit)

// 将商品信息格式化为RAG上下文
String formatProductContext(List<ProductInfoDTO> products)
```

### 2. 基于大模型的用户意图识别与多轮对话

**实现位置：** [`AiGuideServiceImpl.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java)

**功能特点：**
- ✅ 用户意图识别：question（咨询）、recommend（推荐）、complaint（投诉）、other（其他）
- ✅ 多轮对话管理：基于 Redis 存储会话历史
- ✅ 上下文保持：自动维护最近 N 轮对话历史
- ✅ 自然语言回复：调用通义千问 qwen-plus 模型生成回复

**意图识别逻辑：**
```java
public String detectIntent(String message) {
    // 推荐意图：包含"推荐"、"想买"、"想要"等关键词
    // 问题意图：包含"什么"、"怎么"、"如何"等疑问词
    // 投诉意图：包含"投诉"、"差评"、"退款"等关键词
}
```

**对话历史管理：**
- 使用 Redis List 存储会话消息
- 键格式：`ai:session:{sessionId}`
- 自动限制历史记录长度（默认10轮）
- 24小时自动过期

### 3. AI 导购逻辑与商品推荐

**实现位置：** [`AiGuideServiceImpl.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java) + [`ProductKnowledgeService.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\ProductKnowledgeService.java)

**功能特点：**
- ✅ 根据对话内容自动检索相关商品
- ✅ RAG 技术确保推荐基于真实商品数据
- ✅ 生成个性化推荐理由
- ✅ 支持多维度筛选：价格、品牌、分类、销量

**推荐流程：**
```
用户提问 → 提取关键词 → 检索商品库 → 获取Top K相关商品 
→ 构建Prompt（含商品信息）→ 调用大模型 → 生成推荐回复
```

**推荐理由生成：**
```java
String generateRecommendReason(String productInfo, String userPreference)
```

### 4. 高可用工程优化

**实现位置：** [`Resilience4jConfig.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\config\Resilience4jConfig.java) + [`AiGuideServiceImpl.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java)

**功能特点：**
- ✅ **限流保护**：基于 Resilience4j 实现 QPS 限流
  - 默认限流：10 请求/秒（可通过 `mall.ai.rate-limit-qps` 配置）
  - 超过限制返回友好提示
  
- ✅ **重试机制**：API 调用失败自动重试（配置中预留）
  - 最大重试次数：3次（可通过 `mall.ai.max-retries` 配置）
  - 超时时间：30秒（可通过 `mall.ai.api-timeout` 配置）

- ✅ **降级策略**：服务异常时返回降级响应
  ```java
  try {
      // 正常逻辑
  } catch (Exception e) {
      log.error("AI对话失败", e);
      return buildErrorResponse(...); // 降级响应
  }
  ```

- ✅ **Redis 缓存**：
  - 会话历史缓存
  - 商品搜索结果缓存（预留）
  - 热点数据缓存（预留）

### 5. 前后端对话界面与流式输出

**实现位置：** [`AiGuideController.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\controller\AiGuideController.java) + [`AiGuideServiceImpl.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java)

**功能特点：**
- ✅ RESTful API 接口设计
- ✅ 支持普通响应（JSON）
- ✅ 支持流式输出（SSE - Server-Sent Events）
- ✅ Swagger/Knife4j API 文档集成

**API 接口：**

1. **普通对话（非流式）**
   ```
   POST /ai/guide/chat
   Content-Type: application/json
   
   Request:
   {
     "userId": 1,
     "sessionId": "session_123",
     "message": "我想买一部手机",
     "stream": false
   }
   
   Response:
   {
     "sessionId": "session_123",
     "reply": "为您推荐以下几款手机...",
     "recommendedProducts": [...],
     "intent": "recommend",
     "success": true
   }
   ```

2. **流式对话（SSE）**
   ```
   POST /ai/guide/stream
   Content-Type: application/json
   Accept: text/event-stream
   
   Response: Server-Sent Events 流式输出
   ```

## 三、技术架构

### 1. 整体架构图

```
┌─────────────┐
│   前端应用   │
└──────┬──────┘
       │ HTTP/SSE
       ▼
┌─────────────┐
│  Gateway    │ ← 路由、限流、鉴权
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  mall-ai    │ ← AI智能导购服务
│             │
│ ┌─────────┐ │
│ │Controller│ │ ← REST API
│ └────┬────┘ │
│      │      │
│ ┌────▼────┐ │
│ │ Service │ │ ← 业务逻辑
│ └────┬────┘ │
│      │      │
│ ┌────▼────┐ │
│ │ Mapper  │ │ ← 数据访问
│ └────┬────┘ │
└──────┼──────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
┌─────┐ ┌──────┐
│MySQL│ │Redis │
└─────┘ └──────┘
   ▲
   │
   ▼
┌──────────┐
│DashScope │ ← 通义千问API
│ (Qwen)   │
└──────────┘
```

### 2. RAG 工作流程

```
┌──────────┐
│ 用户提问  │
└────┬─────┘
     │
     ▼
┌──────────────┐
│ 关键词提取    │ ← extractKeywords()
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ 商品检索      │ ← ProductKnowledgeMapper
│ (基于关键词)  │    searchProductsByKeyword()
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ 上下文构建    │ ← formatProductContext()
│ (格式化商品信息)│
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ Prompt 组装   │ ← System Prompt + 商品上下文 + 对话历史
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ 调用大模型    │ ← DashScopeChatModel.call()
│ (通义千问)    │
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ 生成回复      │ ← 解析模型输出
└────┬─────────┘
     │
     ▼
┌──────────────┐
│ 返回结果      │ ← ChatResponseDTO
└──────────────┘
```

## 四、配置说明

### 1. Maven 依赖

已在 `pom.xml` 中配置：
- `spring-ai-alibaba-starter` - Spring AI Alibaba 核心依赖
- `resilience4j-spring-boot3` - 限流和熔断
- `spring-boot-starter-webflux` - 响应式编程支持（流式输出）

### 2. 应用配置

本项目配置分为两部分：**Spring AI Alibaba 模型配置**（用于连接大模型）和 **AI 导购业务配置**（用于控制业务逻辑）。

#### （1）Spring AI Alibaba 模型配置

这部分配置用于初始化 `DashScopeChatModel`，通常位于 `application.yml` 或 Nacos 配置中心的 `spring.ai` 节点下。

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:sk-your-api-key-here}  # 阿里云百炼API Key
      chat:
        options:
          model: qwen-plus              # 模型名称
          temperature: 0.7              # 温度参数（0-1，越高越有创造性）
          max-completion-tokens: 2000   # 最大生成token数
```

**配置说明：**
- `api-key`: 阿里云百炼平台的 API Key。建议通过环境变量 `AI_DASHSCOPE_API_KEY` 注入，避免硬编码。
- `model`: 使用的通义千问模型版本，如 `qwen-plus`, `qwen-turbo`, `qwen-max` 等。
- `temperature`: 控制输出的随机性。
- `max-completion-tokens`: 限制单次生成的最大 Token 数量。

#### （2）AI 导购业务配置

这部分配置由 [`AiGuideConfig.java`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\config\AiGuideConfig.java) 类加载，前缀为 `mall.ai`。

```yaml
mall:
  ai:
    enable-stream: true                # 是否启用流式输出
    max-history-size: 10               # 会话历史保留的最大轮数
    rag-top-k: 5                       # RAG 检索相关商品的数量 (Top K)
    api-timeout: 30000                 # 调用大模型 API 的超时时间 (毫秒)
    max-retries: 3                     # API 调用失败后的最大重试次数
    rate-limit-qps: 10                 # 接口限流 QPS (每秒请求数)
```

**配置类定义：**
```java
@Data
@Component
@ConfigurationProperties(prefix = "mall.ai")
public class AiGuideConfig {
    private Boolean enableStream = true;
    private Integer maxHistorySize = 10;
    private Integer ragTopK = 5;
    private Long apiTimeout = 30000L;
    private Integer maxRetries = 3;
    private Integer rateLimitQps = 10;
}
```

### 3. 环境变量与安全

**设置 API Key：**

为了安全起见，不建议将真实的 API Key 直接写在配置文件中，推荐使用环境变量。

**Linux/Mac:**
```bash
export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**Windows:**
```cmd
set AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**IDEA 中设置：**
1. 打开 `Run/Debug Configurations`
2. 在 `Environment variables` 中添加：`AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here`

**安全最佳实践：**
- ⚠️ **严禁**将包含真实 API Key 的文件提交到 Git 仓库。
- ✅ 生产环境建议使用 Nacos 配置中心或 Kubernetes Secrets 管理敏感配置。
- ✅ 定期轮换 API Key。
- 📖 更多细节请参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md)。

## 五、部署与运行

### 1. 前置条件

- ✅ JDK 17+
- ✅ Maven 3.6+
- ✅ MySQL 5.7+（已有 mall 数据库）
- ✅ Redis 7.0+
- ✅ Nacos 2.x（服务注册与配置中心，可选，若使用本地 application.yml 则非必需）
- ✅ 阿里云百炼 API Key

### 2. 启动步骤

```bash
# 1. 编译项目
cd mall-swarm
mvn clean package -DskipTests

# 2. 启动基础中间件（如果未启动）
# 启动 Nacos (如果使用 Nacos 配置)
cd nacos/bin
startup.cmd -m standalone  # Windows
# ./startup.sh -m standalone # Linux/Mac

# 确保 MySQL 和 Redis 已启动

# 3. 设置环境变量 (如果未在配置文件中硬编码 API Key)
export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here

# 4. 启动 mall-ai 服务
cd mall-ai
java -jar target/mall-ai-1.0-SNAPSHOT.jar

# 或在 IDEA 中直接运行 MallAiApplication
```

### 3. 验证服务

启动成功后，可以通过以下方式验证：

1. **Swagger/Knife4j 文档**:
   - 直接访问: `http://localhost:8094/doc.html`
   - 通过 Gateway: `http://localhost:8201/mall-ai/doc.html`

2. **运行测试脚本**:
   ```bash
   cd mall-ai
   chmod +x test-api.sh
   ./test-api.sh  # Linux/Mac
   # Windows 用户可以使用 PowerShell 执行等效命令或使用 Postman
   ```

## 六、性能优化建议

### 1. 短期优化

- [ ] 实现商品搜索结果 Redis 缓存（[`ProductKnowledgeService.searchWithCache()`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\ProductKnowledgeService.java#L177-L189) 已预留）
- [ ] 添加常见问题（FAQ）缓存
- [ ] 优化关键词提取算法（使用分词工具如 HanLP）

### 2. 中期优化

- [ ] 引入向量数据库（Milvus/Elasticsearch）提升检索精度
- [ ] 实现商品 Embedding 向量化
- [ ] 添加语义相似度检索

### 3. 长期优化

- [ ] 用户画像与个性化推荐
- [ ] A/B 测试框架
- [ ] 对话质量评估与反馈机制
- [ ] 多模型融合与路由

## 七、安全注意事项

1. **API Key 保护**
   - ✅ 不要将 API Key 提交到代码仓库
   - ✅ 使用环境变量或配置中心管理
   - ✅ 定期轮换 API Key
   - 📖 参考 [`API_KEY_SECURITY.md`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\API_KEY_SECURITY.md)

2. **输入验证**
   - 对用户输入进行 sanitization
   - 限制输入长度
   - 防止 Prompt Injection 攻击

3. **速率限制**
   - ✅ 已实现 QPS 限流（Resilience4j）
   - 建议添加用户级别限流
   - 监控异常调用模式

4. **数据隐私**
   - 不传输敏感用户信息到大模型
   - 对话记录脱敏处理
   - 符合 GDPR 等隐私法规

## 八、监控与日志

### 1. 关键指标

- API 调用量
- 平均响应时间
- 错误率
- 限流触发次数
- Token 消耗量

### 2. 日志记录

已在代码中添加详细日志：
- INFO：服务启动、关键操作
- WARN：参数错误、降级触发
- ERROR：异常堆栈、API 调用失败

### 3. 集成 Spring Boot Admin

mall-ai 已集成 Spring Boot Admin Client，可在监控中心查看：
- 服务健康状态
- JVM 指标
- HTTP 请求统计
- 日志实时查看

## 九、扩展开发指南

### 1. 添加新的意图类型

修改 [`AiGuideServiceImpl.detectIntent()`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java#L178-L197) 方法：

```java
if (lowerMessage.matches(".*售后|维修|保养.*")) {
    return "after_sales";
}
```

### 2. 优化商品检索算法

修改 [`ProductKnowledgeMapper.xml`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\resources\mapper\ProductKnowledgeMapper.xml) 中的 SQL：
- 添加全文索引
- 优化排序规则
- 增加过滤条件

### 3. 自定义系统提示词

修改 [`AiGuideServiceImpl.SYSTEM_PROMPT`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\impl\AiGuideServiceImpl.java#L50-L62) 常量，调整 AI 助手的角色定位和行为规范。

### 4. 集成更多数据源

在 [`ProductKnowledgeService`](file://e:\大学课程\软件工程\xiangmu\mall-swarm\mall-ai\src\main\java\com\macro\mall\ai\service\ProductKnowledgeService.java) 中添加：
- 用户评价数据
- 商品问答数据
- 促销活动信息

## 十、总结

本项目成功实现了基于大模型的 AI 智能导购与商品问答助手，核心亮点包括：

✅ **RAG 技术**：基于真实商品数据生成回答，避免幻觉  
✅ **多轮对话**：完整的会话管理和上下文保持  
✅ **意图识别**：自动识别用户需求类型  
✅ **智能推荐**：个性化商品推荐和理由生成  
✅ **高可用**：限流、重试、降级等多重保障  
✅ **流式输出**：提升用户体验的 SSE 支持  
✅ **工程化**：完整的配置、文档、测试脚本  

该系统可显著提升电商平台的购物体验和转化效率，为用户提供 7x24 小时的智能购物助手服务。

---

**相关文档：**
- [API Key 安全配置指南](API_KEY_SECURITY.md)
- [快速开始指南](QUICK_START.md)
- [项目总结](PROJECT_SUMMARY.md)
- [Postman 测试集合](mall-ai-api.postman_collection.json)
