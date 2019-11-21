package com.legooframework.model.smsgateway.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DeductionTotalEntityAction extends BaseEntityAction<DeductionTotalEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DeductionTotalEntityAction.class);

    public DeductionTotalEntityAction() {
        super(null);
    }

    /**
     * 生成汇总记录
     *
     * @param store       门店
     * @param smsQuantity 短信数量
     * @return 存储批次号
     */
    public String insert(StoEntity store, SMSSendRuleEntity businessRule, BusinessType businessType, String smsBatchNo,
                         long smsQuantity, long wxQuantity, boolean isAuto, String smsContext) {
        DeductionTotalEntity billingSummary = DeductionTotalEntity.createInstance(store, businessRule, businessType, smsBatchNo,
                isAuto, smsQuantity < 0 ? 0 : smsQuantity, wxQuantity < 0 ? 0 : wxQuantity, smsContext);
        super.updateAction(billingSummary, "insert");
        if (logger.isDebugEnabled())
            logger.debug(String.format("insert(%s,%s,%s)", store.getId(), businessRule, smsQuantity));
        return billingSummary.getId();
    }

    @Override
    protected RowMapper<DeductionTotalEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    private static class RowMapperImpl implements RowMapper<DeductionTotalEntity> {
        @Override
        public DeductionTotalEntity mapRow(ResultSet res, int i) throws SQLException {
            return new DeductionTotalEntity(res.getString("id"), res);
        }
    }
}
