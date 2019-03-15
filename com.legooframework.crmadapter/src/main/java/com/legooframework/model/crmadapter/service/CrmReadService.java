package com.legooframework.model.crmadapter.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CrmReadService extends CrmAdapterService {

    private static final Logger logger = LoggerFactory.getLogger(CrmReadService.class);

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

    LoginUser loadByLoginName(Integer companyId, String loingName) {
        try {
            String url = getBean(TenantsRouteFactory.class).getUrl(companyId, "loadLoginInfo");
            Optional<JsonElement> jsonElement = getBean(TenantsRouteFactory.class)
                    .post(url, null, companyId, loingName);
            if (!jsonElement.isPresent()) return null;
            jsonElement.ifPresent(System.out::println);
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
            String name = (json.get("name") == null || json.get("name").isJsonNull()) ? "登陆者" : json.get("name").getAsString();
            LoginUser user = new LoginUser(json.get("id").getAsLong(), json.get("cId").getAsLong(),
                    name, json.get("pwd").getAsString(),
                    role_list, json.get("sId").getAsInt(), json.get("oId").getAsInt(), storeIds,
                    json.get("cName").getAsString(), json.get("sName").getAsString());
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
