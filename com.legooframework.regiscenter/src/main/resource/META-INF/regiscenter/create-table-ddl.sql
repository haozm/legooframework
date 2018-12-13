-- 设备激活码
DROP TABLE IF EXISTS device_pin_code;
CREATE TABLE device_pin_code (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  company_id BIGINT(20) NOT NULL DEFAULT 0,
  pin_code VARCHAR(32) NOT NULL,
  pin_enabled INT(1) NOT NULL DEFAULT 1,
  device_id VARCHAR(128) DEFAULT NULL,
  deadline datetime DEFAULT NULL,
  bind_date datetime DEFAULT NULL,
  delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
  creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id)
) COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

DROP TABLE IF EXISTS tenant_net_config;
CREATE TABLE tenant_net_config (
  id BIGINT(20) NOT NULL AUTO_INCREMENT,
  company_id BIGINT(20) NOT NULL,
  web_domain VARCHAR(128) NOT NULL,
  web_port INT(5) NOT NULL DEFAULT 80,
  delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
  creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (id)
) COLLATE='utf8_general_ci'
ENGINE=InnoDB;

INSERT INTO tenant_net_config (company_id, web_domain, web_port,  tenant_id, creator,createTime)
VALUES(100000000, '113.106.222.250', 9001, 100000000, -1,NOW());
