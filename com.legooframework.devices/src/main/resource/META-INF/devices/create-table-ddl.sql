DROP TABLE IF EXISTS device_base_info;
CREATE TABLE device_base_info (
	id 	CHAR(32) NOT NULL ,
	imei VARCHAR(128) NOT NULL ,
	name VARCHAR(128) NOT NULL,
	brand VARCHAR(128) DEFAULT NULL,
	model VARCHAR(128) DEFAULT NULL,
	color VARCHAR(64) DEFAULT NULL,
	cpu VARCHAR(64) DEFAULT NULL,
	memory_size INT(10) DEFAULT NULL,
	os VARCHAR(64) DEFAULT NULL,
	xport_os VARCHAR(64) DEFAULT NULL,
	screen_size DOUBLE(3,1) DEFAULT NULL,
	os_type INT(1) DEFAULT NULL,
	price DOUBLE(11,1) DEFAULT NULL,
	state INT(1) DEFAULT NULL,
	production_date DATETIME NULL DEFAULT NULL,
	reqair_reason VARCHAR(512) DEFAULT NULL,
	scrap_reason VARCHAR(512) DEFAULT NULL,
	createTime DATETIME NOT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

RENAME TABLE device_base_info TO DEVICE_BASE_INFO;