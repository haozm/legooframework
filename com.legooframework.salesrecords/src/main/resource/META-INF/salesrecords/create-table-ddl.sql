CREATE INDEX crm_salerecord_updateTime_IDX USING BTREE ON acp.crm_salerecord (updateTime);
ALTER TABLE `acp`.`crm_salerecord` CHANGE `updatetime` `updatetime` TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
UPDATE acp.crm_salerecord SET updatetime = createTime WHERE updatetime IS NULL;