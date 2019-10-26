-- 发送账号管理明细表
DROP TABLE IF EXISTS SMS_SUPPLIER_SUB_ACCOUNT;
CREATE TABLE SMS_SUPPLIER_SUB_ACCOUNT
(
    id              INT              NOT NULL AUTO_INCREMENT,
    supplier_id     VARCHAR(32)      NOT NULL,
    username        VARCHAR(64)      NOT NULL,
    password        VARCHAR(256)     NOT NULL,
    apikey          VARCHAR(512)     NOT NULL,
    sms_channel     TINYINT UNSIGNED NOT NULL,
    sms_suffix      VARCHAR(64)      NULL DEFAULT NUll,
    http_send_url   VARCHAR(512)     NOT NULL,
    http_status_url VARCHAR(512)     NOT NULL,
    http_replay_url VARCHAR(512)     NULL,
    enabled         TINYINT UNSIGNED NOT NULL DEFAULT 1,
    delete_flag     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id       BIGINT(20)       NULL     DEFAULT NULL,
    creator         BIGINT(20)       NOT NULL DEFAULT -1,
    createTime      DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor          BIGINT(20)       NULL     DEFAULT NULL,
    editTime        DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
)
    DEFAULT CHARSET = utf8mb4
    COLLATE = 'utf8mb4_general_ci'
    ENGINE = InnoDB;

-- 发送账号管理明细表
DROP TABLE IF EXISTS SMS_SUPPLIER_INFO;
CREATE TABLE SMS_SUPPLIER_INFO
(
    id            VARCHAR(5)       NOT NULL,
    supplier_name VARCHAR(64)      NOT NULL,
    enabled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
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

INSERT INTO SMS_SUPPLIER_INFO (id, supplier_name, enabled, tenant_id)
VALUES ('00001', '美联软通', 1, -1);

-- 初始化数据
INSERT INTO SMS_SUPPLIER_SUB_ACCOUNT
(supplier_id, username, password, apikey, sms_channel, http_send_url, http_status_url, http_replay_url, tenant_id,
 creator, createTime)
VALUES ('00001', 'yiyuanxx', 'Yy32189', '811d62ead7ff783efb6effd05f07a524', 1,
        'http://m.5c.com.cn/api/send/', 'http://m.5c.com.cn/api/recv/index.php',
        'http://m.5c.com.cn/api/reply/index.php',
        -1, -1, CURRENT_TIMESTAMP);
INSERT INTO SMS_SUPPLIER_SUB_ACCOUNT
(supplier_id, username, password, apikey, sms_channel, sms_suffix, http_send_url, http_status_url, http_replay_url, tenant_id,
 creator, createTime)
VALUES ('00001', 'yiyuanxxyx', 'Yy32189', '1c9f7d1514bc7518598ae9a16d5ee9e3', 2,
        '退订回T', 'http://m.5c.com.cn/api/send/', 'http://m.5c.com.cn/api/recv/index.php',
        'http://m.5c.com.cn/api/reply/index.php',
        -1, -1, CURRENT_TIMESTAMP);
