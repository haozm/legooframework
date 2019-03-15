package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BetweenDayDto;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.module.base.cache.GuavaCache;
import com.csosm.module.base.cache.GuavaCacheManager;
import com.csosm.module.base.entity.*;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BaseModelServer extends AbstractBaseServer {

    private static final Logger logger = LoggerFactory.getLogger(BaseModelServer.class);

    public void cleanCache(String cacheName) {
        getBean(GuavaCacheManager.class).clearByName(cacheName);
    }

    public Collection<GuavaCache> getAllCaches() {
        return getBean(GuavaCacheManager.class).getAllCache();
    }

    public Optional<List<OrganizationEntity>> findOrgByIds(Integer companyId, Collection<Integer> ids) {
        return getBean(OrganizationEntityAction.class).findOrgByIds(companyId, ids);
    }

    // 给定ID 获取下级全部门店信息  ID可以门店ID 或者组织ID
    public Optional<List<StoreEntity>> loadAllSubStoreByOrg(Integer id, boolean isOrg, Integer companyId) {
        Preconditions.checkNotNull(companyId);
        if (isOrg) {
            OrganizationEntity organization;
            if (companyId.equals(id)) {
                Optional<OrganizationEntity> orgs = getBean(OrganizationEntityAction.class).findCompanyById(companyId);
                if (!orgs.isPresent()) return Optional.absent();
                organization = orgs.get();
            } else {
                Optional<List<OrganizationEntity>> orgs = getBean(OrganizationEntityAction.class)
                        .findOrgByIds(companyId, Lists.newArrayList(id));
                if (!orgs.isPresent()) return Optional.absent();
                organization = orgs.get().get(0);
            }
            return getBean(StoreEntityAction.class).loadAllSubStoreByOrg(organization);
        } else {
            Optional<StoreEntity> store = getBean(StoreEntityAction.class).findById(id);
            if (!store.isPresent()) return Optional.absent();
            List<StoreEntity> list = Lists.newArrayList(store.get());
            return Optional.of(list);
        }
    }

    /**
     * 服务:返回指定 根部门ID= organizationId 的 组织树（不包含部门信息）
     *
     * @param organizationId Integer
     * @return Optional-OrgTreeViewDto
     */
    public Optional<OrgTreeViewDto> loadOrgTreeByOrgId(Integer organizationId, LoginUserContext userContext) {
        Preconditions.checkNotNull(organizationId);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> entityOptional =
                getBean(OrganizationEntityAction.class).findById(organizationId);
        if (!entityOptional.isPresent()) return Optional.absent();

        OrgTreeViewDto root = entityOptional.get().buildOrgTreeDto();
        Optional<List<OrganizationEntity>> sub_orgs_opt = getBean(OrganizationEntityAction.class)
                .loadDirectSubOrgs(entityOptional.get(), userContext.getCompany().get().getId());

        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());

        OrgTreeViewDto.buildTree(root, allNodes);
        return Optional.of(root);
    }

    /**
     * 通过组织ID 获取部门组织树 含 门店 信息 一次性返回
     *
     * @param organizationId 指定的组织ID
     * @return OrgTreeViewDto
     */
    public Optional<OrgTreeViewDto> loadOrgTreeWithoutStoreByOrgId(Integer organizationId, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取用户登录上下文...");
        Preconditions.checkNotNull(organizationId);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> org_root_opt =
                getBean(OrganizationEntityAction.class).findById(organizationId);
        if (!org_root_opt.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> sub_orgs_opt = getBean(OrganizationEntityAction.class)
                .loadAllSubOrgs(org_root_opt.get(), userContext.getCompany().get().getId());
        OrgTreeViewDto root = org_root_opt.get().buildOrgTreeDto();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 含下级组织 %s 个",
                    org_root_opt.get().getName(),
                    sub_orgs_opt.isPresent() ? sub_orgs_opt.get().size() : 0));
        OrgTreeViewDto.buildTree(root, allNodes);
        return Optional.of(root);
    }

    /**
     * 获取当前指定节点的下级组织节点(不含自身)
     */
    public Optional<List<OrganizationEntity>> loadDirectSubOrgs(Integer org_id, LoginUserContext userContext) {
        Preconditions.checkNotNull(org_id);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> entityOptional = getBean(OrganizationEntityAction.class).findById(org_id);
        return getBean(OrganizationEntityAction.class)
                .loadDirectSubOrgs(entityOptional.get(), userContext.getCompany().get().getId());
    }

    /**
     * 获取当前指定节点的下级组织节点以及门店
     */
    public Optional<List<OrgTreeViewDto>> loadDirectSubOrgsAndStores(Integer org_id, LoginUserContext userContext) {
        Optional<OrganizationEntity> org_opt = getBean(OrganizationEntityAction.class).findById(org_id);
        if (!org_opt.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> orgs_opt = loadDirectSubOrgs(org_id, userContext);
        Optional<List<StoreEntity>> stores_opt =
                getBean(StoreEntityAction.class).loadDirectSubStoreByOrg(org_opt.get());
        List<OrgTreeViewDto> orgTreeView = Lists.newArrayList();

        if (orgs_opt.isPresent())
            for (OrganizationEntity $it : orgs_opt.get()) orgTreeView.add($it.buildOrgTreeDto());

        if (stores_opt.isPresent())
            for (StoreEntity $it : stores_opt.get()) orgTreeView.add($it.buildOrgTreeDto());
        return Optional.fromNullable(CollectionUtils.isEmpty(orgTreeView) ? null : orgTreeView);
    }

    /**
     * 通过组织ID 获取部门组织树 含 门店 信息 一次性返回
     *
     * @param organizationId 指定的组织ID
     * @return OrgTreeViewDto
     */
    public Optional<OrgTreeViewDto> loadOrgTreeWithStoreByOrgId(Integer organizationId, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取用户登录上下文...");
        Optional<RoleEntity> maxRole = userContext.getMaxPowerRole();
        // 三无人士 以及 导购 无法查看组织书
        // if (!maxRole.isPresent() || maxRole.get().isDaoGou()) return Optional.absent();
        // 最大角色为店长时 需特殊处理
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

        Preconditions.checkNotNull(organizationId);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> org_root_opt =
                getBean(OrganizationEntityAction.class).findById(organizationId);
        if (!org_root_opt.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> sub_orgs_opt = getBean(OrganizationEntityAction.class)
                .loadAllSubOrgs(org_root_opt.get(), userContext.getCompany().get().getId());
        Optional<List<StoreEntity>> strore_list_opt =
                getBean(StoreEntityAction.class).loadAllSubStoreByOrg(org_root_opt.get());
        OrgTreeViewDto root = org_root_opt.get().buildOrgTreeDto();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (strore_list_opt.isPresent())
            for (StoreEntity o : strore_list_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 含下级组织 %s 个 与 门店 %s 个",
                    org_root_opt.get().getName(),
                    sub_orgs_opt.isPresent() ? sub_orgs_opt.get().size() : 0,
                    strore_list_opt.isPresent() ? strore_list_opt.get().size() : 0));
        OrgTreeViewDto.buildTree(root, allNodes);
        return Optional.of(root);
    }

    public Optional<OrgTreeViewDto> loadRootTreeWithEmps(LoginUserContext userContext) {
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        OrganizationEntity company = userContext.getCompany().get();
        OrgTreeViewDto root = company.buildOrgTreeDto();
        return Optional.of(root);
    }

    public Optional<List<OrgTreeViewDto>> loadOrgTreeWithEmpsByOrgId(Integer organizationId, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取用户登录上下文...");
        Preconditions.checkNotNull(organizationId);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<OrganizationEntity> orgOpt =
                getBean(OrganizationEntityAction.class).findById(organizationId);
        if (!orgOpt.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> subOrgOpt = getBean(OrganizationEntityAction.class)
                .loadDirectSubOrgs(orgOpt.get(), userContext.getCompany().get().getId());
        Optional<List<StoreEntity>> storesOpt =
                getBean(StoreEntityAction.class).loadDirectSubStoreByOrg(orgOpt.get());
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (subOrgOpt.isPresent())
            for (OrganizationEntity o : subOrgOpt.get()) allNodes.add(o.buildOrgTreeDto());
        if (storesOpt.isPresent())
            for (StoreEntity o : storesOpt.get()) allNodes.add(o.buildOrgTreeDto());
        Optional<List<EmployeeEntity>> employees = getBean(EmployeeEntityAction.class).loadEmployeesByOrgOrCom(orgOpt.get(), null);
        if (employees.isPresent())
            employees.get().forEach(x -> allNodes.add(x.buildOrgTreeDto()));
        return Optional.of(allNodes);
    }

    public Optional<List<OrgTreeViewDto>> loadOrgTreeWithEmpByStoreId(Integer storeId, LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext, "无法获取用户登录上下文...");
        Preconditions.checkNotNull(storeId);
        Preconditions.checkArgument(userContext.getCompany().isPresent(), "无法获取当前用户所在的公司.");
        Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class).findById(storeId);
        if (!storeOpt.isPresent()) return Optional.absent();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        Optional<List<EmployeeEntity>> employeesOpt =
                getBean(EmployeeEntityAction.class).loadEmployeesByStore(storeOpt.get(), null);
        if (employeesOpt.isPresent())
            for (EmployeeEntity emp : employeesOpt.get()) allNodes.add(emp.buildOrgTreeDto());
        return Optional.of(allNodes);
    }

    /**
     * 统计报表机构数据
     *
     * @return OrgTreeViewDto
     */
    public OrgTreeViewDto loadOrgTree4Report(BetweenDayDto betweenDate, LoginUserContext userContext) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadOrgTree4Report(%s, user) ", betweenDate.toString()));
        Optional<RoleEntity> maxRole = userContext.getMaxPowerRole();
        // 三无人士 以及 导购 无法查看组织书
        if (!maxRole.isPresent() || maxRole.get().isShoppingGuide()) return null;
        // 最大角色为店长时 需特殊处理
        if (maxRole.get().isStoreManager()) {
            Optional<Integer> store_id = userContext.getEmployee().getStoreId();
            if (!store_id.isPresent()) {
                logger.warn(String.format("当前店长%s没有绑定任何门店.", userContext.getEmployee().getUserName()));
                return null;
            }
            StoreEntity storeEntity = getBean(StoreEntityAction.class).loadById(store_id.get());
            return storeEntity.buildOrgTreeDto();
        }

        Preconditions.checkState(userContext.getOrganization().isPresent(), "当前用户尚未分配到组织，无法加载组织树.");
        Optional<OrganizationEntity> org_root_opt = getBean(OrganizationEntityAction.class)
                .findById(userContext.getOrganization().get().getId());

        if (!org_root_opt.isPresent()) return null;

        Optional<List<OrganizationEntity>> sub_orgs_opt =
                getBean(OrganizationEntityAction.class).loadAllOrgs4Report(betweenDate, userContext);

        Optional<List<StoreEntity>> strore_list_opt =
                getBean(StoreEntityAction.class).loadAllSubStoreByOrg(org_root_opt.get());

        OrgTreeViewDto root = org_root_opt.get().buildOrgTreeDto();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (strore_list_opt.isPresent())
            for (StoreEntity o : strore_list_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 含下级组织 %s 个 与 门店 %s 个",
                    org_root_opt.get().getName(),
                    sub_orgs_opt.isPresent() ? sub_orgs_opt.get().size() : 0,
                    strore_list_opt.isPresent() ? strore_list_opt.get().size() : 0));
        OrgTreeViewDto.buildTree(root, allNodes);
        return root;
    }

    public LoginUserContext loginUserContextByDeviceId(String deviceId, HttpServletRequest request) {
        Preconditions.checkArgument(null != deviceId, "入参 deviceId 不可以为空值.");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).loadStoreByDeviceId(deviceId);
        Preconditions.checkState(store.isPresent(), "非法的设备ID=%s", deviceId);
        Preconditions.checkState(store.get().getCompanyId().isPresent(), "门店%s无公司信息", store.get().getName());
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class).findCompanyById(store.get().getCompanyId().get());
        Preconditions.checkState(company.isPresent(), "Id=%s 对应的公司不存在...", store.get().getCompanyId().get());
        String loginDomain = "http://localhost:8080/";
        if (request != null) {
            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).build();
            loginDomain = String.format("%s://%s:%s",
                    uriComponents.getScheme(), uriComponents.getHost(), uriComponents.getPort());
        }
        String ip = "localhost";
        if (request != null) ip = MyWebUtil.getIpAddr(request);
        return new LoginUserContext(store.get(), company.get(), loginDomain, ip);
    }

    public LoginUserContext loadByUserName(Integer companyId, String userName) {
        // 登陆账户所属公司
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class)
                .findCompanyById(Integer.valueOf(companyId));
        Preconditions.checkState(company.isPresent(), "Id =%s 对应的公司不存在...", companyId);
        // 登陆账户信息
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class).findByLoginName(userName, company.get());
        if (!employee.isPresent()) return null;
        
        // 登陆账户角色信息
        RoleSet roleSet = getBean(RoleEntityAction.class).loadRoleSetByUser(employee.get());

        // 登陆账户门店信息
        Optional<StoreEntity> store = Optional.absent();
        if (employee.get().getStoreId().isPresent()) {
            store = getBean(StoreEntityAction.class).findById(employee.get().getStoreId().get());
            Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...");
        }

        // 登陆账户所在组织以及下级组织
        Optional<OrganizationEntity> organization = Optional.absent();
        Set<Integer> subOrgs = null;
        if (employee.get().getOrganizationId().isPresent()) {
            organization = getBean(OrganizationEntityAction.class)
                    .findById(employee.get().getOrganizationId().get());
            Preconditions.checkState(organization.isPresent(), "Id=%s 对应的组织节点不存在...");
            Optional<List<OrganizationEntity>> organizations = getBean(OrganizationEntityAction.class)
                    .loadAllSubOrgs(Integer.valueOf(companyId), employee.get().getOrganizationId().get(), true);
            if (organizations.isPresent()) {
                subOrgs = Sets.newHashSet();
                for (OrganizationEntity sub : organizations.get()) {
                    subOrgs.add(sub.getId());
                }
            }
        }

        // 登陆账户所在门店
        List<Integer> subStoreIds = Lists.newArrayList();
        boolean hasStoreView = false;
        if(getBean(StoreViewEntityAction.class).hasStoreView(employee.get())) {
        	List<StoreEntity> treeStores = getBean(StoreEntityAction.class).loadTreeStores(employee.get());
        	treeStores.forEach(x -> subStoreIds.add(x.getId()));
        	hasStoreView = true;
        }else if (roleSet.getMaxPowerRole().isPresent() && !(roleSet.getMaxPowerRole().get().isStoreManager() ||
                roleSet.getMaxPowerRole().get().isShoppingGuide()) && organization.isPresent()) {
            Optional<List<StoreEntity>> listOptional = getBean(StoreEntityAction.class)
                    .loadAllSubStoreByOrg(organization.get());
            if (listOptional.isPresent())
                for (StoreEntity $it : listOptional.get()) subStoreIds.add($it.getId());
        }

        String loginDomain = "http://localhost:80/";
        String ip = "localhost";
        
        LoginUserContext user = new LoginUserContext(employee.get(), organization.isPresent() ? organization.get() : null,
                store, company.get(), roleSet, loginDomain, ip, subStoreIds, subOrgs, null,hasStoreView);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByUserName( %s , %s) return %s", companyId, userName, user));
        return user;
    }

    private LoginUserContext loadLoginUser(EmployeeEntity employee, HttpServletRequest request) {
        RoleSet roleSet = getBean(RoleEntityAction.class).loadRoleSetByUser(employee);

        Optional<StoreEntity> store = Optional.absent();
        if (employee.getStoreId().isPresent()) {
            store = getBean(StoreEntityAction.class).findById(employee.getStoreId().get());
        }

        Optional<OrganizationEntity> organization = Optional.absent();
        Set<Integer> subOrgs = null;
        if (employee.getOrganizationId().isPresent()) {
            organization = getBean(OrganizationEntityAction.class)
                    .findById(employee.getOrganizationId().get());
            Optional<List<OrganizationEntity>> organizations = getBean(OrganizationEntityAction.class)
                    .loadAllSubOrgs(employee.getCompanyId().get(), employee.getOrganizationId().get(), true);
            if (organizations.isPresent()) {
                subOrgs = Sets.newHashSet();
                for (OrganizationEntity sub : organizations.get()) {
                    subOrgs.add(sub.getId());
                }
            }
        }

        List<Integer> subStoreIds = Lists.newArrayList();
        boolean hasStoreView = false;
        if(getBean(StoreViewEntityAction.class).hasStoreView(employee)) {
        	List<StoreEntity> treeStores = getBean(StoreEntityAction.class).loadTreeStores(employee);
        	treeStores.forEach(x -> subStoreIds.add(x.getId()));
        	hasStoreView = true;
        }else if (roleSet.getMaxPowerRole().isPresent() && !(roleSet.getMaxPowerRole().get().isStoreManager() ||
                roleSet.getMaxPowerRole().get().isShoppingGuide()) && organization.isPresent()) {
            Optional<List<StoreEntity>> listOptional = getBean(StoreEntityAction.class)
                    .loadAllSubStoreByOrg(organization.get());
            if (listOptional.isPresent())
                for (StoreEntity $it : listOptional.get()) subStoreIds.add($it.getId());
        }

        Optional<OrganizationEntity> company = Optional.absent();
        int org_deep = -1;
        if (employee.getCompanyId().isPresent()) {
            company = getBean(OrganizationEntityAction.class).findCompanyById(employee.getCompanyId().get());
            Preconditions.checkState(company.isPresent(), "无法获取绑定用户Id=%d 对应的公司信息。",
                    employee.getCompanyId().get());
            org_deep = getBean(OrganizationEntityAction.class).loadReportOrgDeep(employee.getCompanyId().get());
        }

        String loginDomain = "http://localhost:80/";
        if (request != null) {
            UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(request.getRequestURL().toString()).build();
            if (uriComponents.getPort() == -1) {
                loginDomain = String.format("http://%s", uriComponents.getHost());
            } else {
                loginDomain = String.format("http://%s:%s", uriComponents.getHost(), uriComponents.getPort());
            }
        }

        String ip = "localhost";
        if (request != null) ip = MyWebUtil.getIpAddr(request);

        return new LoginUserContext(employee, organization.isPresent() ? organization.get() : null,
                store, company.isPresent() ? company.get() : null, roleSet, loginDomain, ip,
                subStoreIds, subOrgs, null,hasStoreView);
    }

    public void registerCompany(LoginUserContext loginUser, Integer companyId, String comName, String comShortName,
                                String loginName, String linkMan, String linkPhone, Integer industryType) {
        Objects.requireNonNull(loginUser);
//    	Preconditions.checkState(loginUser.getRoleSet().hasDBARole(), "用户无权限创建公司");
        OrganizationEntity company = getBean(OrganizationEntityAction.class).saveCompany(loginUser, companyId,
                comName, industryType, comShortName, linkMan, linkPhone);
        getBean(EmployeeEntityAction.class).saveOrgEmployee(loginUser, company, loginName, null, null, 1, null);
    }

    public Optional<OrgTreeViewDto> loadOrgTree(Integer companyId) {
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class).findCompanyById(companyId);
        if (!company.isPresent()) return Optional.absent();
        Optional<List<OrganizationEntity>> sub_orgs_opt = getBean(OrganizationEntityAction.class)
                .loadAllByCompany(company.get());
        Optional<List<StoreEntity>> strore_list_opt =
                getBean(StoreEntityAction.class).loadAllStoreByCompany(company.get());
        OrgTreeViewDto root = company.get().buildOrgTreeDto();
        List<OrgTreeViewDto> allNodes = Lists.newArrayList();
        if (sub_orgs_opt.isPresent())
            for (OrganizationEntity o : sub_orgs_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (strore_list_opt.isPresent())
            for (StoreEntity o : strore_list_opt.get()) allNodes.add(o.buildOrgTreeDto());
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s 含下级组织 %s 个 与 门店 %s 个",
                    company.get().getName(),
                    sub_orgs_opt.isPresent() ? sub_orgs_opt.get().size() : 0,
                    strore_list_opt.isPresent() ? strore_list_opt.get().size() : 0));
        OrgTreeViewDto.buildTree(root, allNodes);
        return Optional.of(root);
    }

    /**
     * 加载当前登录用户上下文
     *
     * @param userId 登陆用户Id
     * @return LoginUserContext
     */
    public LoginUserContext loadContextByUserId(Integer userId, OrganizationEntity company) {
        Preconditions.checkArgument(null != userId, "入参 userId 不可以为空值.");
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class).findByUserId(userId, company);
        Preconditions.checkState(employee.isPresent(), "不存在employeeId=%s 对应的系统用户信息.", userId);
        return loadLoginUser(employee.get(), null);
    }
}
