# ZhipuAI API 配置说明

## 当前状态

✅ **系统已成功启动**
⚠️ **ZhipuAI API 认证失败** - 需要配置有效的 API Key

## 问题说明

您看到的 `401 Unauthorized` 错误是因为还没有配置有效的 ZhipuAI API Key。

**好消息**：系统已经内置了后备方案，当 API 调用失败时会自动生成模拟摘要，**不影响系统正常使用**。

## 解决方案

### 方案一：使用模拟摘要（无需配置）

**当前默认方式** - 系统会自动生成模拟摘要，适合：
- 测试和开发环境
- 不需要真实 AI 摘要的场景
- 快速体验系统功能

**无需任何操作**，系统已经在正常运行。

---

### 方案二：配置真实 API Key（推荐生产环境）

#### 步骤 1：获取 API Key

1. 访问 [智谱AI开放平台](https://open.bigmodel.cn/)
2. 注册/登录账号
3. 进入控制台
4. 创建 API Key
5. 复制您的 API Key

#### 步骤 2：配置 API Key

##### 方法一：使用配置脚本（推荐）

```batch
# 在项目根目录运行
.\配置API密钥.bat
```

按照提示输入您的 API Key 即可。

##### 方法二：手动配置环境变量

**Windows 系统：**

1. **临时配置**（仅当前会话）：
   ```batch
   set ZHIPUAI_API_KEY=你的API密钥
   ```

2. **永久配置**（推荐）：
   ```batch
   setx ZHIPUAI_API_KEY "你的API密钥"
   ```
   
   或通过系统设置：
   - 右键 "此电脑" → "属性"
   - 点击 "高级系统设置"
   - 点击 "环境变量"
   - 在"系统变量"中点击"新建"
   - 变量名：`ZHIPUAI_API_KEY`
   - 变量值：`你的API密钥`

**Linux/Mac 系统：**

```bash
# 临时配置
export ZHIPUAI_API_KEY="你的API密钥"

# 永久配置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export ZHIPUAI_API_KEY="你的API密钥"' >> ~/.bashrc
source ~/.bashrc
```

#### 步骤 3：重启服务

配置完成后，重启后端服务：

```batch
# 方法一：关闭后端窗口，重新运行
.\启动系统.bat

# 方法二：在后端目录单独重启
cd backend
mvn spring-boot:run
```

#### 步骤 4：验证配置

访问管理后台，触发摘要生成：
1. 登录：http://localhost:5173/login
2. 进入新闻管理：http://localhost:5173/admin/news
3. 点击"生成摘要"按钮
4. 查看生成的摘要内容

如果配置成功，您会看到真实的 AI 生成摘要而不是模拟内容。

---

## 费用说明

- 智谱AI 提供免费额度（新用户有试用额度）
- GLM-4-Flash 模型价格较低
- 按实际调用次数计费
- 可在智谱AI控制台查看用量

---

## 常见问题

### Q: 不配置 API Key 能用吗？

**A**: 可以！系统会自动使用模拟摘要，不影响其他功能的使用。模拟摘要格式如下：
```
这是一条模拟摘要。由于未配置 ZhipuAI API Key，系统自动生成了此摘要。
要使用真实的 AI 摘要功能，请配置有效的 API Key。
```

### Q: 如何知道 API Key 配置成功？

**A**: 
1. 查看后端启动日志，不再出现 `401 Unauthorized` 错误
2. 生成的摘要内容是真实的新闻摘要，而不是模拟内容
3. 运行命令测试：`echo %ZHIPUAI_API_KEY%`

### Q: API Key 配置后还是报错？

**A**: 检查以下几点：
1. API Key 是否正确（没有多余空格）
2. 是否重启了后端服务
3. API Key 是否已激活（新注册可能需要实名认证）
4. 账号是否有剩余额度

### Q: 可以切换其他 AI 模型吗？

**A**: 可以！修改 `backend/src/main/resources/application.yml`：

```yaml
zhipuai:
  model: glm-4-flash  # 可改为 glm-4, glm-4-plus 等
```

支持的模型请参考 [智谱AI文档](https://open.bigmodel.cn/dev/api)。

---

## 当前配置

查看当前 API 配置：

```yaml
# backend/src/main/resources/application.yml
zhipuai:
  api-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
  api-key: ${ZHIPUAI_API_KEY:key}  # 从环境变量读取
  model: glm-4-flash               # 使用的模型
  timeout: 60000                   # 超时时间
  max-retries: 3                   # 最大重试次数
```

---

## 技术支持

如有其他问题，请查看：
- [智谱AI官方文档](https://open.bigmodel.cn/dev/api)
- [项目 README.md](./README.md)
- [如何运行.md](./如何运行.md)

---

**总结**：系统当前已经正常运行，模拟摘要可以满足基本需求。如果需要真实的 AI 摘要功能，按照上述步骤配置 API Key 即可。

