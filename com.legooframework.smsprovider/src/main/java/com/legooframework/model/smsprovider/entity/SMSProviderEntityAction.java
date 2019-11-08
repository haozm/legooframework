package com.legooframework.model.smsprovider.entity;

import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SMS 供应商通道管理
 */
public class SMSProviderEntityAction extends BaseEntityAction<SMSProviderEntity> {

    public SMSProviderEntityAction() {
        super("smsProviderCache");
    }

    public SMSProviderEntity loadSMSSupplier() {
        List<SMSProviderEntity> providers = this.loadAllProviders();
        Preconditions.checkState(providers.size() == 1);
        List<SMSSubAccountEntity> subAccounts = loadAllSubAccounts();
        providers.get(0).setSubAccounts(subAccounts);
        return providers.get(0);
    }

    List<SMSSubAccountEntity> loadEnabledSubAccounts() {
        List<SMSSubAccountEntity> accounts = loadAllSubAccounts();
        accounts = accounts.stream().filter(SMSSubAccountEntity::isEnabled).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(accounts), "无可激活的通道定义....");
        return accounts;
    }

    public Optional<List<SMSSubAccountEntity>> findEnabledSubAccounts() {
        List<SMSSubAccountEntity> accounts = loadAllSubAccounts();
        accounts = accounts.stream().filter(SMSSubAccountEntity::isEnabled).collect(Collectors.toList());
        return Optional.ofNullable(CollectionUtils.isEmpty(accounts) ? null : accounts);
    }

    public SMSSubAccountEntity loadSubAccountByChannel(SMSChannel channel) {
        List<SMSSubAccountEntity> accounts = loadAllSubAccounts();
        Optional<SMSSubAccountEntity> optional = accounts.stream().filter(SMSSubAccountEntity::isEnabled).filter(x -> x.isChannel(channel))
                .findFirst();
        Preconditions.checkState(optional.isPresent(), "无SMSChannel=%s 对应的通道.....", channel);
        return optional.get();
    }

    List<SMSSubAccountEntity> loadAllSubAccounts() {
        final String cache_key = String.format("%s_company_all", getModelName());
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<SMSSubAccountEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return list;
        }
        Optional<List<SMSSubAccountEntity>> accounts = super.queryForEntities("loadAllSubAccounts", null,
                new SMSSubAccountRowMapperImpl());
        Preconditions.checkState(accounts.isPresent(), "不存在发送渠道定义....");
        getCache().ifPresent(c -> accounts.ifPresent(l -> c.put(cache_key, l)));
        return accounts.get();
    }

    List<SMSProviderEntity> loadAllProviders() {
        final String cache_key = String.format("%s_provider_all", getModelName());
        if (getCache().isPresent()) {
            @SuppressWarnings("unchecked")
            List<SMSProviderEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return list;
        }
        Optional<List<SMSProviderEntity>> accounts = super.queryForEntities("loadAllProviders", null, new SMSProviderRowMapperImpl());
        Preconditions.checkState(accounts.isPresent(), "不存在发送渠道定义....");
        getCache().ifPresent(c -> accounts.ifPresent(l -> c.put(cache_key, l)));
        return accounts.get();
    }

    @Override
    protected RowMapper<SMSProviderEntity> getRowMapper() {
        return new SMSProviderRowMapperImpl();
    }

    private static class SMSSubAccountRowMapperImpl implements RowMapper<SMSSubAccountEntity> {
        @Override
        public SMSSubAccountEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSSubAccountEntity(res.getString("id"), res);
        }
    }

    private static class SMSProviderRowMapperImpl implements RowMapper<SMSProviderEntity> {
        @Override
        public SMSProviderEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSProviderEntity(res.getString("id"), res);
        }
    }
}
