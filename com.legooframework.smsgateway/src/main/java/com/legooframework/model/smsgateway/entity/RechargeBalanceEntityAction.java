package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class RechargeBalanceEntityAction extends BaseEntityAction<RechargeBalanceEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RechargeBalanceEntityAction.class);

    private static final Ordering<RechargeBalanceEntity> ordering = Ordering
            .from(Comparator.comparingInt((ToIntFunction<RechargeBalanceEntity>) value -> value.getRechargeScope().getScope()))
            .reverse();

    public RechargeBalanceEntityAction() {
        super(null);
    }

    public void addBalance(RechargeDetailEntity recharge, int smsNum) {
        Preconditions.checkNotNull(recharge);
        RechargeBalanceEntity instance = new RechargeBalanceEntity(recharge);
        Optional<RechargeBalanceEntity> exits = findByInstance(instance);
        if (exits.isPresent()) {
            exits.get().addBalance(smsNum);
            super.updateAction(exits.get(), "update");
        } else {
            instance.addBalance(smsNum);
            super.updateAction(instance, "insert");
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("addBalance(%s,%s) is ok", recharge, smsNum));
    }

    Optional<RechargeBalanceEntity> findByInstance(RechargeBalanceEntity instance) {
        Optional<RechargeBalanceEntity> optional = queryForEntity("findByInstance", instance.toParamMap(), getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByInstance(%s) res %s", instance, optional.orElse(null)));
        return optional;
    }

    public void batchUpdateBalance(Collection<RechargeBalanceEntity> balances) {
        super.batchUpdate("batchUpdateBalance", (ps, balance) -> {
            ps.setObject(1, balance.getBalance());
            ps.setObject(2, balance.getId());
        }, balances);
    }

    /**
     * 加载一批指定ID的 余额记录
     *
     * @param balanceIds
     * @return
     */
    public List<RechargeBalanceEntity> loadByIds(Collection<String> balanceIds) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(balanceIds));
        Map<String, Object> params = Maps.newHashMap();
        params.put("balanceIds", balanceIds);
        Optional<List<RechargeBalanceEntity>> balanceList = super.queryForEntities("loadByIds", params, getRowMapper());
        Preconditions.checkState(balanceList.isPresent());
        return balanceList.get();
    }

    /**
     * 门店可供扣除的余额
     *
     * @param store
     * @return
     */
    public RechargeBalanceList loadOrderEnabledByStore(CrmStoreEntity store) {
        Optional<List<RechargeBalanceEntity>> optional = loadAllByCompanyId(store.getCompanyId());
        Preconditions.checkState(optional.isPresent(), "当前门店以及公司没有可供支配的短信余额...");
        List<RechargeBalanceEntity> list = optional.get().stream().filter(x -> x.contains(store))
                .filter(RechargeBalanceEntity::hasBlance).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(list), "当前门店以及公司没有可供支配的短信余额...");
        if (CollectionUtils.isNotEmpty(list)) list.sort(ordering);
        return new RechargeBalanceList(list);
    }

    private Optional<List<RechargeBalanceEntity>> loadAllByCompanyId(Integer companyId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        Optional<List<RechargeBalanceEntity>> optional = super.queryForEntities("loadAllByCompany", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllByCompanyId(%s) size is %s", companyId, optional.map(List::size).orElse(0)));
        return optional;
    }

    @Override
    protected RowMapper<RechargeBalanceEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<RechargeBalanceEntity> {
        @Override
        public RechargeBalanceEntity mapRow(ResultSet res, int i) throws SQLException {
            return new RechargeBalanceEntity(res.getString("id"), res);
        }
    }
}
