-- 事件源
DROP TABLE IF EXISTS TASK_SOURCE_LOG;
CREATE TABLE TASK_SOURCE_LOG
(
    id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id    INT(11)          NOT NULL ,
    org_id        INT(11)          NOT NULL DEFAULT 0,
    store_id      INT(11)          NOT NULL DEFAULT 0,
    employee_id   INT(11)          NOT NULL DEFAULT 0,
    member_id     INT(11)          NOT NULL DEFAULT 0,
    weixin_id     VARCHAR(128)     NULL DEFAULT NULL,
    business_type TINYINT UNSIGNED NOT NULL,
    openid        VARCHAR(128)     NULL DEFAULT NULL,
    status        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    payload_type  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    generation_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    message       VARCHAR(512)     NULL DEFAULT NULL,
    payload       TEXT             NULL DEFAULT NULL,
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


DROP TABLE IF EXISTS TASK_RULE_INFO;
CREATE TABLE TASK_RULE_INFO
(
    id            INT(11)          NOT NULL AUTO_INCREMENT,
    company_id    INT(11)          NOT NULL ,
    org_id        INT(11)          NOT NULL DEFAULT 0,
    store_id      INT(11)          NOT NULL DEFAULT 0,
    business_type TINYINT UNSIGNED NOT NULL,
    delay_type    TINYINT UNSIGNED NOT NULL,
    delay_time    VARCHAR(128)     NULL DEFAULT NULL,
    send_channel  TINYINT UNSIGNED NOT NULL,
    send_target   VARCHAR(128)     NOT NULL DEFAULT 'Member',
    template      TEXT             NULL DEFAULT NULL,
    enabled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
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

-- 执行表
DROP TABLE IF EXISTS TASK_EXECUTE_LOG;
CREATE TABLE TASK_EXECUTE_LOG
(
    id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id    INT(11)          NOT NULL ,
    org_id        INT(11)          NOT NULL DEFAULT 0,
    store_id      INT(11)          NOT NULL DEFAULT 0,
    status        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    source_id     BIGINT(20)       NOT NULL,
    rule_id       INT(11)          NOT NULL,
    send_channel  TINYINT UNSIGNED NOT NULL,
    business_type TINYINT UNSIGNED NOT NULL,
    delay_type    TINYINT UNSIGNED NOT NULL,
    send_target   VARCHAR(128)     NOT NULL DEFAULT 'Member',
    delay_time    VARCHAR(128)     NULL DEFAULT NULL,
    plan_execute_time  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    generation_time  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    send_info01   VARCHAR(128)     NULL DEFAULT NULL,
    send_info02   VARCHAR(128)     NULL DEFAULT NULL,
    send_info03   VARCHAR(128)     NULL DEFAULT NULL,
    message       TEXT             NULL DEFAULT NULL,
    template      VARCHAR(512)     NULL DEFAULT NULL,
    uuid          VARCHAR(64)      NOT NULL,
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


-- 业务开关
DROP TABLE IF EXISTS TASK_PWOER_SWITCH;
CREATE TABLE TASK_PWOER_SWITCH
(
    id            INT(11)       NOT NULL AUTO_INCREMENT,
    company_id    INT(11)          NOT NULL ,
    business_type TINYINT UNSIGNED NOT NULL,
    enabled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
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
