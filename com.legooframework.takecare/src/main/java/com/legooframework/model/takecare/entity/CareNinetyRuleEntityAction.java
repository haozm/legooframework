package com.legooframework.model.takecare.entity;

import com.legooframework.model.core.base.entity.BaseEntityAction;
import org.springframework.jdbc.core.RowMapper;

public class CareNinetyRuleEntityAction extends BaseEntityAction<CareNinetyRuleEntity> {

    public CareNinetyRuleEntityAction() {
        super(null);
    }

    @Override
    protected RowMapper<CareNinetyRuleEntity> getRowMapper() {
        return null;
    }
}

