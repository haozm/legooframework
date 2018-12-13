-- 机构表
DROP TABLE IF EXISTS org_base_info;
CREATE TABLE org_base_info (
	id BIGINT(20) NOT NULL,
	org_code VARCHAR(64) NOT NULL,
	org_type INT(1) NOT NULL DEFAULT 0,
	full_name VARCHAR(256) ,
	short_name VARCHAR(256) ,
	business_license VARCHAR(256),
	detail_address VARCHAR(256),
	legal_person VARCHAR(64),
	contact_number VARCHAR(32),
	org_remark TEXT,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

DELETE FROM org_base_info WHERE id='COM-CSOSM-000001';
INSERT INTO csosm_main.org_base_info
(id, org_code, org_type, full_name, short_name, business_license, detail_address, legal_person, contact_number, org_remark,  tenant_id, creator, createTime)
VALUES(100000000, 'CSOSM-001',0, '广州羿聊有限公司', '羿聊公司', 'GZ1988791728937213', '广州海珠区琶洲', 'Admin', '1876658127', '测试公司', 100000000, 100000000, NOW());

-- 门店附加表
DROP TABLE IF EXISTS org_store_info;
CREATE TABLE org_store_info (
	id BIGINT(20) NOT NULL,
	store_status INT(1) NOT NULL DEFAULT '1',
	store_type VARCHAR(16) NOT NULL DEFAULT '0000',
	type_dict VARCHAR(16) NOT NULL DEFAULT 'STORETYPE',
	status_dict VARCHAR(16) NOT NULL DEFAULT 'STORESTATUS',
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

-- 门店透视图（含组织结构）
DROP TABLE IF EXISTS org_store_tree;
CREATE TABLE org_store_tree (
  id BIGINT(20) NOT NULL,
  parentId BIGINT(20) DEFAULT NULL,
  node_name varchar(256) NOT NULL,
  node_type INT(1) NOT NULL DEFAULT 0,
  tree_type INT(2) NOT NULL,
  node_seq INT(2) NOT NULL DEFAULT 0,
  children text,
  node_desc varchar(512) DEFAULT NULL,
  delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

INSERT INTO org_store_tree
            (id, parentId, node_name, node_type, tree_type, node_seq,children, node_desc, delete_flag, tenant_id, creator, createTime)
     VALUES (1000000, 1000000, '羿聊公司', 0, 0, 0, null, '羿聊公司', 0, 100000000, 100000000, NOW());

-- 系统数据权限透视图 data permission
DROP TABLE IF EXISTS org_store_permission;
CREATE TABLE org_store_permission (
  id BIGINT(20) NOT NULL,
  employee_id BIGINT(20) DEFAULT NULL,
  org_ids TEXT DEFAULT NULL,
  store_ids TEXT DEFAULT NULL,
  delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

-- 门店所属公司表
DROP TABLE IF EXISTS org_store_map_company;
CREATE TABLE org_store_map_company (
  id INT NOT NULL AUTO_INCREMENT,
	company_id BIGINT(20) NOT NULL,
	store_id BIGINT(20) NOT NULL,
	enabled INT(1) NOT NULL DEFAULT 1,
	join_date DATETIME NOT NULL,
	unjoin_date DATETIME,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

DELETE FROM dict_kv_data WHERE dict_type='STORETYPE';
INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORETYPE', '0000', '直营店', 0, '直营店', 100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORETYPE', '0001', '加盟店', 1, '加盟店', 100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORETYPE', '0002', '联体店', 2, '联体店', 100000000,100000000, NOW());

DELETE FROM dict_kv_data WHERE dict_type='STORESTATUS';
INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORESTATUS', '1', '营业', 0, '正常营业',100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORESTATUS', '2', '暂停', 1, '暂停营业', 100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'STORESTATUS', '3', '关店', 2, '关店', 100000000,100000000, NOW());

-- 公司设备映射表
DROP TABLE IF EXISTS org_device_info;
CREATE TABLE org_device_info (
	id VARCHAR(36) NOT NULL,
	device_enbaled INT(1) NOT NULL DEFAULT 1,
	device_type CHAR(1) NOT NULL,
	device_type_dict VARCHAR(32) NOT NULL DEFAULT 'DEVICETYPE',
	remark VARCHAR(512) DEFAULT NULL,
	activate_date DATETIME NULL DEFAULT NULL,
	company_id BIGINT(20) NOT NULL,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id,company_id)
)
ENGINE=InnoDB;

DELETE FROM dict_kv_data WHERE dict_type='DEVICETYPE';
INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'DEVICETYPE', 'X', '主机', 1, '羿聊主机',100000000, 100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES( 'DEVICETYPE', 'A', 'APP机', 2, '羿聊APP机', 100000000, 100000000, NOW());

-- 门店设备映射表
DROP TABLE IF EXISTS org_store_map_device;
CREATE TABLE org_store_map_device (
	id INT NOT NULL AUTO_INCREMENT,
	store_id  BIGINT(20) NOT NULL,
	device_id VARCHAR(36) NOT NULL,
	main_flag INT(1) NOT NULL DEFAULT 0,
	effective_flag INT(1) NOT NULL DEFAULT 1,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

-- 门店微信映射表
DROP TABLE IF EXISTS org_store_map_wechat;
CREATE TABLE org_store_map_wechat (
  id INT NOT NULL AUTO_INCREMENT,
	store_id BIGINT(20) NOT NULL,
	wechat_id VARCHAR(64) NOT NULL,
	webchat_use INT(1) DEFAULT NULL,
	effective_flag INT(1) NOT NULL DEFAULT 1,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

CREATE UNIQUE INDEX org_store_map_wechat_id_IDX USING BTREE ON org_store_map_wechat (store_id,wechat_id);

-- 职员表
DROP TABLE IF EXISTS org_employee_info;
CREATE TABLE org_employee_info (
	id BIGINT(20) NOT NULL,
	work_no VARCHAR(64) DEFAULT NULL,
	account_id BIGINT(20),
	user_name VARCHAR(64) DEFAULT NULL,
	birthday DATETIME DEFAULT NULL,
	com_workstatus CHAR(1) DEFAULT NULL,
	com_workstatus_dict VARCHAR(32) DEFAULT 'WORKSTATUS',
	user_sex CHAR(1) DEFAULT NULL,
	sex_dict VARCHAR(32) DEFAULT 'SEX',
	phone_no VARCHAR(64) DEFAULT NULL,
	remark VARCHAR(512) DEFAULT NULL,
	employee_time DATETIME DEFAULT NULL,
	org_id BIGINT(20) NOT NULL,
	company_id BIGINT(20) NOT NULL,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

CREATE UNIQUE INDEX org_employee_info_work_no_IDX USING BTREE ON org_employee_info (work_no) ;


DELETE FROM dict_kv_data WHERE dict_type='WORKSTATUS';
INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES('WORKSTATUS', '1', '在职', 0, '在职', 100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES('WORKSTATUS', '0', '离职', 1, '离职', 100000000,100000000, NOW());

INSERT INTO dict_kv_data ( dict_type, field_value, field_name, field_index, field_desc, tenant_id, creator, createTime)
VALUES('WORKSTATUS', '2', '休假', 2, '休假', 100000000,100000000, NOW());

-- 门店职员映射表
DROP TABLE IF EXISTS org_store_map_employee;
CREATE TABLE org_store_map_employee (
  id INT NOT NULL AUTO_INCREMENT,
	store_id BIGINT(20) NOT NULL,
	employee_id BIGINT(20) NOT NULL,
	effective_flag INT(1) NOT NULL DEFAULT 1,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

-- 设备关联关系表
DROP TABLE IF EXISTS org_link_device_info;
CREATE TABLE org_link_device_info (
  id INT NOT NULL AUTO_INCREMENT,
  wechat_id VARCHAR(64) NULL,
  employee_id BIGINT(20) NULL DEFAULT -1,
  device_imie VARCHAR(128) NULL,
	store_id BIGINT(20) NOT NULL,
	effective_flag INT(1) NOT NULL DEFAULT 1,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

DROP TABLE IF EXISTS wechat_group_info;
CREATE TABLE wechat_group_info (
	id CHAR(36) NOT NULL,
	group_name VARCHAR(128) DEFAULT NULL,
	store_id BIGINT(20) NOT NULL,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor  BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

DROP TABLE IF EXISTS wechat_group_map_friend;
CREATE TABLE wechat_group_map_friend (
	id INT NOT NULL AUTO_INCREMENT,
	group_id VARCHAR(64) DEFAULT NULL,
	weixin_id VARCHAR(64) NOT NULL,
	weixin_username VARCHAR(128) NOT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

DROP TABLE IF EXISTS wechat_group_map_employee;
CREATE TABLE wechat_group_map_employee (
	id INT NOT NULL AUTO_INCREMENT,
	employee_id VARCHAR(32) DEFAULT NULL,
	group_id VARCHAR(64) NOT NULL,
	type INT(1) DEFAULT 0 NOT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	PRIMARY KEY (id)
)
ENGINE=InnoDB;

