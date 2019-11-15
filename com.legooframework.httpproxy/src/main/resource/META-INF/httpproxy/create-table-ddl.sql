-- 网关访问量统计等
DROP TABLE IF EXISTS PROXY_NET_FUSE;
CREATE TABLE PROXY_NET_FUSE
(
    id              BIGINT(20)       NOT NULL AUTO_INCREMENT,
    module_name     VARCHAR(64)      NOT NULL,
    req_path        VARCHAR(255)     NOT NULL,
    fuse_time       VARCHAR(64)      NOT NULL,
    req_query       VARCHAR(255)     NOT NULL,
    fuse_count      INT(11)          NOT NULL DEFAULT 0,
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

