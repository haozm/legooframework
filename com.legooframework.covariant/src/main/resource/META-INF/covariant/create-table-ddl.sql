-- 临时模板记录
DROP TABLE IF EXISTS MSG_TEMPLATE_CONTEXT;
CREATE TABLE MSG_TEMPLATE_CONTEXT
(
    id           BIGINT(20)       NOT NULL AUTO_INCREMENT,
    company_id   INT(11)          NOT NULL ,
    org_id       INT(11)          NOT NULL DEFAULT 0,
    store_id     INT(11)          NOT NULL DEFAULT 0,
    classifies   VARCHAR(128)     NOT NULL,
    use_scopes   VARCHAR(64)      NOT NULL DEFAULT '1',
    expire_date  DATETIME         NULL     DEFAULT NULL,
    is_default   TINYINT UNSIGNED NOT NULL DEFAULT 0,
    temp_title   VARCHAR(128)     NOT NULL DEFAULT '-1',
    temp_context VARCHAR(512)     NOT NULL,
    enabled      TINYINT UNSIGNED NOT NULL DEFAULT 0,
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


-- 门店透视图
DROP TABLE IF EXISTS STORE_TREE_VIEW;
CREATE TABLE STORE_TREE_VIEW
(
    id           CHAR(36)         NOT NULL,
    parentId     CHAR(36)         NULL     DEFAULT NULL,
    nodeName     VARCHAR(255)     NOT NULL,
    tree_type    TINYINT UNSIGNED NOT NULL,
    owner_id     INT(11)          NULL     DEFAULT NULL,
    storeIds     TEXT             NULL     DEFAULT NULL,
    store_info   TEXT             NULL     DEFAULT NULL,
    company_id   INT(11)          NULL     DEFAULT NULL,
    node_desc    VARCHAR(255)     NULL     DEFAULT NULL,
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

-- 透视图结构
DROP TABLE IF EXISTS STORE_TREE_VIEW_STRUCTURE;
CREATE TABLE STORE_TREE_VIEW_STRUCTURE
(
    id           int(11) AUTO_INCREMENT NOT NULL,
    node_id      CHAR(36)               NOT NULL,
    owner_id     INT(11)                NOT NULL,
    company_id   INT(11)                NOT NULL,
    node_path    VARCHAR(255)           NOT NULL,
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