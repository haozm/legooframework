package com.legooframework.model.smsprovider.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SMSSettingEntityAction extends BaseEntityAction<SMSSettingEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSSettingEntityAction.class);

    public SMSSettingEntityAction() {
        super("smsProviderCache");
    }

    public Optional<List<SMSSettingEntity>> checkSmsPrefix(StoEntity store, String smsPrefix) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsPrefix", smsPrefix);
        Optional<List<SMSSettingEntity>> exit_list = super.queryForEntities("findBySMSPrefix", params, new RowMapperImpl());
        exit_list.ifPresent(smsSettings -> smsSettings.forEach(x -> Preconditions.checkState(x.getCompanyId().equals(store.getCompanyId()),
                "跨公司存在前缀同样的设置,修改失败....")));
        if (!exit_list.isPresent()) return Optional.empty();
        List<SMSSettingEntity> sub_list = exit_list.get().stream().filter(x -> !x.getStoreId().equals(store.getId()))
                .collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_list) ? null : sub_list);
    }

    public void changeSmsPrefix(StoEntity store, String smsPrefix) {
        LoginContext user = LoginContextHolder.get();
        checkSmsPrefix(store, smsPrefix);
        SMSSettingEntity smsSetting = loadByStoreId(store.getCompanyId(), store.getId());
        Optional<SMSSettingEntity> clone = smsSetting.changeSmsPrefix(smsPrefix, user);
        clone.ifPresent(x -> {
            super.updateAction(x, "updateSmsPrefix");
            final String cache_key = String.format("%s_setting_%s", getModelName(), store.getCompanyId());
            getCache().ifPresent(c -> c.evict(cache_key));
        });
    }

    public SMSSettingEntity loadByStore(StoEntity store) {
        List<SMSSettingEntity> settings = loadAllByCompany(store.getCompanyId());
        Optional<SMSSettingEntity> setting = null;
        setting = settings.stream().filter(x -> x.isStore(store.getCompanyId(), store.getId())).findFirst();
        if (setting.isPresent()) return setting.get();
        setting = settings.stream().filter(x -> x.isCompany(store.getCompanyId())).findFirst();
        Preconditions.checkState(setting.isPresent(), "公司%s,门店%s尚未初始化短信相关配置...", store.getCompanyId(), store.getId());
        return setting.get();
    }

    public SMSSettingEntity loadByStoreId(final Integer companyId, final Integer storeId) {
        Preconditions.checkNotNull(companyId);
        List<SMSSettingEntity> settings = loadAllByCompany(companyId);
        Optional<SMSSettingEntity> setting = null;
        if (storeId != -1) {
            setting = settings.stream().filter(x -> x.isStore(companyId, storeId)).findFirst();
            if (setting.isPresent()) return setting.get();
        }
        setting = settings.stream().filter(x -> x.isCompany(companyId)).findFirst();
        Preconditions.checkState(setting.isPresent(), "公司%s,门店%s尚未初始化短信相关配置...", companyId, storeId);
        return setting.get();
    }

    public List<SMSSettingEntity> loadByStoreIds(final Integer companyId, final Integer... storeIds) {
        Preconditions.checkNotNull(companyId);
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(storeIds), "门店ID不可以空值...");
        List<SMSSettingEntity> settings = loadAllByCompany(companyId);
        List<SMSSettingEntity> sub_list = settings.stream().filter(x -> ArrayUtils.contains(storeIds, x.getStoreId()))
                .collect(Collectors.toList());
        return sub_list;
    }

    @SuppressWarnings("unchecked")
    private List<SMSSettingEntity> loadAllByCompany(Integer companyId) {
        final String cache_key = String.format("%s_setting_%s", getModelName(), companyId);
        if (getCache().isPresent()) {
            List<SMSSettingEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return list;
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<SMSSettingEntity>> settings = super.queryForEntities("loadAllByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompany(%s) size is %s", companyId, settings.map(List::size).orElse(0)));
        Preconditions.checkState(settings.isPresent(), "公司%s尚未初始化短信相关配置...", companyId);
        return settings.get();
    }

    @Override
    protected RowMapper<SMSSettingEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SMSSettingEntity> {
        @Override
        public SMSSettingEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSSettingEntity(res);
        }
    }
}
