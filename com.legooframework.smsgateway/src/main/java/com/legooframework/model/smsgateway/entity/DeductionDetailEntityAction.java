package com.legooframework.model.smsgateway.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.ToIntFunction;

public class DeductionDetailEntityAction extends BaseEntityAction<DeductionDetailEntity> {

    private static final Logger logger = LoggerFactory.getLogger(DeductionDetailEntityAction.class);

    private static final Comparator<DeductionDetailEntity> comparable = Comparator
            .comparingInt((ToIntFunction<DeductionDetailEntity>) detail -> detail.getSmsBatchNo().hashCode())
            .thenComparingInt(detail -> detail.getRechargeScope().getScope());

    public DeductionDetailEntityAction() {
        super(null);
    }

    public void batchInsert(Collection<DeductionDetailEntity> billingDetails) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(billingDetails));
        super.batchInsert("batchInsert", billingDetails);
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchInsert(...) size is %s", billingDetails.size()));
    }

    /**
     * 加载一批指定 发送批次号码对应的扣款明细
     *
     * @param smsBatchNos 发送批次号
     * @return
     */
    public Multimap<String, DeductionDetailEntity> loadBySmsBatchNos(Collection<String> smsBatchNos) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(smsBatchNos));
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNos", smsBatchNos);
        Optional<List<DeductionDetailEntity>> billDetails = super.queryForEntities("loadBySmsBatchNos", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBySmsBatchNos(%s) return %s", smsBatchNos, billDetails.orElse(null)));
        Preconditions.checkState(billDetails.isPresent());
        billDetails.get().sort(comparable);
        ArrayListMultimap<String, DeductionDetailEntity> multimap = ArrayListMultimap.create();
        billDetails.get().forEach(x -> multimap.put(x.getSmsBatchNo(), x));
        return multimap;
    }

    /**
     * 加载一批指定 发送批次号码对应的扣款明细
     *
     * @param smsBatchNo 发送批次号
     * @return
     */
    public List<DeductionDetailEntity> loadBySmsBatchNo(String smsBatchNo) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("smsBatchNos", new String[]{smsBatchNo});
        Optional<List<DeductionDetailEntity>> billDetails = super.queryForEntities("loadBySmsBatchNos", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBySmsBatchNo(%s) return %s", smsBatchNo, billDetails.orElse(null)));
        Preconditions.checkState(billDetails.isPresent());
        billDetails.get().sort(comparable);
        return billDetails.get();
    }

    public void batchWriteOff(Collection<DeductionDetailEntity> chargeDetails) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(chargeDetails));
        super.batchUpdate("batchWriteOff", (ps, chargeDetail) -> {
            ps.setObject(1, chargeDetail.getReimburseNum());
            ps.setObject(2, chargeDetail.getId());
        }, chargeDetails);
    }

    @Override
    protected RowMapper<DeductionDetailEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<DeductionDetailEntity> {
        @Override
        public DeductionDetailEntity mapRow(ResultSet res, int i) throws SQLException {
            return new DeductionDetailEntity(res.getString("id"), res);
        }
    }
}
