package com.legooframework.model.crmadapter.entity;


import com.google.common.base.Objects;
import com.legooframework.model.core.base.entity.BaseEntity;

import java.util.List;

/**
 * 门店任意虚拟分组 实体建模
 */
public class StoreViewEntity extends BaseEntity<String> {

    private String nodeName;
    private String parentId;
    private final Integer companyId, treeType;
    private List<Integer> storeIds;
    final static int TYPE_DATA_PERMISSION = 1;
    final static int TYPE_SMS_RECHARGE = 2;

    boolean isDataPermissionTree() {
        return this.treeType == 1;
    }

    boolean isSmsRechargeTree() {
        return this.treeType == 2;
    }

    // 构造函数 4 DB
    StoreViewEntity(String id, Integer treeType, String pid, String nodeName, List<Integer> storeIds, Integer companyId) {
        super(id);
        this.storeIds = storeIds;
        this.treeType = treeType;
        this.nodeName = nodeName;
        this.parentId = pid;
        this.companyId = companyId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public List<Integer> getStoreIds() {
        return storeIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoreViewEntity)) return false;
        if (!super.equals(o)) return false;
        StoreViewEntity that = (StoreViewEntity) o;
        return Objects.equal(nodeName, that.nodeName) &&
                Objects.equal(parentId, that.parentId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(treeType, that.treeType) &&
                Objects.equal(storeIds, that.storeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), nodeName, parentId, companyId, treeType, storeIds);
    }
}
