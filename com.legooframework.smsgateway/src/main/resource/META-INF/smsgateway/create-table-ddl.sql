-- SMS_KEY_WORDER
DROP TABLE IF EXISTS SMS_KEY_WORDS;
CREATE TABLE SMS_KEY_WORDS
(
  id          INT              NOT NULL AUTO_INCREMENT,
  key_word    VARCHAR(32)      NOT NULL,
  enabled     TINYINT UNSIGNED NOT NULL DEFAULT 1,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL,
  creator     BIGINT(20)       NOT NULL,
  createTime  DATETIME         NOT NULL,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;


-- 短息数据字典表
DROP TABLE IF EXISTS SMS_DICT_INFO;
CREATE TABLE SMS_DICT_INFO
(
  id          INT          NOT NULL AUTO_INCREMENT,
  dict_type   VARCHAR(32)  NOT NULL,
  field_value VARCHAR(32)  NOT NULL,
  field_name  VARCHAR(128) NULL     DEFAULT NULL,
  field_index INT(8)       NOT NULL,
  field_desc  TEXT         NULL     DEFAULT NULL,
  delete_flag TINYINT(1)   NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)   NOT NULL,
  creator     BIGINT(20)   NOT NULL,
  createTime  DATETIME     NOT NULL,
  editor      BIGINT(20)   NULL     DEFAULT NULL,
  editTime    DATETIME     NULL     DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DELETE
FROM SMS_DICT_INFO;

INSERT INTO SMS_DICT_INFO
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('SMS_PREFIX', 'WHITELIST', '白名单', 0,
        '^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$', -1, -1,
        NOW());

-- 门店短信设定
DROP TABLE IF EXISTS SMS_CONFIG_SETTING;
CREATE TABLE SMS_CONFIG_SETTING
(
  company_id  INT(11)          NOT NULL,
  store_id    INT(11)          NOT NULL DEFAULT -1,
  sms_prefix  VARCHAR(128)     NOT NULL,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL,
  creator     BIGINT(20)       NOT NULL,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (company_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 业务规则定义
DROP TABLE IF EXISTS SMS_SEND_RULE;
CREATE TABLE SMS_SEND_RULE
(
  id            CHAR(16)         NOT NULL,
  business_type VARCHAR(32)      NOT NULL,
  sms_channel   TINYINT UNSIGNED NOT NULL,
  free_send     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  enbaled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NULL     DEFAULT 100000,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DELETE
FROM SMS_SEND_RULE;
INSERT INTO SMS_SEND_RULE (id, business_type, sms_channel, free_send, tenant_id, creator)
VALUES ('0000000000000000', 'TOUCHED90', 2, 0, -1, -1);
INSERT INTO SMS_SEND_RULE (id, business_type, sms_channel, free_send, tenant_id, creator)
VALUES ('0000000000000001', 'BIRTHDAYTOUCH', 2, 0, -1, -1);

-- 短信充值规则
DROP TABLE IF EXISTS SMS_RECHARGE_RULES;
CREATE TABLE SMS_RECHARGE_RULES
(
  id           CHAR(16)         NOT NULL,
  unit_price   DECIMAL(11, 3)   NOT NULL,
  amount_range VARCHAR(128)     NOT NULL,
  company_id   INT(11)          NOT NULL DEFAULT -1,
  enbaled      TINYINT UNSIGNED NOT NULL DEFAULT 1,
  temporary    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  expired_date DATETIME         NULL     DEFAULT NULL,
  remarks      TEXT             NULL     DEFAULT NULL,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NULL     DEFAULT 100000,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DELETE
FROM SMS_RECHARGE_RULES;
INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, remarks, delete_flag, tenant_id,
                                creator, createTime)
VALUES ('1000000000000001', 5.500, '[330000..33000000]', -1, 1, '3300元（6万条）起充，550的整数倍', 0, -1, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, remarks, delete_flag, tenant_id,
                                creator, createTime)
VALUES ('1000000000000002', 6.000, '[240000..339999]', -1, 1, '2400元（4万条）起充，600的整数倍', 0, -1, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, remarks, delete_flag, tenant_id,
                                creator, createTime)
VALUES ('1000000000000003', 7.000, '[70000..239999]', -1, 1, '700元（1万条）起充，700正数倍', 0, -1, -1, CURRENT_TIMESTAMP);

-- 充值明细流水表
DROP TABLE IF EXISTS SMS_RECHARGE_DETAIL;
CREATE TABLE SMS_RECHARGE_DETAIL
(
  id              CHAR(16)         NOT NULL,
  company_id      INT(11)          NOT NULL,
  store_id        INT(11)          NOT NULL DEFAULT -1,
  store_ids       VARCHAR(256)     NULL     DEFAULT NULL,
  recharge_scope  TINYINT UNSIGNED NOT NULL,
  recharge_type   TINYINT UNSIGNED NOT NULL,
  rule_id         CHAR(16)         NOT NULL,
  recharge_amount BIGINT(20)       NOT NULL,
  total_quantity  INT(11)          NOT NULL,
  used_quantity   INT(11)          NOT NULL DEFAULT 0,
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

CREATE INDEX SMS_RECHARGE_DETAIL_MIXED_IDX USING BTREE ON SMS_RECHARGE_DETAIL (company_id,store_id,store_ids);

-- SMSTransportLogSplitter 短信余额表
DROP TABLE IF EXISTS SMS_RECHARGE_BALANCE;
CREATE TABLE SMS_RECHARGE_BALANCE
(
  id             CHAR(16)         NOT NULL,
  company_id     INT(11)          NOT NULL,
  store_id       INT(11)          NOT NULL DEFAULT -1,
  store_ids      VARCHAR(256)     NULL     DEFAULT NULL,
  recharge_scope TINYINT UNSIGNED NOT NULL,
  sms_balance    BIGINT(20)       NOT NULL DEFAULT 0,
  group_name     VARCHAR(256)     NULL     DEFAULT NULL,
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

-- 抵扣流水
DROP TABLE IF EXISTS SMS_RECHARGE_DEDUCTION_DETAIL;
CREATE TABLE SMS_RECHARGE_DEDUCTION_DETAIL
(
  id           CHAR(16)         NOT NULL,
  recharge_id  CHAR(16)         NOT NULL,
  deduction_id CHAR(16)         NOT NULL,
  record_id    CHAR(16)         NOT NULL,
  finish_flag  TINYINT UNSIGNED NOT NULL DEFAULT 1,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NULL     DEFAULT NULL,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 扣费概要账户表
DROP TABLE IF EXISTS SMS_CHARGE_SUMMARY;
CREATE TABLE SMS_CHARGE_SUMMARY
(
  id            CHAR(36)         NOT NULL,
  company_id    INT(11)          NOT NULL,
  store_id      INT(11)          NOT NULL,
  send_mode     TINYINT UNSIGNED NOT NULL,
  finish_send   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  sms_batchno   VARCHAR(32)      NOT NULL,
  free_send     TINYINT UNSIGNED NOT NULL,
  business_type VARCHAR(32)      NOT NULL,
  sms_quantity  BIGINT(20)       NOT NULL DEFAULT 0,
  wx_quantity   BIGINT(20)       NOT NULL DEFAULT 0,
  sms_context   VARCHAR(255)     NULL,
  store_name    VARCHAR(255)     NULL,
  company_name  VARCHAR(255)     NULL,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NULL     DEFAULT NULL,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 消费明细
DROP TABLE IF EXISTS SMS_CHARGE_DETAIL;
CREATE TABLE SMS_CHARGE_DETAIL
(
  id            CHAR(36)         NOT NULL,
  company_id    INT(11)          NOT NULL,
  store_id      INT(11)          NOT NULL,
  sms_batchno   VARCHAR(32)      NOT NULL,
  blance_id     CHAR(16)         NOT NULL,
  blance_num    BIGINT(20)       NOT NULL DEFAULT 0,
  deduction_num BIGINT(20)       NOT NULL DEFAULT 0,
  reimburse_num BIGINT(20)       NOT NULL DEFAULT 0,
  delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id     BIGINT(20)       NULL     DEFAULT NULL,
  creator       BIGINT(20)       NOT NULL DEFAULT -1,
  createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor        BIGINT(20)       NULL     DEFAULT NULL,
  editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 短信发送记录跟踪表
DROP TABLE IF EXISTS SMS_TRANSPORT_LOG;
CREATE TABLE SMS_TRANSPORT_LOG
(
  id                    CHAR(36)         NOT NULL,
  company_id            INT(11)          NOT NULL,
  store_id              INT(11)          NOT NULL,
  member_id             INT(11)          NULL     DEFAULT -1,
  send_channel          TINYINT UNSIGNED NOT NULL DEFAULT 1,
  job_id                INT(18)          NULL     DEFAULT NULL,
  weixin_id             VARCHAR(128)     NULL     DEFAULT NULL,
  device_id             VARCHAR(128)     NULL     DEFAULT NULL,
  free_send             TINYINT UNSIGNED NOT NULL,
  sms_channel           TINYINT UNSIGNED NOT NULL,
  send_mode             TINYINT UNSIGNED NOT NULL DEFAULT 2,
  send_status           TINYINT UNSIGNED NOT NULL,
  businsess_type        TINYINT UNSIGNED NOT NULL,
  send_batchno          VARCHAR(64)      NOT NULL,
  final_state           TINYINT UNSIGNED NOT NULL DEFAULT 0,
  final_state_date      DATETIME         NULL,
  reimburse_state       TINYINT UNSIGNED NOT NULL DEFAULT 0,
  reimburse_state_date  DATETIME         NULL,
  phone_no              VARCHAR(64)      NULL     DEFAULT NULL,
  sms_enabled           TINYINT UNSIGNED NOT NULL DEFAULT 1,
  sms_count             TINYINT UNSIGNED NOT NULL DEFAULT 0,
  word_count            TINYINT UNSIGNED NOT NULL DEFAULT 0,
  member_name           VARCHAR(64)      NULL     DEFAULT NULL,
  sms_context           VARCHAR(1024)    NOT NULL,
  send_res_code         VARCHAR(64)      NULL     DEFAULT NULL,
  final_state_desc      VARCHAR(1024)    NULL,
  send_local_date       DATETIME         NULL     DEFAULT NULL,
  remarks               VARCHAR(1024)    NULL     DEFAULT NULL,
  delete_flag           TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id             BIGINT(20)       NULL     DEFAULT NULL,
  creator               BIGINT(20)       NOT NULL DEFAULT -1,
  createTime            DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor                BIGINT(20)       NULL     DEFAULT NULL,
  editTime              DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 短息提交批次记录等
DROP TABLE IF EXISTS SMS_TRANSPORT_BATCH;
CREATE TABLE SMS_TRANSPORT_BATCH
(
  id           BIGINT(20)       NOT NULL AUTO_INCREMENT,
  company_id   INT(11)          NOT NULL,
  store_id     INT(11)          NOT NULL,
  send_batchno VARCHAR(64)      NOT NULL,
  is_billing   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  sms_write_count    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  sms_write_ok_count TINYINT UNSIGNED NOT NULL DEFAULT 0,
  wx_write_count     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  wx_write_ok_count  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NULL     DEFAULT NULL,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, company_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE INDEX SMS_TRANSPORT_BATCH_MIXING_IDX USING BTREE ON acp.SMS_TRANSPORT_BATCH (company_id,store_id,send_batchno);

-- 电话黑名单
DROP TABLE IF EXISTS SMS_BLACKLIST;
CREATE TABLE SMS_BLACKLIST
(
  id           VARCHAR(32)      NOT NULL,
  company_id   INT(11)          NOT NULL,
  store_id     INT(11)          NOT NULL,
  is_disable   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  is_effective TINYINT UNSIGNED NOT NULL DEFAULT 1,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NULL     DEFAULT NULL,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, company_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 同步短信记录
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
VALUES ('SYNC_SMS_BLACK', CURRENT_TIMESTAMP);

