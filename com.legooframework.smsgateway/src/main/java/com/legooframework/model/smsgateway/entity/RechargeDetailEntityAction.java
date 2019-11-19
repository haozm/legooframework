package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;

public class RechargeDetailEntityAction extends BaseEntityAction<RechargeDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RechargeDetailEntityAction.class);

    private static Comparator<RechargeDetailEntity> STORE_FST =
            Comparator.comparingInt((ToIntFunction<RechargeDetailEntity>) value -> value.getRechargeScope().getScope())
                    .reversed();

    public RechargeDetailEntityAction() {
        super(null);
    }

    /**
     * 短信 充值  弘治皇帝
     *
     * @param company
     * @param store
     * @param rechargeRule
     * @param rechargeAmount
     * @return
     */
    public RechargeResDto recharge(OrgEntity company, StoEntity store, String storeIds, RechargeRuleEntity rechargeRule,
                                   long rechargeAmount) {
        Preconditions.checkNotNull(company);
        Preconditions.checkArgument(rechargeAmount > 0);
        RechargeDetailEntity recharge;
        final LoginContext user = LoginContextHolder.get();
        if (Strings.isNotEmpty(storeIds)) {
            recharge = RechargeDetailEntity.rechargeByStoreGroup(company, storeIds, rechargeRule, rechargeAmount);
        } else if (store != null) {
            recharge = RechargeDetailEntity.rechargeByStore(store, rechargeRule, rechargeAmount);
        } else {
            recharge = RechargeDetailEntity.rechargeByCompany(company, rechargeRule, rechargeAmount);
        }
        // 历史预充值 抵扣
        Optional<List<RechargeDetailEntity>> deductions = loadUnDeductionRecharge(recharge);
        if (deductions.isPresent()) {
            Integer nums = deductions.get().stream()
                    .mapToInt(RechargeDetailEntity::getTotalQuantity).boxed().reduce(0, Integer::sum);
            Preconditions.checkState(recharge.getTotalQuantity() >= nums, "充值短信数量%s需大于或等于之前预充值短信总和%s",
                    recharge.getTotalQuantity(), nums);
        }
        super.updateAction(recharge, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("recharge(%s,%s) change %s return %s", company.getId(),
                    store == null ? null : store.getId(), rechargeAmount, recharge.getId()));
        final RechargeResDto rechargeResDto = new RechargeResDto(recharge);
        List<RechargeDeductionDetailEntity> details = Lists.newArrayList();
        List<RechargeDetailEntity> new_deductions = Lists.newArrayList();
        final RechargeDetailEntity recharge_source = recharge;
        deductions.ifPresent(ds -> ds.forEach(d -> {
            RechargeDetailEntity clone = d.deduction(user);
            new_deductions.add(clone);
            rechargeResDto.subtraction(d.getTotalQuantity());
            RechargeDeductionDetailEntity deductionDetail = new RechargeDeductionDetailEntity(recharge_source,
                    d, clone, user);
            details.add(deductionDetail);
        }));
        if (CollectionUtils.isNotEmpty(new_deductions)) {
            super.batchInsert("batchInsert", new_deductions);
            super.batchInsert("batchInsertDeduction", details);
        }
        return rechargeResDto;
    }

    /**
     * 预先充值模式
     *
     * @param company
     * @param store
     * @param rechargeRule
     * @param rechargeAmount
     * @return
     */
    public RechargeResDto precharge(OrgEntity company, StoEntity store, String storeIds, RechargeRuleEntity rechargeRule,
                                    long rechargeAmount) {
        Preconditions.checkNotNull(company);
        Preconditions.checkArgument(rechargeAmount > 0);
        RechargeDetailEntity recharge;
        if (StringUtils.isNotEmpty(storeIds)) {
            recharge = RechargeDetailEntity.prechargeByStoreGroup(company, storeIds, rechargeRule, rechargeAmount);
        } else if (store != null) {
            recharge = RechargeDetailEntity.prechargeByStore(store, rechargeRule, rechargeAmount);
        } else {
            recharge = RechargeDetailEntity.prechargeByCompany(company, rechargeRule, rechargeAmount);
        }
        super.updateAction(recharge, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("precharge(%s,%s) change %s return %s", company.getId(),
                    store == null ? null : store.getId(), rechargeAmount, recharge.getId()));
        return new RechargeResDto(recharge);
    }

    /**
     * 免费赠送
     *
     * @param company       公司
     * @param store         么门店
     * @param totalQuantity 数量
     * @return RechargeRes 充值结果
     */
    public RechargeResDto freecharge(OrgEntity company, StoEntity store, String storeIds, int totalQuantity) {
        Preconditions.checkNotNull(company);
        RechargeDetailEntity recharge;
        if (StringUtils.isNotEmpty(storeIds)) {
            recharge = RechargeDetailEntity.freechargeByStoreGroup(company, storeIds, totalQuantity);
        } else if (store != null) {
            recharge = RechargeDetailEntity.freechargeByStore(store, totalQuantity);
        } else {
            recharge = RechargeDetailEntity.freechargeByCompany(company, totalQuantity);
        }
        super.updateAction(recharge, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("freecharge(%s,%s) change %s return %s", company.getId(),
                    store == null ? null : store.getId(), totalQuantity, recharge.getId()));
        return new RechargeResDto(recharge);
    }

    private Optional<List<RechargeDetailEntity>> loadUnDeductionRecharge(RechargeDetailEntity rechargeDetail) {
        Preconditions.checkNotNull(rechargeDetail);
        Map<String, Object> params = rechargeDetail.toParamMap();
        Optional<List<RechargeDetailEntity>> rechargeDetails =
                super.queryForEntities("loadUnDeductionRecharge", params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUnDeductionRecharge(%s) res is %s", rechargeDetail,
                    rechargeDetails.map(List::size).orElse(0)));
        return rechargeDetails;
    }

    /**
     * 加载 门店可用余额
     *
     * @param store
     * @return
     */
    public Optional<List<RechargeDetailEntity>> loadStoreBalance(StoEntity store) {
        Preconditions.checkNotNull(store, "入参 CrmStoreEntity store 为 null....");
        Map<String, Object> params = store.toParamMap();
        Optional<List<RechargeDetailEntity>> res = super.queryForEntities("loadStoreBalance", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadStoreBalance(%s) return %s", store.getId(), res.orElse(null)));
        res.ifPresent(x -> x.sort(STORE_FST));
        return res;
    }

    public void batchWriteOff(Collection<RechargeDetailEntity> rechargeDetails) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(rechargeDetails));
        super.batchInsert("batchInsert", rechargeDetails);
    }

    /**
     * 批量充值门店
     *
     * @param recharges
     */
    public void batchBillByStore(Collection<RechargeDetailEntity> recharges) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(recharges));
        super.batchUpdate("batchBillByStore", (ps, o) -> {
            ps.setObject(1, o.getUsedQuantity());
            ps.setObject(2, o.getId());
        }, recharges);
    }

    @Override
    protected RowMapper<RechargeDetailEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<RechargeDetailEntity> {
        @Override
        public RechargeDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return new RechargeDetailEntity(res.getString("id"), res);
        }
    }

}
