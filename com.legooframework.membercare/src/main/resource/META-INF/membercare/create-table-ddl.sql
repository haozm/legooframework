-- 公司任务开关
DROP TABLE IF EXISTS TASK_CASE_SWITCH;
CREATE TABLE TASK_CASE_SWITCH
(
  id          BIGINT(20)       NOT NULL AUTO_INCREMENT,
  company_id  INT(11)          NOT NULL,
  store_id    INT(11)          NOT NULL DEFAULT -1,
  taskType    INT(2)           NOT NULL,
  enbaled     INT(1)           NOT NULL DEFAULT 1,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NULL     DEFAULT 100000,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 任务规则表
DROP TABLE IF EXISTS TASK_CASE_RULE;
CREATE TABLE TASK_CASE_RULE
(
  company_id  INT(11)          NOT NULL,
  store_id    INT(11)          NOT NULL DEFAULT -1,
  task_type   INT(2)           NOT NULL,
  enbaled     INT(1)           NOT NULL DEFAULT 1,
  automatic   INT(1)           NOT NULL DEFAULT 1,
  content     VARCHAR(2500)    NOT NULL,
  details     VARCHAR(2500)    NULL,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NULL     DEFAULT NULL,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id, store_id, task_type)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 任务执行跟踪表
DROP TABLE IF EXISTS TASK_RUN_MONITOR;
CREATE TABLE TASK_RUN_MONITOR
(
  id             VARCHAR(36)      NOT NULL,
  task_type      INT(2)           NOT NULL,
  task_status    INT(2)           NOT NULL DEFAULT 1,
  automatic      INT(1)           NOT NULL DEFAULT 0,
  cross_store    INT(1)           NOT NULL DEFAULT 0,
  member_id      INT(11)          NOT NULL,
  service_userid INT(11)          NULL,
  store_id       INT(11)          NOT NULL,
  company_id     INT(11)          NOT NULL,
  source_id      INT(11)          NOT NULL,
  merge_info     VARCHAR(2500)    NOT NULL,
  delete_flag    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id      BIGINT(20)       NULL     DEFAULT NULL,
  creator        BIGINT(20)       NOT NULL DEFAULT -1,
  createTime     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor         BIGINT(20)       NULL     DEFAULT NULL,
  editTime       DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- CREATE INDEX task_member_list_mixed_IDX USING BTREE ON acp.task_member_list (member_id,store_id,company_id);

-- 任务明细
DROP TABLE IF EXISTS TASK_RUN_MONITOR_DETAIL;
CREATE TABLE TASK_RUN_MONITOR_DETAIL
(
  id            VARCHAR(36)      NOT NULL,
  task_id       VARCHAR(36)      NOT NULL,
  task_status   INT(2)           NOT NULL DEFAULT 1,
  start_date    DATETIME         NOT NULL,
  expired_date  DATETIME         NOT NULL,
  finished_date DATETIME         NULL,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NOT NULL DEFAULT 100000,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- ALTER TABLE acp.TASK_RUN_MONITOR_DETAIL ADD CONSTRAINT TASK_RUN_MONITOR_DETAIL_TASK_RUN_MONITOR_FK FOREIGN KEY (task_id) REFERENCES acp.TASK_RUN_MONITOR(id);

-- 90服务日志
DROP TABLE IF EXISTS TASK_TOUCH90_LOG;
CREATE TABLE TASK_TOUCH90_LOG
(
  company_id  INT(11)           NOT NULL,
  store_id    INT(11)           NOT NULL,
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
  PRIMARY KEY (company_id, store_id, log_date)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
