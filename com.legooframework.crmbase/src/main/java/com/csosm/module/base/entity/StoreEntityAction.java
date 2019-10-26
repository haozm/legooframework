package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class StoreEntityAction extends BaseEntityAction<StoreEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreEntityAction.class);

    public StoreEntityAction() {
        super("StoreEntity", "adapterCache");
    }

    private static Predicate<StoreEntity> predicate = new Predicate<StoreEntity>() {
        @Override
        public boolean apply(@Nullable StoreEntity storeEntity) {
            return storeEntity.isStatusEnbaled();
        }
    };

    @Override
    protected ResultSetExtractor<StoreEntity> getResultSetExtractor() {
        return new ResultSetExtractorImpl();
    }
    /**
     * 新增门店
     * @param company
     * @param parent
     * @param name
     * @param phone
     * @param type
     * @param address
     */
    public void saveStore(LoginUserContext loginUser,OrganizationEntity company,OrganizationEntity parent,String name,String phone,Integer type,Integer state,String address) {
    	StoreEntity entity = new StoreEntity(company, parent, type, name, address, phone, state);
    	entity.setCreateUser(loginUser);
    	String sql = getExecSql("add_store", entity.toMap());
    	int result = getNamedParameterJdbcTemplate().update(sql, entity.toMap());
    	Preconditions.checkState(result == 1, "新增门店发生异常");
    	if(getCache().isPresent()) getCache().get().invalidateAll();
    	logProxy(SystemlogEntity.create( this.getClass(), "saveStore", String.format("组织【%s】添加门店【%s】", parent.getId(),entity),
                "组织管理"));
    }
    /**
     * 编辑门店
     * @param store
     * @param name
     * @param phone
     * @param type
     * @param address
     * @param hiddenPhone
     */
    public void editStore(LoginUserContext loginUser,StoreEntity store,String name,String phone,Integer type,Integer state,String address,Integer hiddenPhone) {
    	Objects.requireNonNull(store,"入参store不允许为空");
    	StoreEntity clone = store.modify(type, name, address, phone, state, hiddenPhone);
    	if(store.equals(clone)) return;
    	clone.setModifyUser(loginUser);
    	String sql = getExecSql("update_store", clone.toMap());
    	int result = getNamedParameterJdbcTemplate().update(sql, clone.toMap());
    	Preconditions.checkState(result == 1, "编辑门店发生异常");
    	if(getCache().isPresent()) getCache().get().invalidateAll();
    	logProxy(SystemlogEntity.update( this.getClass(), "editStore", String.format("编辑门店【%s】", clone),
                "组织管理"));
    }
    /**
     * 迁移门店
     * @param parent 上级组织
     * @param store 待迁移门店
     */
    public void switchStore(OrganizationEntity parent,StoreEntity store) {
    	Objects.requireNonNull(parent,"入参parent不能为空");
    	Objects.requireNonNull(store,"入参store不能为空");
    	if(store.isParent(parent)) return ;
    	store.changeParent(parent);
    	String sql = getExecSql("update_parent", store.toMap());
    	int result = getNamedParameterJdbcTemplate().update(sql, store.toMap());
    	Preconditions.checkState(result == 1, "迁移门店发生异常");
    	if(getCache().isPresent()) getCache().get().invalidateAll();
    	logProxy(SystemlogEntity.update( this.getClass(), "switchStore", String.format("迁移门店 from【%s】to 【%s】", store.getId(),parent.getId()),
                "组织管理"));
    }
    
    public boolean saveOrupdateBeforeDays(Integer storeId, int beforeDays) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空值....");
        Preconditions.checkArgument(beforeDays > 0, "必须填写大于0的数值...");
        Optional<StoreEntity> store = findById(storeId);
        Preconditions.checkState(store.isPresent(), "id=%s 对应的门店不存在...", storeId);
        Optional<StoreEntity> clone = store.get().modifyBeforeDays(beforeDays);
        if (clone.isPresent()) {
            Optional<Map<String, Object>> exits = getStoreExtInfo(store.get());
            if (exits.isPresent()) {
                getJdbc().update(getExecSql("updateBeforeDays", null), clone.get().toMap());
            } else {
                getJdbc().update(getExecSql("insertBeforeDays", null), clone.get().toMap());
            }
            logProxy(SystemlogEntity.update( this.getClass(), "saveOrupdateBeforeDays", String.format("设置门店【%s】新增天数【%s】", storeId,beforeDays),
                    "组织管理"));
            return true;
        }
        return false;
    }
    
    public boolean saveOrupdateBirthdayBefore(Integer storeId, int beforeDays) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空值....");
        Preconditions.checkArgument(beforeDays > 0, "必须填写大于0的数值...");
        Optional<StoreEntity> store = findById(storeId);
        Preconditions.checkState(store.isPresent(), "id=%s 对应的门店不存在...", storeId);
        Optional<StoreEntity> clone = store.get().modifyBirthdayBefore(beforeDays);
        if (clone.isPresent()) {
            Optional<Map<String, Object>> exits = getStoreExtInfo(store.get());
            if (exits.isPresent()) {
                getJdbc().update(getExecSql("updateBirthdayBefore", null), clone.get().toMap());
            } else {
                getJdbc().update(getExecSql("insertBirthdayBefore", null), clone.get().toMap());
            }
            logProxy(SystemlogEntity.update( this.getClass(), "saveOrupdateBeforeDays", String.format("设置门店【%s】新增天数【%s】", storeId,beforeDays),
                    "组织管理"));
            return true;
        }
        return false;
    }

    private Optional<Map<String, Object>> getStoreExtInfo(StoreEntity store) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("companyId", store.getCompanyId().or(-1));
        try {
            Map<String, Object> map = getJdbc().queryForMap(getExecSql("exitsStoreExt", null), params);
            return Optional.fromNullable(MapUtils.isEmpty(map) ? null : map);
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public boolean saveOrupdateQRcode(String deviceId, String qrCode) {
        Preconditions.checkNotNull(deviceId, "设备ID不可以为空值....");
        Preconditions.checkNotNull(qrCode, "门店二维码地址不可以为空...");
        Optional<StoreEntity> store = loadStoreByDeviceId(deviceId);
        Preconditions.checkState(store.isPresent(), "设备[%s]对应的门店不存在...", deviceId);
        Optional<StoreEntity> clone = store.get().modifyQRcode(qrCode);
        if (clone.isPresent()) {
            Optional<Map<String, Object>> exits = getStoreExtInfo(store.get());
            if (exits.isPresent()) {
                getJdbc().update(getExecSql("updateStoreQrCode", null), clone.get().toMap());
            } else {
                getJdbc().update(getExecSql("insertStoreQrCode", null), clone.get().toMap());
            }
            return true;
        }
        return false;
    }

    public Optional<StoreEntity> loadStoreByDeviceId(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "入参 String deviceId 不可以为空...");
        Map<String, Object> param = Maps.newHashMap();
        param.put("deviceId", deviceId);
        try {
            StoreEntity store = getNamedParameterJdbcTemplate()
                    .queryForObject(getExecSql("loadStoreByDeviceId", param), param, new RowMapperImpl());
            return Optional.fromNullable(store.isStatusEnbaled() ? store : null);
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public Optional<StoreEntity> findStoreFromCompany(int storeId, OrganizationEntity organization) {
        Preconditions.checkNotNull(organization, "入参 organization 不可以为空...");
        Preconditions.checkState(organization.isCompany());
        Map<String, Object> param = Maps.newHashMap();
        param.put("id", storeId);
        param.put("companyId", organization.getId());
        try {
            StoreEntity store = getNamedParameterJdbcTemplate()
                    .queryForObject(getExecSql("findStoreFromCompany", param), param, new RowMapperImpl());
            return Optional.fromNullable(store.isStatusEnbaled() ? store : null);
        } catch (EmptyResultDataAccessException e) {
            return Optional.absent();
        }
    }

    public Optional<List<StoreEntity>> loadAllStoreByCompany(OrganizationEntity company) {
        Preconditions.checkNotNull(company, "入参OrganizationEntity company 不可以为空...");
        Preconditions.checkState(company.isCompany(), "转入组织非公司....");
        Map<String, Object> param = Maps.newHashMap();
        param.put("companyId", company.getId());
        List<StoreEntity> stores = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadAllStoreByCompany", param), param, new RowMapperImpl());
        List<StoreEntity> _sts = filter(stores);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }
    
    public Optional<List<StoreEntity>> loadAllStoreByOrganization(OrganizationEntity organization) {
        Preconditions.checkNotNull(organization, "入参OrganizationEntity organization 不可以为空...");
        Map<String, Object> param = Maps.newHashMap();
        param.put("orgId", organization.getId());
        List<StoreEntity> stores = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadAllStoreByOrg", param), param, new RowMapperImpl());
        List<StoreEntity> _sts = filter(stores);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }

    public Optional<List<StoreEntity>> loadByHasDevice() {
        Map<String, Object> param = Maps.newHashMap();
        List<StoreEntity> stores = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadByHasDevice", param), param, new RowMapperImpl());
        List<StoreEntity> _sts = filter(stores);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }

    /**
     * 通过一组门店ID获取对应的实体列表
     *
     * @param ids Collection
     * @return StoreEntity 列表， 可能为null
     */
    public Optional<List<StoreEntity>> findByIds(Collection<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return Optional.absent();
        Map<String, Object> param = Maps.newHashMap();
        param.put("ids", ids);
        if (logger.isDebugEnabled())
            logger.debug(String.format("query(%s,%s,%s)", getModel(), "findByIds", param));
        List<StoreEntity> storeEntities =
                getNamedParameterJdbcTemplate()
                        .query(getExecSql("findByIds", param), param, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("ids=%s query store size is %s",
                    ids, CollectionUtils.isEmpty(storeEntities) ? 0 : storeEntities.size()));
        List<StoreEntity> _sts = filter(storeEntities);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }

    private List<StoreEntity> filter(List<StoreEntity> list) {
        if (CollectionUtils.isEmpty(list)) return null;
        Collection<StoreEntity> _sts = Collections2.filter(list, predicate);
        return CollectionUtils.isEmpty(_sts) ? null : Lists.newArrayList(_sts);
    }

    /**
     * 通过组织 该组织下的所有门店实体列表,含下级所有组织节点
     *
     * @param organization 组织实体
     * @return 门店列表
     */
    @SuppressWarnings("unchecked")
    public Optional<List<StoreEntity>> loadAllSubStoreByOrg(OrganizationEntity organization) {
        Preconditions.checkNotNull(organization, "入参部门实体不可以为NULL");
        final String cache_key =
                String.format("%s_all_storesByOrg_%s", getModel(), organization.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().getIfPresent(cache_key);
            if (cache_val != null) {
                List<StoreEntity> store_list = (List<StoreEntity>) cache_val;
                if (logger.isDebugEnabled())
                    logger.debug(
                            String.format("%s get loadAllSubStoreByOrg From Cahce And size is %s",
                                    organization.getName(),
                                    CollectionUtils.isEmpty(store_list) ? 0 : store_list.size()));
                return Optional.fromNullable(CollectionUtils.isEmpty(store_list) ? null : store_list);
            }
        }

        Map<String, Object> param = Maps.newHashMap();
        param.put("orgCode", String.format("%s%%", organization.getCode()));
        if (logger.isDebugEnabled())
            logger.debug(String.format("query(%s,%s,%s)", getModel(), "loadAllSubStoreByOrg", param));
        List<StoreEntity> store_list = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadAllSubStoreByOrg", param), param, new RowMapperImpl());
        List<StoreEntity> _sts = filter(store_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s loadAllSubStoreByOrg size is %s",
                    organization.getName(), CollectionUtils.isEmpty(_sts) ? 0 : _sts.size()));
        if (getCache().isPresent() && !CollectionUtils.isEmpty(_sts))
            getCache().get().put(cache_key, _sts);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }

    @SuppressWarnings("unchecked")
    public Optional<List<StoreEntity>> loadDirectSubStoreByOrg(OrganizationEntity organization) {
        Preconditions.checkNotNull(organization, "入参部门实体不可以为NULL");
        final String cache_key =
                String.format("%s_direc_storesByOrg_%s", getModel(), organization.getId());
        if (getCache().isPresent()) {
            Object cache_val = getCache().get().getIfPresent(cache_key);
            if (cache_val != null) {
                List<StoreEntity> store_list = (List<StoreEntity>) cache_val;
                if (logger.isDebugEnabled())
                    logger.debug(String.format("%s get loadDirectSubStoreByOrg From Cahce And size is %s",
                            organization.getName(), CollectionUtils.isEmpty(store_list) ? 0 : store_list.size()));
                return Optional.fromNullable(CollectionUtils.isEmpty(store_list) ? null : store_list);
            }
        }

        Map<String, Object> param = Maps.newHashMap();
        param.put("orgId", organization.getId());
        if (logger.isDebugEnabled())
            logger.debug(String.format("query(%s,%s,%s)", getModel(), "loadDirectSubStoreByOrg", param));
        List<StoreEntity> store_list = getNamedParameterJdbcTemplate()
                .query(getExecSql("loadDirectSubStoreByOrg", param), param, new RowMapperImpl());
        if(CollectionUtils.isEmpty(store_list)) return Optional.absent();
        List<StoreEntity> _sts = filter(store_list);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s has sub direct Store size is %s",
                    organization.getName(), CollectionUtils.isEmpty(_sts) ? 0 : _sts.size()));
        if (getCache().isPresent() || !CollectionUtils.isEmpty(_sts))
            getCache().get().put(cache_key, _sts);
        return Optional.fromNullable(CollectionUtils.isEmpty(_sts) ? null : _sts);
    }
    
    public List<StoreEntity> loadTreeStores(EmployeeEntity employee){
    	Objects.requireNonNull(employee);
    	Integer empId = employee.getId();
    	Map<String,Object> params = Maps.newHashMap();
    	params.put("empId", empId);
    	String sql = getExecSql("load_tree_stores", params);
    	List<StoreEntity> stores = getNamedParameterJdbcTemplate()
                .query(sql, params, new RowMapperImpl());
    	if(CollectionUtils.isEmpty(stores)) return Collections.EMPTY_LIST;
    	return stores;
    }
    class RowMapperImpl implements RowMapper<StoreEntity> {
        @Override
        public StoreEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new StoreEntity(
                    resultSet.getInt("id"),
                    resultSet.getInt("type"),
                    resultSet.getString("name"),
                    resultSet.getString("address"),
                    resultSet.getString("oldStoreId"),
                    resultSet.getInt("status"),
                    resultSet.getInt("orgId"),
                    resultSet.getInt("companyId"),
                    resultSet.getString("deviceIds"),
                    resultSet.getString("qrCode"),
                    resultSet.getInt("birthdayBefore"),
                    resultSet.getInt("birthdayBefore"),
                    resultSet.getString("phone"),
                    resultSet.getInt("hiddenMemberPhoneFlag"),
                    resultSet.getInt("state"),
                    resultSet.getObject("rfmSetting") == null ? null : resultSet.getInt("rfmSetting"));
        }
    }

    class ResultSetExtractorImpl implements ResultSetExtractor<StoreEntity> {

        @Override
        public StoreEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            if (resultSet.next()) {
                return new StoreEntity(
                        resultSet.getInt("id"),
                        resultSet.getInt("type"),
                        resultSet.getString("name"),
                        resultSet.getString("address"),
                        resultSet.getString("oldStoreId"),
                        resultSet.getInt("status"),
                        resultSet.getObject("orgId") == null ? null : resultSet.getInt("orgId"),
                        resultSet.getObject("companyId") == null ? null : resultSet.getInt("companyId"),
                        resultSet.getString("deviceIds"),
                        resultSet.getString("qrCode"),
                        resultSet.getInt("birthdayBefore"),
                        resultSet.getInt("beforeDays"),
                        resultSet.getString("phone"),
                        resultSet.getInt("hiddenMemberPhoneFlag"),
                        resultSet.getInt("state"),
                        resultSet.getObject("rfmSetting") == null ? null : resultSet.getInt("rfmSetting")
                        );
            }
            return null;
        }
    }
}
