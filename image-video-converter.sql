-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS `image-video-converter`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 2. 切换到目标数据库
USE `image-video-converter`;

-- 3. 创建user表
CREATE TABLE IF NOT EXISTS `user` (
  `id` INT UNSIGNED AUTO_INCREMENT COMMENT '用户ID（自增主键）',
  `account_id` VARCHAR(50) NOT NULL COMMENT '用户账号（唯一标识）',
  `username` VARCHAR(50) NOT NULL COMMENT '用户昵称',
  `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  `avatar` VARCHAR(255) NULL COMMENT '头像路径（可为空）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_id` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 4. user表插入3条数据（确保account唯一，非空字段完整）
INSERT INTO `user` (`account_id`, `username`, `password`)
VALUES ('yixi', '忆昔', '666');

INSERT INTO `user` (`account_id`, `username`, `password`, `avatar`)
VALUES ('abc', '阿丙', '123', '/avatars/abc.jpg');

INSERT INTO `user` (`account_id`, `username`, `password`)
VALUES ('def', '德发', '456');