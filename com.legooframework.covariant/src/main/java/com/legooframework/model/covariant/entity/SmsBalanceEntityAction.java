package com.legooframework.model.covariant.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SmsBalanceEntityAction extends BaseEntityAction<SmsBalanceEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SmsBalanceEntityAction.class);

    public SmsBalanceEntityAction() {
        super(null);
    }

    Optional<SmsBalanceEntity> find4Com(StoEntity store) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("orgId", store.getCompanyId());
        params.put("sql", "findByOrg");
        Optional<SmsBalanceEntity> exits = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Org=%s SmsBalance is %s", store.getCompanyId(), exits.map(SmsBalanceEntity::getCount).orElse(0)));
        exits.ifPresent(c -> c.setType(1));
        return exits;
    }

    Optional<SmsBalanceEntity> find4Store(StoEntity store) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("sql", "findByStore");
        Optional<SmsBalanceEntity> exits = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Store=%s SmsBalance is %s", store.getId(), exits.map(SmsBalanceEntity::getCount).orElse(0)));
        exits.ifPresent(c -> c.setType(3));
        return exits;
    }

    private Optional<SmsBalanceEntity> find4Com(OrgEntity company) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("orgId", company.getId());
        params.put("sql", "findByOrg");
        Optional<SmsBalanceEntity> exits = findByParams(params);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Org=%s SmsBalance is %s", company.getId(), exits.map(SmsBalanceEntity::getCount).orElse(0)));
        exits.ifPresent(c -> c.setType(1));
        return exits;
    }

    /**
     * 发送短信失败
     *
     * @param store StoEntity
     * @param count count
     * @throws SmsBillingException e
     */
    public synchronized void billing(StoEntity store, int count) throws SmsBillingException {
        int subtraction = count;
        List<SmsBalanceEntity> smsBalances = Lists.newArrayList();
        Optional<SmsBalanceEntity> store_balance = find4Store(store);
        if (store_balance.isPresent() && store_balance.get().hasBalance()) {
            subtraction = store_balance.get().billing(subtraction);
            smsBalances.add(store_balance.get());
            if (logger.isDebugEnabled())
                logger.debug(String.format("billing(storeId= %s,%s) by store remaining %s", store.getId(), count, subtraction));
        }
        if (subtraction != 0) {
            Optional<SmsBalanceEntity> com_balance = find4Com(store);
            if (com_balance.isPresent()) {
                int before_sub = subtraction;
                subtraction = com_balance.get().billing(subtraction);
                smsBalances.add(com_balance.get());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("billing(companyId = %s,%s) by company", store.getCompanyId(), before_sub));
            }
        }
        if (subtraction != 0)
            throw new SmsBillingException(String.format("billing(store= %s,smsCount= %s) 余额不足,计费失败。", store.getId(), count));
        // UPDATE
        batchUpdate(smsBalances);
    }

    public synchronized void billing(OrgEntity company, int count) throws SmsBillingException {
        int subtraction = count;
        List<SmsBalanceEntity> smsBalances = Lists.newArrayList();
        Optional<SmsBalanceEntity> com_balance = find4Com(company);
        if (com_balance.isPresent()) {
            int before_sub = subtraction;
            subtraction = com_balance.get().billing(subtraction);
            smsBalances.add(com_balance.get());
            if (logger.isDebugEnabled())
                logger.debug(String.format("billing(companyId = %s,%s) by company", company.getId(), before_sub));
        }
        if (subtraction != 0)
            throw new SmsBillingException(String.format("billing(store= %s,smsCount= %s) 余额不足,计费失败。", company.getId(), count));
        // UPDATE
        batchUpdate(smsBalances);
    }

    private void batchUpdate(List<SmsBalanceEntity> smsBalances) {
        if (CollectionUtils.isEmpty(smsBalances)) return;
        super.batchUpdate("updateBalance", (ps, balance) -> {
            ps.setObject(1, balance.getCount());
            ps.setObject(2, balance.getId());
        }, smsBalances);
    }

    private Optional<SmsBalanceEntity> findByParams(Map<String, Object> params) {
        return super.queryForEntity("query4Obj", params, getRowMapper());
    }

    @Override
    protected RowMapper<SmsBalanceEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<SmsBalanceEntity> {
        @Override
        public SmsBalanceEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new SmsBalanceEntity(resultSet.getInt("id"), resultSet);
        }
    }
}
