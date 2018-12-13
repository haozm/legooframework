package com.legooframework.model.organization.entity;

import com.google.common.base.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.commons.dto.DefautTreeStructure;
import com.legooframework.model.commons.dto.TreeStructure;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class StoreTreeEntity extends BaseEntity<Long> {

    private Long pid;
    private String nodeName;
    private int nodeType;
    private int nodeSeq;
    private List<Long> storeIds;

    StoreTreeEntity(Long id, LoginContext loginUser, StoreTreeEntity parent, String nodeName, int nodeSeq) {
        super(id, loginUser.getTenantId(), loginUser.getLoginId());
        this.pid = parent.getId();
        this.nodeType = 0;
        this.nodeSeq = nodeSeq;
        this.storeIds = null;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "节点名称不可以为空...");
        this.nodeName = nodeName;
    }

    StoreTreeEntity(Long id, ResultSet res) {
        super(id, res);
        try {
            this.pid = ResultSetUtil.getObject(res, "pid", Long.class);
            this.nodeName = ResultSetUtil.getObject(res, "nodeName", String.class);
            this.nodeType = res.getInt("nodeType");
            this.nodeSeq = res.getInt("nodeSeq");
            String opt_ids = ResultSetUtil.getOptString(res, "children", null);
            if (Strings.isNullOrEmpty(opt_ids)) {
                this.storeIds = null;
            } else {
                this.storeIds = Splitter.on(',').splitToList(opt_ids).stream().map(Long::valueOf)
                        .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore StoreTreeEntity has SQLException", e);
        }
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> data = super.toParamMap("storeIds");
        data.put("storeIds", CollectionUtils.isEmpty(this.storeIds) ? null : Joiner.on(',').join(this.storeIds));
        return data;
    }

    // 添加门店
    Optional<StoreTreeEntity> addStores(Collection<StoreEntity> stores) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(stores), "待添加的门店列表不可以为空...");
        Set<Long> add_ids = stores.stream().map(StoreEntity::getId).collect(Collectors.toSet());
        if (exitsStores()) {
            if (this.storeIds.containsAll(add_ids)) return Optional.empty();
            add_ids.addAll(this.storeIds);
            StoreTreeEntity clone = (StoreTreeEntity) cloneMe();
            clone.storeIds = Lists.newArrayList(add_ids);
            return Optional.of(clone);
        }
        StoreTreeEntity clone = (StoreTreeEntity) cloneMe();
        clone.storeIds = Lists.newArrayList(add_ids);
        return Optional.of(clone);
    }

    Optional<StoreTreeEntity> removeStores(Collection<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores) || !exitsStores()) return Optional.empty();
        Set<Long> str_ids = stores.stream().map(StoreEntity::getId).collect(Collectors.toSet());
        Set<Long> _ids = Sets.newHashSet(this.storeIds);
        _ids.removeAll(str_ids);
        if (SetUtils.isEqualSet(this.storeIds, _ids)) return Optional.empty();
        StoreTreeEntity clone = (StoreTreeEntity) cloneMe();
        clone.storeIds = Lists.newArrayList(_ids);
        return Optional.of(clone);

    }


    Optional<StoreTreeEntity> change(String nodeName, int nodeSeq) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "节点名称不可以为空....");
        if (StringUtils.equals(this.nodeName, nodeName) && this.nodeSeq == nodeSeq) return Optional.empty();
        StoreTreeEntity clone = (StoreTreeEntity) super.cloneMe();
        clone.nodeName = nodeName;
        clone.nodeSeq = nodeSeq;
        return Optional.of(clone);
    }

    boolean isParent(StoreTreeEntity parent) {
        return this.pid.equals(parent.getId());
    }

    boolean isChild(StoreTreeEntity child) {
        return this.getId().equals(child.getPid());
    }

    public Long getPid() {
        return pid;
    }

    public String getNodeName() {
        return nodeName;
    }

    public int getNodeType() {
        return nodeType;
    }

    public boolean exitsStores() {
        return CollectionUtils.isNotEmpty(this.storeIds);
    }

    public List<Long> getStoreIds() {
        Preconditions.checkState(CollectionUtils.isNotEmpty(this.storeIds));
        return ImmutableList.copyOf(storeIds);
    }

    public int getNodeSeq() {
        return nodeSeq;
    }

    public boolean isRoot() {
        return Objects.equals(this.getId(), this.pid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StoreTreeEntity that = (StoreTreeEntity) o;
        return nodeType == that.nodeType &&
                nodeSeq == that.nodeSeq &&
                Objects.equals(pid, that.pid) &&
                Objects.equals(nodeName, that.nodeName) &&
                SetUtils.isEqualSet(storeIds, that.storeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), pid, nodeName, nodeSeq, nodeType, storeIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("pid", pid)
                .add("nodeName", nodeName)
                .add("nodeType", nodeType)
                .add("storeIds", storeIds)
                .toString();
    }

    private TreeStructure treeNode;

    public TreeStructure getTreeNode() {
        if (this.treeNode != null) return this.treeNode;
        DefautTreeStructure node = new DefautTreeStructure(String.format("ORG_%s", this.getId()),
                isRoot() ? "root" : String.format("ORG_%s", this.pid), this.nodeName, this.getId());
        node.setAttachData("storeIds", this.storeIds);
        node.setAttachData("type", "ORG");
        this.treeNode = node;
        return node;
    }
}
