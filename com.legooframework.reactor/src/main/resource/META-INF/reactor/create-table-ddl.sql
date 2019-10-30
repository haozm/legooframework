-- 临时模板记录
DROP TABLE IF EXISTS REACTOR_LOG_RECORD;
CREATE TABLE REACTOR_LOG_RECORD
(
    id           BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id   INT(11)          NOT NULL ,
    org_id       INT(11)          NOT NULL DEFAULT 0,
    store_id     INT(11)          NOT NULL DEFAULT 0,
    source_id    VARCHAR(64)      NOT NULL ,
    source_table VARCHAR(64)      NOT NULL ,
    template_id  BIGINT(20)       NULL DEFAULT -1,
    error_code   VARCHAR(64)      NOT NULL DEFAULT '0000',
    before_ctx   VARCHAR(512)     NULL DEFAULT NULL,
    after_ctx    VARCHAR(512)     NULL DEFAULT NULL,
    error_msg    VARCHAR(512)     NULL DEFAULT null,
    delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id    BIGINT(20)       NULL     DEFAULT -1,
    creator      BIGINT(20)       NOT NULL DEFAULT -1,
    createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor       BIGINT(20)       NULL     DEFAULT NULL,
    editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 记录开关
DROP TABLE IF EXISTS REACTOR_RECORD_SWITCH;
CREATE TABLE REACTOR_RECORD_SWITCH
(
    id           BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id   INT(11)          NOT NULL,
    enabled      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    switch_type  VARCHAR(32)      NOT NULL,
    allow_store_ids   VARCHAR(512)     NULL DEFAULT NULL,
    forbid_store_ids  VARCHAR(512)     NULL DEFAULT NULL,
    delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id    BIGINT(20)       NULL     DEFAULT -1,
    creator      BIGINT(20)       NOT NULL DEFAULT -1,
    createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor       BIGINT(20)       NULL     DEFAULT NULL,
    editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;