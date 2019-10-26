package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.entity.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

public class OrganizationServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(StoreServer.class);

    /**
     * 新建组织
     *
     * @param parentId  组织父节点
     * @param name      组织名称
     * @param shortName 组织短称
     */
    public void saveOrganization(LoginUserContext loginUser, Integer parentId, String name, String shortName) {
        Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
        Preconditions.checkState(loginUser.getRoleSet().hasAdminRole(), "登录用户无权限新增组织");
        Objects.requireNonNull(parentId, "父节点编号【parentId】不能为空");
        OrganizationEntity parent = getBean(OrganizationEntityAction.class).loadById(parentId);
        getBean(OrganizationEntityAction.class).saveOrgination(loginUser, parent, name, shortName);
    }

    /**
     * 编辑组织
     *
     * @param orgId       组织编号
     * @param name        组织名称
     * @param shortName   组织短称
     * @param hiddenPhone 是否隐藏会员电话
     */
    public void editOrganization(LoginUserContext loginUser, Integer orgId, String name, String shortName,
                                 Integer industryType, Integer orgShowFlag, Integer hiddenPhone) {
        Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
        Preconditions.checkState(!loginUser.getRoleSet().hasShoppingGuideRole()
                && !loginUser.getRoleSet().hasShoppingGuideRole(), "登录用户无权限修改组织信息");
        Objects.requireNonNull(orgId, "组织编号【orgId】不能为空");
        OrganizationEntity org = getBean(OrganizationEntityAction.class).loadById(orgId);
        if (org.isCompany()) {
            getBean(OrganizationEntityAction.class).editCompany(loginUser, org, name, shortName, industryType, orgShowFlag, hiddenPhone);
            return;
        }
        getBean(OrganizationEntityAction.class).editOrgination(loginUser, org, name, shortName, hiddenPhone);
    }

    /**
     * 删除组织
     *
     * @param orgId 组织编号
     */
    public void removeOrganization(LoginUserContext loginUser, Integer orgId) {
        Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
        Preconditions.checkState(loginUser.getRoleSet().hasAdminRole(), "登录用户无权限移除组织");
        Objects.requireNonNull(orgId, "组织编号【orgId】不能为空");
        OrganizationEntity org = getBean(OrganizationEntityAction.class).loadById(orgId);
        Preconditions.checkState(!org.hasSubOrgs(), "组织包含下级组织,不允许删除");
        Optional<List<StoreEntity>> storesOpt = getBean(StoreEntityAction.class).loadAllStoreByOrganization(org);
        Preconditions.checkState(!storesOpt.isPresent()||CollectionUtils.isEmpty(storesOpt.get()), "组织包含下级门店,不允许删除");
        getBean(OrganizationEntityAction.class).removeOrgination(org);
    }

    /**
     * 迁移组织
     *
     * @param loginUser
     * @param parentId
     * @param orgId
     */
    public void switchOrganization(LoginUserContext loginUser, Integer parentId, Integer orgId) {
        Objects.requireNonNull(loginUser, "当前登录用户[loginUser]不能为空");
        Preconditions.checkState(loginUser.getRoleSet().hasAdminRole(), "登录用户无权限迁移组织");
        Objects.requireNonNull(parentId, "父组织编号【parentId】不能为空");
        Objects.requireNonNull(orgId, "组织编号【orgId】不能为空");
        OrganizationEntity parent = getBean(OrganizationEntityAction.class).loadById(parentId);
        OrganizationEntity org = getBean(OrganizationEntityAction.class).loadById(orgId);
        getBean(OrganizationEntityAction.class).switchOrgization(parent, org);
    }


    /**
     * 迁移组织
     */
    public Optional<OrgTreeViewDto> loadOgrTreeWithStoreNoPower(Integer companyId) {
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "Id=%s 对应的公司不存在...", companyId);
        Optional<List<OrganizationEntity>> orgs = getBean(OrganizationEntityAction.class).loadAllByCompany(company.get());
        Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class).loadAllStoreByCompany(company.get());
        OrgTreeViewDto root = company.get().buildOrgTreeDto();
        List<OrgTreeViewDto> tree_nodes = Lists.newArrayList();
        if (orgs.isPresent()) {
            for (OrganizationEntity org : orgs.get()) {
                tree_nodes.add(org.buildOrgTreeDto());
            }
        }
        if (stores.isPresent()) {
            for (StoreEntity str : stores.get()) {
                tree_nodes.add(str.buildOrgTreeDto());
            }
        }
        OrgTreeViewDto.buildTree(root, tree_nodes);
        return Optional.of(root);
    }
}