USE CSOSM_CRM_DB;
-- 组织机构表
DROP TABLE IF EXISTS acp_organization;
CREATE TABLE acp_organization
(
    id                       int(11) NOT NULL AUTO_INCREMENT,
    code                     varchar(255)   DEFAULT NULL,
    createTime               datetime       DEFAULT NULL,
    depth                    int(11)        DEFAULT NULL,
    icon                     varchar(255)   DEFAULT NULL,
    logoFileName             varchar(255)   DEFAULT NULL,
    name                     varchar(255)   DEFAULT NULL,
    oldOrgId                 varchar(255)   DEFAULT NULL,
    orgType                  int(11)        DEFAULT NULL,
    parentId                 int(11)        DEFAULT NULL,
    rootNode                 tinyint(1)     DEFAULT NULL,
    shortName                varchar(255)   DEFAULT NULL,
    sortNo                   int(11)        DEFAULT NULL,
    status                   int(11)        DEFAULT '1',
    updateTime               datetime       DEFAULT NULL,
    createUser_id            int(11)        DEFAULT NULL,
    updateUser_id            int(11)        DEFAULT NULL,
    smsPrice                 decimal(19, 3) DEFAULT NULL,
    totalSMSAmount           decimal(19, 2) DEFAULT NULL,
    totalSMSCount            bigint(20)     DEFAULT NULL,
    limitLogin               int(11)        DEFAULT NULL,
    smsCount                 int(11)        DEFAULT NULL,
    orgShowFlag              int(11)        DEFAULT NULL,
    createWeixinAccountCount int(11)        DEFAULT NULL,
    orgShowLimitFlag         int(11)        DEFAULT NULL,
    hiddenMemberPhoneFlag    int(11)        DEFAULT NULL,
    industryType             int(11)        DEFAULT NULL,
    callCount                int(11)        DEFAULT NULL,
    linkman_contact          varchar(255)   DEFAULT NULL,
    linkman_phone            varchar(255)   DEFAULT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 门店表
DROP TABLE IF EXISTS acp_store;
CREATE TABLE acp_store
(
    id                       int(11)   NOT NULL AUTO_INCREMENT,
    name                     varchar(255)   DEFAULT NULL,
    oldStoreId               varchar(255)   DEFAULT NULL,
    phone                    varchar(255)   DEFAULT NULL,
    sortNo                   int(11)        DEFAULT NULL,
    status                   int(11)        DEFAULT '1',
    storeState               int(11)        DEFAULT NULL,
    storeType                int(11)        DEFAULT NULL,
    company_id               int(11)        DEFAULT NULL,
    organization_id          int(11)        DEFAULT NULL,
    performanceFlag          int(11)        DEFAULT NULL,
    createWeixinAccountCount int(11)        DEFAULT '1',
    hiddenMemberPhoneFlag    int(11)        DEFAULT NULL,
    phoneAmount              int(11)        DEFAULT NULL,
    area                     decimal(19, 2) DEFAULT NULL,
    detailAddress            varchar(255)   DEFAULT NULL,
    icon                     varchar(255)   DEFAULT NULL,
    logoFile                 tinyblob,
    wxExpireTime             datetime       DEFAULT NULL,
    brandCode                varchar(255)   DEFAULT NULL,
    brandName                varchar(255)   DEFAULT NULL,
    initDataTimestamp        datetime       DEFAULT NULL,
    companypy                varchar(255)   DEFAULT NULL,
    callCount                int(11)        DEFAULT NULL,
    smsCount                 int(11)        DEFAULT NULL,
    smsPrice                 decimal(19, 2) DEFAULT NULL,
    totalSMSAmount           decimal(19, 2) DEFAULT NULL,
    totalSMSCount            int(11)        DEFAULT NULL,
    crc32id                  varchar(255)   DEFAULT NULL,
    createTime               datetime       DEFAULT NULL,
    createUserId             int(11)        DEFAULT NULL,
    updateUserId             int(11)        DEFAULT NULL,
    updateTime               timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS acp_store_ext;
CREATE TABLE acp_store_ext
(
    id              int(11)    NOT NULL,
    company_id      int(11)    NOT NULL,
    birthday_before int(11)             DEFAULT NULL,
    qr_code         varchar(254)        DEFAULT NULL,
    delete_flag     tinyint(1) NOT NULL DEFAULT '0',
    creator         bigint(20) NOT NULL,
    createTime      datetime   NOT NULL,
    editor          bigint(20)          DEFAULT NULL,
    editTime        datetime            DEFAULT NULL,
    before_days     int(5)              DEFAULT '2',
    PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 职员信息表
DROP TABLE IF EXISTS acp_employee;
CREATE TABLE acp_employee
(
    id              int(11)   NOT NULL AUTO_INCREMENT,
    birthday        datetime       DEFAULT NULL,
    certificate     varchar(255)   DEFAULT NULL,
    certificateType int(11)        DEFAULT NULL,
    employeeType    int(11)        DEFAULT NULL,
    loginuser_id    int(11)        DEFAULT NULL,
    password        varchar(255)   DEFAULT NULL,
    createTime      datetime       DEFAULT NULL,
    remark          varchar(255)   DEFAULT NULL,
    deviceId        varchar(255)   DEFAULT NULL,
    sex             int(11)        DEFAULT NULL,
    organization_id int(11)        DEFAULT NULL,
    phone           varchar(255)   DEFAULT NULL,
    agingTime       datetime       DEFAULT NULL,
    name            varchar(255)   DEFAULT NULL,
    photoFileName   varchar(255)   DEFAULT NULL,
    source_from     int(11)        DEFAULT NULL,
    source_channel  varchar(50)    DEFAULT NULL,
    store_id        int(11)        DEFAULT NULL,
    employeeState   int(11)        DEFAULT NULL,
    status          int(11)        DEFAULT NULL,
    oldEmployeeId   varchar(50)    DEFAULT NULL,
    company_id      int(11)        DEFAULT NULL,
    telephone       varchar(255)   DEFAULT NULL,
    oldStoreId      varchar(255)   DEFAULT NULL,
    loginTimeE      int(11)        DEFAULT NULL,
    loginTimeS      int(11)        DEFAULT NULL,
    weixinConfig_id int(11)        DEFAULT NULL,
    companypy       varchar(255)   DEFAULT NULL,
    crc32id         varchar(255)   DEFAULT NULL,
    login_name      varchar(255)   DEFAULT NULL,
    pass_word       varchar(255)   DEFAULT NULL,
    role_ids        varchar(255)   DEFAULT NULL,
    createUser_id   int(11)        DEFAULT NULL,
    updateUser_id   int(11)        DEFAULT NULL,
    updateTime      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

INSERT INTO acp_organization
(id, code, createTime, depth, name, orgType, shortName, status, createUser_id)
VALUES (-1, '-1', NOW(), 0, '羿元信息科技公司', 1, 'YGKJ', 1, -1);


INSERT INTO acp_employee
(employeeType, createTime, remark, createUser_id, name, company_id, employeeState, status, login_name, pass_word,
 role_ids)
VALUES (0, NOW(), 'DBA', -1, '运维管理员', -1, 1, 1, 'Administrator', '6a647a2f4f6f4430474a7968796491f9db0106e8344355e4',
        '2');

-- 系统角色表
DROP TABLE IF EXISTS acp_role;
CREATE TABLE acp_role
(
    id          INT(11)      NOT NULL,
    role_name   VARCHAR(256) NOT NULL,
    role_desc   VARCHAR(512) NOT NULL,
    enbaled     INT(1)       NOT NULL DEFAULT 1,
    priority    INT(2)       NOT NULL DEFAULT 0,
    resources   VARCHAR(512),
    delete_flag INT(1)       NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)   NOT NULL,
    creator     BIGINT(20)   NOT NULL,
    createTime  DATETIME     NOT NULL,
    editor      BIGINT(20)   NULL     DEFAULT NULL,
    editTime    DATETIME     NULL     DEFAULT NULL,
    PRIMARY KEY (id, tenant_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

INSERT INTO acp_role
(id, role_name, role_desc, enbaled, priority, resources, tenant_id, creator, createTime)
VALUES (2, 'ManagerRole', '系统管理员', 1, 80, null, -1, -1, NOW());

DROP TABLE IF EXISTS dict_kv_data;
CREATE TABLE dict_kv_data
(
    id          int(11)     NOT NULL AUTO_INCREMENT,
    dict_type   varchar(32) NOT NULL,
    field_value varchar(32) NOT NULL,
    field_name  varchar(128)         DEFAULT NULL,
    field_other varchar(128)         DEFAULT NULL,
    field_index int(8)      NOT NULL,
    field_desc  text,
    delete_flag tinyint(1)  NOT NULL DEFAULT 0,
    tenant_id   bigint(20)  NOT NULL,
    creator     bigint(20)  NOT NULL,
    createTime  datetime    NOT NULL,
    editor      bigint(20)           DEFAULT NULL,
    editTime    datetime             DEFAULT NULL,
    PRIMARY KEY (id, tenant_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 门店类型
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
VALUES ('STORETYPE', '1', '直营店', 0, NULL, 0, -1, -1, NOW()),
       ('STORETYPE', '2', '联体店', 1, NULL, 0, -1, -1, NOW()),
       ('STORETYPE', '3', '加盟店', 2, NULL, 0, -1, -1, NOW());
       
INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
VALUES ('MEMBERCARDTYPE', '01', '贵宾VIP会员', 0, NULL, 0, 1, -1, NOW()),
       ('MEMBERCARDTYPE', '02', '金卡VIP会员', 1, NULL, 0, 1, -1, NOW()),
       ('MEMBERCARDTYPE', '03', '员工内购卡', 2, NULL, 0, 1, -1, NOW()),
       ('MEMBERCARDTYPE', '04', '精塑卡 58800', 3, NULL, 0, 1, -1, NOW()),
       ('MEMBERCARDTYPE', '05', '女王卡  108800', 4, NULL, 0, 1, -1, NOW()),
       ('MEMBERCARDTYPE', '06', '享塑卡 208800', 5, NULL, 0, 1, -1, NOW());

INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
VALUES ('INDUSTRYTYPE', '1', '女装', 0, NULL, 0, -1, -1, NOW()),
       ('INDUSTRYTYPE', '2', '女士内衣', 1, NULL, 0, -1, -1, NOW()),
       ('INDUSTRYTYPE', '3', '童装', 2, NULL, 0, -1, -1, NOW()),
       ('INDUSTRYTYPE', '4', '包包', 3, NULL, 0, -1, -1, NOW());

INSERT INTO dict_kv_data (dict_type, field_value, field_name, field_index, field_desc, delete_flag, tenant_id, creator,
                          createTime)
VALUES ('PAYMENTMODE', '1', '现金', 0, NULL, 0, -1, -1, NOW()),
       ('PAYMENTMODE', '2', '银行卡', 1, NULL, 0, -1, -1, NOW()),
       ('PAYMENTMODE', '3', '定金', 2, NULL, 0, -1, -1, NOW()),
       ('PAYMENTMODE', '4', '积分抵扣', 3, NULL, 0, -1, -1, NOW()),
       ('PAYMENTMODE', '5', '其他', 3, NULL, 0, -1, -1, NOW());

-- 待定初始化
DROP TABLE IF EXISTS acp_systemlog;
CREATE TABLE acp_systemlog
(
    id         INT(18)  NOT NULL AUTO_INCREMENT,
    crud       CHAR(1)  NOT NULL,
    operation  VARCHAR(255)      DEFAULT NULL,
    sub_type   VARCHAR(255)      DEFAULT NULL,
    message    text              DEFAULT NULL,
    creator    INT(18)  NOT NULL DEFAULT -1,
    company_id INT(19)  NOT NULL DEFAULT -1,
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 门店透视图
DROP TABLE IF EXISTS acp_store_view;
CREATE TABLE acp_store_view
(
    id           CHAR(36)         NOT NULL,
    parentId     CHAR(36)         NULL     DEFAULT NULL,
    nodeName     VARCHAR(255)     NOT NULL,
    tree_type    TINYINT UNSIGNED NOT NULL,
    owner_id     INT(11)          NULL     DEFAULT NULL,
    storeIds     TEXT             NULL     DEFAULT NULL,
    store_info   TEXT             NULL     DEFAULT NULL,
    company_id   INT(11)          NULL     DEFAULT NULL,
    node_desc    VARCHAR(255)     NULL     DEFAULT NULL,
    createUserId INT(11)          NULL     DEFAULT NULL,
    createTime   DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP,
    modifyUserId INT(11)          NULL     DEFAULT NULL,
    modifyTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 透视图结构
DROP TABLE IF EXISTS acp_store_view_structure;
CREATE TABLE acp_store_view_structure
(
    id           int(11) AUTO_INCREMENT NOT NULL,
    node_id      CHAR(36)               NOT NULL,
    owner_id     INT(11)                NOT NULL,
    company_id   INT(11)                NOT NULL,
    node_path    VARCHAR(255)           NOT NULL,
    createUserId INT(11)                NULL     DEFAULT NULL,
    createTime   DATETIME               NULL     DEFAULT CURRENT_TIMESTAMP,
    modifyUserId INT(11)                NULL     DEFAULT NULL,
    modifyTime   DATETIME               NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员基础表
DROP TABLE IF EXISTS crm_member;
CREATE TABLE crm_member
(
    id                    int(11)   NOT NULL AUTO_INCREMENT,
    birthday              datetime       DEFAULT NULL,
    calendarType          int(11)        DEFAULT NULL,
    createTime            datetime       DEFAULT NULL,
    email                 varchar(255)   DEFAULT NULL,
    memberType            int(11)        DEFAULT NULL,
    name                  varchar(255)   DEFAULT NULL,
    oldMemberCode         varchar(255)   DEFAULT NULL,
    oldShoppingGuideId    varchar(255)   DEFAULT NULL,
    oldStoreId            varchar(255)   DEFAULT NULL,
    mobilePhone           varchar(255)   DEFAULT NULL,
    qqNum                 varchar(255)   DEFAULT NULL,
    labelIds              varchar(255)   DEFAULT NULL,
    remarkIds             varchar(255)   DEFAULT NULL,
    remark                varchar(255)   DEFAULT NULL,
    serviceLevel          int(11)        DEFAULT NULL,
    sex                   int(11)        DEFAULT NULL,
    source_channel        varchar(255)   DEFAULT NULL,
    source_from           int(11)        DEFAULT NULL,
    status                int(11)        DEFAULT '1',
    updateTime            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    wechatNum             varchar(255)   DEFAULT NULL,
    weiboNum              varchar(255)   DEFAULT NULL,
    company_id            int(11)        DEFAULT NULL,
    company_id1           int(11)        DEFAULT NULL,
    company_id2           int(11)        DEFAULT NULL,
    createUser_id         int(11)        DEFAULT NULL,
    updateUser_id         int(11)        DEFAULT NULL,
    weixin_id             varchar(255)   DEFAULT NULL,
    weixinUser_wx_user_id int(11)        DEFAULT NULL,
    guide_id              int(11)        DEFAULT NULL,
    telephone             varchar(255)   DEFAULT NULL,
    effectiveFlag         int(11)        DEFAULT '1',
    namePinyin            varchar(100)   DEFAULT NULL,
    lunarBirthday         datetime       DEFAULT NULL,
    iconUrl               varchar(256)   DEFAULT NULL,
    companypy             varchar(255)   DEFAULT NULL,
    reachable             int(11)        DEFAULT '1',
    awakenCount           int(11)        DEFAULT NULL,
    crc32id               varchar(255)   DEFAULT NULL,
    store_ids             varchar(255)   DEFAULT NULL,
    orgin_store_id        int(11)        DEFAULT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员附加信息表
DROP TABLE IF EXISTS member_addition_info;
CREATE TABLE member_addition_info
(
    member_id       int(11)   NOT NULL UNIQUE,
    carePeople      varchar(255)   DEFAULT NULL,
    characterType   int(11)        DEFAULT NULL,
    faithType       int(11)        DEFAULT NULL,
    hobby           varchar(255)   DEFAULT NULL,
    certificate     varchar(255)   DEFAULT NULL,
    certificateType int(11)        DEFAULT NULL,
    idols           varchar(255)   DEFAULT NULL,
    jobType         varchar(255)   DEFAULT NULL,
    likeBrand       varchar(255)   DEFAULT NULL,
    likeContact     int(11)        DEFAULT NULL,
    marryStatus     int(11)        DEFAULT NULL,
    specialDay      varchar(255)   DEFAULT NULL,
    zodiac          int(11)        DEFAULT NULL,
    education       int(11)        DEFAULT NULL,
    likeContactTime int(11)        DEFAULT NULL,
    detailAddress   varchar(255)   DEFAULT NULL,
    createTime      datetime       DEFAULT NULL,
    updateTime      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员消费行为表
DROP TABLE IF EXISTS member_consume_behavior;
CREATE TABLE member_consume_behavior
(
    member_id                 int(11)   NOT NULL UNIQUE,
    rechargeAmount            decimal(19, 2) DEFAULT NULL,
    lastVisitTime             datetime       DEFAULT NULL,
    rfm                       varchar(255)   DEFAULT NULL,
    firstSaleRecordAmount     decimal(19, 2) DEFAULT NULL,
    firstSaleRecordNo         varchar(255)   DEFAULT NULL,
    consumeTotalCount         int(11)        DEFAULT NULL,
    consumeTotalCountCurYear  int(11)        DEFAULT NULL,
    maxConsumePrice           decimal(19, 2) DEFAULT NULL,
    maxConsumePriceCurYear    decimal(19, 2) DEFAULT NULL,
    totalConsumeAmount        decimal(19, 2) DEFAULT NULL,
    totalConsumeAmountCurYear decimal(19, 2) DEFAULT NULL,
    createTime                datetime       DEFAULT NULL,
    updateTime                timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 会员卡信息表
DROP TABLE IF EXISTS member_card_info;
CREATE TABLE member_card_info
(
    member_id         int(11)   NOT NULL UNIQUE,
    memberCardType_id int(11)        DEFAULT NULL,
    memberCardNum     varchar(255)   DEFAULT NULL,
    createCardTime    datetime       DEFAULT NULL,
    createStoreId     int(11)        DEFAULT NULL,
    limitday          int(11)        DEFAULT NULL,
    createTime        datetime       DEFAULT NULL,
    totalScore        int(11)        DEFAULT NULL,
    updateTime        timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS crm_memberextrainfo;
CREATE TABLE crm_memberextrainfo
(
    member_id          int(11)   NOT NULL UNIQUE,
    afterFork          decimal(19, 2) DEFAULT NULL,
    beforeFork         decimal(19, 2) DEFAULT NULL,
    bottomsSize        varchar(255)   DEFAULT NULL,
    braSize            varchar(255)   DEFAULT NULL,
    briefsSize         varchar(255)   DEFAULT NULL,
    chest              decimal(19, 2) DEFAULT NULL,
    clothingLong       decimal(19, 2) DEFAULT NULL,
    footLength         decimal(19, 2) DEFAULT NULL,
    hipline            decimal(19, 2) DEFAULT NULL,
    jacketSize         varchar(255)   DEFAULT NULL,
    kneeCircumference  decimal(19, 2) DEFAULT NULL,
    onChest            decimal(19, 2) DEFAULT NULL,
    outseam            decimal(19, 2) DEFAULT NULL,
    shoeSize           varchar(255)   DEFAULT NULL,
    shoulder           decimal(19, 2) DEFAULT NULL,
    sleeveLength       decimal(19, 2) DEFAULT NULL,
    status             int(11)        DEFAULT '1',
    thighCircumference decimal(19, 2) DEFAULT NULL,
    trouserLeg         decimal(19, 2) DEFAULT NULL,
    underChest         decimal(19, 2) DEFAULT NULL,
    waistline          decimal(19, 2) DEFAULT NULL,
    createTime         datetime       DEFAULT NULL,
    updateTime         timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (member_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS crm_assignmemberrecord;
CREATE TABLE crm_assignmemberrecord
(
    id         int(11) NOT NULL AUTO_INCREMENT,
    createTime datetime DEFAULT NULL,
    employeeId int(11)  DEFAULT NULL,
    memberId   int(11)  DEFAULT NULL,
    storeId    int(11)  DEFAULT NULL,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 商品销售记录表
DROP TABLE IF EXISTS crm_goods;
CREATE TABLE crm_goods
(
    id               int(11)   NOT NULL AUTO_INCREMENT,
    artNo            varchar(255)   DEFAULT NULL,
    barCode          varchar(255)   DEFAULT NULL,
    createTime       datetime       DEFAULT NULL,
    fabric           varchar(255)   DEFAULT NULL,
    factoryPrice     decimal(19, 2) DEFAULT NULL,
    function         varchar(255)   DEFAULT NULL,
    name             varchar(255)   DEFAULT NULL,
    oldGoodsId       varchar(255)   DEFAULT NULL,
    retailPrice      decimal(19, 2) DEFAULT NULL,
    season           varchar(255)   DEFAULT NULL,
    status           int(11)        DEFAULT '1',
    styleName        varchar(255)   DEFAULT NULL,
    tradePrice       decimal(19, 2) DEFAULT NULL,
    unit             varchar(255)   DEFAULT NULL,
    updateTime       timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    brand_id         int(11)        DEFAULT NULL,
    color_id         int(11)        DEFAULT NULL,
    company_id       int(11)        DEFAULT NULL,
    createUser_id    int(11)        DEFAULT NULL,
    goodsClassify_id int(11)        DEFAULT NULL,
    modelSize_id     int(11)        DEFAULT NULL,
    updateUser_id    int(11)        DEFAULT NULL,
    companypy        varchar(255)   DEFAULT NULL,
    crc32id          varchar(255)   DEFAULT NULL,
    PRIMARY KEY (id),
    KEY crc32id (oldGoodsId, crc32id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS crm_salerecord;
CREATE TABLE crm_salerecord
(
    id                    int(11)   NOT NULL AUTO_INCREMENT,
    consumeType           int(11)        DEFAULT NULL,
    createTime            datetime       DEFAULT NULL,
    discount              decimal(19, 2) DEFAULT NULL,
    oldMemberId           varchar(255)   DEFAULT NULL,
    oldSaleRecordId       varchar(255)   DEFAULT NULL,
    oldStoreId            varchar(255)   DEFAULT NULL,
    saleCount             int(11)        DEFAULT NULL,
    saleOrderNo           varchar(255)   DEFAULT NULL,
    saleTotalAmount       decimal(19, 2) DEFAULT NULL,
    scoreMultiple         int(11)        DEFAULT NULL,
    status                int(11)        DEFAULT '1',
    updateTime            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    company_id            int(11)        DEFAULT NULL,
    createUser_id         int(11)        DEFAULT NULL,
    member_id             int(11)        DEFAULT NULL,
    store_id              int(11)        DEFAULT NULL,
    ninetyPlanFollowUp_id int(11)        DEFAULT NULL,
    oldShoppingGuideId    varchar(255)   DEFAULT NULL,
    sure                  int(11)        DEFAULT NULL,
    companypy             varchar(255)   DEFAULT NULL,
    crc32id               varchar(255)   DEFAULT NULL,
    PRIMARY KEY (id),
    KEY oldSaleRecordId (oldSaleRecordId),
    KEY crc32id (crc32id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DROP TABLE IF EXISTS crm_salesubrecord;
CREATE TABLE crm_salesubrecord
(
    id                 int(11)   NOT NULL AUTO_INCREMENT,
    createTime         datetime       DEFAULT NULL,
    discount           decimal(19, 2) DEFAULT NULL,
    goodsCount         int(11)        DEFAULT NULL,
    goodsPrice         decimal(19, 2) DEFAULT NULL,
    oldGoodsId         varchar(255)   DEFAULT NULL,
    oldSaleRecordId    varchar(255)   DEFAULT NULL,
    remark             varchar(255)   DEFAULT NULL,
    salePrice          decimal(19, 2) DEFAULT NULL,
    status             int(11)        DEFAULT '1',
    totalPrice         decimal(19, 2) DEFAULT NULL,
    updateTime         timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    createUser_id      int(11)        DEFAULT NULL,
    goods_id           int(11)        DEFAULT NULL,
    saleRecord_id      int(11)        DEFAULT NULL,
    updateUser_id      int(11)        DEFAULT NULL,
    cardPrice          decimal(19, 2) DEFAULT NULL,
    oldSaleSubRecordId varchar(255)   DEFAULT NULL,
    company_id         int(11)        DEFAULT NULL,
    color              varchar(255)   DEFAULT NULL,
    modelSize          varchar(255)   DEFAULT NULL,
    companypy          varchar(255)   DEFAULT NULL,
    crc32id            varchar(255)   DEFAULT NULL,
    PRIMARY KEY (id),
    KEY oldGoodsId (oldGoodsId),
    KEY oldSaleRecordId (oldSaleRecordId),
    KEY oldSaleSubRecordId (oldSaleSubRecordId),
    KEY crc32id (crc32id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 模板使用分类树
DROP TABLE IF EXISTS MSG_TEMPLATE_CLASSIFY;
CREATE TABLE MSG_TEMPLATE_CLASSIFY
(
    id          CHAR(4)      NOT NULL,
    pid         CHAR(4)      NOT NULL,
    classify    VARCHAR(64)  NOT NULL,
    deep_path   VARCHAR(128) NOT NULL,
    company_id  INT(11)      NOT NULL,
    delete_flag INT(1)       NOT NULL DEFAULT 0,
    tenant_id   BIGINT(20)   NULL     DEFAULT NULL,
    creator     BIGINT(20)   NOT NULL,
    createTime  DATETIME     NOT NULL,
    editor      BIGINT(20)   NULL     DEFAULT NULL,
    editTime    DATETIME     NULL     DEFAULT NULL,
    PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

DELETE
FROM MSG_TEMPLATE_CLASSIFY;

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2000', '0000', '节日服务', '0000-2000', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3000', '0000', '90服务', '0000-3000', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('4000', '0000', '感动服务', '0000-4000', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9000', '0000', '其他模板', '0000-9000', -1, -1, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3001', '3000', '第一节点', '0000-3000-3001', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3002', '3000', '第二节点', '0000-3000-3002', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3003', '3000', '第三节点', '0000-3000-3003', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3004', '3000', '第四节点', '0000-3000-3004', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3005', '3000', '第五节点', '0000-3000-3005', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3006', '3000', '第六节点', '0000-3000-3006', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3007', '3000', '第七节点', '0000-3000-3007', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3008', '3000', '第八节点', '0000-3000-3008', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3009', '3000', '第九节点', '0000-3000-3009', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('3010', '3000', '第十节点', '0000-3000-3010', -1, -1, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2001', '2000', '春节', '0000-2000-2001', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2002', '2000', '元宵节', '0000-2000-2002', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2003', '2000', '三八妇女节', '0000-2000-2003', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2004', '2000', '五四青年节', '0000-2000-2004', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2005', '2000', '六一儿童节', '0000-2000-2005', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2007', '2000', '国庆节童节', '0000-2000-2007', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2010', '2000', '圣诞节', '0000-2000-2010', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2011', '2000', '元旦节', '0000-2000-2011', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2012', '2000', '情人节', '0000-2000-2012', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2013', '2000', '父亲节', '0000-2000-2013', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2014', '2000', '母亲节', '0000-2000-2014', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('2015', '2000', '教师节', '0000-2000-2015', -1, -1, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('4001', '4000', '生日祝福', '0000-4000-4001', -1, -1, -1, NOW());

INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9001', '9000', '双十一促销节', '0000-9000-9001', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9002', '9000', '双十二促销节', '0000-9000-9002', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9003', '9000', '常用模板', '0000-9000-9003', -1, -1, -1, NOW());
INSERT INTO MSG_TEMPLATE_CLASSIFY (id, pid, classify, deep_path, company_id, tenant_id, creator, createTime)
VALUES ('9004', '9000', '店庆模板', '0000-9000-9004', -1, -1, -1, NOW());

-- 短信模板内容
DROP TABLE IF EXISTS MSG_TEMPLATE_CONTEXT;
CREATE TABLE MSG_TEMPLATE_CONTEXT
(
    id           CHAR(16)         NOT NULL,
    company_id   INT(18)          NOT NULL DEFAULT -1,
    org_id       INT(18)          NOT NULL DEFAULT -1,
    store_id     INT(18)          NOT NULL DEFAULT -1,
    blacked      TINYINT UNSIGNED NOT NULL DEFAULT 0,
    classifies   VARCHAR(128)     NOT NULL,
    use_scopes   VARCHAR(64)      NOT NULL,
    expire_date  DATETIME         NULL     DEFAULT NULL,
    temp_title   VARCHAR(128)     NOT NULL DEFAULT '-1',
    temp_context VARCHAR(512)     NOT NULL,
    delete_flag  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id    BIGINT(20)       NULL     DEFAULT -1,
    creator      BIGINT(20)       NOT NULL DEFAULT -1,
    createTime   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor       BIGINT(20)       NULL     DEFAULT NULL,
    editTime     DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id, company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 模板黑名单
DROP TABLE IF EXISTS MSG_TEMPLATE_BLACKLIST;
CREATE TABLE MSG_TEMPLATE_BLACKLIST
(
    company_id INT(18)       NOT NULL DEFAULT -1,
    org_id     INT(18)       NOT NULL DEFAULT -1,
    store_id   INT(18)       NOT NULL DEFAULT -1,
    black_list VARCHAR(2048) NULL     DEFAULT NULL,
    createTime DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editTime   DATETIME      NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id, org_id, store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 模版替换字典
DROP TABLE IF EXISTS MSG_TEMPLATE_REPLACE;
CREATE TABLE MSG_TEMPLATE_REPLACE
(
    id            BIGINT(20)       NOT NULL AUTO_INCREMENT,
    field_tag     VARCHAR(32)      NOT NULL,
    replace_token VARCHAR(64)      NOT NULL,
    token_type    VARCHAR(64)      NOT NULL,
    default_value VARCHAR(64)      NULL     DEFAULT NULL,
    enbaled       TINYINT UNSIGNED NOT NULL DEFAULT 1,
    delete_flag   TINYINT UNSIGNED NOT NULL DEFAULT 0,
    tenant_id     BIGINT(20)       NOT NULL DEFAULT -1,
    creator       BIGINT(20)       NOT NULL DEFAULT -1,
    createTime    DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    editor        BIGINT(20)       NULL     DEFAULT NULL,
    editTime      DATETIME         NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 标签信息
DROP TABLE IF EXISTS user_label_tree;
  CREATE TABLE user_label_tree (
  id bigint(20) NOT NULL,
  pid bigint(20) NOT NULL,
  label_type int(1) NOT NULL,
  label_name varchar(512) COLLATE utf8_bin DEFAULT NULL,
  label_ctx varchar(512) COLLATE utf8_bin DEFAULT NULL,
  label_desc varchar(512) COLLATE utf8_bin DEFAULT NULL,
  label_enbale int(1) NOT NULL DEFAULT '1',
  store_id int(11) NOT NULL DEFAULT '-1',
  company_id int(11) NOT NULL,
  delete_flag int(1) NOT NULL DEFAULT '0',
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  PRIMARY KEY (id,store_id,company_id),
  KEY user_label_tree_store_id_IDX (store_id,company_id) USING BTREE
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
-- 标签信息
DROP TABLE IF EXISTS user_label_remark;
  CREATE TABLE user_label_remark (
  id int(11) NOT NULL AUTO_INCREMENT,
  member_id int(11) DEFAULT NULL,
  weixin_id varchar(250) CHARACTER SET utf8 DEFAULT NULL,
  label_id bigint(20) NOT NULL,
  enabled int(1) NOT NULL DEFAULT '1',
  store_id int(11) DEFAULT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY user_label_remark_member_id_IDX (member_id,store_id,company_id) USING BTREE,
  KEY user_label_remark_weixin_id_IDX (weixin_id,store_id,company_id) USING BTREE
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 微信白名单
DROP TABLE IF EXISTS acp_wxmsg_whitelist;
  CREATE TABLE acp_wxmsg_whitelist (
  id int(11) NOT NULL AUTO_INCREMENT,
  prohibit_tag int(1) NOT NULL DEFAULT '0',
  inclouds_ids varchar(255) DEFAULT NULL,
  exclouds_ids varchar(255) DEFAULT NULL,
  store_id int(11) NOT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) NOT NULL,
  createTime datetime NOT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 微信会员关联关系
DROP TABLE IF EXISTS acp_weixin_member;
  CREATE TABLE acp_weixin_member (
  id char(36) NOT NULL,
  weixin_id varchar(256) NOT NULL,
  member_id int(11) NOT NULL,
  store_id int(11) NOT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime NOT NULL,
  PRIMARY KEY (id),
  KEY acp_weixin_member_weixin_id_IDX (weixin_id) USING BTREE,
  KEY acp_weixin_member_member_id_IDX (member_id,store_id,company_id) USING BTREE
)DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 微信好友分组表
DROP TABLE IF EXISTS acp_group_friend;
  CREATE TABLE acp_group_friend (
  id char(36) NOT NULL,
  group_id char(36) NOT NULL,
  friend_id varchar(256) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY friend_id (friend_id),
  KEY group_id (group_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 微信信息表
DROP TABLE IF EXISTS acp_weixin_msgs;
  CREATE TABLE acp_weixin_msgs (
  id char(36) NOT NULL,
  msg_type int(1) DEFAULT NULL,
  weixin_num int(11) DEFAULT NULL,
  msg_num int(11) NOT NULL DEFAULT '1',
  image_url mediumtext,
  msg_text text,
  weixin_ids mediumtext NOT NULL,
  store_id int(11) NOT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime NOT NULL,
  msg_temp_id bigint(20) NOT NULL DEFAULT '-1',
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 微信认领表
DROP TABLE IF EXISTS acp_weixin_sign;
  CREATE TABLE acp_weixin_sign (
  id int(11) NOT NULL AUTO_INCREMENT,
  employee_id int(11) NOT NULL,
  weixin_id varchar(128) NOT NULL,
  store_id int(11) NOT NULL,
  company_id int(11) NOT NULL,
  signTime datetime NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime NOT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 备注列表
DROP TABLE IF EXISTS user_remarks_list;
  CREATE TABLE user_remarks_list (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  member_id int(11) DEFAULT NULL,
  weixin_id varchar(250) CHARACTER SET utf8 DEFAULT NULL,
  remarks varchar(256) NOT NULL,
  delete_flag int(1) NOT NULL DEFAULT '0',
  store_id int(11) DEFAULT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) NOT NULL,
  createTime datetime DEFAULT NULL,
  type int(10) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  KEY user_remarks_list_member_id_IDX (member_id,store_id,company_id) USING BTREE,
  KEY user_remarks_list_weixin_id_IDX (weixin_id,store_id,company_id) USING BTREE
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 沟通统计信息表
DROP TABLE IF EXISTS count_wx_comm_info;
  CREATE TABLE count_wx_comm_info (
  compute_time datetime DEFAULT CURRENT_TIMESTAMP,
  count_date date NOT NULL,
  company_id int(11) NOT NULL,
  store_id int(20) NOT NULL,
  WX_S_R_Total int(11) DEFAULT '0',
  WX_S_Total int(11) DEFAULT '0',
  WX_R_Total int(11) DEFAULT '0',
  WX_SR_Percent_Total decimal(24,2) DEFAULT '0.00',
  WX_S_R_PrivateChat int(11) DEFAULT '0',
  WX_S_PrivateChat int(11) DEFAULT '0',
  WX_R_PrivateChat int(11) DEFAULT '0',
  WX_SR_Percent_PrivateChat decimal(24,2) DEFAULT '0.00',
  WX_S_R_ChatRoom int(11) DEFAULT '0',
  WX_S_ChatRoom int(11) DEFAULT '0',
  WX_R_ChatRoom int(11) DEFAULT '0',
  WX_SR_Percent_ChatRoom decimal(24,2) DEFAULT '0.00',
  PRIMARY KEY (count_date,company_id,store_id),
  KEY use_count_index (count_date,company_id,store_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 微信分组表
DROP TABLE IF EXISTS acp_group_instance;
  CREATE TABLE acp_group_instance (
  id char(36) NOT NULL,
  store_id int(11) NOT NULL,
  group_name varchar(64) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  type int(1) DEFAULT '0',
  company_id int(11) DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 微信好友分组关系表
DROP TABLE IF EXISTS acp_group_guide;
  CREATE TABLE acp_group_guide (
  id char(36) NOT NULL,
  group_id char(36) NOT NULL,
  guide_id int(11) NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  store_id int(11) NOT NULL,
  company_id int(11) DEFAULT NULL,
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 微信分组分配表
DROP TABLE IF EXISTS acp_group_log;
CREATE TABLE acp_group_log (
  group_id char(36) NOT NULL,
  group_name varchar(64) NOT NULL,
  granted_stores varchar(2048) DEFAULT NULL
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

  
-- 素材表
DROP TABLE IF EXISTS chat_material_detail;
CREATE TABLE chat_material_detail (
  id bigint(20) NOT NULL,
  company_id int(11) NOT NULL DEFAULT '0',
  meta_range int(1) NOT NULL DEFAULT '0',
  meta_group int(2) NOT NULL DEFAULT '0',
  org_id int(11) NOT NULL,
  meta_size int(2) NOT NULL,
  meta_type int(2) NOT NULL,
  meta_deadline date DEFAULT NULL,
  meta_enabled int(2) NOT NULL DEFAULT '1',
  meta_ctx text NOT NULL,
  createUserId int(11) DEFAULT NULL,
  createTime datetime DEFAULT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  user_times int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;

-- 素材分组表
DROP TABLE IF EXISTS chat_material_group;
  CREATE TABLE chat_material_group (
  id int(2) NOT NULL,
  group_name varchar(64) NOT NULL,
  company_id int(11) NOT NULL,
  createUserId int(11) NOT NULL DEFAULT '-1',
  createTime datetime NOT NULL,
  modifyUserId int(11) DEFAULT NULL,
  modifyTime datetime DEFAULT NULL,
  group_type int(2) DEFAULT '1',
  PRIMARY KEY (id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;
  
-- 素材黑名单表
DROP TABLE IF EXISTS chat_material_blacklist;
  CREATE TABLE chat_material_blacklist (
  org_id int(11) NOT NULL,
  meta_range int(1) NOT NULL DEFAULT '0',
  company_id int(11) NOT NULL DEFAULT '0',
  blacklist text,
  whitelist text,
  PRIMARY KEY (org_id,meta_range,company_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = 'utf8mb4_general_ci'
  ENGINE = InnoDB;