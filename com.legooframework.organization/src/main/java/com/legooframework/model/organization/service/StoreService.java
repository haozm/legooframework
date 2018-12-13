package com.legooframework.model.organization.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.commons.dto.TreeStructure;
import com.legooframework.model.commons.dto.TreeStructureBuilder;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.organization.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StoreService extends OrgService {

    private static final Logger logger = LoggerFactory.getLogger(StoreService.class);

    public Optional<List<StoreEntity>> loadStoresByCompany(Long companyId) throws Exception {
        Preconditions.checkNotNull(companyId, "门店companyId不可以为空.");
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(companyId);
        Preconditions.checkArgument(company.isPresent(), "id= %s 对应的公司不存在.", companyId);
        return getBean(StoreEntityAction.class).loadAllByCompany(company.get());
    }

    public Optional<List<StoreEntity>> loadOpeningStoresByCompany(String companyId) throws Exception {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(companyId), "门店companyId不可以为空.");
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(companyId);
        Preconditions.checkArgument(company.isPresent(), "id= %s 对应的公司不存在.",
                companyId);
        Optional<List<StoreEntity>> list = getBean(StoreEntityAction.class).loadAllByCompany(company.get());
        if (list.isPresent()) {
            List<StoreEntity> ss = list.get().stream().filter(StoreEntity::isOpenning).collect(Collectors.toList());
            return Optional.ofNullable(CollectionUtils.isEmpty(ss) ? null : ss);
        }
        return Optional.empty();
    }

    public void bindingDeviceToStore(Long storeId, String deviceId) {
        Preconditions.checkNotNull(storeId, "门店ID不可以为空");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备ID不可以为空值");
        Optional<EquipmentEntity> equipment = getBean(EquipmentEntityAction.class).findById(deviceId);
        Preconditions.checkState(equipment.isPresent(), "ID=%s对应的设备不存在。", deviceId);
        boolean res = getBean(StoreEntityAction.class).bindingDeviceToStore(storeId, equipment.get());
        if (res && !equipment.get().isActivated()) {
            // 激活设备
            getBean(EquipmentEntityAction.class).activedDevice(deviceId);
        }
    }

    // 获取当前登陆账号对应的整个公司组织树
    @SuppressWarnings("unchecked")
    public TreeStructure loadAllStoreTree(Long companyId) {
        Preconditions.checkNotNull(companyId, "入参 Long companyId 不可以为空...");
        Optional<CompanyEntity> company = getBean(CompanyEntityAction.class).findById(companyId);
        Preconditions.checkState(company.isPresent(), "当前登陆用户%s对应的公司不存在...", companyId);
        Optional<List<StoreTreeEntity>> tree_nodes = getBean(StoreTreeAction.class).loadAll(company.get());
        Preconditions.checkState(tree_nodes.isPresent(), "当前公司不存在对应的组织树配置...");
        List<TreeStructure> trees = tree_nodes.get().stream().map(StoreTreeEntity::getTreeNode)
                .collect(Collectors.toList());
        Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).loadAllByCompany(company.get());
        List<TreeStructure> _trees = Lists.newArrayList(trees);
        stores.ifPresent($st -> _trees.forEach(x -> {
            List<Long> storeIds = (List<Long>) x.getAttachData().get("storeIds");
            if (CollectionUtils.isNotEmpty(storeIds)) {
                $st.stream().filter($t -> storeIds.contains($t.getId()))
                        .collect(Collectors.toSet())
                        .forEach($u -> trees.add($u.getTreeNode(x)));
            }
        }));
        Optional<TreeStructure> root = TreeStructureBuilder.buildTree(trees);
        Preconditions.checkState(root.isPresent(), "装配公司%s组织树目录失败...", company.get().getFullName());
        return root.get();
    }

    public StoreTreeEntity addTreeNode(Long pid, String nodeName, int nodeSeq, LoginContext loginUser) {
        Preconditions.checkNotNull(loginUser, "入参 LoginContext loginUser 不可以为空...");
        LoginContextHolder.setCtx(loginUser);
        Optional<StoreTreeEntity> entity = getBean(StoreTreeAction.class).findById(pid);
        Preconditions.checkState(entity.isPresent(), "id=%s 对应的组织节点不存在...", pid);
        Long id = getBean(StoreTreeAction.class).addNode(entity.get(), nodeName, nodeSeq);
        Optional<StoreTreeEntity> exits = getBean(StoreTreeAction.class).findById(id);
        Preconditions.checkState(exits.isPresent(), "新增的节点无法通过ID=%s获取", id);
        return exits.get();
    }

    /**
     * 挂载门店到组织树
     *
     * @param nodeId   组织节点ID
     * @param storeIds 门店ID
     * @return TreeStructure
     */
    public boolean addStoreToTree(Long nodeId, Long... storeIds) throws Exception {
        Preconditions.checkNotNull(nodeId, "待添加门店的上级节点不可以为空...");
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(storeIds), "待添加门店ID不可以为空...");
        Optional<List<StoreEntity>> stores = loadStoresByCompany(LoginContextHolder.get().getTenantId());
        Preconditions.checkState(stores.isPresent(), "当前用户所在的公司无有效门店...");
        Set<StoreEntity> sub_stores = stores.get().stream().filter(x -> ArrayUtils.contains(storeIds, x.getId()))
                .collect(Collectors.toSet());
        Preconditions.checkState(sub_stores.size() == storeIds.length,
                "数据异常，ID=%s 对应的实体门店数量%s不一致...", Arrays.toString(storeIds), sub_stores.size());
        return getBean(StoreTreeAction.class).addStores(nodeId, sub_stores);
    }

    /**
     * 删除门店cong组织树
     *
     * @return TreeStructure
     */
    public boolean removeStoreFromTree(Long nodeId, Long... storeIds) throws Exception {
        Preconditions.checkNotNull(nodeId, "待删除门店的上级节点不可以为空...");
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(storeIds), "待删除门店ID不可以为空...");
        Optional<List<StoreEntity>> stores = loadStoresByCompany(LoginContextHolder.get().getTenantId());
        Preconditions.checkState(stores.isPresent(), "当前用户所在的公司无有效门店...");
        Set<StoreEntity> sub_stores = stores.get().stream().filter(x -> ArrayUtils.contains(storeIds, x.getId()))
                .collect(Collectors.toSet());
        Preconditions.checkState(sub_stores.size() == storeIds.length,
                "数据异常，ID=%s 对应的实体门店数量%s不一致...", Arrays.toString(storeIds), sub_stores.size());
        return getBean(StoreTreeAction.class).removeStores(nodeId, sub_stores);
    }

}
