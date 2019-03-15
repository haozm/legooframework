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
VALUES ('SMS_BUS_TYPE', '90TOUCHED', '90服务', 0, '90服务', -1, -1, NOW());
INSERT INTO SMS_DICT_INFO
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('SMS_BUS_TYPE', 'BIRTHDAYTOUCH', '生日感动', 0, '生日感动', -1, -1, NOW());

INSERT INTO SMS_DICT_INFO
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('SMS_PREFIX', 'WHITELIST', '白名单', 0,
        '^((13[0-9])|(14[5,7,9])|(15([0-3]|[5-9]))|(166)|(17[0,1,3,5,6,7,8])|(18[0-9])|(19[8|9]))\\d{8}$', -1, -1,
        NOW());

-- 门店短信设定
DROP TABLE IF EXISTS SMS_CONFIG_SETTING;
CREATE TABLE SMS_CONFIG_SETTING
(
  id          INT              NOT NULL AUTO_INCREMENT,
  company_id  INT(11)          NOT NULL,
  store_id    INT(11)          NOT NULL DEFAULT -1,
  sms_prefix  VARCHAR(128)     NOT NULL,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL,
  creator     BIGINT(20)       NOT NULL,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
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

INSERT INTO SMS_BUSINESS_RULE(id, business_type, sms_channel, free_send, tenant_id, creator, createTime)
VALUES ('0000000000000000', '90TOUCHED', 2, 0, 100000, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_BUSINESS_RULE(id, business_type, business_desc, sms_channel, free_send, tenant_id, creator, createTime)
VALUES ('0000000000000001', 'BIRTHDAYTOUCH', 2, 0, 100000, -1, CURRENT_TIMESTAMP);

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

INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, delete_flag, tenant_id, creator,
                                createTime)
VALUES ('1111222233334444', 5.000, '[200100..99999999999999]', -1, 1, 0, 100000, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, delete_flag, tenant_id, creator,
                                createTime)
VALUES ('1111222233335555', 6.000, '[70100..200000]', -1, 1, 0, 100000, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_RECHARGE_RULES (id, unit_price, amount_range, company_id, enbaled, delete_flag, tenant_id, creator,
                                createTime)
VALUES ('1111222233336666', 7.000, '[100..70000]', -1, 1, 0, 100000, -1, CURRENT_TIMESTAMP);

-- 充值明细流水表
DROP TABLE IF EXISTS SMS_RECHARGE_DETAIL;
CREATE TABLE SMS_RECHARGE_DETAIL
(
  id              CHAR(16)         NOT NULL,
  company_id      INT(11)          NOT NULL,
  store_id        INT(11)          NOT NULL DEFAULT -1,
  store_group_id  VARCHAR(36)      NULL     DEFAULT NULL,
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

-- SMSTransportLogSplitter 短信余额表
DROP TABLE IF EXISTS SMS_RECHARGE_BALANCE;
CREATE TABLE SMS_RECHARGE_BALANCE
(
  id             CHAR(16)         NOT NULL,
  company_id     INT(11)          NOT NULL,
  store_id       INT(11)          NOT NULL DEFAULT -1,
  store_group_id VARCHAR(36)      NULL     DEFAULT NULL,
  recharge_scope TINYINT UNSIGNED NOT NULL,
  sms_balance    BIGINT(20)       NOT NULL DEFAULT 0,
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

-- 扣费概要账户表
DROP TABLE IF EXISTS SMS_CHARGE_SUMMARY;
CREATE TABLE SMS_CHARGE_SUMMARY
(
  id              CHAR(36)         NOT NULL,
  company_id      INT(11)          NOT NULL,
  store_id        INT(11)          NOT NULL,
  send_mode       TINYINT UNSIGNED NOT NULL,
  finish_send     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  sms_batchno     VARCHAR(32)      NOT NULL,
  business_ruleid CHAR(16)         NOT NULL,
  business_type   VARCHAR(32)      NOT NULL,
  sms_quantity    BIGINT(20)       NOT NULL DEFAULT 0,
  operator_name   VARCHAR(255)     NULL,
  sms_context     VARCHAR(255)     NULL,
  store_name      VARCHAR(255)     NULL,
  company_name    VARCHAR(255)     NULL,
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
  id             CHAR(36)         NOT NULL,
  company_id     INT(11)          NOT NULL,
  store_id       INT(11)          NOT NULL,
  member_id      INT(11)          NULL     DEFAULT -1,
  free_send      TINYINT UNSIGNED NOT NULL,
  sms_channel    TINYINT UNSIGNED NOT NULL,
  businsess_type VARCHAR(32)      NOT NULL,
  sms_batchno    VARCHAR(32)      NOT NULL,
  send_status    TINYINT UNSIGNED NOT NULL,
  res_code       CHAR(4)          NULL,
  phone_no       VARCHAR(64)      NOT NULL,
  sms_count      TINYINT UNSIGNED NOT NULL DEFAULT 0,
  word_count     TINYINT UNSIGNED NOT NULL DEFAULT 0,
  member_name    VARCHAR(64)      NULL     DEFAULT NULL,
  sms_context    VARCHAR(512)     NOT NULL,
  remarks        VARCHAR(512)     NULL,
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

-- 电话黑名单
DROP TABLE IF EXISTS SMS_BLACKLIST;
CREATE TABLE SMS_BLACKLIST
(
  id           VARCHAR(32)      NOT NULL,
  company_id   INT(11)          NOT NULL,
  is_effective TINYINT UNSIGNED NOT NULL DEFAULT 1,
  is_disable   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  member_id    INT(11)          NULL,
  delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id    BIGINT(20)       NULL     DEFAULT NULL,
  creator      BIGINT(20)       NOT NULL DEFAULT -1,
  createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor       BIGINT(20)       NULL     DEFAULT NULL,
  editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 视图创建
DROP VIEW IF EXISTS VIEW_ORGANIZATION_OR_STORE;
CREATE VIEW VIEW_ORGANIZATION_OR_STORE AS
  (SELECT org.id                                               AS 'id',
          org.orgType                                          AS 'orgType',
          org.name                                             AS 'orgName',
          CASE LOCATE('_', org.code)
            WHEN 0 THEN org.id
            ELSE LEFT(org.code, LOCATE('_', org.code) - 1) END AS 'companyId'
   FROM csosm_crm.acp_organization AS org
   WHERE org.status = 1)
  UNION
  (SELECT sto.id AS 'id', 3 AS 'orgType', sto.name AS 'orgName', sto.company_id AS 'companyId'
   FROM csosm_crm.acp_store AS sto
   WHERE sto.status = 1
     AND sto.company_id IS NOT NULL);

