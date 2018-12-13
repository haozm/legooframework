package com.legooframework.model.organization.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LegooOrg;
import com.legooframework.model.core.base.runtime.LegooOrgImpl;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.commons.dto.DefautTreeStructure;
import com.legooframework.model.commons.dto.TreeStructure;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.wechat.entity.WechatAccountEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StoreEntity extends BaseOrganization {

    private String storeStatus;
    private String storeStatusName;
    public final static String STORESTATUS_DICT = "STORESTATUS";
    private String storeType;
    private String storeTypeName;
    public static final String TYPE_DICT = "STORETYPE";
    private String[] companyIds = new String[0];

    private List<WechatInfo> wechatInfos;
    // 绑定设备信息
    private List<DeviceInfo> deviceInfos;

    StoreEntity(Long storeId, String storeCode, String fullName, String shortName,
                String businessLicense, String detailAddress, String legalPerson,
                String contactNumber, String remark, KvDictDto storeType, LoginContext loginUser) {
        super(storeId, storeCode, loginUser.getTenantId(), loginUser.getLoginId(), fullName, shortName, businessLicense,
                detailAddress, legalPerson, contactNumber, remark, 1);
        this.storeStatus = "1";
        Preconditions.checkNotNull(storeType, "门店类型不可以为空.");
        Preconditions.checkArgument(StringUtils.equals(TYPE_DICT, storeType.getType()),
                "非法门店类型字典 %s", storeType);
        this.storeType = storeType.getValue();
    }

    StoreEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.legalPerson = ResultSetUtil.getOptString(res, "storeLegalPerson", null);
            this.orgCode = ResultSetUtil.getOptString(res, "orgCode", null);
            this.detailAddress = ResultSetUtil.getOptString(res, "storeDetailAddress", null);
            this.businessLicense = ResultSetUtil.getOptString(res, "storeBusinessLicense", null);
            this.shortName = ResultSetUtil.getOptString(res, "storeShortName", null);
            this.fullName = ResultSetUtil.getOptString(res, "storeFullName", null);
            this.fullName = ResultSetUtil.getOptString(res, "storeFullName", null);
            this.contactNumber = ResultSetUtil.getOptString(res, "storeContactNumber", null);
            this.remark = ResultSetUtil.getOptString(res, "storeRemark", null);
            this.storeType = ResultSetUtil.getString(res, "storeType");
            this.storeTypeName = ResultSetUtil.getString(res, "storeTypeName");
            this.storeStatus = ResultSetUtil.getString(res, "storeStatus");
            this.storeStatusName = ResultSetUtil.getString(res, "storeStatusName");
            Optional<String> _companyIds = ResultSetUtil.getOptObject(res, "companyIds", String.class);
            _companyIds.ifPresent(x -> this.companyIds = StringUtils.split(x, ','));
            Optional<String> _wechatInfos = ResultSetUtil.getOptObject(res, "wechatInfo", String.class);
            _wechatInfos.ifPresent(x -> {
                this.wechatInfos = Lists.newArrayList();
                Stream.of(StringUtils.split(x, '$')).forEach(c -> this.wechatInfos.add(new WechatInfo(c)));
            });

            Optional<String> _deviceInfo = ResultSetUtil.getOptObject(res, "deviceInfo", String.class);
            _deviceInfo.ifPresent(x -> {
                this.deviceInfos = Lists.newArrayList();
                Stream.of(StringUtils.split(x, '$')).forEach(c -> this.deviceInfos.add(new DeviceInfo(c)));
            });
        } catch (SQLException e) {
            throw new RuntimeException("Restore StoreEntity has SQLException", e);
        }
    }

    Optional<Map<String, Object>> assignEquipment(EquipmentEntity equipment, boolean mainflag) {
        Preconditions.checkState(equipment.isEnabled(), "当前设备处于%s状态，不允许分配.",
                equipment.getEnabledName());
        Preconditions.checkState(this.isEffective(), "当前门店%s状态无效，无法执行业务.", getFullName());
        if (hasDevices()) {
            Optional<DeviceInfo> exits = deviceInfos.stream()
                    .filter(x -> StringUtils.equals(x.imei, equipment.getId())).findFirst();
            if (exits.isPresent()) return Optional.empty();
        }
        Map<String, Object> map = LoginContextHolder.get().toParams();
        map.put("storeId", this.getId());
        map.put("equipmentId", equipment.getId());
        map.put("mainTag", mainflag ? 1 : 0);
        map.put("effective", 1);
        return Optional.of(map);
    }

    public LegooOrg toLegooOrg() {
        Set<String> deviceIds = CollectionUtils.isEmpty(this.deviceInfos) ? null :
                this.deviceInfos.stream().map(DeviceInfo::getImei).collect(Collectors.toSet());
        return LegooOrgImpl.store(this.getId(), this.fullName, deviceIds);
    }

    Optional<Map<String, Object>> assignWechat(WechatAccountEntity wechat, int webchatUse) {
        Preconditions.checkNotNull(wechat, "待添加的微信账号不可以空值...");
        Preconditions.checkState(this.isEffective(), "当前门店%s状态无效，无法执行业务.", getFullName());
        if (hasWorkWechat()) {
            Optional<WechatInfo> exits = wechatInfos.stream()
                    .filter(x -> StringUtils.equals(x.wechatId, wechat.getUserName())).findFirst();
            if (exits.isPresent()) return Optional.empty();
        }
        Map<String, Object> map = LoginContextHolder.get().toParams();
        map.put("storeId", this.getId());
        map.put("weichatId", wechat.getUserName());
        map.put("webchatUse", webchatUse);
        map.put("effective", 1);
        return Optional.of(map);
    }

    public Optional<List<WechatInfo>> getWechatInfos() {
        return Optional.ofNullable(CollectionUtils.isEmpty(wechatInfos) ? null : wechatInfos);
    }

    public Optional<List<DeviceInfo>> getDeviceInfos() {
        return Optional.ofNullable(CollectionUtils.isEmpty(deviceInfos) ? null : deviceInfos);
    }

    public String getStoreTypeName() {
        return storeTypeName;
    }

    public String getStoreStatusName() {
        return storeStatusName;
    }

    public boolean isEffective() {
        return StringUtils.containsAny("1,2", this.storeStatus);
    }

    public boolean isPaused() {
        return StringUtils.equals("2", this.storeStatus);
    }

    public boolean isOpenning() {
        return StringUtils.equals("1", this.storeStatus);
    }

    public boolean isClosed() {
        return StringUtils.equals("3", this.storeStatus);
    }

    public boolean hasDevices() {
        return CollectionUtils.isNotEmpty(this.deviceInfos);
    }

    // 临时变量 避免反复运算
    private int has_work_wx = -1;
    private int has_fans_wx = -1;

    public boolean hasWorkWechat() {
        if (has_work_wx == -1) {
            if (getWechatInfos().isPresent()) {
                Optional<WechatInfo> exits = getWechatInfos().get().stream().filter(WechatInfo::isWorker).findFirst();
                has_work_wx = exits.isPresent() ? 1 : 0;
                return exits.isPresent();
            }
            has_work_wx = 0;
            return false;
        }
        return has_work_wx == 1;
    }

    public boolean hasFansWechat() {
        if (has_fans_wx == -1) {
            if (getWechatInfos().isPresent()) {
                Optional<WechatInfo> exits = getWechatInfos().get().stream().filter(WechatInfo::isFanser).findFirst();
                has_fans_wx = exits.isPresent() ? 1 : 0;
                return exits.isPresent();
            }
            has_fans_wx = 0;
            return false;
        }
        return has_fans_wx == 1;
    }

    public Optional<List<String>> loadAllWorkWechat() {
        if (getWechatInfos().isPresent()) {
            List<String> ids = getWechatInfos().get().stream().filter(WechatInfo::isWorker)
                    .collect(Collectors.toList()).stream().map(WechatInfo::getWechatId).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(ids) ? null : ids);
        }
        return Optional.empty();
    }

    public Optional<List<String>> loadAllFansWechat() {
        if (getWechatInfos().isPresent()) {
            List<String> ids = getWechatInfos().get().stream().filter(WechatInfo::isFanser)
                    .collect(Collectors.toList()).stream().map(WechatInfo::getWechatId).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(ids) ? null : ids);
        }
        return Optional.empty();
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> paramMap = super.toParamMap("wechatInfos", "wechatInfos", "storeStatus", "storeType");
        paramMap.put("storeTypeName", storeTypeName);
        paramMap.put("storeStatus", storeStatus);
        paramMap.put("storeType", storeType);
        paramMap.put("storeStatusName", storeStatusName);
        return paramMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreEntity)) return false;
        if (!super.equals(o)) return false;
        StoreEntity that = (StoreEntity) o;
        return Objects.equal(storeStatus, that.storeStatus) &&
                Objects.equal(storeType, that.storeType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), storeStatus, storeType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companys's size", ArrayUtils.isEmpty(companyIds) ? 0 : companyIds.length)
                .add("orgCode", getOrgCode())
                .add("wechatInfos", wechatInfos)
                .add("deviceInfos", deviceInfos)
                .add("storeStatus", storeStatus)
                .add("storeStatusName", storeStatusName)
                .add("storeType", storeType)
                .add("storeTypeName", storeTypeName)
                .add("fullName", fullName)
                .add("shortName", shortName)
                .add("businessLicense", businessLicense)
                .add("detailAddress", detailAddress)
                .add("legalPerson", legalPerson)
                .add("contactNumber", contactNumber)
                .add("remark", remark)
                .toString();
    }

    public class WechatInfo {

        private final String wechatId;
        private final int type;

        WechatInfo(String wechatInfo) {
            String[] args = StringUtils.split(wechatInfo, ',');
            this.wechatId = args[0];
            this.type = Integer.valueOf(args[1]);
        }

        public boolean isFanser() {
            return 2 == this.type;
        }

        public boolean isWorker() {
            return 1 == this.type;
        }

        public String getWechatId() {
            return wechatId;
        }

        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WechatInfo)) return false;
            WechatInfo that = (WechatInfo) o;
            return type == that.type &&
                    Objects.equal(wechatId, that.wechatId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(wechatId, type);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("wechatId", wechatId)
                    .add("type", type)
                    .toString();
        }
    }

    public class DeviceInfo {
        private final String imei;
        private final int type;

        DeviceInfo(String deviceInfo) {
            String[] args = StringUtils.split(deviceInfo, ',');
            this.imei = args[0];
            this.type = Integer.valueOf(args[1]);
        }

        public String getImei() {
            return imei;
        }

        public int getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeviceInfo)) return false;
            DeviceInfo that = (DeviceInfo) o;
            return type == that.type &&
                    Objects.equal(imei, that.imei);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(imei, type);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("imei", imei)
                    .add("type", type)
                    .toString();
        }
    }

    private TreeStructure treeNode;

    public TreeStructure getTreeNode(TreeStructure parent) {
        if (this.treeNode != null) return this.treeNode;
        DefautTreeStructure node = new DefautTreeStructure(String.format("STR_%s", this.getId()),
                parent.getId(), this.fullName, this.getId());
        node.setAttachData("type", "STR");
        this.treeNode = node;
        return node;
    }
}
