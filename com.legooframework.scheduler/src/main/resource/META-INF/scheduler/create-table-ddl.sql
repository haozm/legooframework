-- 任务触发记录器
DROP TABLE IF EXISTS SCHEDULE_JOB_DETAIL;
CREATE TABLE SCHEDULE_JOB_DETAIL
(
  id              BIGINT(20)       NOT NULL AUTO_INCREMENT,
  job_name        VARCHAR(128)     NOT NULL,
  group_name      VARCHAR(128)     NOT NULL,
  company_id      INT(11)          NOT NULL DEFAULT -1,
  store_id        INT(11)          NOT NULL DEFAULT -1,
  target_beanname VARCHAR(128)     NOT NULL,
  target_method   VARCHAR(128)     NOT NULL,
  trigger_type    TINYINT UNSIGNED NOT NULL DEFAULT 1,
  owner_bundle    VARCHAR(128)     NOT NULL,
  start_delay     INT(11)          NOT NULL DEFAULT 0,
  repeat_interval INT(11)          NOT NULL DEFAULT 0,
  cron_expression VARCHAR(128)     NULL,
  enabled         TINYINT UNSIGNED NOT NULL DEFAULT 1,
  fixed_params    VARCHAR(256)     NULL,
  job_desc        VARCHAR(256)     NULL,
  delete_flag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id       BIGINT(20)       NOT NULL DEFAULT 0,
  creator         BIGINT(20)       NOT NULL DEFAULT -1,
  createTime      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor          BIGINT(20)       NULL     DEFAULT NULL,
  editTime        DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
)
  DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX SCHEDULE_JOB_DETAIL_JOB_NAME_IDX USING BTREE ON SCHEDULE_JOB_DETAIL (job_name, group_name);
-- BIGINT(20)  AUTO_INCREMENT,

