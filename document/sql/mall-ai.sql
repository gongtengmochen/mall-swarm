-- mall-ai 模块数据库初始化脚本
-- 注意：本模块主要使用现有的 pms_product、pms_brand、pms_product_category 表
-- 以下脚本用于创建 AI 会话记录表（可选）

-- 创建 AI 对话会话表
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- 创建 AI 对话消息表
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) NOT NULL COMMENT '会话ID',
  `role` varchar(20) NOT NULL COMMENT '角色：user/assistant/system',
  `content` text COMMENT '消息内容',
  `intent` varchar(50) DEFAULT NULL COMMENT '用户意图',
  `product_ids` varchar(500) DEFAULT NULL COMMENT '推荐商品ID列表，逗号分隔',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- 插入测试数据提示
-- 注意：实际的商品数据来自 pms_product 等现有表，无需额外插入
