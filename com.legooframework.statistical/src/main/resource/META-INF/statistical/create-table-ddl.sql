DROP TABLE IF EXISTS STATISTICAL_DEF_INFO;
CREATE TABLE STATISTICAL_DEF_INFO
(
  id              INT(11)          NOT NULL AUTO_INCREMENT,
  company_id      INT(11) UNSIGNED NOT NULL,
  role_id         INT(11) UNSIGNED NOT NULL DEFAULT 0,
  statistical_id  VARCHAR(128)     NULL     DEFAULT NULL,
  table_field_ids VARCHAR(255)     NULL     DEFAULT NULL,
  echart_id       VARCHAR(128)     NULL     DEFAULT NULL,
  delete_flag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id       BIGINT(20)       NULL     DEFAULT -1,
  creator         BIGINT(20)       NOT NULL DEFAULT -1,
  createTime      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor          BIGINT(20)       NULL     DEFAULT NULL,
  editTime        DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;


DROP TABLE IF EXISTS STATISTICAL_LAYOUT_INFO;
CREATE TABLE STATISTICAL_LAYOUT_INFO
(
  id             INT(11)          NOT NULL AUTO_INCREMENT,
  company_id     INT(11) UNSIGNED NOT NULL,
  role_id        INT(11)          NOT NULL DEFAULT -1,
  title          VARCHAR(128)     NULL,
  layout_type    VARCHAR(128)     NULL     DEFAULT NULL,
  statistical_id VARCHAR(128)     NULL     DEFAULT NULL,
  region_01      VARCHAR(255)     NULL     DEFAULT NULL,
  region_02      VARCHAR(255)     NULL     DEFAULT NULL,
  region_03      VARCHAR(255)     NULL     DEFAULT NULL,
  region_04      VARCHAR(255)     NULL     DEFAULT NULL,
  region_05      VARCHAR(255)     NULL     DEFAULT NULL,
  region_06      VARCHAR(255)     NULL     DEFAULT NULL,
  region_07      VARCHAR(255)     NULL     DEFAULT NULL,
  region_08      VARCHAR(255)     NULL     DEFAULT NULL,
  region_09      VARCHAR(255)     NULL     DEFAULT NULL,
  region_10      VARCHAR(255)     NULL     DEFAULT NULL,
  region_11      VARCHAR(255)     NULL     DEFAULT NULL,
  region_12      VARCHAR(255)     NULL     DEFAULT NULL,
  delete_flag    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id      BIGINT(20)       NULL     DEFAULT -1,
  creator        BIGINT(20)       NOT NULL DEFAULT -1,
  createTime     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor         BIGINT(20)       NULL     DEFAULT NULL,
  editTime       DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;