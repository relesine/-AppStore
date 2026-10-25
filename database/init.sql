-- ============================================================
-- 轻量应用商店系统 AppStore 数据库初始化脚本
-- 数据库版本: MySQL 8.0+
-- 字符集: utf8mb4
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS appstore DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE appstore;

-- ============================================================
-- 1. 用户表 sys_user
-- ============================================================
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    user_type TINYINT DEFAULT 1 COMMENT '用户类型：1-普通用户 2-作者 3-管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    -- username 已由 UNIQUE 约束自动建索引，无需重复建立 idx_username（v1.2 优化）
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================================
-- 2. 角色表 sys_role
-- ============================================================
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ============================================================
-- 3. 用户角色关联表 sys_user_role
-- ============================================================
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ============================================================
-- 4. 权限表 sys_permission
-- ============================================================
DROP TABLE IF EXISTS sys_permission;
CREATE TABLE sys_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_type TINYINT DEFAULT 1 COMMENT '类型：1-菜单 2-按钮',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ============================================================
-- 5. 角色权限关联表 sys_role_permission
-- ============================================================
DROP TABLE IF EXISTS sys_role_permission;
CREATE TABLE sys_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ============================================================
-- 6. 应用分类表 app_category
-- ============================================================
DROP TABLE IF EXISTS app_category;
CREATE TABLE app_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID（0为一级分类）',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用分类表';

-- ============================================================
-- 7. 应用信息表 app_info
-- ============================================================
DROP TABLE IF EXISTS app_info;
CREATE TABLE app_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '应用ID',
    app_name VARCHAR(100) NOT NULL COMMENT '应用名称',
    app_icon VARCHAR(255) DEFAULT NULL COMMENT '应用图标URL',
    category_id BIGINT NOT NULL COMMENT '一级分类ID',
    sub_category_id BIGINT DEFAULT NULL COMMENT '二级分类ID',
    author_id BIGINT NOT NULL COMMENT '作者用户ID',
    description TEXT COMMENT '应用描述',
    short_desc VARCHAR(200) DEFAULT NULL COMMENT '简短描述',
    version VARCHAR(20) DEFAULT NULL COMMENT '当前版本号',
    size BIGINT DEFAULT 0 COMMENT '应用大小（字节）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    rating DECIMAL(2,1) DEFAULT 0.0 COMMENT '评分（0-5）',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝 3-已下架',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核 1-审核中 2-已通过 3-已拒绝',
    screenshots TEXT COMMENT '截图URL列表（JSON数组）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category_id (category_id),
    INDEX idx_author_id (author_id),
    INDEX idx_status (status),
    INDEX idx_audit_status (audit_status),
    INDEX idx_download_count (download_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用信息表';

-- ============================================================
-- 8. 应用版本表 app_version
-- ============================================================
DROP TABLE IF EXISTS app_version;
CREATE TABLE app_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    version_no VARCHAR(20) NOT NULL COMMENT '版本号',
    version_code INT DEFAULT 1 COMMENT '版本内部编号（递增）',
    file_url VARCHAR(255) NOT NULL COMMENT '安装包文件URL',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小（字节）',
    file_md5 VARCHAR(32) DEFAULT NULL COMMENT '文件MD5校验值',
    update_log TEXT COMMENT '更新日志',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核 1-已通过 2-已拒绝',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_app_id (app_id),
    INDEX idx_version_no (version_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用版本表';

-- ============================================================
-- 9. 评论表 app_comment
-- ============================================================
DROP TABLE IF EXISTS app_comment;
CREATE TABLE app_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    content VARCHAR(500) NOT NULL COMMENT '评论内容',
    rating TINYINT DEFAULT 5 COMMENT '评分（1-5星）',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID（0为顶级评论）',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_app_id (app_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_app_status (app_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================================
-- 10. 收藏表 app_favorite
-- ============================================================
DROP TABLE IF EXISTS app_favorite;
CREATE TABLE app_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    UNIQUE KEY uk_user_app (user_id, app_id)
    -- idx_user_id 由 uk_user_app 联合索引最左前缀覆盖，无需重复建立（v1.2 优化）
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================================
-- 11. 下载记录表 app_download
-- ============================================================
DROP TABLE IF EXISTS app_download;
CREATE TABLE app_download (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    version_id BIGINT DEFAULT NULL COMMENT '版本ID',
    download_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
    ip VARCHAR(50) DEFAULT NULL COMMENT '下载IP',
    -- idx_user_id 由 idx_user_app 联合索引最左前缀覆盖，无需重复建立（v1.2 优化）
    INDEX idx_app_id (app_id),
    INDEX idx_download_time (download_time),
    INDEX idx_user_app (user_id, app_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载记录表';

-- ============================================================
-- 12. 审核记录表 app_audit
-- ============================================================
DROP TABLE IF EXISTS app_audit;
CREATE TABLE app_audit (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核ID',
    app_id BIGINT NOT NULL COMMENT '应用ID',
    version_id BIGINT DEFAULT NULL COMMENT '版本ID（可为空，应用上架审核）',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核员ID',
    audit_type TINYINT DEFAULT 1 COMMENT '审核类型：1-应用上架 2-版本更新',
    audit_result TINYINT DEFAULT 0 COMMENT '审核结果：0-待审核 1-通过 2-拒绝',
    audit_remark VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_app_id (app_id),
    INDEX idx_auditor_id (auditor_id),
    INDEX idx_audit_result (audit_result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';

-- ============================================================
-- 13. 公告表 sys_notice
-- ============================================================
DROP TABLE IF EXISTS sys_notice;
CREATE TABLE sys_notice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公告ID',
    title VARCHAR(100) NOT NULL COMMENT '公告标题',
    content TEXT COMMENT '公告内容',
    notice_type TINYINT DEFAULT 1 COMMENT '类型：1-系统公告 2-更新公告 3-活动公告',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿 1-已发布',
    publisher_id BIGINT DEFAULT NULL COMMENT '发布人ID',
    publish_time DATETIME DEFAULT NULL COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_status (status),
    INDEX idx_notice_type (notice_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- ============================================================
-- 初始数据：角色
-- ============================================================
INSERT INTO sys_role (role_name, role_code, description) VALUES
('普通用户', 'USER', '应用下载普通用户'),
('作者', 'AUTHOR', '应用上传开发者'),
('管理员', 'ADMIN', '系统管理员');

-- ============================================================
-- 初始数据：用户（密码均为 123456，BCrypt加密）
-- BCrypt of "123456": $2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.
-- ============================================================
INSERT INTO sys_user (username, password, nickname, email, phone, user_type, status) VALUES
('user1', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '张三', 'zhangsan@example.com', '13800138001', 1, 1),
('user2', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '李四', 'lisi@example.com', '13800138002', 1, 1),
('author1', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '王开发者', 'wangdev@example.com', '13900139001', 2, 1),
('author2', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '李工作室', 'listudio@example.com', '13900139002', 2, 1),
('author3', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '赵科技', 'zhaotech@example.com', '13900139003', 2, 1),
('admin', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '超级管理员', 'admin@example.com', '13700137000', 3, 1),
('admin2', '$2a$10$BG6eVxVTQQ3PHiV0hqYuSuxOJpli8ZosaVlQtUu.5FTsYkoIhdIf.', '审核员小李', 'admin2@example.com', '13700137002', 3, 1);

-- ============================================================
-- 初始数据：用户角色关联
-- ============================================================
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1), (2, 1),
(3, 2), (4, 2), (5, 2),
(6, 3), (7, 3);

-- ============================================================
-- 初始数据：应用分类（4大一级分类，每个2个二级分类）
-- ============================================================
INSERT INTO app_category (parent_id, category_name, sort_order) VALUES
-- 一级分类
(0, '系统工具', 1),
(0, '实用工具', 2),
(0, '游戏娱乐', 3),
(0, '办公学习', 4),
-- 系统工具下的二级分类
(1, '系统优化', 1),
(1, '安全防护', 2),
-- 实用工具下的二级分类
(2, '文件管理', 1),
(2, '图像处理', 2),
-- 游戏娱乐下的二级分类
(3, '休闲益智', 1),
(3, '动作冒险', 2),
-- 办公学习下的二级分类
(4, '文档编辑', 1),
(4, '在线学习', 2);

-- ============================================================
-- 初始数据：10款示例应用
-- ============================================================
INSERT INTO app_info (app_name, category_id, sub_category_id, author_id, description, short_desc, version, size, download_count, rating, rating_count, status, audit_status) VALUES
('极速清理大师', 1, 5, 3, '一款专业的手机清理加速软件，深度清理垃圾文件，释放存储空间，提升手机运行速度。支持一键加速、CPU降温、电池管理等功能。', '专业手机清理加速工具', '2.5.0', 25600000, 15234, 4.5, 328, 1, 2),
('安全卫士Pro', 1, 6, 3, '全方位手机安全防护软件，提供病毒查杀、骚扰拦截、隐私保护、应用锁等功能，守护您的手机安全。', '全方位手机安全防护', '3.1.2', 45800000, 28910, 4.3, 567, 1, 2),
('文件管理器', 2, 7, 4, '强大的文件管理工具，支持分类浏览、压缩解压、文件传输、云盘同步等功能，让文件管理更轻松。', '强大的文件管理工具', '1.8.5', 15300000, 12450, 4.6, 234, 1, 2),
('图片编辑器', 2, 8, 4, '专业的图片编辑应用，提供滤镜、调色、裁剪、贴纸、文字等丰富编辑功能，轻松制作精美图片。', '专业图片编辑工具', '4.2.0', 38700000, 19876, 4.4, 445, 1, 2),
('消消乐翻天', 3, 9, 5, '经典三消休闲游戏，丰富的关卡设计，炫酷的消除特效，随时随地来一局，轻松解压好心情。', '经典三消休闲游戏', '5.0.1', 89200000, 45678, 4.7, 1234, 1, 2),
('勇者大冒险', 3, 10, 5, '横版动作冒险游戏，扮演勇者探索神秘大陆，击败怪物收集宝藏，丰富的技能系统和装备系统等你体验。', '横版动作冒险游戏', '1.2.3', 156000000, 32100, 4.2, 876, 1, 2),
('文档助手', 4, 11, 3, '全能文档编辑工具，支持Word、Excel、PPT等多种格式，提供模板库、云同步、PDF转换等功能。', '全能文档编辑工具', '2.0.8', 52400000, 18765, 4.5, 389, 1, 2),
('学习通', 4, 12, 4, '在线学习平台，汇集海量优质课程，涵盖编程、设计、语言、考证等领域，支持离线下载和学习进度追踪。', '在线学习平台', '3.5.0', 67800000, 25430, 4.6, 678, 1, 2),
('笔记达人', 4, 11, 5, '简洁高效的笔记应用，支持富文本编辑、Markdown、手写笔记、待办清单、多端同步，让记录更自由。', '简洁高效的笔记应用', '1.5.2', 20100000, 9876, 4.8, 156, 1, 2),
('计算器Pro', 2, 7, 3, '功能强大的计算器应用，支持科学计算、单位换算、汇率转换、房贷计算等多种实用计算功能。', '功能强大的计算器', '2.1.0', 8500000, 34567, 4.4, 512, 1, 2);

-- ============================================================
-- 初始数据：应用版本
-- ============================================================
INSERT INTO app_version (app_id, version_no, version_code, file_url, file_size, update_log, status, publish_time) VALUES
(1, '2.5.0', 25, '/uploads/apps/1/cleaner_2.5.0.apk', 25600000, '优化清理引擎，提升扫描速度；修复已知bug', 1, '2026-08-15 10:00:00'),
(2, '3.1.2', 31, '/uploads/apps/2/security_3.1.2.apk', 45800000, '更新病毒库；新增隐私相册功能', 1, '2026-08-20 14:30:00'),
(3, '1.8.5', 18, '/uploads/apps/3/filemanager_1.8.5.apk', 15300000, '支持更多压缩格式；优化文件搜索', 1, '2026-08-10 09:00:00'),
(4, '4.2.0', 42, '/uploads/apps/4/photoeditor_4.2.0.apk', 38700000, '新增AI抠图功能；优化滤镜效果', 1, '2026-08-25 16:00:00'),
(5, '5.0.1', 50, '/uploads/apps/5/match3_5.0.1.apk', 89200000, '新增50个关卡；优化游戏性能', 1, '2026-09-01 11:00:00'),
(6, '1.2.3', 12, '/uploads/apps/6/adventure_1.2.3.apk', 156000000, '新增第二章剧情；修复闪退问题', 1, '2026-08-28 15:00:00'),
(7, '2.0.8', 20, '/uploads/apps/7/docassistant_2.0.8.apk', 52400000, '优化文档渲染；新增模板', 1, '2026-08-18 10:30:00'),
(8, '3.5.0', 35, '/uploads/apps/8/learning_3.5.0.apk', 67800000, '新增直播课程；优化播放体验', 1, '2026-09-02 08:00:00'),
(9, '1.5.2', 15, '/uploads/apps/9/notes_1.5.2.apk', 20100000, '支持手写笔；优化同步速度', 1, '2026-08-22 13:00:00'),
(10, '2.1.0', 21, '/uploads/apps/10/calculator_2.1.0.apk', 8500000, '新增汇率实时更新；优化界面', 1, '2026-08-12 17:00:00');

-- ============================================================
-- 初始数据：评论
-- ============================================================
INSERT INTO app_comment (app_id, user_id, content, rating, like_count) VALUES
(1, 1, '清理效果很好，手机明显变快了！', 5, 23),
(1, 2, '界面简洁，操作方便，推荐使用。', 4, 15),
(2, 1, '防护功能全面，用着很安心。', 5, 34),
(3, 2, '文件管理功能很强大，压缩解压都支持。', 5, 12),
(4, 1, '滤镜效果不错，AI抠图很实用。', 4, 28),
(5, 2, '关卡设计很有趣，停不下来！', 5, 56),
(5, 1, '后期关卡有点难，不过整体很好玩。', 4, 18),
(6, 2, '打击感不错，剧情也很吸引人。', 4, 22),
(7, 1, '办公必备，文档编辑很方便。', 5, 19),
(8, 2, '课程质量很高，学到了很多东西。', 5, 31),
(9, 1, '笔记功能简洁好用，同步也很快。', 5, 14),
(10, 2, '计算功能丰富，日常使用足够了。', 4, 9);

-- ============================================================
-- 初始数据：公告
-- ============================================================
INSERT INTO sys_notice (title, content, notice_type, status, publisher_id, publish_time) VALUES
('轻量应用商店正式上线', '欢迎使用轻量应用商店！我们为您精选了各类优质应用，涵盖系统工具、实用工具、游戏娱乐、办公学习四大分类。如有任何问题或建议，欢迎联系我们。', 1, 1, 6, '2026-08-01 09:00:00'),
('9月应用更新公告', '本月共有10款应用完成版本更新，包括极速清理大师、安全卫士Pro等热门应用。各位用户可在应用详情页查看更新日志并下载最新版本。', 2, 1, 6, '2026-09-03 10:00:00'),
('开发者入驻指南', '欢迎广大开发者入驻轻量应用商店！开发者注册后即可上传应用，我们将在1-3个工作日内完成审核。详情请查看开发者文档。', 1, 1, 6, '2026-08-15 14:00:00');

-- ============================================================
-- 初始数据：下载记录（模拟数据）
-- ============================================================
INSERT INTO app_download (user_id, app_id, version_id, ip) VALUES
(1, 1, 1, '192.168.1.10'),
(1, 5, 5, '192.168.1.10'),
(1, 8, 8, '192.168.1.10'),
(2, 2, 2, '192.168.1.11'),
(2, 4, 4, '192.168.1.11'),
(2, 7, 7, '192.168.1.11');

-- ============================================================
-- 初始数据：收藏
-- ============================================================
INSERT INTO app_favorite (user_id, app_id) VALUES
(1, 1), (1, 5), (1, 9),
(2, 2), (2, 8);

-- ============================================================
-- 初始数据：审核记录
-- ============================================================
INSERT INTO app_audit (app_id, version_id, auditor_id, audit_type, audit_result, audit_remark, audit_time) VALUES
(1, 1, 6, 1, 1, '应用信息完整，符合上架要求', '2026-08-15 11:00:00'),
(2, 2, 6, 1, 1, '审核通过', '2026-08-20 15:00:00'),
(3, 3, 7, 1, 1, '审核通过', '2026-08-10 10:00:00'),
(4, 4, 7, 1, 1, '审核通过', '2026-08-25 17:00:00'),
(5, 5, 6, 1, 1, '审核通过', '2026-09-01 12:00:00'),
(6, 6, 6, 1, 1, '审核通过', '2026-08-28 16:00:00'),
(7, 7, 7, 1, 1, '审核通过', '2026-08-18 11:00:00'),
(8, 8, 7, 1, 1, '审核通过', '2026-09-02 09:00:00'),
(9, 9, 6, 1, 1, '审核通过', '2026-08-22 14:00:00'),
(10, 10, 6, 1, 1, '审核通过', '2026-08-12 18:00:00');

SELECT 'Database initialization completed!' AS result;
