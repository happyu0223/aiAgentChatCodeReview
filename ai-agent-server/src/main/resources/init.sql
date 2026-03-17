-- AI Agent System Database Init SQL
-- Database: ai_agentFZ

CREATE DATABASE IF NOT EXISTS ai_agentFZ DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_agentFZ;

-- System User Table
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'User ID',
    username        VARCHAR(50)     NOT NULL UNIQUE COMMENT 'Username',
    password        VARCHAR(200)    NOT NULL COMMENT 'Password (encrypted)',
    nickname        VARCHAR(50)     COMMENT 'Nickname',
    email           VARCHAR(100)    COMMENT 'Email',
    phone           VARCHAR(20)     COMMENT 'Phone',
    avatar          VARCHAR(500)    COMMENT 'Avatar URL',
    role            VARCHAR(20)     DEFAULT 'user' COMMENT 'Role: admin/user/approver',
    status          TINYINT         DEFAULT 1 COMMENT 'Status: 0-disabled 1-normal',
    last_login_time DATETIME        COMMENT 'Last login time',
    created_at      DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT         DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System user table';

-- Code Review Table
CREATE TABLE IF NOT EXISTS code_review (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Review ID',
    user_id             BIGINT          NOT NULL COMMENT 'Submitter ID',
    project_name        VARCHAR(100)    COMMENT 'Project name',
    file_name           VARCHAR(200)    COMMENT 'File name',
    file_path           VARCHAR(500)    COMMENT 'File path',
    code_content        TEXT            NOT NULL COMMENT 'Code content',
    language            VARCHAR(20)     COMMENT 'Language: java/python/vue/javascript',
    review_type         VARCHAR(20)     DEFAULT 'manual' COMMENT 'Review type',
    review_result       JSON            COMMENT 'Review result (JSON)',
    issues_count        INT             DEFAULT 0 COMMENT 'Issue count',
    severity_critical   INT             DEFAULT 0 COMMENT 'Critical issue count',
    severity_major      INT             DEFAULT 0 COMMENT 'Major issue count',
    severity_minor      INT             DEFAULT 0 COMMENT 'Minor issue count',
    status              VARCHAR(20)     DEFAULT 'pending' COMMENT 'Status',
    optimized_code      TEXT            COMMENT 'Optimized code',
    optimize_suggestion TEXT            COMMENT 'Optimization suggestion',
    review_time         INT             COMMENT 'Review time (ms)',
    model_used          VARCHAR(50)     COMMENT 'Model used',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT         DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Code review record table';

-- Approval Flow Table
CREATE TABLE IF NOT EXISTS approval_flow (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Approval ID',
    review_id           BIGINT          COMMENT 'Related review ID',
    flow_no             VARCHAR(50)     NOT NULL UNIQUE COMMENT 'Flow number',
    title               VARCHAR(200)    NOT NULL COMMENT 'Approval title',
    applicant_id        BIGINT          NOT NULL COMMENT 'Applicant ID',
    approver_id         BIGINT          COMMENT 'Approver ID',
    flow_type           VARCHAR(20)     DEFAULT 'code_review' COMMENT 'Flow type',
    content             TEXT            COMMENT 'Approval content',
    status              VARCHAR(20)     DEFAULT 'pending' COMMENT 'Status',
    priority            TINYINT         DEFAULT 1 COMMENT 'Priority: 1-low 2-medium 3-high',
    due_date            DATETIME        COMMENT 'Due date',
    approval_comment    TEXT            COMMENT 'Approval comment',
    notify_channels     VARCHAR(100)    DEFAULT 'webhook' COMMENT 'Notify channels',
    notify_status       VARCHAR(20)     DEFAULT 'not_sent' COMMENT 'Notify status',
    approved_at         DATETIME        COMMENT 'Approved time',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT         DEFAULT 0,
    INDEX idx_flow_no (flow_no),
    INDEX idx_review_id (review_id),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Approval flow table';

-- Knowledge Document Table
CREATE TABLE IF NOT EXISTS knowledge_doc (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Document ID',
    title               VARCHAR(200)    NOT NULL COMMENT 'Document title',
    content             TEXT            NOT NULL COMMENT 'Document content',
    doc_type            VARCHAR(20)     DEFAULT 'general' COMMENT 'Document type',
    file_url            VARCHAR(500)    COMMENT 'Original file URL',
    file_type           VARCHAR(20)     COMMENT 'File type',
    file_size           BIGINT          COMMENT 'File size (bytes)',
    vector_status       VARCHAR(20)     DEFAULT 'pending' COMMENT 'Vectorization status',
    vector_id           VARCHAR(100)    COMMENT 'Vector ID',
    chunks_count        INT             DEFAULT 0 COMMENT 'Chunk count',
    uploader_id          BIGINT          NOT NULL COMMENT 'Uploader ID',
    is_public           TINYINT         DEFAULT 1 COMMENT 'Public: 0-private 1-public',
    tags                VARCHAR(500)    COMMENT 'Tags (comma separated)',
    view_count          INT             DEFAULT 0 COMMENT 'View count',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT         DEFAULT 0,
    INDEX idx_doc_type (doc_type),
    INDEX idx_vector_status (vector_status),
    INDEX idx_uploader_id (uploader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Knowledge document table';

-- Chat Session Table
CREATE TABLE IF NOT EXISTS chat_session (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Session ID',
    user_id             BIGINT          NOT NULL COMMENT 'User ID',
    session_id          VARCHAR(50)     NOT NULL UNIQUE COMMENT 'Session UUID',
    title               VARCHAR(200)    COMMENT 'Session title',
    agent_type          VARCHAR(20)     DEFAULT 'chat' COMMENT 'Agent type',
    context_count       INT             DEFAULT 0 COMMENT 'Context message count',
    last_message_time   DATETIME        COMMENT 'Last message time',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted             TINYINT         DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat session table';

-- Chat Message Table
CREATE TABLE IF NOT EXISTS chat_message (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Message ID',
    user_id             BIGINT          NOT NULL COMMENT 'User ID',
    session_id          VARCHAR(50)     NOT NULL COMMENT 'Session ID',
    role                VARCHAR(20)     NOT NULL COMMENT 'Role: user/assistant/system',
    content             TEXT            NOT NULL COMMENT 'Message content',
    model_used          VARCHAR(50)     COMMENT 'Model used',
    token_count         INT             COMMENT 'Token count',
    metadata            JSON            COMMENT 'Metadata',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    deleted             TINYINT         DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Chat message table';

-- System Config Table
CREATE TABLE IF NOT EXISTS system_config (
    id                  BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'Config ID',
    config_key          VARCHAR(50)     NOT NULL UNIQUE COMMENT 'Config key',
    config_value        TEXT            COMMENT 'Config value',
    config_type         VARCHAR(20)     DEFAULT 'string' COMMENT 'Type: string/json/number/boolean',
    description         VARCHAR(200)    COMMENT 'Description',
    is_encrypted        TINYINT         DEFAULT 0 COMMENT 'Encrypted: 0-no 1-yes',
    is_system           TINYINT         DEFAULT 0 COMMENT 'System config: 0-no 1-yes',
    created_at          DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System config table';

-- Insert default config
INSERT INTO system_config (config_key, config_value, config_type, description, is_system) VALUES
('ollama.base_url', 'http://localhost:11434', 'string', 'Ollama service address', 1),
('ollama.default_model', 'llama3', 'string', 'Default model', 1),
('ollama.code_model', 'codellama', 'string', 'Code review model', 1),
('vector.db_type', 'milvus', 'string', 'Vector database type', 1),
('vector.dimension', '1024', 'number', 'Vector dimension', 1),
('approval.default_approver', '', 'string', 'Default approver ID', 1),
('approval.notify_enabled', 'true', 'boolean', 'Enable approval notification', 1),
('system.max_code_length', '50000', 'number', 'Max code length', 1);

-- Insert default admin user (password: admin123)
INSERT INTO sys_user (username, password, nickname, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'Administrator', 'admin', 1);
