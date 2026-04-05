-- 创建数据库
CREATE DATABASE IF NOT EXISTS `user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `problem` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS `record` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表（根据实际表结构调整）
USE `user`;
CREATE TABLE IF NOT EXISTS `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) COMMENT '邮箱',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息表';

-- 题目表（根据实际表结构调整）
USE `problem`;
CREATE TABLE IF NOT EXISTS `problem` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `title` VARCHAR(200) NOT NULL COMMENT '题目标题',
    `description` TEXT COMMENT '题目描述',
    `input_description` TEXT COMMENT '输入描述',
    `output_description` TEXT COMMENT '输出描述',
    `time_limit` INT DEFAULT 1000 COMMENT '时间限制(ms)',
    `memory_limit` INT DEFAULT 65536 COMMENT '内存限制(KB)',
    `difficulty` VARCHAR(20) COMMENT '难度',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

CREATE TABLE IF NOT EXISTS `test_case` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '测试用例ID',
    `problem_id` BIGINT NOT NULL COMMENT '题目ID',
    `input_path` VARCHAR(255) COMMENT '输入文件路径',
    `output_path` VARCHAR(255) COMMENT '输出文件路径',
    `score` INT DEFAULT 10 COMMENT '分值',
    `order_num` INT COMMENT '顺序',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_problem_id` (`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用例表';

-- 提交记录表（根据实际表结构调整）
USE `record`;
CREATE TABLE IF NOT EXISTS `submission_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '提交ID',
    `problem_id` BIGINT NOT NULL COMMENT '题目ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `language` VARCHAR(20) COMMENT '编程语言',
    `code` TEXT COMMENT '代码',
    `status` VARCHAR(20) COMMENT '状态',
    `score` INT DEFAULT 0 COMMENT '得分',
    `time_used` INT COMMENT '耗时(ms)',
    `memory_used` INT COMMENT '内存(KB)',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `judge_time` DATETIME COMMENT '判题时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_problem_id` (`problem_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提交记录表';
