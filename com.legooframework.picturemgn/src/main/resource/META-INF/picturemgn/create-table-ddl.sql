/**
 * 创建图片标签基础表
 */
DROP TABLE IF EXISTS LABEL_BASE_INFO;
CREATE TABLE LABEL_BASE_INFO(
	id bigint(20) NOT NULL,
	pid bigint(20) NOT NULL,
  label_name varchar(512) NOT NULL,
	label_desc varchar(512) DEFAULT NULL,
	label_ctx varchar(512) DEFAULT NULL,
	label_enable int(1) NOT NULL DEFAULT 1,
	store_id int(11) NOT NULL DEFAULT -1,
	company_id int(11) NOT NULL DEFAULT -1,
	delete_flag int(1) NOT NULL DEFAULT '0',
  tenant_id bigint(20) DEFAULT NULL,
  creator bigint(20) NOT NULL,
  createTime datetime NOT NULL,
  editor bigint(20) DEFAULT NULL,
  editTime datetime DEFAULT NULL,
	PRIMARY KEY (id,store_id,company_id)
)DEFAULT CHARSET=utf8mb4 COLLATE ='utf8mb4_general_ci'
ENGINE=InnoDB 

DROP TABLE IF EXISTS PICTURE_BASE_INFO;
CREATE TABLE PICTURE_BASE_INFO(
	id CHAR(32) NOT NULL,
	url varchar(512) NOT NULL,
  thumbnailUrl varchar(512) DEFAULT NULL,
	size bigint(20) NOT NULL,
	description varchar(128) DEFAULT NULL,
	label_ids varchar(512) DEFAULT NULL,
	delete_flag int(1) NOT NULL DEFAULT 0,
	recovery_flag int(1) NOT NULL DEFAULT 0,
  tenant_id bigint(20) DEFAULT NULL,
  creator bigint(20) NOT NULL,
  createTime datetime NOT NULL,
  editor bigint(20) DEFAULT NULL,
  editTime datetime DEFAULT NULL,
	PRIMARY KEY (id)
)DEFAULT CHARSET=utf8mb4 COLLATE ='utf8mb4_general_ci'
ENGINE=InnoDB 

DROP TABLE IF EXISTS MEMBER_PICTURE_INFO;
CREATE TABLE MEMBER_PICTURE_INFO(
	member_id int(11) NOT NULL,
  picture_id char(32) NOT NULL,
	upload_emp int(11) DEFAULT NULL,
	upload_time datetime DEFAULT NULL,
	store_id int(11)  DEFAULT NULL,
	company_id int(11) DEFAULT NULL,
  tenant_id bigint(20) DEFAULT NULL,
  creator bigint(20) NOT NULL,
  createTime datetime NOT NULL,
  editor bigint(20) DEFAULT NULL,
  editTime datetime DEFAULT NULL,
	PRIMARY KEY (member_id,picture_id)
)DEFAULT CHARSET=utf8mb4 COLLATE ='utf8mb4_general_ci'
ENGINE=InnoDB 