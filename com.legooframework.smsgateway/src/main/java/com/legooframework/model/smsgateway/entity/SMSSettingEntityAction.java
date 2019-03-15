package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SMSSettingEntityAction extends BaseEntityAction<SMSSettingEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSSettingEntityAction.class);

    public SMSSettingEntityAction() {
        super("smsGateWayCache");
    }

    public SMSSettingEntity loadByStore(CrmStoreEntity store) {
        Preconditions.checkNotNull(store);
        List<SMSSettingEntity> settings = loadAllByCompany(store.getCompanyId());
        Optional<SMSSettingEntity> setting = settings.stream().filter(x -> x.isStore(store)).findFirst();
        if (setting.isPresent()) return setting.get();
        setting = settings.stream().filter(x -> x.isCompany(store)).findFirst();
        Preconditions.checkState(setting.isPresent(), "公司%s,门店%s尚未初始化短信相关配置...", store.getCompanyId(),
                store.getId());
        return setting.get();
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

    class RowMapperImpl implements RowMapper<SMSSettingEntity> {
        @Override
        public SMSSettingEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSSettingEntity(res.getInt("id"), res);
        }
    }
}
