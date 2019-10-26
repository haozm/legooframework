-- 公司任务开关
DROP TABLE IF EXISTS TASK_JOB_SWITCH;
CREATE TABLE TASK_JOB_SWITCH
(
  id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
  company_id    INT(11)          NOT NULL,
  store_id      INT(11)          NOT NULL DEFAULT -1,
  business_type VARCHAR(64)      NOT NULL,
  start_date    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  enbaled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NULL     DEFAULT -1,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX TASK_JOB_SWITCH_COMPANY_ID_IDX USING BTREE ON TASK_JOB_SWITCH (company_id, store_id, business_type);

-- 任务规则表
DROP TABLE IF EXISTS TASK_JOB_RULE;
CREATE TABLE TASK_JOB_RULE
(
  id                   VARCHAR(64)      NOT NULL,
  company_id           INT(11)          NOT NULL,
  store_id             INT(11)          NOT NULL DEFAULT -1,
  business_type        VARCHAR(64)      NOT NULL,
  categories           VARCHAR(64)      NOT NULL DEFAULT '0',
  enabled              TINYINT UNSIGNED NOT NULL DEFAULT 1,
  merge_builder_spec   VARCHAR(1024)    NULL,
  rule_builder_spec    VARCHAR(2048)    NULL,
  autorun_builder_spec VARCHAR(2048)    NULL,
  uuid                 CHAR(36)         NOT NULL,
  delete_flag          TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id            BIGINT(20)       NULL     DEFAULT -1,
  creator              BIGINT(20)       NOT NULL DEFAULT -1,
  createTime           DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor               BIGINT(20)       NULL     DEFAULT NULL,
  editTime             DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, uuid, delete_flag)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 任务执行实例表 TASK_JOB_INSTANCE
DROP TABLE IF EXISTS TASK_JOB_INSTANCE;
CREATE TABLE TASK_JOB_INSTANCE
(
  id             INT(18)          NOT NULL,
  rule_id        VARCHAR(64)      NOT NULL,
  business_type  VARCHAR(64)      NOT NULL,
  categories     VARCHAR(64)      NOT NULL DEFAULT '0',
  cross_store    INT(1)           NOT NULL DEFAULT 0,
  enabled        TINYINT UNSIGNED NOT NULL DEFAULT 1,
  member_id      INT(11)          NOT NULL,
  service_userid INT(11)          NULL,
  store_id       INT(11)          NOT NULL,
  company_id     INT(11)          NOT NULL,
  source_id      INT(11)          NOT NULL,
  merge_info     VARCHAR(2048)    NOT NULL,
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

CREATE INDEX TASK_JOB_INSTANCE_MIXED_IDX USING BTREE ON TASK_JOB_INSTANCE (company_id, store_id, member_id);

-- 任务明细 TASK_JOB_DETAIL
DROP TABLE IF EXISTS TASK_JOB_DETAIL;
CREATE TABLE TASK_JOB_DETAIL
(
  id            INT(18)          NOT NULL,
  task_id       INT(18)          NOT NULL,
  rule_id       VARCHAR(64)      NOT NULL,
  sub_rule_id   VARCHAR(64)      NOT NULL,
  business_type VARCHAR(64)      NOT NULL,
  categories    VARCHAR(64)      NOT NULL DEFAULT '0',
  member_id     INT(11)          NOT NULL,
  store_id      INT(11)          NOT NULL,
  task_status   INT(2)           NOT NULL DEFAULT 1,
  step_index    VARCHAR(36)      NOT NULL,
  auto_run      TINYINT UNSIGNED NOT NULL DEFAULT 0,
  auto_run_time TIME             NULL,
  start_date    DATETIME         NOT NULL,
  expired_date  DATETIME         NOT NULL,
  finished_date DATETIME         NULL,
  remarks       VARCHAR(512)     NULL,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NOT NULL DEFAULT -1,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

ALTER TABLE TASK_JOB_DETAIL
  ADD CONSTRAINT TASK_JOB_DETAIL_TASK_JOB_INSTANCE_FK
    FOREIGN KEY (task_id) REFERENCES TASK_JOB_INSTANCE (id);

-- 具体执行完成执行任务明细
DROP TABLE IF EXISTS TASK_JOB_DETAIL_EXECUTION;
CREATE TABLE TASK_JOB_DETAIL_EXECUTION
(
  id           BIGINT(20)       NOT NULL AUTO_INCREMENT,
  task_id      INT(18)          NOT NULL,
  detail_id    INT(11)          NOT NULL,
  store_id     INT(11)          NOT NULL,
  exec_channel INT(11)          NOT NULL,
  content      VARCHAR(512)     NULL,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NOT NULL DEFAULT -1,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 90服务模板分类
DROP TABLE IF EXISTS TASK_TOUCH90_TEMPLATE;
CREATE TABLE TASK_TOUCH90_TEMPLATE
(
  id            VARCHAR(64)      NOT NULL,
  company_id    INT(11)          NOT NULL,
  categories    VARCHAR(32)      NOT NULL,
  template_id   VARCHAR(32)      NOT NULL,
  template_name VARCHAR(512)     NOT NULL,
  store_ids     VARCHAR(512)     NOT NULL,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NOT NULL DEFAULT -1,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, categories, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 90服务日志
DROP TABLE IF EXISTS TASK_TOUCH90_LOG;
CREATE TABLE TASK_TOUCH90_LOG
(
  company_id  INT(11)           NOT NULL,
  store_id    INT(11)           NOT NULL,
  categories  VARCHAR(64)       NOT NULL DEFAULT '0',
  log_date_pk CHAR(10)          NOT NULL,
  log_date    DATE              NOT NULL,
  add_list    VARCHAR(2000),
  add_size    SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  update_list VARCHAR(2000),
  update_size SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  delete_flag TINYINT UNSIGNED  NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)        NOT NULL DEFAULT 1000000,
  creator     BIGINT(20)        NOT NULL DEFAULT -1,
  createTime  DATETIME          NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)        NULL     DEFAULT NULL,
  editTime    DATETIME          NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id, store_id, categories, log_date_pk)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
