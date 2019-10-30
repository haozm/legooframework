package com.legooframework.model.reactor.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactorSwitchEntity extends BaseEntity<Long> {

    private final static String TYPE_RETAILFACT = "RetailFact";

    private final Integer companyId;
    private boolean enabled;
    private final String type;
    private Set<Integer> allowStoreIds, forbidStoreIds;

    ReactorSwitchEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.companyId = res.getInt("companyId");
            this.type = res.getString("switch_type");
            this.enabled = res.getInt("enabled") == 1;
            String allowStoreIds = res.getString("allow_store_ids");
            if (Strings.isNullOrEmpty(allowStoreIds)) {
                this.allowStoreIds = null;
            } else {
                this.allowStoreIds = Stream.of(StringUtils.split(allowStoreIds, ','))
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
            }
            String forbidStoreIds = res.getString("forbid_store_ids");
            if (Strings.isNullOrEmpty(forbidStoreIds)) {
                this.forbidStoreIds = null;
            } else {
                this.forbidStoreIds = Stream.of(StringUtils.split(forbidStoreIds, ','))
                        .mapToInt(Integer::parseInt).boxed().collect(Collectors.toSet());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore ReactorSwitchEntity has SQLException", e);
        }
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("enabled", enabled)
                .add("type", type)
                .add("allowStoreIds", allowStoreIds)
                .add("forbidStoreIds", forbidStoreIds)
                .toString();
    }
}
