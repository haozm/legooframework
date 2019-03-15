package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.*;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller(value = "crmAdapterController")
@RequestMapping(value = "/inner")
public class CrmAdapterController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CrmAdapterController.class);

    @RequestMapping(value = "/{type}/{companyId}/all.json")
    @ResponseBody
    public Map<String, Object> loadAllCompOrOrg(@PathVariable(value = "type") String type,
                                                @PathVariable(value = "companyId") Integer companyId,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllCompany(url=%s)", request.getRequestURL()));
        if (StringUtils.equalsIgnoreCase(type, "company")) {
            Optional<List<OrganizationEntity>> coms = getBean(OrganizationEntityAction.class, request).loadAllCompanies();
            if (coms.isPresent()) {
                List<Map<String, Object>> datas = Lists.newArrayList();
                for (OrganizationEntity $it : coms.get()) {
                    Map<String, Object> data = Maps.newHashMap();
                    data.put("id", $it.getId());
                    data.put("companyId", $it.getId());
                    data.put("name", $it.getName());
                    data.put("code", $it.getCode());
                    data.put("shortName", $it.getShortName().or("未定义"));
                    datas.add(data);
                }
                return wrapperResponse(datas);
            }
        } else if (StringUtils.equalsIgnoreCase(type, "org")) {
            Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
            Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);
            Optional<List<OrganizationEntity>> list = getBean(OrganizationEntityAction.class, request)
                    .loadAllByCompany(company.get());
            if (list.isPresent()) {
                List<Map<String, Object>> datas = Lists.newArrayList();
                for (OrganizationEntity $it : list.get()) {
                    Map<String, Object> data = Maps.newHashMap();
                    data.put("id", $it.getId());
                    data.put("companyId", companyId);
                    data.put("name", $it.getName());
                    data.put("code", $it.getCode());
                    data.put("shortName", null);
                    datas.add(data);
                }
                return wrapperResponse(datas);
            }
        } else if (StringUtils.equalsIgnoreCase(type, "store")) {
            if (logger.isDebugEnabled())
                logger.debug(String.format("loadAllStore(url=%s)", request.getRequestURL()));
            Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
            Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);

            Optional<List<OrganizationEntity>> orgs = getBean(OrganizationEntityAction.class, request)
                    .loadAllByCompany(company.get());

            Optional<List<StoreEntity>> list = getBean(StoreEntityAction.class, request)
                    .loadAllStoreByCompany(company.get());
            Preconditions.checkState(list.isPresent(), "当前公司 %s 无门店信息。", company.get().getName());

            List<Map<String, Object>> datas = Lists.newArrayList();
            for (StoreEntity $it : list.get()) {
                Map<String, Object> data = Maps.newHashMap();
                data.put("id", $it.getId());
                data.put("companyId", companyId);
                data.put("name", $it.getName());
                data.put("orgId", $it.getOrganizationId().or(-1));
                data.put("orgCode", "-1");
                if (orgs.isPresent()) {
                    for (OrganizationEntity org : orgs.get()) {
                        if ($it.getOrganizationId().or(-1).equals(org.getId())) {
                            data.put("orgCode", org.getCode());
                            break;
                        }
                    }
                }
                datas.add(data);
            }
            return wrapperResponse(datas);
        }
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/employee/{companyId}/{storeId}/bystore.json")
    @ResponseBody
    public Map<String, Object> loadByStore(@PathVariable(value = "companyId") Integer companyId,
                                           @PathVariable(value = "storeId") Integer storeId,
                                           HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByStore(url=%s)", request.getRequestURL()));
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);

        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, company.get());
        Preconditions.checkState(store.isPresent(), "不存在 storeId = %s 对应的门店", storeId);

        Optional<List<EmployeeEntity>> emps = getBean(EmployeeEntityAction.class, request).loadEmployeesByStore(store.get(), null);
        Preconditions.checkState(emps.isPresent(), "当前门店 %s 无职员信息...", store.get().getName());

        List<Map<String, Object>> datas = Lists.newArrayList();
        for (EmployeeEntity $it : emps.get()) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("id", $it.getId());
            data.put("companyId", companyId);
            data.put("orgId", $it.getOrganizationId().or(-1));
            data.put("storeId", $it.getStoreId().or(-1));
            data.put("name", $it.getUserName());
            data.put("roleIds", null);
            if ($it.getRoleIds().isPresent()) data.put("roleIds", Joiner.on(',').join($it.getRoleIds().get()));
            datas.add(data);
        }
        return wrapperResponse(datas);
    }

    @RequestMapping(value = "/employee/{companyId}/{type}/{userInfo}/employee.json")
    @ResponseBody
    public Map<String, Object> loadByUserId(@PathVariable(value = "companyId") Integer companyId,
                                            @PathVariable(value = "type") String type,
                                            @PathVariable(value = "userInfo") String userInfo,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadById(url=%s)", request.getRequestURL()));

        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);
        Optional<EmployeeEntity> emps = null;
        if (StringUtils.equals("byUserId", type)) {
            Integer userId = Integer.valueOf(userInfo);
            emps = getBean(EmployeeEntityAction.class, request).findByUserId(userId, company.get());
            Preconditions.checkState(emps.isPresent(), "ID = %s 职员不存在...", userId);
        } else if (StringUtils.equals("byLoginName", type)) {
            emps = getBean(EmployeeEntityAction.class, request).findByLoginName(userInfo, company.get());
            Preconditions.checkState(emps.isPresent(), "LoginName = %s 职员不存在...", userInfo);
        } else {
            throw new IllegalArgumentException(String.format("非法的请求参数[%s]...", userInfo));
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put("id", emps.get().getId());
        data.put("companyId", companyId);
        data.put("orgId", emps.get().getOrganizationId().or(-1));
        data.put("storeId", emps.get().getStoreId().or(-1));
        data.put("name", emps.get().getUserName());
        data.put("pwd", emps.get().getPassowrd());
        data.put("roleIds", null);
        if (emps.get().getRoleIds().isPresent())
            data.put("roleIds", Joiner.on(',').join(emps.get().getRoleIds().get()));
        return wrapperResponse(data);
    }

    @RequestMapping(value = "/employee/{companyId}/{loginName}/loginuser.json")
    @ResponseBody
    public Map<String, Object> loadLoginUserName(@PathVariable(value = "companyId") Integer companyId,
                                                 @PathVariable(value = "loginName") String loginName,
                                                 HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadById(url=%s)", request.getRequestURL()));
        if (-1 == companyId) {
            Map<String, Object> params = Maps.newHashMap();
            params.put("id", 999);
            params.put("name", "运维人员");
            params.put("pwd", "******");
            params.put("cId", -1);
            params.put("oId", -1);
            params.put("sId", -1);
            params.put("roles", "ROLE_ManagerRole");
            params.put("strIds", null);
            return wrapperResponse(params);
        }
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);
        Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request).findByLoginName(loginName, company.get());
        Preconditions.checkState(employee.isPresent(), "%s@%s对应的账户存在...", companyId, loginName);
        LoginUserContext user = getBean(BaseModelServer.class, request).loadContextByUserId(employee.get().getId(),
                company.get());
        Preconditions.checkState(user != null, "%s@%s对应的账户存在...", companyId, loginName);
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", user.getUserId());
        params.put("name", user.getUsername() == null ? "登陆者" : user.getUsername());
        params.put("pwd", user.getEmployee().getPassowrd());
        params.put("cId", companyId);
        params.put("oId", user.getStore().isPresent() ? -1 : user.getOrganization().get().getId());
        params.put("sId", user.getStore().isPresent() ? user.getStore().get().getId() : -1);
        Set<Integer> storeIds = Sets.newHashSet();
        if (user.getStore().isPresent()) {
            storeIds.add(user.getStore().get().getId());
        } else if (user.getSubStoreIds().isPresent()) {
            storeIds.addAll(user.getSubStoreIds().get());
        }
        params.put("strIds", CollectionUtils.isEmpty(storeIds) ? null : Joiner.on(',').join(storeIds));
        if (CollectionUtils.isNotEmpty(user.getRoleSet().getRoleSet())) {
            List<String> role_names = user.getRoleSet().getRoleSet().stream()
                    .map(x -> String.format("ROLE_%s", x.getName())).collect(Collectors.toList());
            params.put("roles", Joiner.on(',').join(role_names));
        } else {
            params.put("roles", "ROLE_LoginerUser");
        }
        return wrapperResponse(params);
    }

}
