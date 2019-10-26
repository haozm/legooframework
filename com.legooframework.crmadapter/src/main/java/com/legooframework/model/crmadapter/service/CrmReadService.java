package com.legooframework.model.crmadapter.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrmReadService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(CrmReadService.class);

    public Optional<CrmStoreEntity> loadStores(LoginContext user) {
        if (user.isShoppingGuide() || user.isStoreManager()) {
            Integer storeId = user.getStoreId();
            Optional<CrmOrganizationEntity> company = getOrgAction().findCompanyById(user.getTenantId().intValue());
            Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在...", user.getTenantId());
            Optional<CrmStoreEntity> store = getStoreAction().findById(company.get(), storeId);
            Preconditions.checkState(store.isPresent(), "id=%s 对应的门店不存在...", storeId);
            return store;
        }
        return Optional.empty();
    }

    public Optional<List<CrmStoreEntity>> loadStores(Integer companyId, Collection<Integer> storeIds) {
        Optional<CrmOrganizationEntity> company = getOrgAction().findCompanyById(companyId);
        if (!company.isPresent()) return Optional.empty();
        return getStoreAction().findByIds(company.get(), storeIds);
    }

    /**
     * 获取下级组织所有门店合集
     *
     * @return
     */
    public Optional<Collection<CrmStoreEntity>> loadSubStores(Integer companyId, Integer orgId) {
        Preconditions.checkNotNull(companyId, "公司不可以为空...");
        Optional<CrmOrganizationEntity> company = getOrgAction().findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        List<CrmOrganizationEntity> orgs = getOrgAction().loadSubOrganizations(company.get().getCompanyId(), orgId);
        List<CrmStoreEntity> stores = getStoreAction().loadAllByCompany(company.get());
        if (CollectionUtils.isEmpty(stores)) return Optional.empty();
        Set<CrmStoreEntity> sub_stores = Sets.newHashSet();
        orgs.forEach(x -> stores.forEach(s -> {
            if (s.isOwnerOrg(x)) {
                sub_stores.add(s);
            }
        }));
        return Optional.ofNullable(CollectionUtils.isEmpty(sub_stores) ? null : sub_stores);
    }

    public CrmAllInOne loadCrmAllInOne(Integer companyId, Integer storeId, Integer employeeId,
                                       Collection<Integer> memberIds) {
        Optional<CrmOrganizationEntity> company = getOrgAction().findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "ID=%s 对应的公司不存在...", companyId);
        Optional<CrmStoreEntity> stores = getStoreAction().findById(company.get(), storeId);
        Preconditions.checkState(stores.isPresent(), "ID=%s 对应的门店不存在...", storeId);
        Optional<CrmEmployeeEntity> employee = Optional.empty();
        if (employeeId != null) {
            employee = getEmployeeAction().findById(company.get(), employeeId);
            Preconditions.checkState(employee.isPresent(), "ID=%s 对应的职员不存在...", employeeId);
        }
        List<CrmMemberEntity> subMember = null;
        if (CollectionUtils.isNotEmpty(memberIds)) {
            Optional<List<CrmMemberEntity>> members = getMemberAction().loadAllByStore(stores.get());
            Preconditions.checkState(members.isPresent(), "门店 %s 无会员信息...", stores.get().getName());
            subMember = members.get().stream().filter(x -> memberIds.contains(x.getId()))
                    .collect(Collectors.toList());
        }
        return new CrmAllInOne(company.get(), stores.get(), employee.orElse(null), subMember);
    }

    public LoginUser loadByLoginName(Integer companyId, String loingName) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByLoginName(companyId:%s,loingName:%s)", companyId, loingName));
        try {
            String url = getBean(TenantsRouteFactory.class).getUrl(companyId, "crmbase.loadLoginInfo");
            Optional<JsonElement> jsonElement = getBean(TenantsRouteFactory.class)
                    .post(url, null, companyId, loingName);
            if (!jsonElement.isPresent()) return null;
            JsonObject json = jsonElement.get().getAsJsonObject();
            List<Integer> storeIds = null;
            JsonElement strIds = json.get("strIds");
            if (strIds != null && !strIds.isJsonNull()) {
                String strIds_args = strIds.getAsString();
                storeIds = Stream.of(StringUtils.split(strIds_args, ',')).mapToInt(Integer::valueOf).boxed()
                        .collect(Collectors.toList());
            }
            String roles = json.get("roles").getAsString();
            List<String> role_list = Splitter.on(',').splitToList(roles);

            List<Integer> _roleIds = null;
            JsonElement roleIds = json.get("roleIds");
            if (roleIds != null && !roleIds.isJsonNull()) {
                String roleIds_args = roleIds.getAsString();
                _roleIds = Stream.of(StringUtils.split(roleIds_args, ',')).mapToInt(Integer::valueOf).boxed()
                        .collect(Collectors.toList());
            }
            LoginUser user = new LoginUser(json.get("id").getAsLong(),
                    json.get("cId").getAsLong(),
                    json.get("name").getAsString(),
                    json.get("pwd").getAsString(),
                    role_list, _roleIds,
                    json.get("sId").getAsInt(),
                    json.get("oId").getAsInt(),
                    storeIds,
                    json.get("cName").getAsString(),
                    StringUtils.equals(json.get("sName").getAsString(), "NULL") ? null : json.get("sName").getAsString());
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadByLoginName(%s,%s) res %s", companyId, loingName, user));
            return user;
        } catch (Exception e) {
            logger.error("还原登陆用户发生异常...", e);
            throw new RestoreLoginUserException(String.format("loadByLoginName(%s,%s) 发生异常", companyId, loingName),
                    e);
        }
    }

}
