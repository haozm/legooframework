-- 朋友圈权限设定
DROP TABLE IF EXISTS WECHAT_CIRCLE_SYNCCYCLE;
CREATE TABLE WECHAT_CIRCLE_SYNCCYCLE
(
  id          VARCHAR(64)      NOT NULL,
  sync_type   TINYINT UNSIGNED NOT NULL,
  start_time  BIGINT(20)       NOT NULL,
  last_time   BIGINT(20)       NOT NULL,
  start_date  DATETIME         NULL,
  last_date   DATETIME         NULL,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL DEFAULT 0,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, sync_type)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 朋友圈权限设定
DROP TABLE IF EXISTS WECHAT_CIRCLE_PERMISSION;
CREATE TABLE WECHAT_CIRCLE_PERMISSION
(
  weixin_id   VARCHAR(64)      NOT NULL,
  permission  INT(4) UNSIGNED  NOT NULL DEFAULT 0,
  block_list  VARCHAR(1024)    NULL,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL DEFAULT 0,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (weixin_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 朋友圈内容
DROP TABLE IF EXISTS WECHAT_CIRCLE_CONTENT;
CREATE TABLE WECHAT_CIRCLE_CONTENT
(
  id             BIGINT(20)       NOT NULL,
  weixin_id      VARCHAR(64)      NOT NULL,
  circle_id      VARCHAR(64)      NOT NULL,
  circle_type    TINYINT UNSIGNED NOT NULL DEFAULT 1,
  image_num      TINYINT UNSIGNED NOT NULL DEFAULT 0,
  title          VARCHAR(1024)    NULL,
  url            VARCHAR(2048)    NULL,
  sub_url        VARCHAR(2048)    NULL,
  send_time      BIGINT(20)       NOT NULL,
  message        TEXT             NULL,
  description    TEXT             NULL,
  read_status    VARCHAR(128)     NULL,
  sources_from   VARCHAR(128)     NOT NULL,
  source_wx_ids  VARCHAR(128)     NOT NULL,
  source_com_ids VARCHAR(128)     NOT NULL,
  source_sto_ids VARCHAR(128)     NOT NULL,
  delete_flag    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id      BIGINT(20)       NOT NULL DEFAULT 0,
  creator        BIGINT(20)       NOT NULL DEFAULT -1,
  createTime     DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor         BIGINT(20)       NULL     DEFAULT NULL,
  editTime       DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, weixin_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE INDEX WECHAT_CIRCLE_CONTENT_weixin_id_IDX USING BTREE ON CSOSM_WECHAT_CIRCLE.WECHAT_CIRCLE_CONTENT (weixin_id, circle_id);
CREATE INDEX WECHAT_CIRCLE_CONTENT_sources_from_IDX USING BTREE ON CSOSM_WECHAT_CIRCLE.WECHAT_CIRCLE_CONTENT (sources_from, source_wx_ids, source_com_ids, source_sto_ids);

-- 朋友圈图片信息记录
DROP TABLE IF EXISTS WECHAT_CIRCLE_IMAGES;
CREATE TABLE WECHAT_CIRCLE_IMAGES
(
  id          VARCHAR(64)      NOT NULL,
  owner_id    BIGINT(20)       NOT NULL,
  circle_id   VARCHAR(64)      NOT NULL,
  url         VARCHAR(2048)    NULL,
  sub_url     VARCHAR(2048)    NULL,
  img_order   TINYINT UNSIGNED NOT NULL DEFAULT 0,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL DEFAULT 0,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, circle_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE INDEX WECHAT_CIRCLE_IMAGES_circle_id_IDX USING BTREE ON CSOSM_WECHAT_CIRCLE.WECHAT_CIRCLE_IMAGES (circle_id, owner_id);

-- 朋友圈评论
DROP TABLE IF EXISTS WECHAT_CIRCLE_COMMENT;
CREATE TABLE WECHAT_CIRCLE_COMMENT
(
  id                INT(11)          NOT NULL,
  circle_id         BIGINT(20)       NOT NULL,
  weixin_id         VARCHAR(64)      NOT NULL,
  comment_type      TINYINT UNSIGNED NOT NULL,
  reading_mark      TINYINT UNSIGNED NOT NULL DEFAULT 1,
  comment_wx_id     VARCHAR(64)      NOT NULL,
  comment_wx_name   VARCHAR(256)     NULL,
  comment_ref_id    INT(11)          NULL,
  comment_ref_wx_id VARCHAR(64)      NULL,
  message           TEXT             NULL,
  comment_time      BIGINT(20)       NOT NULL,
  read_status       VARCHAR(128)     NULL,
  sources_from      VARCHAR(128)     NOT NULL,
  source_wx_ids     VARCHAR(128)     NOT NULL,
  source_com_ids    VARCHAR(128)     NOT NULL,
  source_sto_ids    VARCHAR(128)     NOT NULL,
  delete_flag       TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id         BIGINT(20)       NOT NULL DEFAULT 0,
  creator           BIGINT(20)       NOT NULL DEFAULT -1,
  createTime        DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor            BIGINT(20)       NULL     DEFAULT NULL,
  editTime          DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id, circle_id, weixin_id, comment_type, comment_wx_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

CREATE INDEX WECHAT_CIRCLE_COMMENT_circle_id_IDX USING BTREE ON CSOSM_WECHAT_CIRCLE.WECHAT_CIRCLE_COMMENT (circle_id, weixin_id);
CREATE INDEX WECHAT_CIRCLE_COMMENT_read_status_IDX USING BTREE ON CSOSM_WECHAT_CIRCLE.WECHAT_CIRCLE_COMMENT (read_status);

-- 朋友圈参数设定
DROP TABLE IF EXISTS WECHAT_CIRCLE_SETTING;
CREATE TABLE WECHAT_CIRCLE_SETTING
(
  id          BIGINT(20)       NOT NULL AUTO_INCREMENT,
  company_id  TINYINT UNSIGNED NOT NULL DEFAULT 0,
  store_id    TINYINT UNSIGNED NOT NULL DEFAULT 0,
  device_id   VARCHAR(64)      NULL,
  page_size   TINYINT UNSIGNED NOT NULL DEFAULT 3,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NOT NULL DEFAULT 0,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;