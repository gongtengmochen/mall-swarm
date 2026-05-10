# API Key 安全配置指南

## ⚠️ 重要提醒

**不要将真实的 API Key 提交到 Git 仓库！**

当前项目已配置为从环境变量读取 API Key，确保您的密钥安全。

---

## 🔧 配置方式

### 方式一：环境变量（推荐）✅

#### Windows 系统

**临时设置（当前命令行窗口有效）：**
```cmd
set AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**永久设置（系统级别）：**
1. 右键"此电脑" → "属性" → "高级系统设置"
2. 点击"环境变量"
3. 在"用户变量"或"系统变量"中点击"新建"
4. 变量名：`AI_DASHSCOPE_API_KEY`
5. 变量值：`sk-your-actual-api-key-here`
6. 点击确定保存

**验证配置：**
```cmd
echo %AI_DASHSCOPE_API_KEY%
```

#### Linux/Mac 系统

**临时设置（当前终端会话有效）：**
```bash
export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
```

**永久设置（推荐）：**
编辑 `~/.bashrc` 或 `~/.zshrc` 文件：
```bash
echo 'export AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here' >> ~/.bashrc
source ~/.bashrc
```

**验证配置：**
```bash
echo $AI_DASHSCOPE_API_KEY
```

#### IDEA 中设置环境变量

1. Run → Edit Configurations
2. 找到 MallAiApplication 配置
3. 在 "Environment variables" 中添加：
   ```
   AI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
   ```
4. 点击 Apply → OK

---

### 方式二：IDEA 运行配置（开发环境）✅

1. 打开 Run → Edit Configurations
2. 选择 MallAiApplication
3. 在 "VM options" 中添加：
   ```
   -DAI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here
   ```
4. 或者在 "Environment variables" 中添加键值对

---

### 方式三：Maven 命令行（测试用）⚠️

```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DAI_DASHSCOPE_API_KEY=sk-your-actual-api-key-here"
```

**注意：** 这种方式会在命令行历史中留下密钥，不推荐长期使用。

---

### 方式四：Nacos 配置中心（生产环境推荐）🏆

**重要说明：** 本项目使用 Spring AI Alibaba 框架，API Key 配置必须使用标准配置属性名 `spring.ai.dashscope.api-key`。

1. 登录 Nacos 控制台：http://localhost:8848/nacos
2. 进入"配置管理" → "配置列表"
3. 创建新配置：
   - Data ID: `mall-ai-dev.yaml`
   - Group: `DEFAULT_GROUP`
   - 配置格式: YAML
   - 配置内容：
     ```yaml
     spring:
       ai:
         dashscope:
           api-key: ${AI_DASHSCOPE_API_KEY:your-api-key-here}
           chat:
             options:
               model: qwen-plus
               temperature: 0.7
               max-completion-tokens: 2000
     ```
4. 点击发布

**优点：**
- 集中管理配置
- 支持动态刷新
- 可以配置权限控制
- 不同环境使用不同配置

---

## 📝 配置文件说明

### 1. 本地配置文件（application.yml）

当前 `mall-ai/src/main/resources/application.yml` 中的配置：

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
```

### 2. Nacos 配置中心文件

`config/ai/mall-ai-dev.yaml` 中的配置：

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
```

**解释：**
- `spring.ai.dashscope.api-key` - Spring AI Alibaba 标准配置属性名
- `${AI_DASHSCOPE_API_KEY}` - 从环境变量读取 API Key
- `:sk-9d5c65f97a184e17a08e88a6de17c097` - 如果环境变量未设置，使用默认值（仅用于开发测试提示）

**配置优先级：**
1. Nacos 配置中心 > 本地 application.yml
2. 环境变量 > 配置文件中的默认值

**工作流程：**
1. Spring Boot 启动时先加载 bootstrap.yml，连接 Nacos
2. 从 Nacos 拉取远程配置（如果存在）
3. 尝试从环境变量 `AI_DASHSCOPE_API_KEY` 获取值
4. 如果环境变量存在，使用该值
5. 如果环境变量不存在，使用配置文件中的默认值

---

## 🔒 安全最佳实践

### ✅ 应该做的

1. **使用环境变量存储敏感信息**
   ```bash
   export AI_DASHSCOPE_API_KEY=sk-xxx
   ```

2. **将包含真实密钥的文件加入 .gitignore**
   ```
   # .gitignore
   application-local.yml
   .env
   *.key
   ```

3. **使用配置中心管理生产环境密钥**
   - Nacos Config（本项目已集成）
   - Apollo
   - AWS Secrets Manager
   - HashiCorp Vault

4. **定期轮换 API Key**
   - 建议每 3-6 个月更换一次
   - 发现泄露立即更换

5. **限制 API Key 权限**
   - 只授予必要的权限
   - 设置用量上限
   - 监控异常使用

6. **使用 Spring AI Alibaba 标准配置属性**
   - 必须使用 `spring.ai.dashscope.api-key`
   - 不能使用自定义属性名如 `mall.ai.api-key`

### ❌ 不应该做的

1. **不要硬编码 API Key**
   ```yaml
   # ❌ 错误示例
   spring:
     ai:
       dashscope:
         api-key: sk-9d5c65f97a184e17a08e88a6de17c097
   ```

2. **不要使用错误的配置属性名**
   ```yaml
   # ❌ 错误示例 - 这不是 Spring AI Alibaba 的标准配置
   mall:
     ai:
       api-key: sk-xxx
   ```

3. **不要提交包含密钥的配置文件到 Git**
   ```bash
   # ❌ 危险操作
   git add application.yml  # 如果包含真实密钥
   git commit -m "update config"
   ```

4. **不要在日志中打印 API Key**
   ```java
   // ❌ 错误示例
   log.info("API Key: {}", apiKey);
   ```

5. **不要在代码注释中包含密钥**
   ```java
   // ❌ 错误示例
   // API Key: sk-xxx
   ```

6. **不要通过 URL 参数传递密钥**
   ```
   # ❌ 错误示例
   http://api.example.com?key=sk-xxx
   ```

---

## 🛡️ 检查清单

在提交代码前，请确认：

- [ ] API Key 已通过环境变量配置
- [ ] 使用 Spring AI Alibaba 标准配置属性 `spring.ai.dashscope.api-key`
- [ ] 配置文件中使用的是 `${AI_DASHSCOPE_API_KEY:placeholder}` 格式
- [ ] 没有硬编码的真实 API Key
- [ ] `.gitignore` 已配置忽略敏感文件
- [ ] 本地测试时使用测试密钥或占位符
- [ ] 生产环境使用 Nacos 配置中心管理密钥
- [ ] Nacos 配置中也使用环境变量引用格式

---

## 🔍 如何检查是否泄露

### 检查 Git 历史

```bash
# 搜索 Git 历史中是否包含 API Key
git log --all -p | grep "sk-[a-zA-Z0-9]"
```

### 如果发现泄露

1. **立即撤销相关提交**
   ```bash
   git revert <commit-hash>
   ```

2. **清理 Git 历史（谨慎操作）**
   ```bash
   # 使用 BFG Repo-Cleaner 或 git filter-branch
   ```

3. **立即更换 API Key**
   - 登录阿里云百炼平台
   - 删除旧密钥
   - 生成新密钥
   - 更新所有使用该密钥的服务

4. **检查是否有未授权的调用**
   - 查看阿里云控制台的使用记录
   - 分析异常调用模式

---

## 📞 获取帮助

如遇到配置问题：

1. 检查环境变量是否正确设置
   ```bash
   echo $AI_DASHSCOPE_API_KEY  # Linux/Mac
   echo %AI_DASHSCOPE_API_KEY% # Windows
   ```

2. 查看应用启动日志
   ```
   寻找类似：spring.ai.dashscope.api-key = *** (masked)
   ```

3. 验证 Nacos 配置是否正确导入
   - 访问 Nacos 控制台：http://localhost:8848/nacos
   - 检查配置列表中是否存在 `mall-ai-dev.yaml`
   - 确认配置内容格式正确

4. 参考官方文档
   - [Spring AI Alibaba 官方文档](https://sca.aliyun.com/docs/ai/)
   - [Spring Boot 外部化配置](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
   - [阿里云百炼平台](https://bailian.console.aliyun.com/)

---

## 💡 小贴士

1. **开发环境可以使用测试密钥**
   - 申请专门的测试账号
   - 设置较低的用量限额
   - 避免使用生产环境密钥

2. **使用 .env 文件（不提交到 Git）**
   ```bash
   # .env 文件
   AI_DASHSCOPE_API_KEY=sk-test-key
   
   # .gitignore 中添加
   .env
   ```

3. **IDEA 插件推荐**
   - EnvFile：自动加载 .env 文件
   - Configuration as Code：管理运行配置

4. **CI/CD 中配置密钥**
   - GitHub Actions: Settings → Secrets
   - Jenkins: Credentials
   - GitLab CI: Settings → CI/CD → Variables

5. **Spring AI Alibaba 配置注意事项**
   - 必须使用 `spring.ai.dashscope.api-key` 作为配置属性名
   - 配置选项参数名称必须严格匹配 Java Bean 的 Setter 方法名
   - 使用 1.0.0-M3.2 及以上版本时，使用 `max-completion-tokens` 而非 `max-tokens`

---

**记住：API Key 是您的数字资产，请像保护银行卡密码一样保护它！** 🔐
