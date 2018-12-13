package com.legooframework.model.organization.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.utils.ExceptionUtil;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.wechat.entity.WechatAccountEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StoreEntityAction extends BaseEntityAction<StoreEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreEntityAction.class);

    public StoreEntityAction() {
        super("OrganizationCache");
    }

    /**
     * @return 新增后的门店ID
     */
    public Long insert(String storeCode, String fullName, String shortName,
                       String businessLicense, String detailAddress, String legalPerson,
                       String contactNumber, String remark, KvDictDto storeType) {
        LoginContextHolder.get();
        idGenerator++;
        long id = idGenerator;
        StoreEntity entity = new StoreEntity(id, storeCode,
                fullName, shortName, businessLicense, detailAddress, legalPerson, contactNumber, remark, storeType,
                LoginContextHolder.get());
        Optional<List<StoreEntity>> exits = queryForEntities(getStatementFactory(), getModelName(), "exits",
                entity.toParamMap(), getRowMapper());
        Preconditions.checkState(!exits.isPresent(),
                "存在门店编码:%s 或门店全称:%s 有重复数据,无法完成门店新增操作.", storeCode, fullName);
        int res = update(getStatementFactory(), getModelName(), "insert", entity);
        update(getStatementFactory(), getModelName(), "insert_store_info", entity);
        Preconditions.checkState(1 == res, "新增门店信息写入数据库失败");
        final String cache_key = String.format("%s_allstore_%s", getModelName(), LoginContextHolder.get().getTenantId());
        getCache().ifPresent(c -> c.evict(cache_key));
        return entity.getId();
    }

    // 通过门店编码获取门店
    @SuppressWarnings("unchecked")
    public Optional<List<StoreEntity>> loadAllByCompany(CompanyEntity company) {
        Preconditions.checkNotNull(company, "in-param company id can not be null");
        final String cache_key = String.format("%s_allstore_%s", getModelName(), company.getId());
        if (getCache().isPresent()) {
            List<StoreEntity> list = (List<StoreEntity>) getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("companyId", company.getId());
            Optional<List<StoreEntity>> entity = super.queryForEntities(getStatementFactory(), getModelName(),
                    "loadAllByCompany", params, getRowMapper());
            if (logger.isDebugEnabled())
                logger.debug(String.format("公司 %s 共计下属 %s 家门店信息.", company.getFullName(),
                        entity.map(List::size).orElse(0)));
            entity.ifPresent(x -> getCache().ifPresent(c -> c.put(cache_key, x)));
            return entity;
        } catch (Exception e) {
            throw ExceptionUtil.handleException(e, String.format("%s(%s) has error", "findAllByCompany",
                    company.getId()), logger);
        }
    }

    public Optional<List<StoreEntity>> loadStoresByCompany(CompanyEntity company, Collection<Long> storeIds) {
        Preconditions.checkNotNull(company, "in-param company id can not be null");
        if (CollectionUtils.isEmpty(storeIds)) return Optional.empty();
        Optional<List<StoreEntity>> stores = loadAllByCompany(company);
        if (!stores.isPresent()) return Optional.empty();
        List<StoreEntity> exits = stores.get().stream().filter(x -> storeIds.contains(x.getId())).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(exits) ? null : exits);
    }

    // 通过设备找门店
    public Optional<StoreEntity> findByEquipment(EquipmentEntity equipment) {
        Preconditions.checkNotNull(equipment, "入参设备(equipment)不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", equipment.getId());
        return super.queryForEntity("findByEquipment", params, getRowMapper());
    }

    // 门店绑定微信
    public boolean bindingWechat(Long storeId, WechatAccountEntity wechatAccount) {
        Preconditions.checkNotNull(wechatAccount, "待添加的微信账号不可以为空...");
        Optional<StoreEntity> store = findById(storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s的门店实体不存在....", storeId);
        Preconditions.checkState(store.get().isEffective(), "当前状态无效，无法开展业务....");
        Map<String, Object> params = LoginContextHolder.get().toParams();
        params.put("wechatId", wechatAccount.getUserName());

        Optional<Long> exits_storeId = super.queryForObject(getStatementFactory(), getModelName(),
                "findStoreIdByWechatId", params, Long.class);
        if (exits_storeId.isPresent()) {
            Preconditions.checkState(Longs.compare(exits_storeId.get(), storeId) == 0,
                    "微信%s已经绑定其他门店....", wechatAccount.getUserName());
            return true;
        }
        Optional<Map<String, Object>> save_data = store.get().assignWechat(wechatAccount, 0);
        if (save_data.isPresent()) {
            int res = super.update(getStatementFactory(), getModelName(), "build_wechat", save_data.get());
            saveOrUpdateLinkedbByWechat(storeId, wechatAccount.getUserName());
            Preconditions.checkState(1 == res, "持久化绑定关系失败...");
            evictEntity(store.get());
            return true;
        }
        return false;
    }

    // 门店绑定收集设备
    public boolean bindingDeviceToStore(Long storeId, EquipmentEntity equipment) {
        Preconditions.checkNotNull(equipment, "待绑定的设备不可以为空值...");
        equipment.canBindingToStore();
        if (equipment.getStoreId().isPresent()) {
            Preconditions.checkState(equipment.getStoreId().get().longValue() == storeId.longValue(),
                    "当前设备已经绑定其他门店，无法重复绑定...");
        }
        Optional<StoreEntity> store = findById(storeId);
        Preconditions.checkState(store.isPresent(), "ID=%s的门店实体不存在....", storeId);
        Preconditions.checkState(store.get().isEffective(), "当前状态无效，无法开展业务....");
        Preconditions.checkState(store.get().isSameTenant(equipment), "指定的设备与门店不属于同一公司，异常操作...");

        Optional<Map<String, Object>> add_eq = store.get().assignEquipment(equipment, true);
        if (add_eq.isPresent()) {
            int res = super.update(getStatementFactory(), getModelName(), "build_device", add_eq.get());
            // 维护其关联关系
            saveOrUpdateLinkedByDevice(store.get().getId(), equipment.getId());
            // 维护其关联关系
            Preconditions.checkState(1 == res, "持久化绑定关系失败...");
            evictEntity(store.get());
            return true;
        }
        return false;
    }

    private void saveOrUpdateLinkedbByWechat(Long storeId, String wechatId) {
        Map<String, Object> params = LoginContextHolder.get().toParams();
        params.put("storeId", storeId);
        params.put("wechatId", wechatId);
        Optional<Integer> exits_id = super.queryForObject(getStatementFactory(), "stroreLinked",
                "findLinkedBystoreId", params, Integer.class);
        if (exits_id.isPresent()) {
            params.put("id", exits_id.get());
            super.update(getStatementFactory(), "stroreLinked", "bildWechatById", params);
        } else {
            super.update(getStatementFactory(), "stroreLinked", "insert4wechat", params);
        }
    }

    private void saveOrUpdateLinkedByDevice(Long storeId, String deviceId) {
        Map<String, Object> params = LoginContextHolder.get().toParams();
        params.put("storeId", storeId);
        params.put("deviceId", deviceId);
        Optional<Integer> exits_id = super.queryForObject(getStatementFactory(), "stroreLinked",
                "findLinkedBystoreId", params, Integer.class);
        if (exits_id.isPresent()) {
            params.put("id", exits_id.get());
            super.update(getStatementFactory(), "stroreLinked", "bildStoreById", params);
        } else {
            super.update(getStatementFactory(), "stroreLinked", "insert4device", params);
        }
    }

    public boolean unBindingByDevice(EquipmentEntity equipment) {
        Optional<StoreEntity> store = findByEquipment(equipment);
        if (!store.isPresent()) {
            logger.warn(String.format("设备%s未绑定任何门店，忽略本次解绑操作...", equipment.getId()));
            return true;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", equipment.getId());
        params.put("storeId", store.get().getId());
        int res = super.update(getStatementFactory(), getModelName(), "unbuild_device", params);
        res += super.update(getStatementFactory(), getModelName(), "unlinked_device", params);
        if (res != 0) {
            if (logger.isInfoEnabled())
                logger.info(String.format("解除门店%s 与设备 %s 绑定关系成功...", store.get().getId(), equipment.getId()));
            evictEntity(store.get());
            return true;
        }
        return false;
    }

    @Override
    public void evictEntity(StoreEntity entity) {
        super.evictEntity(entity);
        final String cache_key = String.format("%s_allstore_%s", getModelName(), entity.getTenantId());
        getCache().ifPresent(c -> c.evict(cache_key));
    }

    @Override
    protected RowMapper<StoreEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<StoreEntity> {
        @Override
        public StoreEntity mapRow(ResultSet res, int rowNum) throws SQLException {
            return new StoreEntity(res.getLong("storeId"), res);
        }
    }

    private long idGenerator;

    public void init() {
        long max_id = queryForLong("SELECT MAX(id) FROM org_base_info ", 10000L);
        this.idGenerator = max_id + 1;
    }
}
