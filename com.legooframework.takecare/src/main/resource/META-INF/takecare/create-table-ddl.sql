
-- modiy 表结构
ALTER TABLE acp.crm_birthdaycareplan ADD company_id INT(11) NULL;
ALTER TABLE acp.crm_birthdaycareplan ADD createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE acp.crm_birthdaycareplan ADD editTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
UPDATE acp.crm_birthdaycareplan SET company_id= (SELECT cm.company_id from acp.crm_member as cm where cm.id = member_id);
ALTER TABLE acp.crm_birthdaycareplan MODIFY COLUMN company_id INT(11) NOT NULL;
ALTER TABLE acp.crm_birthdaycareplan ADD calendarType INT(11) NULL;
ALTER TABLE acp.crm_birthdaycareplan ADD birthday DATETIME NULL;
ALTER TABLE acp.crm_birthdaycareplan ADD this_year_birthday DATETIME NULL;
ALTER TABLE acp.crm_birthdaycareplan ADD care_date DATETIME NULL;
UPDATE acp.crm_birthdaycareplan SET care_date = followUpTime;
-- =========================== user ==============================

DROP TABLE IF EXISTS TAKECARE_RECORD_LOG;
CREATE TABLE TAKECARE_RECORD_LOG
(
    id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
    care_id       INT(11)          NOT NULL,
    sub_care_id   INT(11)          NOT NULL DEFAULT 0,
    company_id    INT(11)          NOT NULL ,
    org_id        INT(11)          NOT NULL DEFAULT 0,
    store_id      INT(11)          NOT NULL DEFAULT 0,
    employee_id   INT(11)          NOT NULL DEFAULT 0,
    member_id     INT(11)          NOT NULL DEFAULT 0,
    error_tag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    send_info01   VARCHAR(128)     NULL DEFAULT NULL,
    send_info02   VARCHAR(128)     NULL DEFAULT NULL,
    business_type TINYINT UNSIGNED NOT NULL,
    send_channel  TINYINT UNSIGNED NOT NULL,
    message       VARCHAR(512)     NULL DEFAULT NULL,
    img_urls      TEXT             NULL DEFAULT NULL,
    context       TEXT             NULL DEFAULT NULL,
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

CREATE INDEX TAKECARE_RECORD_LOG_ANY_IDX USING BTREE ON acp.TAKECARE_RECORD_LOG (company_id,store_id,employee_id,member_id);
CREATE INDEX TAKECARE_RECORD_LOG_care_IDX USING BTREE ON acp.TAKECARE_RECORD_LOG (care_id);
