-- 模板使用分类树
DROP TABLE IF EXISTS MSG_TEMPLATE_CLASSIFY;
CREATE TABLE MSG_TEMPLATE_CLASSIFY (
  id CHAR(4) NOT NULL,
  pid CHAR(4) NOT NULL,
  classify VARCHAR(64) NOT NULL,
  deep_path VARCHAR(128) NOT NULL,
  company_id INT(11) NOT NULL,
  delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
  creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id,company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2000','0000','节日服务','0000-2000',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('3000','0000','90服务','0000-3000',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('4000','0000','感动服务','0000-4000',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('9000','0000','其他模板','0000-9000',:compayId,:compayId,-1,NOW());


INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2001','2000','春节','0000-2000-2001',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2002','2000','元宵节','0000-2000-2002',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2003','2000','三八妇女节','0000-2000-2003',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2004','2000','五四青年节','0000-2000-2004',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2005','2000','六一儿童节','0000-2000-2005',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2007','2000','国庆节童节','0000-2000-2007',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2010','2000','圣诞节','0000-2000-2010',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2011','2000','元旦节','0000-2000-2011',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2012','2000','情人节','0000-2000-2012',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2013','2000','父亲节','0000-2000-2013',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2014','2000','母亲节','0000-2000-2014',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('2015','2000','教师节','0000-2000-2015',:compayId,:compayId,-1,NOW());

INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('4001','4000','生日祝福','0000-4000-4001',:compayId,:compayId,-1,NOW());

INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('9001','9000','双十一促销节','0000-9000-9001',:compayId,:compayId,-1,NOW());
INSERT INTO MSGTEMPLATE_TREE_CLASSIFY (id,pid,classify,deep_path,company_id,tenant_id,creator,createTime)
VALUES ('9002','9000','双十二促销节','0000-9000-9002',:compayId,:compayId,-1,NOW());

-- 短信模板内容
DROP TABLE IF EXISTS MSG_TEMPLATE_CONTEXT;
CREATE TABLE MSG_TEMPLATE_CONTEXT (
  id CHAR(36) NOT NULL,
  company_id INT(18) NOT NULL,
  org_id INT(18) NOT NULL,
  store_id INT(18) NOT NULL,
  classifies VARCHAR(256) NOT NULL,
  use_scopes VARCHAR(8) NOT NULL,
  expire_date DATETIME DEFAULT NULL,
  enbaled     TINYINT UNSIGNED  NOT NULL DEFAULT 1,
  delete_flag TINYINT UNSIGNED NOT NULL DEFAULT 0,
  tenant_id   BIGINT(20)       NULL     DEFAULT 100000,
  creator     BIGINT(20)       NOT NULL DEFAULT -1,
  createTime  DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
  editor      BIGINT(20)       NULL     DEFAULT NULL,
  editTime    DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id,company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
