-- ============================================================
-- 轻量应用商店 AppStore 数据库增量升级脚本
-- 适用版本: 已有 v1.0 数据库 → v1.1（专业优化版）
--           已有 v1.1 数据库 → v1.2（商用加固版）：无表结构变更，仅需代码升级
-- 执行方式: 在 Navicat 或 mysql 命令行中执行本脚本
--   或: mysql -u root -p123456 < database/upgrade.sql
-- 幂等性: 使用 IF NOT EXISTS，可重复执行
-- ============================================================

USE appstore;

-- ------------------------------------------------------------
-- 1. app_info 增加审核状态索引（审核列表查询提速）
-- ------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = 'appstore' AND TABLE_NAME = 'app_info'
                 AND INDEX_NAME = 'idx_audit_status');
SET @sql := IF(@exist = 0, 'ALTER TABLE app_info ADD INDEX idx_audit_status (audit_status)',
               'SELECT ''idx_audit_status 已存在，跳过''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 2. app_version 增加状态索引（版本审核/上架版本查询提速）
-- ------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = 'appstore' AND TABLE_NAME = 'app_version'
                 AND INDEX_NAME = 'idx_status');
SET @sql := IF(@exist = 0, 'ALTER TABLE app_version ADD INDEX idx_status (status)',
               'SELECT ''idx_status 已存在，跳过''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 3. app_comment 增加 app_id+status 联合索引（评论列表查询提速）
-- ------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = 'appstore' AND TABLE_NAME = 'app_comment'
                 AND INDEX_NAME = 'idx_app_status');
SET @sql := IF(@exist = 0, 'ALTER TABLE app_comment ADD INDEX idx_app_status (app_id, status)',
               'SELECT ''idx_app_status 已存在，跳过''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ------------------------------------------------------------
-- 4. app_download 增加 user_id+app_id 联合索引（我的下载去重查询提速）
-- ------------------------------------------------------------
SET @exist := (SELECT COUNT(*) FROM information_schema.STATISTICS
               WHERE TABLE_SCHEMA = 'appstore' AND TABLE_NAME = 'app_download'
                 AND INDEX_NAME = 'idx_user_app');
SET @sql := IF(@exist = 0, 'ALTER TABLE app_download ADD INDEX idx_user_app (user_id, app_id)',
               'SELECT ''idx_user_app 已存在，跳过''');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT '数据库升级完成：已补充审核状态、版本状态、评论、下载联合索引' AS result;

-- ------------------------------------------------------------
-- 5. v1.2（商用加固版）说明
-- ------------------------------------------------------------
-- v1.2 版本无数据库表结构变更，全部为代码层加固与优化：
--   - 应用/版本更新接口字段白名单（防篡改下载量、评分、审核状态）
--   - 公告草稿、未过审版本、未上架应用详情可见性收敛
--   - 评论评分改为原子SQL更新、我的收藏/下载改为SQL层分页
--   - 登录防爆破（Redis）、参数校验、全局异常细化
-- 若需从 v1.0 直接升级到 v1.2，执行上方 1-4 节即可。
SELECT 'v1.2 无需额外数据库变更' AS result;
