-- 模板使用分类树
DROP TABLE IF EXISTS MSG_TEMPLATE_CLASSIFY;
CREATE TABLE MSG_TEMPLATE_CLASSIFY
(
    id          CHAR(4)      NOT NULL,
    pid         CHAR(4)      NOT NULL,
    classify    VARCHAR(64)  NOT NULL,
    deep_path   VARCHAR(128) NOT NULL,
    company_id  INT(11)      NOT NULL,
    delete_flag INT(1)       NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)   NULL     DEFAULT NULL,
    creator     BIGINT(20)   NOT NULL,
    createTime  DATETIME     NOT NULL,
    editor      BIGINT(20)   NULL     DEFAULT NULL,
    editTime    DATETIME     NULL     DEFAULT NULL,
    PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 短信模板内容
DROP TABLE IF EXISTS MSG_TEMPLATE_CONTEXT;
CREATE TABLE MSG_TEMPLATE_CONTEXT
(
    id           CHAR(16)         NOT NULL,
    company_id   INT(18)          NOT NULL DEFAULT -1,
    org_id       INT(18)          NOT NULL DEFAULT -1,
    store_id     INT(18)          NOT NULL DEFAULT -1,
    blacked      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    classifies   VARCHAR(128)     NOT NULL,
    use_scopes   VARCHAR(64)      NOT NULL,
    expire_date  DATETIME         NULL     DEFAULT NULL,
    is_default   TINYINT UNSIGNED NOT NULL DEFAULT 0,
    temp_title   VARCHAR(128)     NOT NULL DEFAULT '-1',
    temp_context VARCHAR(512)     NOT NULL,
    delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id    BIGINT(20)       NULL     DEFAULT -1,
    creator      BIGINT(20)       NOT NULL DEFAULT -1,
    createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor       BIGINT(20)       NULL     DEFAULT NULL,
    editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 模板黑名单
DROP TABLE IF EXISTS MSG_TEMPLATE_BLACKLIST;
CREATE TABLE MSG_TEMPLATE_BLACKLIST
(
    company_id INT(18)       NOT NULL DEFAULT -1,
    org_id     INT(18)       NOT NULL DEFAULT -1,
    store_id   INT(18)       NOT NULL DEFAULT -1,
    black_list VARCHAR(2048) NULL     DEFAULT NULL,
    createTime DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editTime   DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, org_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 模版替换字典
DROP TABLE IF EXISTS MSG_TEMPLATE_REPLACE;
CREATE TABLE MSG_TEMPLATE_REPLACE
(
    id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
    field_tag     VARCHAR(32)      NOT NULL,
    replace_token VARCHAR(64)      NOT NULL,
    token_type    VARCHAR(64)      NOT NULL,
    default_value VARCHAR(64)      NULL     DEFAULT NULL,
    enbaled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
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

INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('memberName', '会员姓名', 'STRING', '会员', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('memberPhone', '会员电话', 'STRING', '会员电话', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('storeName', '门店名称', 'STRING', '我门店', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('storeManager', '店长', 'STRING', '店长', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('storePhone', '门店电话', 'STRING', '门店电话', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('shoppingGuide', '导购姓名', 'STRING', '导购员', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('lastName', '姓', 'STRING', '会员', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('sex', '性别', 'ENUM', '1=先生,2=女士,*=先生/女士', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('birthday', '生日日期', 'DATE', 'yyyy-MM-dd', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
VALUES ('companyName', '公司名称', 'STRING', null, :COMPANY_ID);


-- 节日表
DROP TABLE IF EXISTS holiday_info;
CREATE TABLE holiday_info (
  id varchar(64) NOT NULL UNIQUE,
  name varchar(64) NOT NULL,
  remark varchar(512),
  cron varchar(64) NOT NULL,
  cronContext varchar(64),
  cronType varchar(64) NOT NULL,
  cronDate datetime,
  calendarType varchar(64),
  duration int(11) NOT NULL,
  type varchar(64) NOT NULL,
  enable int(11) NOT NULL,
  editable int(11) NOT NULL DEFAULT 1,
  company_blackList varchar(512),
  store_blackList varchar(512),
  company_id int(11) NOT NULL,
  store_id int(11) NOT NULL,
  createUserId int(11) NOT NULL DEFAULT '-1',
  createTime datetime NOT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
