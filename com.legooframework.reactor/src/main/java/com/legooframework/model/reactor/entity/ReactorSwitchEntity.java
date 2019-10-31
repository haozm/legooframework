package com.legooframework.model.reactor.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.StoEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReactorSwitchEntity extends BaseEntity<Long> {

    final static String TYPE_RETAILFACT = "RetailFact";

    private final Integer companyId;
    private boolean enabled;
    private final String type;
    private Set<Integer> allowStoreIds, forbidStoreIds;

    private ReactorSwitchEntity(Integer companyId, boolean enabled, String type, Set<Integer> allowStoreIds,
                                Set<Integer> forbidStoreIds) {
        super(0L);
        this.companyId = companyId;
        this.enabled = enabled;
        this.type = type;
        this.allowStoreIds = allowStoreIds;
        this.forbidStoreIds = forbidStoreIds;
    }

    static ReactorSwitchEntity retailFactSwitch(OrgEntity company, Collection<StoEntity> stores) {

        return new ReactorSwitchEntity(company.getId(), true, ReactorSwitchEntity.TYPE_RETAILFACT,
                CollectionUtils.isEmpty(stores) ? null :
                        stores.stream().mapToInt(StoEntity::getId).boxed().collect(Collectors.toSet()), null);
    }

    ReactorSwitchEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.companyId = res.getInt("company_id");
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

    Optional<ReactorSwitchEntity> allowStoreIds(Set<StoEntity> stores) {
        Set<Integer> storeIds = CollectionUtils.isEmpty(stores) ? null :
                stores.stream().mapToInt(StoEntity::getId).boxed().collect(Collectors.toSet());
        if (SetUtils.isEqualSet(this.allowStoreIds, storeIds)) return Optional.empty();
        ReactorSwitchEntity clone = (ReactorSwitchEntity) cloneMe();
        clone.enabled = true;
        clone.allowStoreIds = storeIds;
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("id", getId());
        params.put("enabled", enabled ? 1 : 0);
        params.put("switchType", type);
        params.put("allowStoreIds", CollectionUtils.isEmpty(allowStoreIds) ? null : Joiner.on(',').join(allowStoreIds));
        params.put("forbidStoreIds", CollectionUtils.isEmpty(forbidStoreIds) ? null : Joiner.on(',').join(forbidStoreIds));
        return params;
    }

    boolean hasWhiteList() {
        return CollectionUtils.isNotEmpty(this.allowStoreIds);
    }

    Set<Integer> getAllowStoreIds() {
        Preconditions.checkState(hasWhiteList(), "当前白名单列表为空.....");
        return allowStoreIds;
    }

    boolean isCompany(OrgEntity company) {
        return this.companyId.equals(company.getId());
    }

    boolean isType(String type) {
        return StringUtils.equals(this.type, type);
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
