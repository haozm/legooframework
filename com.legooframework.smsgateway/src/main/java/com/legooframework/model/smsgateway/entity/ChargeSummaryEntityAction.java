package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChargeSummaryEntityAction extends BaseEntityAction<ChargeSummaryEntity> {

    private static final Logger logger = LoggerFactory.getLogger(ChargeSummaryEntityAction.class);

    public ChargeSummaryEntityAction() {
        super(null);
    }

    /**
     * 生成汇总记录
     *
     * @param store       门店
     * @param smsQuantity 短信数量
     * @return 存储批次号
     */
    public String insert(CrmStoreEntity store, SMSSendRuleEntity businessRule, BusinessType businessType, String smsBatchNo,
                         long smsQuantity, long wxQuantity, boolean isAuto, String smsContext) {
        ChargeSummaryEntity billingSummary = ChargeSummaryEntity.createInstance(store, businessRule, businessType, smsBatchNo,
                isAuto, smsQuantity < 0 ? 0 : smsQuantity, wxQuantity < 0 ? 0 : wxQuantity, smsContext);
        super.updateAction(billingSummary, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("insert(%s,%s,%s)", store.getId(), businessRule, smsQuantity));
        return billingSummary.getId();
    }

    @Override
    protected RowMapper<ChargeSummaryEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<ChargeSummaryEntity> {
        @Override
        public ChargeSummaryEntity mapRow(ResultSet res, int i) throws SQLException {
            return new ChargeSummaryEntity(res.getString("id"), res);
        }
    }
}
