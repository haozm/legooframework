package com.csosm.module.storeview.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.*;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 门店任意虚拟分组 实体建模
 */
public class StoreViewEntity extends BaseEntity<String> {

    private String nodeName;
    private String nodeDesc, parentId;
    private final Integer companyId, ownerId, treeType;
    private List<Integer> storeIds;
    private Map<Integer, String> storeInfo;
    final static int TYPE_DATA_PERMISSION = 1;
    final static int TYPE_SMS_RECHARGE = 2;


    public boolean isDataPermissionTree() {
        return this.treeType == 1;
    }

    public boolean isSmsRechargeTree() {
        return this.treeType == 2;
    }

    // 构造函数 4 DB
    StoreViewEntity(String id, Integer ownerId, Integer treeType, String pid, String nodeName,
                    String storeIds, String storeInfo, String nodeDesc, Integer companyId, Object createUserId) {
        super(id);
        if (!Strings.isNullOrEmpty(storeIds)) {
            String[] str_id_list = StringUtils.split(storeIds, ',');
            this.storeIds = Stream.of(str_id_list).mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
            Map<String, String> maps = Maps.newHashMap();
            if (!Strings.isNullOrEmpty(storeInfo)) {
                maps.putAll(Splitter.on(',').withKeyValueSeparator('@').split(storeInfo));
                this.storeInfo = Maps.newHashMap();
                maps.entrySet().forEach(k -> {
                    this.storeInfo.put(Integer.valueOf(k.getKey()), k.getValue());
                });
            } else {
                this.storeInfo = null;
            }
        } else {
            this.storeIds = null;
            this.storeInfo = null;
        }
        this.ownerId = ownerId;
        this.treeType = treeType;
        this.nodeName = nodeName;
        this.parentId = pid;
        this.nodeDesc = nodeDesc;
        this.companyId = companyId;
        init4Create(createUserId);
    }

    // for create
    StoreViewEntity(String nodeName, String nodeDesc, StoreViewEntity parent, LoginUserContext loginUser) {
        super(UUID.randomUUID().toString());
        Preconditions.checkNotNull(parent);
        this.ownerId = parent.getOwnerId();
        this.nodeName = nodeName;
        this.parentId = parent.getId();
        this.treeType = parent.treeType;
        this.nodeDesc = nodeDesc;
        Preconditions.checkNotNull(loginUser);
        Preconditions.checkArgument(loginUser.getCompany().isPresent(), "当前用户%s尚未绑定公司信息，无法实例化门店分组视图.",
                loginUser.getEmployee().getUserName());
        this.companyId = loginUser.getCompany().get().getId();
        init4Create(loginUser.getUserId());
    }

    private StoreViewEntity(String nodeName, String nodeDesc, Integer treeType, EmployeeEntity employee,
                            LoginUserContext loginUser) {
        super(UUID.randomUUID().toString());
        this.ownerId = employee.getId();
        this.nodeName = nodeName;
        this.nodeDesc = nodeDesc;
        this.treeType = treeType;
        this.parentId = getId();
        this.companyId = employee.getCompanyId().or(-1);
        init4Create(loginUser.getUserId());
    }

    private StoreViewEntity(String nodeName, String nodeDesc, Integer treeType, OrganizationEntity company,
                            LoginUserContext loginUser) {
        super(UUID.randomUUID().toString());
        this.ownerId = -1;
        this.nodeName = nodeName;
        this.nodeDesc = nodeDesc;
        this.treeType = treeType;
        this.parentId = getId();
        this.companyId = company.getId();
        init4Create(loginUser.getUserId());
    }

    static StoreViewEntity createDataPermissionRoot(EmployeeEntity employee, LoginUserContext loginUser) {
        return new StoreViewEntity(String.format("[%s]视图树", employee.getUserName()), "ROOT", TYPE_DATA_PERMISSION,
                employee, loginUser);
    }

    static StoreViewEntity createSmsRechargeRoot(OrganizationEntity company, LoginUserContext loginUser) {
        Preconditions.checkNotNull(company);
        Preconditions.checkState(company.isCompany(), "入参 OrganizationEntity company 必须为公司...");
        return new StoreViewEntity(String.format("[%s]充值管理树", company.getName()), "ROOT", TYPE_SMS_RECHARGE,
                company, loginUser);
    }

    Integer getOwnerId() {
        return ownerId;
    }

    /**
     * 新增门店到 分组
     *
     * @param stores       待新增的门店实体列表
     * @param modifyUserId 修改人ID
     * @return StoreViewEntity
     */
    StoreViewEntity addStores(Collection<StoreEntity> stores, Object modifyUserId) {
        if (CollectionUtils.isEmpty(stores)) return this;
        try {
            StoreViewEntity clone = (StoreViewEntity) this.clone();
            clone.storeIds = CollectionUtils.isEmpty(this.storeIds) ? Lists.newArrayList() : Lists.newArrayList(this.storeIds);
            clone.storeInfo = CollectionUtils.isEmpty(this.storeIds) ? Maps.newHashMap() : Maps.newHashMap(this.storeInfo);
            for (StoreEntity s : stores) {
                if (clone.storeIds.contains(s.getId())) continue;
                clone.storeIds.add(s.getId());
                clone.storeInfo.put(s.getId(), s.getName());
            }
            clone.storeIds.sort(Comparator.naturalOrder());
            clone.init4LastModify(modifyUserId);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer getCompanyId() {
        return companyId;
    }

    Integer getTreeType() {
        return treeType;
    }

    boolean containsAny(Collection<Integer> storeIds) {
        return CollectionUtils.isNotEmpty(this.storeIds) && this.storeIds.stream().anyMatch(storeIds::contains);
    }

    /**
     * 删除门店 分组
     *
     * @param stores 待移除的门店实体列表
     * @return StoreViewEntity
     */
    java.util.Optional<StoreViewEntity> removeStores(List<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(stores) || CollectionUtils.isEmpty(this.storeIds))
            return java.util.Optional.empty();
        try {
            Set<Integer> new_storeIds = stores.stream().mapToInt(StoreEntity::getId).boxed()
                    .collect(Collectors.toSet());
            StoreViewEntity clone = (StoreViewEntity) this.clone();
            for (StoreEntity store : stores) new_storeIds.add(store.getId());
            clone.storeIds = Lists.newArrayList(this.storeIds);
            clone.storeIds.removeAll(new_storeIds);
            clone.storeIds.sort(Comparator.naturalOrder());
            if (CollectionUtils.isEqualCollection(this.storeIds, clone.storeIds))
                return java.util.Optional.empty();
            clone.storeInfo = Maps.newHashMap(this.storeInfo);
            new_storeIds.forEach(x -> clone.storeInfo.remove(x));
            return java.util.Optional.of(clone);
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    Set<Integer> exitsStores(List<StoreEntity> stores) {
        if (CollectionUtils.isEmpty(this.storeIds) || CollectionUtils.isEmpty(stores)) return null;
        Set<Integer> news = stores.stream().mapToInt(BaseEntity::getId).boxed().collect(Collectors.toSet());
        Set<Integer> olds = Sets.newHashSet(this.storeIds);
        Sets.SetView<Integer> setView = Sets.intersection(news, olds);
        return setView.isEmpty() ? null : setView.immutableCopy();
    }

    /**
     * 修改分组名称
     *
     * @param nodeName    新名字
     * @param userContext 修改人ID
     * @return StoreViewEntity
     */
    StoreViewEntity changeInfo(String nodeName, String nodeDesc, LoginUserContext userContext) {
        try {
            StoreViewEntity clone = (StoreViewEntity) this.clone();
            clone.nodeName = nodeName;
            clone.nodeDesc = nodeDesc;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public StoreTreeViewDto buildTreeNode() {
        return new StoreTreeViewDto(this);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("nodeName", getNodeName());
        map.put("parentId", parentId);
        map.put("nodeDesc", nodeDesc);
        map.put("treeType", treeType);
        map.put("storeIds", CollectionUtils.isEmpty(storeIds) ? null
                : Joiner.on(',').join(storeIds));
        map.put("storeInfo", CollectionUtils.isEmpty(storeIds) ? null
                : Joiner.on(',').withKeyValueSeparator('@').join(storeInfo));
        map.put("ownerId", ownerId);
        map.put("companyId", companyId);
        return map;
    }

    public boolean isParentNode(StoreViewEntity node) {
        Preconditions.checkNotNull(node);
        Preconditions.checkState(!isRoot(), "当前节点为根节点,无上级节点.");
        return Objects.equal(this.parentId, node.getId());
    }

    public boolean isRoot() {
        return getId().equals(parentId);
    }

    public boolean hasStores() {
        return !CollectionUtils.isEmpty(storeIds);
    }

    public int getNumberOfstores() {
        return CollectionUtils.isEmpty(storeIds) ? 0 : storeIds.size();
    }

    public Optional<String> getParentId() {
        return Optional.fromNullable(parentId);
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getNodeDesc() {
        return nodeDesc;
    }

    public Collection<Integer> getStoreIds() {
        return CollectionUtils.isEmpty(storeIds) ? null : ImmutableSet.copyOf(storeIds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreViewEntity entity = (StoreViewEntity) o;
        return Objects.equal(nodeName, entity.nodeName)
                && Objects.equal(parentId, entity.parentId)
                && Objects.equal(ownerId, entity.ownerId)
                && Objects.equal(companyId, entity.companyId)
                && Objects.equal(nodeDesc, entity.nodeDesc)
                && SetUtils.isEqualSet(storeIds, entity.storeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodeName, nodeDesc, parentId, ownerId, companyId, storeIds);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("parentId", parentId)
                .add("nodeName", nodeName)
                .add("ownerId", ownerId)
                .add("storeIds", storeIds)
                .add("companyId", companyId)
                .add("nodeDesc", nodeDesc)
                .toString();
    }
}
