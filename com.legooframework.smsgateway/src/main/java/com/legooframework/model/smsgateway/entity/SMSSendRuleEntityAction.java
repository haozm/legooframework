package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SMSSendRuleEntityAction extends BaseEntityAction<SMSSendRuleEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RechargeDetailEntityAction.class);

    public SMSSendRuleEntityAction() {
        super("smsGateWayCache");
    }

    public String addRule(BusinessType businessType, String businessDesc, SMSChannel smsChannel, boolean freeSend) {
        Optional<List<SMSSendRuleEntity>> rules = loadAllRules();
        Optional<SMSSendRuleEntity> rule = rules.flatMap(r -> r.stream()
                .filter(x -> x.getBusinessType().equals(businessType)).findFirst());
        Preconditions.checkState(!rule.isPresent(), "businessType = %s 对应的规则已经存在..", businessType);
        SMSSendRuleEntity instance = new SMSSendRuleEntity(businessType, smsChannel, freeSend);
        super.updateAction(instance, "insert");
        final String cache_key = String.format("%s_all_rules", getModelName());
        getCache().ifPresent(c -> c.evict(cache_key));
        return instance.getId();
    }

    public SMSSendRuleEntity loadByType(BusinessType businessType) {
        Optional<List<SMSSendRuleEntity>> rules = loadAllRules();
        Optional<SMSSendRuleEntity> rule = rules.flatMap(r -> r.stream()
                .filter(x -> x.getBusinessType() == businessType).findFirst());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByType(%s) return %s", businessType, rule.orElse(null)));
        Preconditions.checkState(rule.isPresent(), "%s 对应的规则定义不存在...", businessType);
        return rule.get();
    }

    public void modify(BusinessType businessType, SMSChannel smsChannel, boolean freeSend) {
        SMSSendRuleEntity exits = loadByType(businessType);
        Optional<SMSSendRuleEntity> clone = exits.modify(smsChannel, freeSend);
        if (!clone.isPresent()) return;
        final String cache_key = String.format("%s_all_rules", getModelName());
        super.updateAction(exits, "disabled");
        super.updateAction(clone.get(), "insert");
        getCache().ifPresent(c -> c.evict(cache_key));
    }

    @SuppressWarnings("unchecked")
    private Optional<List<SMSSendRuleEntity>> loadAllRules() {
        final String cache_key = String.format("%s_all_rules", getModelName());
        if (getCache().isPresent()) {
            List<SMSSendRuleEntity> list = getCache().get().get(cache_key, List.class);
            if (CollectionUtils.isNotEmpty(list)) return Optional.of(list);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("enabled", 1);
        Optional<List<SMSSendRuleEntity>> list = super.queryForEntities("loadAllRules", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllRules() return %s ", list.orElse(null)));
        list.ifPresent(l -> getCache().ifPresent(c -> c.put(cache_key, l)));
        return list;
    }

    @Override
    protected RowMapper<SMSSendRuleEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<SMSSendRuleEntity> {
        @Override
        public SMSSendRuleEntity mapRow(ResultSet res, int i) throws SQLException {
            return new SMSSendRuleEntity(res.getString("id"), res);
        }
    }
}
