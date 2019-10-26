package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SendSmsEntityAction extends BaseEntityAction<SendSmsEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SendSmsEntityAction.class);

    public SendSmsEntityAction() {
        super(Constant.CACHE_ENTITYS);
    }

    public void batchAdd4Send(Collection<SendSmsEntity> smses) {
        if (CollectionUtils.isEmpty(smses)) return;
        super.batchInsert("batchInsert", smses);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次完成短信批量写入[%s]条", smses.size()));
    }

    public String getSmsPrefix(StoEntity store) {
        final String cache_key = String.format("SMS_PREFIX_STORE_%d", store.getId());
        if (getCache().isPresent()) {
            SMSPreAndSuff cache_val = getCache().get().get(cache_key, SMSPreAndSuff.class);
            if (cache_val != null) return cache_val.getPrefix();
        }
        Map<String, Object> params = store.toParamMap();
        Optional<List<Map<String, Object>>> mapList = super.queryForMapList("findSmsPrefix", params);
        Preconditions.checkState(mapList.isPresent(), "公司%d 缺失短信配置...", store.getCompanyId());
        SMSPreAndSuff preAndSuff = new SMSPreAndSuff(mapList.get().get(0), store);
        getCache().ifPresent(c -> c.put(cache_key, preAndSuff));
        return preAndSuff.getPrefix();
    }

    static class SMSPreAndSuff {
        private final String prefix, type;
        private final Integer storeId;

        SMSPreAndSuff(Map<String, Object> params, StoEntity store) {
            String _temp = MapUtils.getString(params, "smsPre");
            if (StringUtils.startsWith(_temp, "【")) {
                this.prefix = _temp;
            } else {
                this.prefix = String.format("【%s】", _temp);
            }
            this.type = MapUtils.getString(params, "type");
            this.storeId = store.getId();
        }

        String getPrefix() {
            return prefix;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("prefix", prefix)
                    .add("type", type)
                    .add("storeId", storeId)
                    .toString();
        }
    }

    @Override
    protected RowMapper<SendSmsEntity> getRowMapper() {
        return null;
    }
}
