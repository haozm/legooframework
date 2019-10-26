package com.legooframework.model.insurance.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InsuranceInfoEntityAction extends BaseEntityAction<InsuranceInfoEntity> {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceInfoEntityAction.class);
    private SimpleJdbcInsert simpleJdbcInsert;

    public InsuranceInfoEntityAction() {
        super(null);
    }

    /**
     * @param insurancePolicy 保单
     * @param insuranceInfos  保险清单
     */
    public void batchInsert(InsurancePolicyEntity insurancePolicy, List<InsuranceInfoEntity> insuranceInfos) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(insuranceInfos));
        insuranceInfos.forEach(x -> x.setInsuranceId(insurancePolicy));
        super.batchInsert("batchInsert", insuranceInfos);
    }

    public List<InsuranceInfoEntity> loadByInsurance(InsurancePolicyEntity insurancePolicy) {
        Optional<List<InsuranceInfoEntity>> list = this.findByInsuranceId(insurancePolicy.getId());
        Preconditions.checkState(list.isPresent(), "保单%s 缺市险种明细数据...", insurancePolicy.getInsuranceNo());
        return list.get();
    }

    Optional<List<InsuranceInfoEntity>> findByInsuranceId(Integer insuranceId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("insuranceId", insuranceId);
        Optional<List<InsuranceInfoEntity>> list = super.queryForEntities("findByInsurance", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByInsuranceId(%s) res is %s", insuranceId, list.orElse(null)));
        return list;
    }

    @Override
    protected void initTemplateConfig() {
        super.initTemplateConfig();
        this.simpleJdbcInsert = new SimpleJdbcInsert(getJdbcTemplate()).withTableName("INSURANCE_LIST_INFO");
        this.simpleJdbcInsert.usingColumns("insurance_id", "insurance_type", "insurance_amount");
        this.simpleJdbcInsert.compile();

    }

    @Override
    protected RowMapper<InsuranceInfoEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<InsuranceInfoEntity> {
        @Override
        public InsuranceInfoEntity mapRow(ResultSet res, int i) throws SQLException {
            return new InsuranceInfoEntity(res.getLong("id"), res);
        }
    }
}
