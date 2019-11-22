package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
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

    public RechargeBalanceEntity createStoreGroupBalance(OrgEntity company, List<StoEntity> stores, String name) {
        RechargeBalanceEntity instance = new RechargeBalanceEntity(company, stores, name);
        Optional<List<RechargeBalanceEntity>> exits_list_opt = findAllStoreGroupBalance(company);
        if (exits_list_opt.isPresent() && !instance.isEmptyStoreIds()) {
            List<RechargeBalanceEntity> exits_list = exits_list_opt.get();
            Set<Integer> _raw = Sets.newHashSet(instance.getStoreIds());
            for (RechargeBalanceEntity exits : exits_list) {
                if (exits.isEmptyStoreIds()) continue;
                Set<Integer> $it = Sets.newHashSet(exits.getStoreIds());
                Sets.SetView<Integer> intersection = Sets.intersection(_raw, $it);
                Preconditions.checkState(CollectionUtils.isEmpty(intersection), "已经存在门店%s 在其他分组", intersection);
            }
        }
        super.updateAction(instance, "insert");
        return instance;
    }

    public void editStoreGroupBalance(OrgEntity company, String blanceId, int action, List<StoEntity> stores) {
        Preconditions.checkState(ArrayUtils.contains(new int[]{0, 1, -1}, action), "非法入参 action=%s", action);
        if (action != -1 && CollectionUtils.isEmpty(stores)) return;
        RechargeBalanceEntity instance = loadById(blanceId);
        Preconditions.checkState(RechargeScope.StoreGroup == instance.getRechargeScope());
        Preconditions.checkState(instance.isEmpty(), "该节点下有充值记录无法编辑");
        if (action == 1) {
            boolean addFlag = instance.addStores(stores);
            if (addFlag) {
                Optional<List<RechargeBalanceEntity>> exits_list_opt = findAllStoreGroupBalance(company);
                if (exits_list_opt.isPresent()) {
                    List<RechargeBalanceEntity> exits_list = exits_list_opt.get();
                    Set<Integer> _raw = Sets.newHashSet(instance.getStoreIds());
                    for (RechargeBalanceEntity exits : exits_list) {
                        if (exits.isEmptyStoreIds() || exits.getId().equals(blanceId)) continue;
                        Set<Integer> $it = Sets.newHashSet(exits.getStoreIds());
                        Sets.SetView<Integer> intersection = Sets.intersection(_raw, $it);
                        Preconditions.checkState(CollectionUtils.isEmpty(intersection), "已经存在门店%s 在其他分组", intersection);
                    }
                }
                Objects.requireNonNull(getJdbcTemplate()).update("UPDATE SMS_RECHARGE_BALANCE SET store_ids = ? WHERE id = ?",
                        instance.getStoreIdsRaw(), instance.getId());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("新增门店[%d]个成功", stores.size()));
            }
        } else if (action == 0) {
            boolean delFlag = instance.delStores(stores);
            if (delFlag) {
                Objects.requireNonNull(getJdbcTemplate()).update("UPDATE SMS_RECHARGE_BALANCE SET store_ids = ? WHERE id = ?",
                        instance.getStoreIdsRaw(), instance.getId());
                if (logger.isDebugEnabled())
                    logger.debug(String.format("软删除门店[%d]个成功", stores.size()));
            }
        } else {
            Objects.requireNonNull(getJdbcTemplate()).update("UPDATE SMS_RECHARGE_BALANCE SET delete_flag = 1 WHERE id = ?",
                    instance.getId());
            if (logger.isDebugEnabled())
                logger.debug(String.format("软删除节点[%s]成功", instance));
        }
    }

    public Optional<List<RechargeBalanceEntity>> findAllStoreGroupBalance(OrgEntity company) {
        Map<String, Object> params = company.toParamMap();
        params.put("rechargeScope", RechargeScope.StoreGroup.getScope());
        params.put("sql", "findAllStoreGroupBalance");
        return super.queryForEntities("findAllStoreGroupBalance", params, getRowMapper());
    }

    public void addBalance(RechargeResDto rechargeResDto) {
        Preconditions.checkNotNull(rechargeResDto);
        if (rechargeResDto.getRechargeDetail().isStoreGroup()) {
            RechargeBalanceEntity exits = loadById(rechargeResDto.getRechargeDetail().getStoreIds());
            exits.addBalance(rechargeResDto.getTotalQuantity());
            super.updateAction(exits, "update");
        } else {
            RechargeBalanceEntity instance = new RechargeBalanceEntity(rechargeResDto.getRechargeDetail());
            Optional<RechargeBalanceEntity> exits = findByInstance(instance);
            if (exits.isPresent()) {
                exits.get().addBalance(rechargeResDto.getTotalQuantity());
                super.updateAction(exits.get(), "update");
            } else {
                instance.addBalance(rechargeResDto.getTotalQuantity());
                super.updateAction(instance, "insert");
            }
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("addBalance(%s) is ok", rechargeResDto));
    }

    private Optional<RechargeBalanceEntity> findByInstance(RechargeBalanceEntity instance) {
        Optional<RechargeBalanceEntity> optional = queryForEntity("findByInstance", instance.toParamMap(), getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByInstance(%s) res %s", instance, optional.orElse(null)));
        return optional;
    }

    public void batchUpdateBalance(Collection<RechargeBalanceEntity> balances) {
        Objects.requireNonNull(getJdbcTemplate())
                .batchUpdate("UPDATE SMS_RECHARGE_BALANCE SET sms_balance = ? WHERE id = ?", balances, 256,
                        (ps, balance) -> {
                            ps.setObject(1, balance.getBalance());
                            ps.setObject(2, balance.getId());
                        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchUpdateBalance() size  %d", balances.size()));
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
        Preconditions.checkState(balanceList.isPresent(), "无对应的实体信息[%s]", balanceIds);
        return balanceList.get();
    }

    @Override
    public RechargeBalanceEntity loadById(Object id) {
        List<RechargeBalanceEntity> list = this.loadByIds(Lists.newArrayList(id.toString()));
        return list.get(0);
    }

    /**
     * 门店可供扣除的余额
     *
     * @param store
     * @return
     */
    public RechargeBalanceAgg loadOrder4RechargeByStore(StoEntity store) {
        Optional<List<RechargeBalanceEntity>> optional = loadAllByCompanyId(store.getCompanyId());
        Preconditions.checkState(optional.isPresent(), "当前门店以及公司没有可供支配的短信余额...");
        List<RechargeBalanceEntity> list = optional.get().stream().filter(x -> x.contains(store))
                .filter(RechargeBalanceEntity::hasBlance).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(list), "当前门店以及公司没有可供支配的短信余额...");
        if (CollectionUtils.isNotEmpty(list)) list.sort(ordering);
        return new RechargeBalanceAgg(list);
    }

    /**
     * 门店可供扣除的余额
     *
     * @param store
     * @return
     */
    public ReimburseBalanceAgg loadOrder4ReimburseByStore(StoEntity store) {
        Optional<List<RechargeBalanceEntity>> optional = loadAllByCompanyId(store.getCompanyId());
        Preconditions.checkState(optional.isPresent(), "当前门店以及公司没有可供退款的账户信息...");
        List<RechargeBalanceEntity> list = optional.get().stream().filter(x -> x.contains(store))
                .filter(RechargeBalanceEntity::hasBlance).collect(Collectors.toList());
        Preconditions.checkState(CollectionUtils.isNotEmpty(list), "当前门店以及公司没有可供退款的账户信息...");
        if (CollectionUtils.isNotEmpty(list)) list.sort(ordering);
        return new ReimburseBalanceAgg(list);
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

    private static class RowMapperImpl implements RowMapper<RechargeBalanceEntity> {
        @Override
        public RechargeBalanceEntity mapRow(ResultSet res, int i) throws SQLException {
            return new RechargeBalanceEntity(res.getString("id"), res);
        }
    }
}
