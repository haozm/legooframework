DROP TABLE IF EXISTS crm_rfm_setting;
CREATE TABLE crm_rfm_setting
(
    company_id  INT(20)          NOT NULL,
    org_id      INT(20)          NOT NULL DEFAULT -1,
    store_id    INT(20)          NOT NULL DEFAULT -1,
    r_v1        INT(18)                   DEFAULT -1,
    r_v2        INT(18)                   DEFAULT -1,
    r_v3        INT(18)                   DEFAULT -1,
    r_v4        INT(18)                   DEFAULT -1,
    f_v1        INT(18)                   DEFAULT -1,
    f_v2        INT(18)                   DEFAULT -1,
    f_v3        INT(18)                   DEFAULT -1,
    f_v4        INT(18)                   DEFAULT -1,
    m_v1        INT(18)                   DEFAULT -1,
    m_v2        INT(18)                   DEFAULT -1,
    m_v3        INT(18)                   DEFAULT -1,
    m_v4        INT(18)                   DEFAULT -1,
    val_type    INT(1)                    DEFAULT 1,
    delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)       NOT NULL DEFAULT -1,
    creator     BIGINT(20)       NOT NULL DEFAULT -1,
    createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor      BIGINT(20)       NULL     DEFAULT NULL,
    editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员RFM
DROP TABLE IF EXISTS crm_member_rfm;
CREATE TABLE crm_member_rfm
(
    member_id    INT(11) NOT NULL,
    company_id   INT(20) NOT NULL,
    S_R_Level    INT(1)   DEFAULT 0,
    S_F_Level    INT(1)   DEFAULT 0,
    S_M_Level    INT(1)   DEFAULT 0,
    C_R_Level    INT(1)   DEFAULT 0,
    C_F_Level    INT(1)   DEFAULT 0,
    C_M_Level    INT(1)   DEFAULT 0,
    S_Level      CHAR(1)  DEFAULT 'E',
    C_Level      CHAR(1)  DEFAULT 'E',
    compute_time datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员历史RMF值
DROP TABLE IF EXISTS crm_member_rfm_history;
CREATE TABLE crm_member_rfm_history
(
    id           BIGINT(20) NOT NULL AUTO_INCREMENT,
    member_id    INT(11)    NOT NULL,
    company_id   INT(20)  DEFAULT NULL,
    S_R_Level    INT(1)   DEFAULT 0,
    S_F_Level    INT(1)   DEFAULT 0,
    S_M_Level    INT(1)   DEFAULT 0,
    C_R_Level    INT(1)   DEFAULT 0,
    C_F_Level    INT(1)   DEFAULT 0,
    C_M_Level    INT(1)   DEFAULT 0,
    S_Level      CHAR(1)  DEFAULT 'E',
    C_Level      CHAR(1)  DEFAULT 'E',
    compute_time datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE UNIQUE INDEX crm_member_rfm_history_company_id_IDX USING BTREE ON crm_member_rfm_history (company_id, member_id);