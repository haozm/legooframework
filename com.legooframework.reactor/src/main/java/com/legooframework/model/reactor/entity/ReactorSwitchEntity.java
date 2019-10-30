package com.legooframework.model.reactor.entity;

import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class ReactorSwitchEntity extends BaseEntity<Long> {

    private final Integer companyId;
    private final String type;
    private Set<Integer> allowStoreIds, forbidStoreIds;

    ReactorSwitchEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.companyId = res.getInt("companyId");
            this.type = res.getString("switch_type");
            String allowStoreIds = res.getString("allow_store_ids");
            if (!Strings.isNullOrEmpty(allowStoreIds)) {

            }
            String forbidStoreIds = res.getString("forbid_store_ids");
            if (!Strings.isNullOrEmpty(forbidStoreIds)) {

            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore RetailFactEntity has SQLException", e);
        }
    }


}
