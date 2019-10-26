package com.legooframework.model.insurance.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class InsurancePolicyEntityAction extends BaseEntityAction<InsurancePolicyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(InsurancePolicyEntityAction.class);

    public InsurancePolicyEntityAction() {
        super(null);
    }

    public void insert(InsurancePolicyEntity insurancePolicy) {
        super.updateAction(insurancePolicy, "insert");
    }

    public Optional<InsurancePolicyEntity> findByInsuranceNo(String insuranceNo) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(insuranceNo));
        Map<String, Object> params = Maps.newHashMap();
        params.put("insuranceNo", insuranceNo);
        Optional<InsurancePolicyEntity> entity = super.queryForEntity("findByInsuranceNo", params, getRowMapper());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByInsuranceNo(%s) retrun %s", insuranceNo, entity.orElse(null)));
        return entity;
    }

    @Override
    protected RowMapper<InsurancePolicyEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<InsurancePolicyEntity> {
        @Override
        public InsurancePolicyEntity mapRow(ResultSet res, int i) throws SQLException {
            return new InsurancePolicyEntity(res.getLong("id"), res);
        }
    }
}
