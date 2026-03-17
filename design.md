# AI 智能体系统完整设计方案

## 一、系统架构详细设计

### 1.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                 用户层                                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│  │  Web 前端   │  │  IDE 插件   │  │  GitLab Hook │  │  API 调用   │        │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘        │
└─────────┼────────────────┼────────────────┼────────────────┼───────────────┘
          │                │                │                │
┌─────────▼────────────────▼────────────────▼────────────────▼───────────────┐
│                                API 网关层                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                     Spring Boot 3.x REST API                        │    │
│  │   /api/code-review  /api/approval  /api/chat  /api/knowledge        │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────┐
│                              Agent 调度层                                    │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                    AgentDispatcher (调度中心)                       │    │
│  │   - 意图识别 (Intent Recognition)                                    │    │
│  │   - Agent 选择 (Agent Selection)                                     │    │
│  │   - 结果聚合 (Result Aggregation)                                   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
          │                │                │                │                │
┌─────────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐       
│ 代码审查Agent  │  │ 改进建议Agent│  │ 审批Agent   │  │  聊天Agent  │       
│CodeReviewAgent │  │OptimizeAgent│  │ApprovalAgent│  │  ChatAgent  │       
└───────┬────────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘       
        │                 │                 │                 │               
┌───────▼─────────────────▼─────────────────▼─────────────────▼─────────────┐
│                              RAG 知识库层                                     │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │ 文档上传    │  │ 文本分块    │  │ 向量化      │  │ 相似度检索  │           │
│  │ Document   │  │  Chunking   │  │ Embedding   │  │  Search    │           │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────┐
│                              AI 能力层                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      Ollama (本地大模型)                            │    │
│  │   - CodeLlama (代码审查)                                            │    │
│  │   - Llama3 (通用对话)                                               │    │
│  │   - Mistral (中文优化)                                             │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
┌─────────────────────────────────────▼───────────────────────────────────────┐
│                              数据存储层                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐           │
│  │   MySQL     │  │   Redis     │  │   Milvus    │  │   MinIO     │           │
│  │ (结构化数据)│  │ (缓存/会话) │  │  (向量存储) │  │ (文件存储)  │           │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.2 技术栈清单

| 层级 | 技术选型 | 版本要求 | 说明 |
|------|---------|---------|------|
| 后端框架 | Spring Boot | 3.2.x | Java 17+ |
| ORM | MyBatis Plus | 3.5.x | 简化 CRUD |
| 数据库 | MySQL | 8.0+ | Docker 部署 |
| 缓存 | Redis | 7.0+ | 会话/缓存 |
| 向量库 | Milvus Lite | 2.3.x | 轻量向量存储 |
| AI 运行时 | Ollama | 0.1.x | 本地大模型 |
| 大模型 | Llama3 / CodeLlama | latest | 模型选择 |
| 前端 | Vue3 | 3.4+ | Composition API |
| UI 组件 | Element Plus | 2.5+ | UI 组件库 |
| 构建工具 | Vite | 5.0+ | 前端构建 |
| 状态管理 | Pinia | 2.1+ | Vue 状态管理 |
| 审批引擎 | Flowable | 6.8.x | 流程引擎 |
| 对象存储 | MinIO | latest | 文件存储 |

---

## 二、数据库详细设计

### 2.1 核心表结构

#### 2.1.1 用户表 (sys_user)

```sql
CREATE TABLE sys_user (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(200)    NOT NULL COMMENT '密码(加密)',
    nickname        VARCHAR(50)     COMMENT '昵称',
    email           VARCHAR(100)    COMMENT '邮箱',
    phone           VARCHAR(20)     COMMENT '手机号',
    avatar          VARCHAR(500)    COMMENT '头像URL',
    role            VARCHAR(20)     DEFAULT 'user' COMMENT '角色: admin/user/approver',
    status          TINYINT         DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    last_login_time DATETIME        COMMENT '最后登录时间',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_status (status)
) COMMENT '系统用户表';
```

#### 2.1.2 代码审查表 (code_review)

```sql
CREATE TABLE code_review (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '审查ID',
    user_id             BIGINT          NOT NULL COMMENT '提交人ID',
    project_name        VARCHAR(100)    COMMENT '项目名称',
    file_name           VARCHAR(200)    COMMENT '文件名',
    file_path           VARCHAR(500)    COMMENT '文件路径',
    code_content        TEXT            NOT NULL COMMENT '代码内容',
    language            VARCHAR(20)     COMMENT '语言: java/python/vue/javascript',
    review_type         VARCHAR(20)     DEFAULT 'manual' COMMENT '审查类型: manual/manual_pr/gitlab_webhook',
    review_result       JSON            COMMENT '审查结果(JSON)',
    issues_count        INT             DEFAULT 0 COMMENT '问题数量',
    severity_critical   INT             DEFAULT 0 COMMENT '严重问题数',
    severity_major      INT             DEFAULT 0 COMMENT '重要问题数',
    severity_minor      INT             DEFAULT 0 COMMENT '次要问题数',
    status              VARCHAR(20)     DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/optimized',
    optimized_code      TEXT            COMMENT '优化后代码',
    optimize_suggestion TEXT            COMMENT '优化建议',
    review_time         INT             COMMENT '审查耗时(毫秒)',
    model_used          VARCHAR(50)     COMMENT '使用模型',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) COMMENT '代码审查记录表';
```

#### 2.1.3 审批流程表 (approval_flow)

```sql
CREATE TABLE approval_flow (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '审批ID',
    review_id           BIGINT          COMMENT '关联审查ID',
    flow_no             VARCHAR(50)     NOT NULL UNIQUE COMMENT '流程编号',
    title               VARCHAR(200)    NOT NULL COMMENT '审批标题',
    applicant_id        BIGINT          NOT NULL COMMENT '申请人ID',
    approver_id         BIGINT          COMMENT '审批人ID',
    flow_type           VARCHAR(20)     DEFAULT 'code_review' COMMENT '流程类型',
    content             TEXT            COMMENT '审批内容',
    status              VARCHAR(20)     DEFAULT 'pending' COMMENT '状态: pending/approved/rejected/cancelled',
    priority            TINYINT         DEFAULT 1 COMMENT '优先级: 1低 2中 3高',
    due_date            DATETIME        COMMENT '截止时间',
    approval_comment    TEXT            COMMENT '审批意见',
    notify_channels     VARCHAR(100)    DEFAULT 'webhook' COMMENT '通知渠道: webhook/email/wechat',
    notify_status        VARCHAR(20)     DEFAULT 'not_sent' COMMENT '通知状态',
    approved_at         DATETIME        COMMENT '审批通过时间',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_flow_no (flow_no),
    INDEX idx_review_id (review_id),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) COMMENT '审批流程表';
```

#### 2.1.4 知识库文档表 (knowledge_doc)

```sql
CREATE TABLE knowledge_doc (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    title               VARCHAR(200)    NOT NULL COMMENT '文档标题',
    content             TEXT            NOT NULL COMMENT '文档内容',
    doc_type            VARCHAR(20)     DEFAULT 'general' COMMENT '文档类型: general/specification/faq/architecture',
    file_url            VARCHAR(500)    COMMENT '原始文件URL',
    file_type           VARCHAR(20)     COMMENT '文件类型: pdf/docx/txt/md',
    file_size           BIGINT          COMMENT '文件大小(字节)',
    vector_status       VARCHAR(20)     DEFAULT 'pending' COMMENT '向量化状态: pending/processing/completed/failed',
    vector_id           VARCHAR(100)    COMMENT '向量库ID',
    chunks_count        INT             DEFAULT 0 COMMENT '分块数量',
    uploader_id          BIGINT          NOT NULL COMMENT '上传人ID',
    is_public           TINYINT         DEFAULT 1 COMMENT '是否公开: 0私有 1公开',
    tags                VARCHAR(500)    COMMENT '标签,逗号分隔',
    view_count          INT             DEFAULT 0 COMMENT '查看次数',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_doc_type (doc_type),
    INDEX idx_vector_status (vector_status),
    INDEX idx_uploader_id (uploader_id)
) COMMENT '知识库文档表';
```

#### 2.1.5 对话历史表 (chat_message)

```sql
CREATE TABLE chat_message (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    user_id             BIGINT          NOT NULL COMMENT '用户ID',
    session_id          VARCHAR(50)     NOT NULL COMMENT '会话ID',
    role                VARCHAR(20)     NOT NULL COMMENT '角色: user/assistant/system',
    content             TEXT            NOT NULL COMMENT '消息内容',
    model_used          VARCHAR(50)     COMMENT '使用的模型',
    token_count         INT             COMMENT 'token数量',
    metadata            JSON            COMMENT '扩展信息',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) COMMENT '对话消息表';
```

#### 2.1.6 会话表 (chat_session)

```sql
CREATE TABLE chat_session (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id             BIGINT          NOT NULL COMMENT '用户ID',
    session_id          VARCHAR(50)     NOT NULL UNIQUE COMMENT '会话UUID',
    title               VARCHAR(200)    COMMENT '会话标题',
    agent_type          VARCHAR(20)     DEFAULT 'chat' COMMENT 'Agent类型: chat/code_review/knowledge',
    context_count       INT             DEFAULT 0 COMMENT '上下文消息数',
    last_message_time   DATETIME        COMMENT '最后消息时间',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
) COMMENT '会话表';
```

#### 2.1.7 系统配置表 (system_config)

```sql
CREATE TABLE system_config (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    config_key          VARCHAR(50)     NOT NULL UNIQUE COMMENT '配置键',
    config_value        TEXT            COMMENT '配置值',
    config_type         VARCHAR(20)     DEFAULT 'string' COMMENT '类型: string/json/number/boolean',
    description         VARCHAR(200)    COMMENT '描述',
    is_encrypted        TINYINT         DEFAULT 0 COMMENT '是否加密: 0否 1是',
    is_system           TINYINT         DEFAULT 0 COMMENT '是否系统配置: 0否 1是',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) COMMENT '系统配置表';

-- 初始配置数据
INSERT INTO system_config (config_key, config_value, config_type, description, is_system) VALUES
('ollama.base_url', 'http://localhost:11434', 'string', 'Ollama服务地址', 1),
('ollama.default_model', 'llama3', 'string', '默认使用模型', 1),
('ollama.code_model', 'codellama', 'string', '代码审查专用模型', 1),
('vector.db_type', 'milvus', 'string', '向量数据库类型', 1),
('vector.dimension', '1024', 'number', '向量维度', 1),
('approval.default_approver', '', 'string', '默认审批人ID', 1),
('approval.notify_enabled', 'true', 'boolean', '是否启用审批通知', 1),
('system.max_code_length', '50000', 'number', '最大代码长度', 1);
```

---

## 三、API 接口详细设计

### 3.1 API 统一规范

```
基础路径: /api/v1

统一响应格式:
{
    "code": 200,           // 状态码
    "message": "success", // 提示信息
    "data": { ... },       // 响应数据
    "timestamp": 1234567890 // 时间戳
}

分页响应格式:
{
    "code": 200,
    "message": "success",
    "data": {
        "records": [...],
        "total": 100,
        "page": 1,
        "size": 10
    }
}
```

### 3.2 代码审查模块 API

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /code-review/submit | 提交代码审查 | 是 |
| POST | /code-review/submit-batch | 批量提交审查 | 是 |
| GET | /code-review/{id} | 获取审查结果 | 是 |
| GET | /code-review/list | 审查列表(分页) | 是 |
| POST | /code-review/{id}/optimize | 获取优化建议 | 是 |
| DELETE | /code-review/{id} | 删除审查记录 | 是 |
| POST | /code-review/webhook/gitlab | GitLab WebHook | 否 |

#### 3.2.1 提交代码审查

```http
POST /api/v1/code-review/submit
Content-Type: application/json
Authorization: Bearer {token}

Request:
{
    "projectName": "user-service",
    "fileName": "UserService.java",
    "filePath": "src/main/java/com/example/UserService.java",
    "codeContent": "public class UserService { ... }",
    "language": "java",
    "reviewType": "manual"
}

Response:
{
    "code": 200,
    "message": "提交成功",
    "data": {
        "id": 1,
        "status": "pending",
        "estimatedTime": 5000
    }
}
```

#### 3.2.2 审查结果详情

```http
GET /api/v1/code-review/{id}

Response:
{
    "code": 200,
    "data": {
        "id": 1,
        "fileName": "UserService.java",
        "language": "java",
        "reviewResult": {
            "summary": {
                "totalIssues": 5,
                "critical": 1,
                "major": 2,
                "minor": 2
            },
            "issues": [
                {
                    "line": 15,
                    "severity": "critical",
                    "category": "security",
                    "message": "SQL注入风险: 直接拼接SQL参数",
                    "suggestion": "使用预编译语句或参数化查询"
                },
                {
                    "line": 28,
                    "severity": "major",
                    "category": "performance",
                    "message": "N+1查询问题",
                    "suggestion": "使用JOIN查询或批量查询"
                }
            ],
            "score": 75,
            "recommendations": [
                "建议添加事务注解",
                "建议添加日志记录"
            ]
        },
        "status": "completed",
        "reviewTime": 3500,
        "createdAt": "2024-01-15 10:30:00"
    }
}
```

### 3.3 审批模块 API

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /approval/create | 创建审批流程 | 是 |
| GET | /approval/{id} | 获取审批详情 | 是 |
| GET | /approval/list | 审批列表 | 是 |
| POST | /approval/{id}/approve | 审批通过 | 是 |
| POST | /approval/{id}/reject | 审批驳回 | 是 |
| POST | /approval/{id}/cancel | 取消审批 | 是 |
| GET | /approval/pending | 待审批列表 | 是 |
| POST | /approval/{id}/notify | 发送通知 | 是 |

### 3.4 聊天模块 API

| 方法 | 路径 |说明 | 认证 |
|------|------|------|------|
| POST | /chat/session/create | 创建会话 | 是 |
| GET | /chat/session/list | 会话列表 | 是 |
| DELETE | /chat/session/{id} | 删除会话 | 是 |
| POST | /chat/message/send | 发送消息 | 是 |
| GET | /chat/message/{sessionId} | 获取历史消息 | 是 |

### 3.5 知识库模块 API

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /knowledge/doc/upload | 上传文档 | 是 |
| GET | /knowledge/doc/list | 文档列表 | 是 |
| GET | /knowledge/doc/{id} | 文档详情 | 是 |
| DELETE | /knowledge/doc/{id} | 删除文档 | 是 |
| POST | /knowledge/doc/{id}/vectorize | 重新向量化 | 是 |
| POST | /knowledge/search | 知识检索 | 是 |
| GET | /knowledge/stats | 知识库统计 | 是 |

---

## 四、核心代码结构设计

### 4.1 后端项目结构

```
ai-agent-server/
├── src/main/java/com/easyiot/aiagent/
│   ├── AiAgentApplication.java          # 启动类
│   │
│   ├── config/                           # 配置类
│   │   ├── OllamaConfig.java             # Ollama配置
│   │   ├── RedisConfig.java              # Redis配置
│   │   ├── MilvusConfig.java             # 向量库配置
│   │   ├── CorsConfig.java               # 跨域配置
│   │   └── SecurityConfig.java           # 安全配置
│   │
│   ├── controller/                       # 控制器
│   │   ├── CodeReviewController.java
│   │   ├── ApprovalController.java
│   │   ├── ChatController.java
│   │   └── KnowledgeController.java
│   │
│   ├── service/                          # 业务服务
│   │   ├── agent/                        # Agent核心
│   │   │   ├── AgentDispatcher.java      # Agent调度器
│   │   │   ├── CodeReviewAgent.java
│   │   │   ├── OptimizeAgent.java
│   │   │   ├── ApprovalAgent.java
│   │   │   ├── ChatAgent.java
│   │   │   └── LearnAgent.java
│   │   ├── code/                         # 代码审查服务
│   │   ├── approval/                     # 审批服务
│   │   ├── chat/                         # 聊天服务
│   │   └── knowledge/                    # 知识库服务
│   │
│   ├── model/                            # 数据模型
│   │   ├── entity/                       # 实体类
│   │   ├── dto/                          # 数据传输对象
│   │   ├── vo/                           # 视图对象
│   │   └── enums/                        # 枚举类
│   │
│   ├── mapper/                           # 数据访问层
│   │   ├── CodeReviewMapper.java
│   │   ├── ApprovalMapper.java
│   │   ├── ChatMessageMapper.java
│   │   └── KnowledgeDocMapper.java
│   │
│   ├── common/                           # 公共组件
│   │   ├── result/                       # 统一响应
│   │   ├── exception/                   # 异常处理
│   │   └── utils/                        # 工具类
│   │
│   └── prompt/                           # 提示词工程
│       ├── CodeReviewPrompt.java
│       ├── OptimizePrompt.java
│       ├── ApprovalPrompt.java
│       ├── ChatPrompt.java
│       └── LearnPrompt.java
│
├── src/main/resources/
│   ├── application.yml                   # 应用配置
│   ├── mapper/                           # MyBatis XML
│   └── prompt/                           # 提示词模板
│
└── pom.xml
```

### 4.2 核心类设计

#### 4.2.1 Agent 调度器 (AgentDispatcher)

```java
@Service
@RequiredArgsConstructor
public class AgentDispatcher {
    
    private final CodeReviewAgent codeReviewAgent;
    private final OptimizeAgent optimizeAgent;
    private final ApprovalAgent approvalAgent;
    private final ChatAgent chatAgent;
    private final LearnAgent learnAgent;
    
    /**
     * 分发请求到对应的Agent
     */
    public AgentResponse dispatch(AgentRequest request) {
        // 1. 意图识别
        Intent intent = recognizeIntent(request.getContent());
        
        // 2. 选择Agent
        BaseAgent agent = selectAgent(intent);
        
        // 3. 执行Agent
        return agent.execute(request);
    }
    
    /**
     * 意图识别 - 简单的规则匹配
     */
    private Intent recognizeIntent(String content) {
        if (content.contains("审查") || content.contains("review") 
            || content.contains("代码")) {
            return Intent.CODE_REVIEW;
        }
        if (content.contains("优化") || content.contains("改进")) {
            return Intent.OPTIMIZE;
        }
        if (content.contains("审批") || content.contains("申请")) {
            return Intent.APPROVAL;
        }
        if (content.contains("知识") || content.contains("文档")) {
            return Intent.LEARN;
        }
        return Intent.CHAT;
    }
    
    private BaseAgent selectAgent(Intent intent) {
        return switch (intent) {
            case CODE_REVIEW -> codeReviewAgent;
            case OPTIMIZE -> optimizeAgent;
            case APPROVAL -> approvalAgent;
            case LEARN -> learnAgent;
            case CHAT -> chatAgent;
        };
    }
}
```

#### 4.2.2 代码审查 Agent

```java
@Service
@Slf4j
public class CodeReviewAgent extends BaseAgent {
    
    private final OllamaService ollamaService;
    private final CodeReviewMapper reviewMapper;
    private final CodeReviewPrompt promptTemplate;
    
    @Override
    public AgentResponse execute(AgentRequest request) {
        CodeReviewDTO dto = (CodeReviewDTO) request.getData();
        
        // 1. 构建提示词
        String prompt = promptTemplate.buildReviewPrompt(
            dto.getCodeContent(),
            dto.getLanguage()
        );
        
        // 2. 调用AI
        long startTime = System.currentTimeMillis();
        String aiResponse = ollamaService.chat(prompt, "codellama");
        long duration = System.currentTimeMillis() - startTime;
        
        // 3. 解析结果
        ReviewResult result = parseReviewResult(aiResponse);
        
        // 4. 保存记录
        CodeReview review = new CodeReview();
        review.setUserId(request.getUserId());
        review.setCodeContent(dto.getCodeContent());
        review.setLanguage(dto.getLanguage());
        review.setReviewResult(JSON.toJSONString(result));
        review.setReviewTime((int) duration);
        reviewMapper.insert(review);
        
        return AgentResponse.success(result);
    }
    
    private ReviewResult parseReviewResult(String aiResponse) {
        // 解析AI返回的JSON结果
        // 提取问题列表、严重程度、建议等
    }
}
```

#### 4.2.3 RAG 知识检索 Agent

```java
@Service
@Slf4j
public class LearnAgent extends BaseAgent {
    
    private final EmbeddingService embeddingService;
    private final VectorSearchService vectorSearchService;
    private final OllamaService ollamaService;
    private final KnowledgeDocMapper docMapper;
    
    public AgentResponse execute(AgentRequest request) {
        LearnDTO dto = (LearnDTO) request.getData();
        
        // 1. 将用户问题向量化
        List<Float> queryVector = embeddingService.embed(dto.getQuestion());
        
        // 2. 向量检索
        List<SearchResult> results = vectorSearchService.search(
            queryVector, 
            topK = 5
        );
        
        // 3. 构建上下文
        String context = buildContext(results);
        
        // 4. 构建提示词
        String prompt = promptTemplate.buildRagPrompt(
            dto.getQuestion(),
            context
        );
        
        // 5. 调用AI生成回答
        String answer = ollamaService.chat(prompt, "llama3");
        
        return AgentResponse.success(answer);
    }
}
```

---

## 五、提示词工程详细设计

### 5.1 代码审查提示词

```java
public class CodeReviewPrompt {
    
    /**
     * 代码审查系统提示词
     */
    public static final String SYSTEM_PROMPT = """
你是资深代码审查专家，擅长Java、Spring Boot、Python、前端Vue/React等技术栈。
你的职责是审查代码并给出专业、详细的审查报告。

## 审查维度
1. 语法错误 (Syntax)
2. 安全漏洞 (Security) - SQL注入、XSS、越权等
3. 性能问题 (Performance) - N+1查询、内存泄漏等
4. 代码规范 (Code Style) - 命名、注释、格式
5. 最佳实践 (Best Practices) - 设计模式、异常处理
6. 可读性 (Readability) - 逻辑清晰度

## 输出格式要求
请以JSON格式返回审查结果，格式如下：
{
    "summary": {
        "totalIssues": 5,
        "critical": 1,
        "major": 2,
        "minor": 2,
        "score": 85
    },
    "issues": [
        {
            "line": 15,
            "severity": "critical",
            "category": "security",
            "title": "SQL注入风险",
            "message": "直接拼接SQL参数",
            "suggestion": "使用PreparedStatement或MyBatis参数绑定",
            "code": "SELECT * FROM user WHERE name = '" + name + "'"
        }
    ],
    "recommendations": [
        "建议添加@Transactional注解保证事务一致性",
        "建议添加统一的异常处理"
    ]
}

注意：
- 只返回JSON，不要有其他内容
- 如果代码没有问题，返回空的issues数组
- severity取值：critical(严重)、major(重要)、minor(次要)
""";
    
    /**
     * 构建审查提示词
     */
    public String buildReviewPrompt(String code, String language) {
        return SYSTEM_PROMPT + "\n\n## 待审查代码\n```" + language + "\n" + code + "\n```";
    }
}
```

### 5.2 优化建议提示词

```java
public class OptimizePrompt {
    
    public static final String SYSTEM_PROMPT = """
你是代码优化专家，擅长将低质量代码重构为高质量代码。
基于代码审查结果，你需要进行以下工作：

## 优化任务
1. 提供优化后的代码
2. 解释为什么这样优化
3. 提供替代方案

## 输出格式
{
    "optimizedCode": "优化后的完整代码",
    "explanation": "优化说明",
    "alternatives": [
        {
            "方案": "使用线程池",
            "优点": "提升并发性能",
            "缺点": "复杂度增加"
        }
    ]
}

注意：只返回JSON格式结果。
""";
}
```

### 5.3 RAG 知识问答提示词

```java
public class LearnPrompt {
    
    public static final String SYSTEM_PROMPT = """
你是公司的智能助手，擅长根据提供的知识库内容回答用户问题。

## 回答原则
1. 只基于提供的上下文回答，不要编造信息
2. 如果上下文中没有相关信息，请明确告知用户
3. 回答要准确、简洁、易懂
4. 如果需要，可以引用知识库中的具体内容

## 输出格式
{
    "answer": "回答内容",
    "sources": [
        {
            "docId": 1,
            "title": "Java开发规范",
            "similarity": 0.95
        }
    ]
}

注意：只返回JSON格式结果。
""";
    
    public String buildRagPrompt(String question, String context) {
        return SYSTEM_PROMPT + "\n\n## 知识库内容\n" + context + "\n\n## 用户问题\n" + question;
    }
}
```

---

## 六、部署方案详细设计

### 6.1 Docker Compose 部署

```yaml
# docker-compose.yml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: ai-agent-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123456
      MYSQL_DATABASE: ai_agent
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password
    networks:
      - ai-agent-net

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: ai-agent-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ai-agent-net

  # Milvus 向量数据库
  milvus:
    image: milvusdb/milvus:v2.3.3
    container_name: ai-agent-milvus
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    ports:
      - "19530:19530"
      - "9091:9091"
    volumes:
      - milvus_data:/var/lib/milvus
    networks:
      - ai-agent-net

  # MinIO 对象存储
  minio:
    image: minio/minio:latest
    container_name: ai-agent-minio
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_data:/data
    command: server /data --console-address ":9001"
    networks:
      - ai-agent-net

  # Ollama AI 运行时
  ollama:
    image: ollama/ollama:latest
    container_name: ai-agent-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    networks:
      - ai-agent-net
    deploy:
      resources:
        limits:
          memory: 16G
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]

networks:
  ai-agent-net:
    driver: bridge

volumes:
  mysql_data:
  redis_data:
  milvus_data:
  minio_data:
  ollama_data:
```

### 6.2 部署步骤

#### 步骤1: 准备环境

```bash
# 1. 安装 Docker Desktop (Windows)
# 下载地址: https://www.docker.com/products/docker-desktop/

# 2. 启动 Docker Desktop

# 3. 创建项目目录
mkdir -p E:/easyiot/ai-agent
cd E:/easyiot/ai-agent
```

#### 步骤2: 启动基础服务

```bash
# 启动 MySQL, Redis, Milvus, MinIO
docker-compose up -d mysql redis milvus minio

# 等待服务启动
docker-compose ps
```

#### 步骤3: 启动 Ollama 并下载模型

```bash
# 启动 Ollama
docker-compose up -d ollama

# 进入 Ollama 容器
docker exec -it ai-agent-ollama bash

# 下载模型
ollama pull llama3
ollama pull codellama

# 验证模型
ollama list

# 测试模型
ollama run llama3 "你好"
```

#### 步骤4: 初始化数据库

```bash
# 创建初始化SQL脚本 init.sql
# 执行建表语句
```

#### 步骤5: 部署后端服务

```bash
# 打包 Spring Boot 应用
mvn clean package -DskipTests

# 构建 Docker 镜像
docker build -t ai-agent-server:latest .

# 运行后端服务
docker run -d \
  --name ai-agent-server \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/ai_agent \
  -e SPRING_REDIS_HOST=redis \
  -e OLLAMA_BASE_URL=http://ollama:11434 \
  --network ai-agent-net \
  ai-agent-server:latest
```

### 6.3 资源要求

| 服务 | CPU | 内存 | 磁盘 |
|------|-----|------|------|
| MySQL | 1核 | 2GB | 20GB |
| Redis | 0.5核 | 1GB | 5GB |
| Milvus | 2核 | 4GB | 30GB |
| MinIO | 1核 | 1GB | 50GB |
| Ollama | 4核+ | 16GB+ | 20GB |
| 后端 | 2核 | 4GB | 10GB |

---

## 七、安全设计

### 7.1 认证授权

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.stateless())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/chat/session/create").permitAll()
                .requestMatchers("/api/v1/code-review/webhook/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### 7.2 数据安全

- 密码加密存储 (BCrypt)
- 敏感配置加密存储
- API 限流防攻击
- SQL 注入防护 (MyBatis #{})
- XSS 防护

---

## 八、扩展性设计

### 8.1 Agent 扩展

新增 Agent 只需：
1. 继承 `BaseAgent` 类
2. 实现 `execute` 方法
3. 注册到 `AgentDispatcher`

### 8.2 模型扩展

```java
@Component
public class ModelSelector {
    
    public String selectModel(String agentType, String language) {
        return switch (agentType) {
            case "code_review" -> "codellama";
            case "chat" -> "llama3";
            case "optimize" -> "codellama:7b-instruct";
            default -> "llama3";
        };
    }
}
```

---

## 九、监控与日志

### 9.1 健康检查

```yaml
# Spring Boot Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

### 9.2 日志配置

```xml
<!-- logback-spring.xml -->
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.easyiot.aiagent" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
```

---

## 十、开发计划

| 阶段 | 任务 | 预计时间 |
|------|------|----------|
| Phase 1 | 基础框架搭建 + MySQL/Redis 部署 | 1天 |
| Phase 2 | Ollama 部署 + 模型下载 | 1天 |
| Phase 3 | 代码审查 Agent 开发 | 2天 |
| Phase 4 | 优化建议 Agent 开发 | 1天 |
| Phase 5 | 审批流程开发 | 2天 |
| Phase 6 | 聊天功能开发 | 1天 |
| Phase 7 | 知识库 RAG 开发 | 2天 |
| Phase 8 | 前端界面开发 | 3天 |
| Phase 9 | 集成测试 + 优化 | 2天 |
| **总计** | | **15天** |

---

*文档版本: v1.0*
*最后更新: 2024-01-15*
