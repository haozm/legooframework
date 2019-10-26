package com.csosm.module.storeview;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.storeview.entity.StoreTreeViewDto;
import com.csosm.module.storeview.entity.StoreViewEntity;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author haoxiaojie
 * @date
 */
public class StoreViewService extends AbstractBaseServer {

    private static Logger logger = LoggerFactory.getLogger(StoreViewService.class);

    /**
     * 加载当前指定用户的 门店透视图的 树结构（含门店） 树 根节点 为 当前用户所在的组织
     *
     * @param employee 用户信息
     * @return TreeData
     */
    public StoreTreeViewDto loadDataPermissionTreeByUser(EmployeeEntity employee, LoginUserContext user) {
        Preconditions.checkNotNull(employee, "请指定入参 employee，不允许为空值.");
        List<StoreViewEntity> tree_root_list = getBean(StoreViewEntityAction.class).loadDataPermissionTree(employee, user);
        List<StoreTreeViewDto> storeTreeView = tree_root_list.stream()
                .map(StoreViewEntity::buildTreeNode).collect(Collectors.toList());
        java.util.Optional<StoreTreeViewDto> rootNode = storeTreeView.stream()
                .filter(StoreTreeViewDto::isRoot).findFirst();
        Preconditions.checkState(rootNode.isPresent(), "缺少根节点,加载树异常....");

        // 实例化门店节点
        Set<Integer> storeIds = Sets.newHashSet();
        tree_root_list.stream().filter(StoreViewEntity::hasStores).forEach(node -> {
            storeIds.addAll(node.getStoreIds());
        });
        Optional<List<StoreEntity>> store_list_opt = getBean(StoreEntityAction.class).findByIds(storeIds);
        // 实例化门店节点完成

        LinkedList<StoreTreeViewDto> linkedList = Lists.newLinkedList();
        linkedList.addLast(rootNode.get());
        StoreTreeViewDto $pop;
        List<StoreTreeViewDto> temp = Lists.newArrayList();
        while (!linkedList.isEmpty()) {
            $pop = linkedList.pop();
            temp.clear();
            if (CollectionUtils.isEmpty(storeTreeView)) break;
            for (StoreTreeViewDto $em : storeTreeView) {
                if ($pop.isMyChild($em)) {
                    $pop.addChildNotCheck($em);
                    linkedList.addLast($em);
                }
            }
            storeTreeView.removeAll(temp);
            $pop.addStoreNode(store_list_opt.orNull());
        }
        return rootNode.get();
    }

    /**
     * 加载当前指定用户的 门店透视图的 树结构（含门店） 树 根节点 为 当前用户所在的组织
     *
     * @param company 公司信息
     * @return TreeData
     */
    public StoreTreeViewDto loadSmsRechargeTree(OrganizationEntity company, LoginUserContext user) {
        Preconditions.checkNotNull(company, "请指定入参 company，不允许为空值.");
        List<StoreViewEntity> tree_root_list = getBean(StoreViewEntityAction.class).loadSmsRechargeTree(company, user);
        List<StoreTreeViewDto> storeTreeView = tree_root_list.stream()
                .map(StoreViewEntity::buildTreeNode).collect(Collectors.toList());
        java.util.Optional<StoreTreeViewDto> rootNode = storeTreeView.stream()
                .filter(StoreTreeViewDto::isRoot).findFirst();
        Preconditions.checkState(rootNode.isPresent(), "缺少根节点,加载树异常....");

        // 实例化门店节点
        Set<Integer> storeIds = Sets.newHashSet();
        tree_root_list.stream().filter(StoreViewEntity::hasStores).forEach(node -> {
            storeIds.addAll(node.getStoreIds());
        });
        Optional<List<StoreEntity>> store_list_opt = getBean(StoreEntityAction.class).findByIds(storeIds);
        // 实例化门店节点完成

        LinkedList<StoreTreeViewDto> linkedList = Lists.newLinkedList();
        linkedList.addLast(rootNode.get());
        StoreTreeViewDto $pop;
        List<StoreTreeViewDto> temp = Lists.newArrayList();
        while (!linkedList.isEmpty()) {
            $pop = linkedList.pop();
            temp.clear();
            if (CollectionUtils.isEmpty(storeTreeView)) break;
            for (StoreTreeViewDto $em : storeTreeView) {
                if ($pop.isMyChild($em)) {
                    $pop.addChildNotCheck($em);
                    linkedList.addLast($em);
                    temp.add($em);
                }
            }
            storeTreeView.removeAll(temp);
            $pop.addStoreNode(store_list_opt.orNull());
        }
        return rootNode.get();
    }

    /**
     * 新增一组门店到指定的叶子节点
     *
     * @param nodeId
     * @param storeIds
     * @param loginUser
     */
    public void addStoresToGroupNode(Object nodeId, Collection<Integer> storeIds, LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser);
        Preconditions.checkNotNull(nodeId);
        if (CollectionUtils.isEmpty(storeIds)) return;
        Optional<List<StoreEntity>> storeListOpt = getBean(StoreEntityAction.class).findByIds(storeIds);
        if (!storeListOpt.isPresent()) return;
        getBean(StoreViewEntityAction.class).addStoresToNode(nodeId, storeListOpt.get(), loginUser);
        logProxy(SystemlogEntity.create(this.getClass(), "addStoresToGroupNode",
                String.format("新增组织分组%s节点", nodeId), "门店透视图"));
    }

    public void removeStoresFromGroupNode(Object nodeId, Collection<Integer> storeIds, LoginUserContext loginUser) {
        Preconditions.checkNotNull(loginUser);
        Preconditions.checkNotNull(nodeId);
        if (CollectionUtils.isEmpty(storeIds)) return;
        Optional<List<StoreEntity>> storeListOpt = getBean(StoreEntityAction.class).findByIds(storeIds);
        if (!storeListOpt.isPresent()) return;
        getBean(StoreViewEntityAction.class).removeStoreFromGroupNode(nodeId, storeListOpt.get());
        logProxy(SystemlogEntity.create(this.getClass(), "removeStoresFromGroupNode",
                String.format("删除组织分组%s节点", nodeId), "门店透视图"));
    }


    /**
     * @param parentId  上级节点ID
     * @param name      分组名称
     * @param loginUser 登陆用户信息
     * @return 新增成功后的节点信息
     */
    public StoreViewEntity addSubGroupNode(String parentId, String name, String nodeDesc, LoginUserContext loginUser) {
        Object id = getBean(StoreViewEntityAction.class).addSubGroupNode(parentId, name, nodeDesc, loginUser);
        Optional<StoreViewEntity> opt = getBean(StoreViewEntityAction.class).findById(id);
        Preconditions.checkState(opt.isPresent());
        logProxy(SystemlogEntity.create(this.getClass(), "addSubGroupNode",
                String.format("新增组织分组%s节点", id), "门店透视图"));
        return opt.get();
    }


    /**
     * 通过组织ID 获取部门组织树 含 门店 信息 一次性返回
     *
     * @return OrgTreeViewDto
     */
    public Optional<OrgTreeViewDto> loadOrgTreeWithStoreByOrgId(LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取用户登录上下文...");
        Optional<RoleEntity> maxRole = userContext.getMaxPowerRole();
        if (maxRole.isPresent() && (maxRole.get().isShoppingGuide() || maxRole.get().isStoreManager())) {
            Optional<Integer> store_id = userContext.getEmployee().getStoreId();
            if (!store_id.isPresent()) {
                logger.warn(String.format("当前 %s 没有绑定任何门店.", userContext.getEmployee().getUserName()));
                return Optional.absent();
            }
            StoreEntity storeEntity = getBean(StoreEntityAction.class).loadById(store_id.get());
            OrgTreeViewDto root = storeEntity.buildOrgTreeDto();
            return Optional.of(root);
        }
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> org_root_opt =
                getBean(OrganizationEntityAction.class).findById(userContext.getCompany().get().getId());
        if (!org_root_opt.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> sub_orgs_opt = getBean(OrganizationEntityAction.class)
                .loadAllSubOrgs(org_root_opt.get(), userContext.getCompany().get().getId());
        Optional<List<StoreEntity>> strore_list_opt =
                getBean(StoreEntityAction.class).loadAllSubStoreByOrg(org_root_opt.get());
        OrgTreeViewDto root = org_root_opt.get().buildOrgTreeDto();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (strore_list_opt.isPresent()) {
            List<StoreEntity> stores = strore_list_opt.get();
            if (userContext.getOrganization().isPresent() && userContext.getOrganization().get().isDept()) {
                List<StoreEntity> treeStores = getBean(StoreEntityAction.class).loadTreeStores(userContext.getEmployee());
                stores = strore_list_opt.get().stream().filter(x -> treeStores.stream().map(y -> y.getId()).collect(Collectors.toList()).contains(x.getId())).collect(Collectors.toList());
            }
            for (StoreEntity o : stores)
                allNodes.add(o.buildOrgTreeDto());
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 含下级组织 %s 个 与 门店 %s 个",
                    org_root_opt.get().getName(),
                    sub_orgs_opt.isPresent() ? sub_orgs_opt.get().size() : 0,
                    strore_list_opt.isPresent() ? strore_list_opt.get().size() : 0));
        OrgTreeViewDto.buildTree(root, allNodes);
        return Optional.of(root);
    }

    public void editSubGroupNode(String nodeId, String nodeName, String nodeDesc, LoginUserContext loginUser) {
        getBean(StoreViewEntityAction.class).editGroupNodeName(nodeId, nodeName, nodeDesc, loginUser);
        logProxy(SystemlogEntity.update(this.getClass(), "editSubGroupNode",
                String.format("编辑组织分组%s节点", nodeId), "门店透视图"));
    }

    public void removeSubGroupNodeById(Object nodeId, LoginUserContext loginUser) {
        getBean(StoreViewEntityAction.class).removeSubNodeById(nodeId, loginUser);
        logProxy(SystemlogEntity.delete(this.getClass(), "removeSubGroupNodeById",
                String.format("删除组织分组%s", nodeId), "门店透视图"));
    }


}
