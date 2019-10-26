package com.csosm.module.storeview.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.Date;
import java.util.Map;

public class StoreViewStructureEntity extends BaseEntity<Integer> {

    private final String nodeId, nodePath;
    private final Integer ownerId, companyId;

    StoreViewStructureEntity(StoreViewEntity storeView, String nodePath) {
        super(0, storeView.getCreateUserId(), new Date());
        this.nodeId = storeView.getId();
        this.nodePath = Strings.isNullOrEmpty(nodePath) ? storeView.getId() :
                String.format("%s$%s", nodePath, storeView.getId());
        this.ownerId = storeView.getOwnerId();
        this.companyId = storeView.getCompanyId();
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> params = super.toMap();
        params.put("nodeId", nodeId);
        params.put("nodePath", nodePath);
        params.put("ownerId", ownerId);
        params.put("companyId", companyId);
        return params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreViewStructureEntity)) return false;
        StoreViewStructureEntity that = (StoreViewStructureEntity) o;
        return Objects.equal(nodeId, that.nodeId) &&
                Objects.equal(nodePath, that.nodePath) &&
                Objects.equal(ownerId, that.ownerId) &&
                Objects.equal(companyId, that.companyId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeId, nodePath, ownerId, companyId);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("nodeId", nodeId)
                .add("nodePath", nodePath)
                .add("ownerId", ownerId)
                .add("companyId", companyId)
                .toString();
    }
}
