-- 登陆用户内容存储表
DROP TABLE IF EXISTS web_login_tokens;
CREATE TABLE web_login_tokens (
	id CHAR(32) NOT NULL,
	login_status INT(1) NOT NULL DEFAULT 1,
	account_id BIGINT(20) NOT NULL,
	device_id VARCHAR(256) NOT NULL,
	login_host VARCHAR(32) NOT NULL,
	login_time DATETIME NOT NULL,
	last_time DATETIME NOT NULL,
	logout_time DATETIME NULL DEFAULT NULL,
	full_token VARCHAR(512) NOT NULL,
	delete_flag INT(1) NOT NULL DEFAULT 0,
	tenant_id BIGINT(20) NULL DEFAULT NULL,
	creator BIGINT(20) NOT NULL,
	createTime DATETIME NOT NULL,
	editor BIGINT(20) NULL DEFAULT NULL,
	editTime DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (id)
)
COLLATE='utf8mb4'
ENGINE=InnoDB;