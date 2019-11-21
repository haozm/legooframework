-- 短息状态回查表
DROP TABLE IF EXISTS SMS_SENDING_LOG;
CREATE TABLE SMS_SENDING_LOG
(
    id               CHAR(36)         NOT NULL,
    company_id       INT(11)          NOT NULL,
    store_id         INT(11)          NOT NULL,
    sms_channel      TINYINT UNSIGNED NOT NULL,
    sms_account      VARCHAR(64)      NULL,
    sms_ext          BIGINT(10)       NOT NULL,
    send_state       TINYINT UNSIGNED NOT NULL DEFAULT 0,
    send_msg_id      VARCHAR(64)      NULL,
    send_date        DATETIME         NULL,
    send_remarks     VARCHAR(255)     NULL,
    final_state      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    final_date       DATETIME         NULL,
    final_desc       VARCHAR(1024)    NULL,
    phone_no         VARCHAR(64)      NOT NULL,
    sms_count        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    word_count       TINYINT UNSIGNED NOT NULL DEFAULT 0,
    sms_context      VARCHAR(512)     NOT NULL,
    delete_flag      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id        BIGINT(20)       NULL     DEFAULT NULL,
    creator          BIGINT(20)       NOT NULL DEFAULT -1,
    createTime       DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor           BIGINT(20)       NULL     DEFAULT NULL,
    editTime         DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE INDEX SMS_SENDING_LOG_MIXING_IDX USING BTREE ON SMS_TRANSPORT_BATCH (company_id,store_id);
CREATE INDEX SMS_SENDING_LOG_SEND_MSG_ID_IDX USING BTREE ON SMS_TRANSPORT_BATCH (send_msg_id);

-- 客户短信回复表
DROP TABLE IF EXISTS SMS_REPLAY_LOG;
CREATE TABLE SMS_REPLAY_LOG
(
    id              BIGINT(20)       NOT NULL AUTO_INCREMENT,
    sms_account     VARCHAR(64)      NULL,
    phone_no        VARCHAR(64)      NOT NULL,
    sms_ext         BIGINT(10)       NOT NULL DEFAULT 0,
    send_msg_id     VARCHAR(64)      NULL,
    sms_context     VARCHAR(512)     NOT NULL,
    sms_replay_date DATETIME         NOT NULL,
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

-- 短息状态回查表
DROP TABLE IF EXISTS SMS_LAST_SYNC_DATE;
CREATE TABLE SMS_LAST_SYNC_DATE
(
    id         BIGINT(20)  NOT NULL AUTO_INCREMENT,
    sync_type  VARCHAR(32) NOT NULL,
    createTime DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

INSERT INTO SMS_LAST_SYNC_DATE (sync_type, createTime)
VALUES ('SYNC_SMS_STATE', CURRENT_TIMESTAMP);

INSERT INTO SMS_LAST_SYNC_DATE (sync_type, createTime)
VALUES ('SYNC_SMS_REPLAY', CURRENT_TIMESTAMP);

DROP TABLE IF EXISTS SMS_ALL_BLACKLIST;
CREATE TABLE SMS_ALL_BLACKLIST
(
    company_id      INT(11)          NOT NULL DEFAULT -1,
    store_id        INT(11)          NOT NULL DEFAULT -1,
    phone_no        VARCHAR(64)      NOT NULL,
    send_msg_id     VARCHAR(64)      NULL,
    sms_context     VARCHAR(512)     NOT NULL,
    sms_replay_date DATETIME         NOT NULL,
    delete_flag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id       BIGINT(20)       NULL     DEFAULT NULL,
    creator         BIGINT(20)       NOT NULL DEFAULT -1,
    createTime      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor          BIGINT(20)       NULL     DEFAULT NULL,
    editTime        DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, store_id, phone_no)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

