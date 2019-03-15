-- KV 职员信息表
DROP TABLE IF EXISTS acp_employee;
CREATE TABLE acp_employee (
	id int(11) AUTO_INCREMENT NOT NULL,
	birthday datetime NULL,
	certificate varchar(255)  NULL,
	certificateType int(11) NULL,
	employeeType int(11) NULL,
	loginuser_id int(11) NULL,
	password varchar(255)  NULL,
	remark varchar(255)  NULL,
	deviceId varchar(255)  NULL,
	sex int(11) NULL,
	organization_id int(11) NULL,
	phone varchar(255)  NULL,
	agingTime datetime NULL,
	name varchar(255)  NULL,
	source_from int(11) NULL,
	source_channel varchar(50)  NULL,
	store_id int(11) NULL,
	employeeState int(11) NULL,
	status int(11) NULL,
	oldEmployeeId varchar(50)  NULL,
	company_id int(11) NULL,
	telephone varchar(255)  NULL,
	oldStoreId varchar(255)  NULL,
	loginTimeE int(11) NULL,
	loginTimeS int(11) NULL,
	crc32id varchar(255)  NULL,
	createUser_id int(11) NULL,
	createTime datetime NULL,
	updateUser_id int(11) NULL,
	updatetime timestamp DEFAULT CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP NOT NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

ALTER TABLE csosm_crm.acp_employee ADD login_name varchar(255) NULL;
ALTER TABLE csosm_crm.acp_employee ADD pass_word varchar(255) NULL;

UPDATE csosm_crm.acp_employee
INNER JOIN ( SELECT id AS 'id',name AS 'name',password as 'pwd' FROM acp.saas_usertemplate ) b ON csosm_crm.acp_employee.loginuser_id = b.id
       SET acp_employee.login_name = b.name , acp_employee.pass_word = b.pwd ;

INSERT INTO acp_organization
            (id, code, createTime, `depth`, name, orgType, shortName, status, createUser_id)
     VALUES (-1, '-1', NOW(),       0,      '公司',  1,     'YGKJ',     1,     -1);
INSERT INTO acp_employee
           ( employeeType, createTime, remark, createUser_id,   name, company_id, employeeState, status,  login_name, pass_word, role_ids)
    VALUES ( 0,  NOW(), 'DBA',   -1,  '运维管理员', -1,  1, 1, 'Administrator', '6a647a2f4f6f4430474a7968796491f9db0106e8344355e4', '2');

-- 系统角色表
DROP TABLE IF EXISTS acp_role;
CREATE TABLE acp_role (
	id INT(11) NOT NULL,
	role_name VARCHAR(256) NOT NULL,
	role_desc VARCHAR(512) NOT NULL,
	enbaled INT(1) NOT NULL DEFAULT 1,
	priority INT(2) NOT NULL DEFAULT 0,
	resources VARCHAR(512),
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NOT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id,tenant_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- init fixed data
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(1, 'AdminRole', '注册人', 1, 90, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(2, 'ManagerRole', '管理员', 1, 80, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(3, 'BossRole', '公司老板', 1, 70, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(4, 'AreaManagerRole', '督导', 1, 60, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(5, 'StoreManagerRole', '店长', 1, 50, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(7, 'ShoppingGuideRole', '导购', 1, 40, null, companyId , -1, NOW());
INSERT INTO csosm_crm.acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES(11, 'StoreMemberRole', '门店会员', 1, 10, null, companyId , -1, NOW());

-- 组织需要数据字典
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator, createTime)
VALUES ('INDUSTRYTYPE', '1', '女装', 0, NULL, 0, -1, -1, NOW()),
  ('INDUSTRYTYPE', '2', '女士内衣', 1, NULL, 0, -1, -1, NOW()),
  ('INDUSTRYTYPE', '3', '童装', 2, NULL, 0, -1, -1, NOW() );
-- 门店类型
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator, createTime)
VALUES ('STORETYPE', '1', '直营店', 0, NULL, 0, -1, -1, NOW()),
  ('STORETYPE', '2', '联体店', 1, NULL, 0, -1, -1, NOW()),
  ('STORETYPE', '3', '加盟店', 2, NULL, 0, -1, -1, NOW());
-- 待定初始化

DROP TABLE IF EXISTS acp_systemlog;
CREATE TABLE acp_systemlog (
  id INT(18) NOT NULL AUTO_INCREMENT,
  crud CHAR(1) NOT NULL,
  operation VARCHAR(255) DEFAULT NULL,
  sub_type VARCHAR(255) DEFAULT NULL,
  message text DEFAULT NULL,
  creator     INT(18)    NOT NULL DEFAULT -1,
  company_id  INT(19) NOT NULL DEFAULT -1,
  createTime  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 门店透视图
DROP TABLE IF EXISTS acp_store_view;
CREATE TABLE acp_store_view (
  id           CHAR(36)     NOT NULL,
  parentId     CHAR(36) NULL DEFAULT NULL,
  nodeName     VARCHAR(255) NOT NULL,
  tree_type    TINYINT UNSIGNED NOT NULL,
  owner_id     INT(11) NULL DEFAULT NULL,
  storeIds     TEXT NULL DEFAULT NULL,
  store_info   TEXT NULL DEFAULT NULL,
  company_id   INT(11) NULL DEFAULT NULL,
  node_desc    VARCHAR(255) NULL DEFAULT NULL,
  createUserId INT(11) NULL DEFAULT NULL,
  createTime   DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  modifyUserId INT(11) NULL DEFAULT NULL,
  modifyTime   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 透视图结构
DROP TABLE IF EXISTS acp_store_view_structure;
CREATE TABLE acp_store_view_structure (
  id int(11) AUTO_INCREMENT NOT NULL,
  node_id     CHAR(36) NOT NULL,
  owner_id     INT(11) NOT NULL,
  company_id   INT(11) NOT NULL,
  node_path    VARCHAR(255) NOT NULL,
  createUserId INT(11) NULL DEFAULT NULL,
  createTime   DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  modifyUserId INT(11) NULL DEFAULT NULL,
  modifyTime   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
-- 会员主表
  DROP TABLE IF EXISTS crm_member;
  CREATE TABLE crm_member (
  id int(11) NOT NULL AUTO_INCREMENT,
  birthday datetime DEFAULT NULL,
  calendarType int(11) DEFAULT NULL,
  certificate varchar(255) DEFAULT NULL,
  certificateType int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  detailAddress varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  jobType varchar(255) DEFAULT NULL,
  marryStatus int(11) DEFAULT NULL,
  memberType int(11) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  oldMemberCode varchar(255) DEFAULT NULL,
  oldShoppingGuideId varchar(255) DEFAULT NULL,
  oldStoreId varchar(255) DEFAULT NULL,
  phone varchar(255) DEFAULT NULL,
  photoFileName varchar(255) DEFAULT NULL,
  qqNum varchar(255) DEFAULT NULL,
  rechargeAmount decimal(19,2) DEFAULT NULL,
  remark varchar(255) DEFAULT NULL,
  serviceLevel int(11) DEFAULT NULL,
  sex int(11) DEFAULT NULL,
  source_channel varchar(255) DEFAULT NULL,
  source_from int(11) DEFAULT NULL,
  status int(11) DEFAULT '1',
  updateTime timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  weiboNum varchar(255) DEFAULT NULL,
  createUser_id int(11) DEFAULT NULL,
  memberCardType_id int(11) DEFAULT NULL,
  updateUser_id int(11) DEFAULT NULL,
  phone02 varchar(255) DEFAULT NULL,
  memberCardNum varchar(255) DEFAULT NULL,
  createStoreId int(11) DEFAULT NULL,
  likeContactTime int(11) DEFAULT NULL,
  validFlag int(11) DEFAULT NULL,
  namePinyin varchar(100) DEFAULT NULL,
  limitday int(11) DEFAULT NULL,
  lunarBirthday datetime DEFAULT NULL,
  iconUrl varchar(256) DEFAULT NULL,
  companypy varchar(255) DEFAULT NULL,
  crc32id varchar(255) DEFAULT NULL,
  totalScore int(11) DEFAULT NULL,
  company_ids varchar(255) DEFAULT NULL,
  company_id1 int(11) DEFAULT NULL,
  company_id2 int(11) DEFAULT NULL,
  company_id3 int(11) DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  -- 会员
  DROP TABLE IF EXISTS crm_member_consume_behavior;
  CREATE TABLE crm_member_consume_behavior (
  member_id int(11) NOT NULL UNIQUE,
  firstSaleRecordAmount decimal(19,2) DEFAULT NULL,
  firstSaleRecordNo varchar(255) DEFAULT NULL,
  lastVisitTime datetime DEFAULT NULL,
  rfm varchar(255) DEFAULT NULL,
  consumeTotalCount int(11) DEFAULT NULL,
  consumeTotalCountCurYear int(11) DEFAULT NULL,
  maxConsumePrice decimal(19,2) DEFAULT NULL,
  maxConsumePriceCurYear decimal(19,2) DEFAULT NULL,
  totalConsumeAmount decimal(19,2) DEFAULT NULL,
  totalConsumeAmountCurYear decimal(19,2) DEFAULT NULL,
  createUserId INT(11) NULL DEFAULT NULL,
  createTime   DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
  modifyUserId INT(11) NULL DEFAULT NULL,
  modifyTime   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
  -- 2019-03-07
  -- 会员基础表
DROP TABLE IF EXISTS crm_member;
CREATE TABLE crm_member (
  id int(11) NOT NULL AUTO_INCREMENT,
  birthday datetime DEFAULT NULL,
  calendarType int(11) DEFAULT NULL,
  certificate varchar(255) DEFAULT NULL,
  certificateType int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  detailAddress varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  memberType int(11) DEFAULT NULL,
  name varchar(255) DEFAULT NULL,
  oldMemberCode varchar(255) DEFAULT NULL,
  oldShoppingGuideId varchar(255) DEFAULT NULL,
  oldStoreId varchar(255) DEFAULT NULL,
  mobilePhone varchar(255) DEFAULT NULL,
  qqNum varchar(255) DEFAULT NULL,
  remarkIds varchar(255) DEFAULT NULL, //remark varchar(255) DEFAULT NULL,
  serviceLevel int(11) DEFAULT NULL,
  sex int(11) DEFAULT NULL,
  source_channel varchar(255) DEFAULT NULL,
  source_from int(11) DEFAULT NULL,
  status int(11) DEFAULT '1',
  updateTime timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  wechatNum varchar(255) DEFAULT NULL,
  weiboNum varchar(255) DEFAULT NULL,
  company_id1 int(11) DEFAULT NULL,
  company_id2 int(11) DEFAULT NULL,
  company_id3 int(11) DEFAULT NULL,
  createUser_id int(11) DEFAULT NULL,
  updateUser_id int(11) DEFAULT NULL,
  weixin_id varchar(255) DEFAULT NULL,//weixinUser_wx_user_id int(11) DEFAULT NULL,
  guide_id int(11) DEFAULT NULL,
  telephone varchar(255) DEFAULT NULL,
  effectiveFlag int(11) DEFAULT NULL,
  namePinyin varchar(100) DEFAULT NULL,
  lunarBirthday datetime DEFAULT NULL,
  iconUrl varchar(256) DEFAULT NULL,
  companypy varchar(255) DEFAULT NULL,
  unreachable int(11) DEFAULT NULL,
  awakenCount int(11) DEFAULT NULL,
  crc32id varchar(255) DEFAULT NULL,
  store_ids varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 会员附加信息表
  DROP TABLE IF EXISTS member_addition_info;
CREATE TABLE member_addition_info (
  member_id int(11) NOT NULL UNIQUE,
  carePeople varchar(255) DEFAULT NULL,
  characterType int(11) DEFAULT NULL,
  faithType int(11) DEFAULT NULL,
  hobby varchar(255) DEFAULT NULL,
  idols varchar(255) DEFAULT NULL,
  jobType varchar(255) DEFAULT NULL,
  likeBrand varchar(255) DEFAULT NULL,
  likeContact int(11) DEFAULT NULL,
  marryStatus int(11) DEFAULT NULL,
  specialDay varchar(255) DEFAULT NULL,
  zodiac int(11) DEFAULT NULL,
  education int(11) DEFAULT NULL,
  likeContactTime int(11) DEFAULT NULL,
  PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  -- 会员消费行为表
  DROP TABLE IF EXISTS member_consume_behavior;
 CREATE TABLE member_consume_behavior (
  member_id int(11) NOT NULL UNIQUE,  
  rechargeAmount decimal(19,2) DEFAULT NULL,
  lastVisitTime datetime DEFAULT NULL,
  rfm varchar(255) DEFAULT NULL,
  firstSaleRecordAmount decimal(19,2) DEFAULT NULL,
  firstSaleRecordNo varchar(255) DEFAULT NULL,
  consumeTotalCount int(11) DEFAULT NULL,
  consumeTotalCountCurYear int(11) DEFAULT NULL,
  maxConsumePrice decimal(19,2) DEFAULT NULL,
  maxConsumePriceCurYear decimal(19,2) DEFAULT NULL,
  totalConsumeAmount decimal(19,2) DEFAULT NULL,
  totalConsumeAmountCurYear decimal(19,2) DEFAULT NULL,
  PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  -- 会员卡信息表
  DROP TABLE IF EXISTS member_card_info;
 CREATE TABLE member_card_info (
  member_id int(11) NOT NULL UNIQUE,
  memberCardType_id int(11) DEFAULT NULL,
  memberCardNum varchar(255) DEFAULT NULL,
  createCardTime datetime DEFAULT NULL,
  createStoreId int(11) DEFAULT NULL,
  limitday int(11) DEFAULT NULL,
  PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
  DROP TABLE IF EXISTS crm_memberextrainfo;
CREATE TABLE crm_memberextrainfo (
  member_id int(11) NOT NULL UNIQUE,
  afterFork decimal(19,2) DEFAULT NULL,
  beforeFork decimal(19,2) DEFAULT NULL,
  bottomsSize varchar(255) DEFAULT NULL,
  braSize varchar(255) DEFAULT NULL,
  briefsSize varchar(255) DEFAULT NULL,
  chest decimal(19,2) DEFAULT NULL,
  clothingLong decimal(19,2) DEFAULT NULL,
  footLength decimal(19,2) DEFAULT NULL,
  hipline decimal(19,2) DEFAULT NULL,
  jacketSize varchar(255) DEFAULT NULL,
  kneeCircumference decimal(19,2) DEFAULT NULL,
  onChest decimal(19,2) DEFAULT NULL,
  outseam decimal(19,2) DEFAULT NULL,
  shoeSize varchar(255) DEFAULT NULL,
  shoulder decimal(19,2) DEFAULT NULL,
  sleeveLength decimal(19,2) DEFAULT NULL,
  status int(11) DEFAULT '1',
  thighCircumference decimal(19,2) DEFAULT NULL,
  trouserLeg decimal(19,2) DEFAULT NULL,
  underChest decimal(19,2) DEFAULT NULL,
  waistline decimal(19,2) DEFAULT NULL,
  member_id int(11) DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;