-- 行业所属表
DROP TABLE IF EXISTS META_TABLES_INFO;
CREATE TABLE META_TABLES_INFO
(
  id          BIGINT(20)       NOT NULL AUTO_INCREMENT,

  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NULL     DEFAULT NULL,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 元数据字段明细信息表
DROP TABLE IF EXISTS META_FIELDS_INFO;
CREATE TABLE META_FIELDS_INFO
(
  id              BIGINT(20)       NOT NULL AUTO_INCREMENT,
  table_id        BIGINT(20)       NOT NULL,
  field_name      VARCHAR(64)      NOT NULL,
  field_cname     VARCHAR(128)     NOT NULL,
  filed_type      VARCHAR(64)      NOT NULL,
  value_range     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  value_space     VARCHAR(128)     NULL,
  is_requied      TINYINT UNSIGNED NOT NULL DEFAULT 0,
  verify_rule     VARCHAR(128)     NULL,
  html_attributes VARCHAR(512)     NULL,
  delete_flag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id       BIGINT(20)       NULL     DEFAULT NULL,
  creator         BIGINT(20)       NOT NULL DEFAULT -1,
  createTime      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor          BIGINT(20)       NULL     DEFAULT NULL,
  editTime        DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

