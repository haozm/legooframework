-- 记录明细
DROP TABLE IF EXISTS crm_salerecord_sharing;
CREATE TABLE crm_salerecord_sharing
(
    id                 BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id         INT(11)          NOT NULL,
    store_id           INT(11)          NOT NULL,
    old_salerecord_id  VARCHAR(64)      NOT NULL,
    sharing_type       TINYINT UNSIGNED NOT NULL DEFAULT 1,
    employee_id        INT(11)          NOT NULL,
    sharing_amount     NUMERIC(10, 2)   NOT NULL DEFAULT 0.00,
    sharing_proportion INT(11)          NOT NULL DEFAULT 0,
    delete_flag        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id          BIGINT(20)       NULL     DEFAULT NULL,
    creator            BIGINT(20)       NOT NULL DEFAULT -1,
    createTime         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor             BIGINT(20)       NULL     DEFAULT NULL,
    editTime           DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 行业分类字段
ALTER TABLE CSOSM_CRM_DB.crm_salerecord ADD categories varchar(100) DEFAULT '0' NOT NULL;

-- 销售主记录是否变化情况字段
-- 字典信息记录顺序如下: company_id,store_id,member_id,oldSaleRecordId,saleTotalAmount,createTime(%Y-%m-%d %H:%i:%s),status
ALTER TABLE crm_salerecord ADD change_flag varchar(100) NULL;
-- 处理历史记录
UPDATE crm_salerecord
   SET change_flag= CONCAT(company_id,store_id,IFNULL(member_id,0),oldSaleRecordId,IFNULL(saleTotalAmount,0), DATE_FORMAT(createTime,'%Y-%m-%d %H:%i:%s'),status);

-- ADD BY HXJ 2019-10-17 Divided into
-- setting_rules [{se=2,ce=1,ce=10},{se=2,ce=1},{se=1}]
DROP TABLE IF EXISTS ACP_EMPLOYEE_ALLOT_RULE;
CREATE TABLE ACP_EMPLOYEE_ALLOT_RULE (
    id                 BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id         INT(11)          NOT NULL,
    store_id           INT(11)          NOT NULL DEFAULT 0,
    member_rule        VARCHAR(512)     DEFAULT NULL,
    no_member_rule     VARCHAR(512)     DEFAULT NULL,
    crs_member_rule    VARCHAR(512)     DEFAULT NULL,
    crs_no_member_rule VARCHAR(512)     DEFAULT NULL,
    auto_run           TINYINT UNSIGNED NOT NULL DEFAULT 1,
    delete_flag        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id          BIGINT(20)       NULL     DEFAULT NULL,
    creator            BIGINT(20)       NOT NULL DEFAULT -1,
    createTime         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor             BIGINT(20)       NULL     DEFAULT NULL,
    editTime           DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX ACP_EMPLOYEE_ALLOT_RULE_company_id_IDX USING BTREE ON acp.ACP_EMPLOYEE_ALLOT_RULE (company_id,store_id);

-- ADD BY HXJ
-- order_type 1,2,3,4      is_detail:0 总账  1 明细张
DROP TABLE IF EXISTS ACP_EMPLOYEE_ALLOT_RESULT;
CREATE TABLE ACP_EMPLOYEE_ALLOT_RESULT (
    id                 BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id         INT(11)          NOT NULL,
    store_id           INT(11)          NOT NULL DEFAULT 0,
    sale_record_id     INT(11)          NOT NULL,
    employee_id        INT(11)          NOT NULL DEFAULT 0,
    employee_type      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    employee_count     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    order_type         TINYINT UNSIGNED NOT NULL,
    member_id          INT(11) NOT NULL DEFAULT 0,
    error_tag          TINYINT UNSIGNED NOT NULL DEFAULT 0,
    is_detail          TINYINT UNSIGNED NOT NULL DEFAULT 0,
    allot_rule         VARCHAR(512)     DEFAULT NULL,
    card_amount        NUMERIC(10,2)    NOT NULL DEFAULT 0.00,
    sale_amount        NUMERIC(10,2)    NOT NULL DEFAULT 0.00,
    allot_msg          VARCHAR(512)     NULL,
    sale_date          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delete_flag        TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id          BIGINT(20)       NULL     DEFAULT NULL,
    creator            BIGINT(20)       NOT NULL DEFAULT -1,
    createTime         DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor             BIGINT(20)       NULL     DEFAULT NULL,
    editTime           DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
