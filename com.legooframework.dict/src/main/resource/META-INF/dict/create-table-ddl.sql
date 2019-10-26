-- KV 简单数据字段存储表
DROP TABLE IF EXISTS dict_kv_data;
CREATE TABLE dict_kv_data (
	id INT NOT NULL AUTO_INCREMENT,
	dict_type VARCHAR(32) NOT NULL,
	field_value VARCHAR(32) NOT NULL,
	field_name VARCHAR(128) NULL DEFAULT NULL,
	field_index INT(8) NOT NULL,
	field_desc TEXT NULL DEFAULT NULL,
	delete_flag TINYINT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NOT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

RENAME TABLE dict_kv_data TO DICT_KV_DATA;
