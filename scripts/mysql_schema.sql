-- MySQL DDL for letters-in-time project
-- 表：scheduled_email

CREATE TABLE IF NOT EXISTS `scheduled_email` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recipient` VARCHAR(255) NOT NULL COMMENT '收件人邮箱',
  `subject` VARCHAR(255) NOT NULL COMMENT '邮件主题',
  `content` TEXT NOT NULL COMMENT '邮件正文',
  `scheduled_time` DATETIME NOT NULL COMMENT '计划发送时间',
  `sent_time` DATETIME DEFAULT NULL COMMENT '实际发送时间',
  `status` VARCHAR(20) NOT NULL COMMENT '状态：PENDING / SENT / FAILED',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_email_status_time` (`status`, `scheduled_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计划发送邮件表';


