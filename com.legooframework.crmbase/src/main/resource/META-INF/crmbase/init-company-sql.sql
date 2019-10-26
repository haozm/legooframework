-- 初始化新公司信息

USE CSOSM_CRM_DB;
DELETE
FROM acp_role
WHERE tenant_id = :COMPANY_ID;

-- init fixed data
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (1, 'AdminRole', '注册人', 1, 90, null, :COMPANY_ID, -1, NOW());
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (3, 'BossRole', '总经理', 1, 70, null, :COMPANY_ID, -1, NOW());
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (4, 'AreaManagerRole', '经理', 1, 60, null, :COMPANY_ID, -1, NOW());
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (5, 'StoreManagerRole', '店长', 1, 50, null, :COMPANY_ID, -1, NOW());
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (7, 'ShoppingGuideRole', '导购', 1, 40, null, :COMPANY_ID, -1, NOW());
INSERT INTO acp_role (id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
    VALUES (11, 'StoreMemberRole', '门店会员', 1, 10, null, :COMPANY_ID, -1, NOW());

-- init dict
DELETE
FROM dict_kv_data
WHERE tenant_id = :COMPANY_ID;
-- 会员类型
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('MEMBERTYPE', '1', '粉丝会员', 0, NULL, 0, :COMPANY_ID, 1, NOW()),
    ('MEMBERTYPE', '2', '普通会员', 1, NULL, 0, :COMPANY_ID, 1, NOW());

-- 服务等级
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('SERVICELEVEL', '1', '粉丝服务', 0, NULL, 0, :COMPANY_ID, 1, NOW()),
    ('SERVICELEVEL', '2', '积分服务', 1, NULL, 0, :COMPANY_ID, 1, NOW()),
    ('SERVICELEVEL', '3', '储值服务', 2, NULL, 0, :COMPANY_ID, 1, NOW());

-- 性别
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('SEX', '2', '女性', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SEX', '1', '男性', 0, NULL, 0, :COMPANY_ID, -1, NOW());

-- 婚礼状况
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('MARRIAGETYPE', '0', '未婚', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('MARRIAGETYPE', '1', '已婚', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('MARRIAGETYPE', '2', '离异', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('MARRIAGETYPE', '3', '再婚', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('MARRIAGETYPE', '4', '丧偶', 4, NULL, 0, :COMPANY_ID, -1, NOW());

-- 星座类型
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('ZODIACTYPE', '0', '白羊座', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '1', '金牛座', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '2', '双子座', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '3', '巨蟹座', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '4', '狮子座', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '5', '处女座', 5, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '6', '天秤座', 6, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '7', '天蝎座', 7, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '8', '射手座', 8, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '9', '摩羯座', 9, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '10', '水瓶座', 10, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('ZODIACTYPE', '11', '双鱼座', 11, NULL, 0, :COMPANY_ID, -1, NOW());

-- 性格特征(characterType)
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('CHARACTERTYPE', '1', '视觉型', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('CHARACTERTYPE', '2', '听觉型', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('CHARACTERTYPE', '3', '感觉型', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('CHARACTERTYPE', '4', '混合型', 3, NULL, 0, :COMPANY_ID, -1, NOW());

-- 宗教信仰
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('RELIGIONTYPE', '1', '基督教', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('RELIGIONTYPE', '2', '佛教', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('RELIGIONTYPE', '3', '天主教', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('RELIGIONTYPE', '4', '伊斯兰教', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('RELIGIONTYPE', '5', '道教', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('RELIGIONTYPE', '99', '其它', 5, NULL, 0, :COMPANY_ID, -1, NOW());

-- 联系方式
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('LINKTYPE', '1', '电话', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('LINKTYPE', '2', '短信', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('LINKTYPE', '3', '微信', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('LINKTYPE', '4', 'QQ', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('LINKTYPE', '5', 'EMIAL', 4, NULL, 0, :COMPANY_ID, -1, NOW());

-- 上衣尺码
-- 上衣尺码
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_other, field_index, field_desc, delete_flag,
                          tenant_id, creator, createTime)
    VALUES ('UPCASESIZE', '150', 'XXS', '150', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '155', 'XS', '155', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '160', 'S', '160', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '165', 'M', '165', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '170', 'L', '170', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '175', 'XL', '175', 5, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '180', 'XXL', '180', 6, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '185', 'XXXL', '185', 7, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('UPCASESIZE', '190', 'XXXXL', '190', 8, NULL, 0, :COMPANY_ID, -1, NOW());

-- 下装尺码
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_other, field_index, field_desc, delete_flag,
                          tenant_id, creator, createTime)
    VALUES ('DOWNCASESIZE', '150-155', '26', '150-155', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '155-160', '28', '155-160', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '160-165', '30', '160-165', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '165-170', '31', '165-170', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '170-175', '32', '170-175', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '175-180', '33', '175-180', 5, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '180-185', '34', '180-185', 6, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '185-190', '36', '185-190', 7, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '190-195', '38', '190-195', 8, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('DOWNCASESIZE', '195+', '40', '195+', 9, NULL, 0, :COMPANY_ID, -1, NOW());

-- 内衣尺码
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('BASSIZE', '70A+', '70A', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70B', '70B', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70C', '70C', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70D', '70D', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70E', '70E', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70F', '70F', 5, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '70G', '70G', 6, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75A', '75A', 7, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75B', '75B', 8, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75C', '75C', 9, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75D', '75D', 10, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75E', '75E', 11, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75F', '75F', 12, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '75G', '75G', 13, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80A', '80A', 14, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80B', '80B', 15, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80C', '80C', 16, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80D', '80D', 17, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80E', '80E', 18, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80F', '80F', 19, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '80G', '80G', 20, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85A', '85A', 21, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85B', '85B', 22, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85C', '85C', 23, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85D', '85D', 24, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85E', '85E', 25, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85E', '85F', 26, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '85G', '85G', 27, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90A', '90A', 28, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90B', '90B', 29, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90C', '90C', 30, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90D', '90D', 31, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90E', '90E', 32, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90F', '90F', 33, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '90G', '90G', 34, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95A', '95A', 35, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95B', '95B', 36, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95C', '95C', 37, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95D', '95D', 38, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95D', '95E', 39, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95F', '95F', 40, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('BASSIZE', '95G', '95G', 41, NULL, 0, :COMPANY_ID, -1, NOW());

-- 内裤尺寸
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('INNERKUSIZE', 'S', 'S', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('INNERKUSIZE', 'M', 'M', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('INNERKUSIZE', 'L', 'L', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('INNERKUSIZE', 'XL', 'XL', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('INNERKUSIZE', 'XXL', 'XXL', 4, NULL, 0, :COMPANY_ID, -1, NOW());

-- 鞋子尺码
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
    VALUES ('SHOSESIZE', '34', '34', 0, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '35', '35', 1, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '36', '36', 2, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '37', '37', 3, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '38', '38', 4, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '39', '39', 5, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '40', '40', 6, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '41', '41', 7, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '42', '42', 8, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '43', '43', 9, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '44', '44', 10, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '45', '45', 11, NULL, 0, :COMPANY_ID, -1, NOW()),
    ('SHOSESIZE', '46', '46', 12, NULL, 0, :COMPANY_ID, -1, NOW());

-- 初始化短信模板相关
USE CSOSM_CRM_DB;
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('memberName', '会员姓名', 'STRING', '会员', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('memberPhone', '会员电话', 'STRING', '会员电话', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('storeName', '门店名称', 'STRING', '我门店', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('storeManager', '店长', 'STRING', '店长', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('storePhone', '门店电话', 'STRING', '门店电话', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('shoppingGuide', '导购姓名', 'STRING', '导购', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('lastName', '姓', 'STRING', '会员', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('sex', '性别', 'ENUM', '1=先生,2=女士,*=先生/女士', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('birthday', '生日日期', 'DATE', 'yyyy-MM-dd', :COMPANY_ID);
INSERT INTO MSG_TEMPLATE_REPLACE (field_tag, replace_token, token_type, default_value, tenant_id)
    VALUES ('companyName', '公司名称', 'STRING', null, :COMPANY_ID);

-- INIT RFM值
INSERT INTO crm_rfm_setting(company_id, org_id, store_id, r_v1, r_v2, r_v3, r_v4, f_v1, f_v2, f_v3, f_v4, m_v1, m_v2,
                            m_v3, m_v4, val_type, delete_flag, tenant_id, creator)
    VALUES (:COMPANY_ID, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 1, 0, :COMPANY_ID, -1);

-- 90任务初始化
INSERT INTO TASK_JOB_SWITCH (company_id, store_id, business_type, start_date, enbaled, tenant_id)
    VALUES (:COMPANY_ID, -1, 'TOUCHED90', ':START_DATE 00:00:01', 0, :COMPANY_ID);

-- 90规则初始化
INSERT INTO TASK_JOB_RULE
(company_id, store_id, business_type, enbaled, automatic, content, details, tenant_id, creator, createTime)
VALUES(:COMPANY_ID, -1, 'TOUCHED90', 1, 0, 'maxConsumptionDays=30,maxAmountOfconsumption=1000,concalBefore=false','delay=1h,expired=1d$delay=1d,expired=1d$delay=5d,expired=2d', :COMPANY_ID, -1, CURRENT_TIMESTAMP );

--  初始化公司配置
INSERT INTO SMS_CONFIG_SETTING (company_id, store_id, sms_prefix, delete_flag, tenant_id, creator, createTime)
    VALUES (:COMPANY_ID, -1, ':COMPANY_NAME', 0, :COMPANY_ID, -1, CURRENT_TIMESTAMP);

INSERT INTO yycomm.device_net_config
(company_id, store_id, center_devid, udp_domain, udp_port, upload_domain, upload_port, upd_page_size, msg_delay_time,
 keeplive_delay_time, remark)
    VALUES (:COMPANY_ID, -1, 'FFFFFFFF', 'etl.csosm.com', 62280, ':UPLOAD_DOMAIN', ':UPLOAD_PORT', 15, 3, 120, ':COMPANY_NAME');

-- 初始化模板
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2000', '0000', '节日服务', '0000-2000', :COMPANY_ID, :COMPANY_ID, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3000', '0000', '90服务', '0000-3000', :COMPANY_ID, :COMPANY_ID, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('4000', '0000', '感动服务', '0000-4000', :COMPANY_ID, :COMPANY_ID, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9000', '0000', '其他模板', '0000-9000', :COMPANY_ID, :COMPANY_ID, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('4001', '4000', '生日祝福', '0000-4000-4001', :COMPANY_ID, :COMPANY_ID, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9001', '9000', '双十一促销节', '0000-9000-9001', :COMPANY_ID, :COMPANY_ID, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9002', '9000', '双十二促销节', '0000-9000-9002', :COMPANY_ID, :COMPANY_ID, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9003', '9000', '通用模板', '0000-9000-9003', :COMPANY_ID, :COMPANY_ID, -1, NOW());