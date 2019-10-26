-- KV 保险使用数据字段存储表
DROP TABLE IF EXISTS INSURANCE_KV_DATA;
CREATE TABLE INSURANCE_KV_DATA
(
    id          INT          NOT NULL AUTO_INCREMENT,
    dict_type   VARCHAR(32)  NOT NULL,
    field_value VARCHAR(32)  NOT NULL,
    field_name  VARCHAR(128) NULL     DEFAULT NULL,
    field_index INT(8)       NOT NULL,
    field_desc  TEXT         NULL     DEFAULT NULL,
    delete_flag TINYINT(1)   NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)   NOT NULL DEFAULT -1,
    creator     BIGINT(20)   NOT NULL,
    createTime  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor      BIGINT(20)   NULL     DEFAULT NULL,
    editTime    DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8
  COLLATE = 'utf8_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS INSURANCE_MEMBER_DATA;
CREATE TABLE INSURANCE_MEMBER_DATA
(
    id             BIGINT(20)   NOT NULL,
    cardId         VARCHAR(32)  NOT NULL,
    name           VARCHAR(64)  NOT NULL,
    sex_type       TINYINT      NOT NULL DEFAULT 1,
    birthday       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    phone          VARCHAR(32)  NULL     DEFAULT NULL,
    mobile         VARCHAR(32)  NULL     DEFAULT NULL,
    education_type TINYINT      NOT NULL DEFAULT 4,
    height         INT(4)       NULL     DEFAULT 0,
    weight         INT(4)       NULL     DEFAULT 0,
    email          VARCHAR(128) NULL     DEFAULT NULL,
    familyAddr     VARCHAR(254) NULL     DEFAULT NULL,
    workAddr       VARCHAR(254) NULL     DEFAULT NULL,
    delete_flag    TINYINT(1)   NOT NULL DEFAULT 0,
    tenant_id      BIGINT(20)   NOT NULL DEFAULT -1,
    creator        BIGINT(20)   NOT NULL DEFAULT -1,
    createTime     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor         BIGINT(20)   NULL     DEFAULT NULL,
    editTime       DATETIME     NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8
  COLLATE = 'utf8_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX insurance_member_data_cardid_idx USING BTREE ON insurance_member_data (cardId);

DROP TABLE IF EXISTS INSURANCE_LIST_INFO;
CREATE TABLE INSURANCE_LIST_INFO
(
    id               BIGINT(20)     NOT NULL AUTO_INCREMENT,
    insurance_id     BIGINT(20)     NOT NULL,
    insurance_type   VARCHAR(32)    NOT NULL,
    insurance_amount NUMERIC(10, 2) NOT NULL,
    is_primary       TINYINT        NOT NULL DEFAULT 0,
    delete_flag      TINYINT(1)     NOT NULL DEFAULT 0,
    tenant_id        BIGINT(20)     NOT NULL DEFAULT -1,
    creator          BIGINT(20)     NOT NULL DEFAULT -1,
    createTime       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor           BIGINT(20)     NULL     DEFAULT NULL,
    editTime         DATETIME       NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8
  COLLATE = 'utf8_general_ci'
  ENGINE = InnoDB;


DROP TABLE IF EXISTS INSURANCE_POLICY_INFO;
CREATE TABLE INSURANCE_POLICY_INFO
(
    id                BIGINT(20)     NOT NULL,
    insurance_no      VARCHAR(128)   NOT NULL,
    insured_date      DATETIME       NOT NULL,
    defrayer_id       BIGINT(20)     NOT NULL,
    accepter_id       BIGINT(20)     NOT NULL,
    relationship_type VARCHAR(32)    NOT NULL,
    is_myself         TINYINT        NOT NULL DEFAULT 0,
    payment_type      VARCHAR(32)    NOT NULL,
    pay_amount        NUMERIC(10, 2) NOT NULL,
    bankcard_id       BIGINT(18)     NOT NULL,
    beneficiary_info  VARCHAR(512)   NULL,
    remarks           VARCHAR(512)   NULL,
    delete_flag       TINYINT(1)     NOT NULL DEFAULT 0,
    tenant_id         BIGINT(20)     NOT NULL DEFAULT -1,
    creator           BIGINT(20)     NOT NULL DEFAULT -1,
    createTime        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor            BIGINT(20)     NULL     DEFAULT NULL,
    editTime          DATETIME       NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8
  COLLATE = 'utf8_general_ci'
  ENGINE = InnoDB;


DROP TABLE IF EXISTS INSURANCE_BANK_CARD;
CREATE TABLE INSURANCE_BANK_CARD
(
    id          BIGINT(20)  NOT NULL,
    member_id   BIGINT(20)  NOT NULL,
    bank_type   VARCHAR(64) NOT NULL,
    account     VARCHAR(64) NOT NULL,
    delete_flag TINYINT(1)  NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)  NOT NULL DEFAULT -1,
    creator     BIGINT(20)  NOT NULL DEFAULT -1,
    createTime  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor      BIGINT(20)  NULL     DEFAULT NULL,
    editTime    DATETIME    NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8
  COLLATE = 'utf8_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX insurance_bank_card_account_idx USING BTREE ON insurance_bank_card (account);

DELETE
FROM INSURANCE_KV_DATA;
INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('SEX', '1', '男性', 0, '男', -1, -1, NOW()),
       ('SEX', '2', '女性', 0, '女', -1, -1, NOW());

INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('EDUCATION', '1', '小学', 0, '小学', -1, -1, NOW()),
       ('EDUCATION', '2', '中学', 1, '中学', -1, -1, NOW()),
       ('EDUCATION', '3', '高中', 2, '高中', -1, -1, NOW()),
       ('EDUCATION', '4', '大学', 3, '大学', -1, -1, NOW()),
       ('EDUCATION', '5', '研究生', 4, '研究生', -1, -1, NOW()),
       ('EDUCATION', '6', '博士', 5, '博士', -1, -1, NOW());

INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('BANK', 'RMYH', '中国银行', 0, '中国银行', -1, -1, NOW()),
       ('BANK', 'PAYH', '平安银行', 1, '平安银行', -1, -1, NOW()),
       ('BANK', 'JSYH', '建设银行', 2, '建设银行', -1, -1, NOW()),
       ('BANK', 'GFYH', '广发银行', 3, '广发银行', -1, -1, NOW()),
       ('BANK', 'YZYH', '邮政银行', 4, '邮政银行', -1, -1, NOW()),
       ('BANK', 'NYYH', '农业银行', 4, '农业银行', -1, -1, NOW()),
       ('BANK', 'GSYH', '工商银行', 4, '工商银行', -1, -1, NOW()),
       ('BANK', 'GZNSYH', '广州农商行', 4, '广州农商行', -1, -1, NOW()),
       ('BANK', 'GDUH', '光大银行', 4, '光大银行', -1, -1, NOW()),
       ('BANK', 'PFYH', '浦发银行', 5, '浦发银行', -1, -1, NOW());

INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('RELATIONSHIP', 'XY', '夫妻', 0, '夫妻', -1, -1, NOW()),
       ('RELATIONSHIP', 'AB', '父子', 0, '父子', -1, -1, NOW()),
       ('RELATIONSHIP', 'AC', '母女', 0, '母女', -1, -1, NOW()),
       ('RELATIONSHIP', 'AF', '母子', 0, '母子', -1, -1, NOW()),
       ('RELATIONSHIP', 'BB', '兄弟', 1, '兄弟', -1, -1, NOW()),
       ('RELATIONSHIP', 'BC', '兄妹', 2, '兄妹', -1, -1, NOW()),
       ('RELATIONSHIP', 'AD', '父女', 0, '父女', -1, -1, NOW());

INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('PAYMENTTYPE', '01', '月缴', 0, '月缴', -1, -1, NOW()),
       ('PAYMENTTYPE', '02', '年缴', 0, '年缴', -1, -1, NOW());


INSERT INTO INSURANCE_KV_DATA
(dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES ('INSURANCE', '00001', '(平安福门)寿险', 0, '(平安福门)寿险', -1, -1, NOW()),
       ('INSURANCE', '00002', '重疾', 0, '重疾', -1, -1, NOW()),
       ('INSURANCE', '00003', 'e生保plus贺岁版', 0, 'e生保plus贺岁版', -1, -1, NOW()),
       ('INSURANCE', '00008', 'e生保贺岁版', 0, 'e生保贺岁版', -1, -1, NOW()),
       ('INSURANCE', '00004', '宝贝卡B限量版', 0, '宝贝卡B限量版', -1, -1, NOW()),
       ('INSURANCE', '00013', '宝贝卡A限量版', 0, '宝贝卡A限量版', -1, -1, NOW()),
       ('INSURANCE', '00009', '宝贝卡限量版', 0, '宝贝卡限量版', -1, -1, NOW()),
       ('INSURANCE', '00005', '住院险', 0, '住院险', -1, -1, NOW()),
       ('INSURANCE', '00006', '意外医疗', 0, '意外医疗', -1, -1, NOW()),
       ('INSURANCE', '00010', '长期意外险', 0, '长期意外险', -1, -1, NOW()),
       ('INSURANCE', '00011', '金瑞人生', 0, '金瑞人生', -1, -1, NOW()),
       ('INSURANCE', '00012', '顺意卡', 0, '顺意卡', -1, -1, NOW()),
       ('INSURANCE', '00007', 'e生保plus', 0, 'e生保plus', -1, -1, NOW());




