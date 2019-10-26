package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntityAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ResultSetExtractor;

public class SystemlogEntityAction extends BaseEntityAction<SystemlogEntity> {

    private static final Logger logger = LoggerFactory.getLogger(StoreEntityAction.class);

    public SystemlogEntityAction() {
        super("SystemlogEntity", null);
    }

    public void insert(SystemlogEntity instance) {
        if (instance == null) return;
        if (instance.getUser() != null)
            getJdbc().update(getExecSql("insert", null), instance.toMap());
    }

    @Override
    protected ResultSetExtractor<SystemlogEntity> getResultSetExtractor() {
        return null;
    }


}
