package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller(value = "rganizationController")
@RequestMapping("/orgmvc")
public class OrganizationController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(OrganizationController.class);

    @RequestMapping(value = "/company/all.json")
    @ResponseBody
    public Map<String, Object> loadAllCompany(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadAllCompany(url=%s)", request.getRequestURL()));
        loadLoginUser(request);
        Optional<List<OrganizationEntity>> list = getBean(OrganizationEntityAction.class, request).loadAllCompanies();
        if (list.isPresent()) {
            List<Map<String, Object>> datas = Lists.newArrayList();
            for (OrganizationEntity $it : list.get()) {
                Map<String, Object> data = Maps.newHashMap();
                data.put("companyId", $it.getId());
                data.put("companyName", $it.getName());
                datas.add(data);
            }
            return wrapperResponse(datas);
        }
        return wrapperResponse(new int[0]);
    }

    @RequestMapping(value = "/company/stores.json")
    @ResponseBody
    public Map<String, Object> loadStoresWithCompany(@RequestBody Map<String, String> requestBody,
                                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadStoresWithCompany( requestBody = %s)", request.getRequestURI(),
                    requestBody));
        int companyId = MapUtils.getIntValue(requestBody, "companyId", -1);
        Preconditions.checkArgument(-1 != companyId, "入参 companyId 不可以为空值...");
        Map<String, Object> res_data = Maps.newHashMap();
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request)
                .findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "id=%s 对应的公司不存在...", companyId);
        res_data.put("id", companyId);
        res_data.put("name", company.get().getName());
        Optional<List<StoreEntity>> list = getBean(StoreEntityAction.class, request)
                .loadAllStoreByCompany(company.get());
        List<Map<String, Object>> stores = Lists.newArrayList();
        res_data.put("stores", stores);
        if (list.isPresent()) {
            for (StoreEntity $it : list.get()) {
                if ($it.isEnabled()) {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("id", $it.getId());
                    map.put("name", $it.getName());
                    stores.add(map);
                }
            }
        }
        return wrapperResponse(res_data);
    }

    @RequestMapping(value = "/suball/stores.json")
    @ResponseBody
    public Map<String, Object> loadAllSubStores(@RequestBody Map<String, String> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]loadAllSubStores( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkState(user.getMaxPowerRole().isPresent(), "当前门店无角色信息...");
        Preconditions.checkState(user.getCompany().isPresent());
        if (user.getMaxPowerRole().get().isShoppingGuide() || user.getMaxPowerRole().get().isStoreManager()) {
            Preconditions.checkState(user.getStore().isPresent(), "当前职员无门店信息...");
            StoreEntity store = user.getStore().get();
            List<Map<String, Object>> params = Lists.newArrayList();
            params.add(store.toMap());
            return wrapperResponse(params);
        } else {
            if (!user.getOrganization().isPresent())
                return wrapperEmptyResponse();
            OrganizationEntity organization = user.getOrganization().get();
            Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class, request)
                    .loadAllSubStoreByOrg(organization);
            if (!stores.isPresent())
                return wrapperEmptyResponse();
            List<Map<String, Object>> params = Lists.newArrayList();
            for (StoreEntity st : stores.get()) {
                params.add(st.toMap());
            }
            return wrapperResponse(params);
        }
    }


    private class ParamsHolder {
        private final Map<String, Object> params;

        public ParamsHolder(Map<String, Object> params) {
            this.params = params;
        }

        public Integer checkAndGetOrgId() {
            Preconditions.checkArgument(params.containsKey("orgId"), "请求参数缺少组织编号[orgId]");
            return MapUtils.getInteger(params, "orgId");
        }

        public Integer checkAndGetParentId() {
            Preconditions.checkArgument(params.containsKey("parentId"), "请求参数缺少父级组织[parentId]");
            return MapUtils.getInteger(params, "parentId");
        }

        public String checkAndGetName() {
            Preconditions.checkArgument(params.containsKey("name"), "请求参数缺少组织名称[name]");
            return MapUtils.getString(params, "name");
        }

        public Integer checkAndGetType() {
            Preconditions.checkArgument(params.containsKey("type"), "请求参数缺少组织类型[type]");
            return MapUtils.getInteger(params, "type");
        }

        public String getShortName() {
            return MapUtils.getString(params, "shortName");
        }

        public Integer checkAndGetHiddenMemberPhoneFlag() {
            Preconditions.checkArgument(params.containsKey("hiddenMemberPhoneFlag"), "请求参数缺少隐藏会员号码[hiddenMemberPhoneFlag]");
            return MapUtils.getInteger(params, "hiddenMemberPhoneFlag");
        }

        public Integer getIndustryType() {
            return MapUtils.getInteger(params, "industryType");
        }

        public Integer getOrgShowFlag() {
            return MapUtils.getInteger(params, "orgShowFlag");
        }
    }

    /**
     * 加载组织信息
     *
     * @param requestBody [入参为 orgId]
     * @param request
     * @return
     */
    @RequestMapping(value = "web/org/load.json")
    @ResponseBody
    public Map<String, Object> loadOrganization(@RequestBody Map<String, Object> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]loadOrganization( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer orgId = holder.checkAndGetOrgId();
        Optional<OrganizationEntity> orgOpt = getBean(OrganizationEntityAction.class, request).findById(orgId);
        Preconditions.checkState(orgOpt.isPresent(), String.format("组织[%s]不存在", orgId));
        OrganizationEntity org = orgOpt.get();
        return wrapperResponse(org.toViewMap());
    }


    /**
     * 新增组织
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "web/org/save.json")
    @ResponseBody
    public Map<String, Object> saveOrganization(@RequestBody Map<String, Object> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]saveOrganization( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer parentId = holder.checkAndGetParentId();
        String name = holder.checkAndGetName();
        String shortName = holder.getShortName();
        getBean(OrganizationServer.class, request).saveOrganization(user, parentId, name, shortName);
        return wrapperEmptyResponse();
    }

    /**
     * 编辑组织
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "web/org/edit.json")
    @ResponseBody
    public Map<String, Object> editOrganization(@RequestBody Map<String, Object> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]editOrganization( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer orgId = holder.checkAndGetOrgId();
        String name = holder.checkAndGetName();
        String shortName = holder.getShortName();
        Integer hiddenMemberPhoneFlag = holder.checkAndGetHiddenMemberPhoneFlag();
        Integer industryType = holder.getIndustryType();
        Integer orgShowFlag = holder.getOrgShowFlag();
        getBean(OrganizationServer.class, request).editOrganization(user, orgId, name, shortName,
                industryType, orgShowFlag, hiddenMemberPhoneFlag);
        return wrapperEmptyResponse();
    }

    /**
     * 删除组织
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "web/org/remove.json")
    @ResponseBody
    public Map<String, Object> removeOrganization(@RequestBody Map<String, Object> requestBody,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]removeOrganization( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer orgId = holder.checkAndGetOrgId();
        getBean(OrganizationServer.class, request).removeOrganization(user, orgId);
        return wrapperEmptyResponse();
    }

    /**
     * 迁移组织
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "web/org/switch.json")
    @ResponseBody
    public Map<String, Object> switchOrganization(@RequestBody Map<String, Object> requestBody,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(
                    String.format("[%s]switchOrganization( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer parentId = holder.checkAndGetParentId();
        Integer orgId = holder.checkAndGetOrgId();
        getBean(OrganizationServer.class, request).switchOrganization(user, parentId, orgId);
        return wrapperEmptyResponse();
    }

    /**
     * 获取用户可访问的组织架构树含门店
     *
     * @param request HttpServletRequest
     * @return Map<StringObject>
     */
    @RequestMapping(value = "/all/tree.json")
    @ResponseBody
    public Map<String, Object> loadUOrgTreeWithStore(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Optional<OrgTreeViewDto> treeDto = getBean(BaseModelServer.class, request).loadOrgTree(companyId);
        if (!treeDto.isPresent()) return wrapperResponse(new String[0]);
        return wrapperResponse(new Object[]{treeDto.get().toMap()});
    }

    /**
     * 获取整个组织架构书 用于内部管理使用
     *
     * @param request HttpServletRequest
     * @return Map<StringObject>
     */
    @RequestMapping(value = "/org-store/tree.json")
    @ResponseBody
    public Map<String, Object> loadOrgTreeWithStore(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空值...");
        Optional<OrgTreeViewDto> treeDto = getBean(OrganizationServer.class, request).loadOgrTreeWithStoreNoPower(companyId);
        if (!treeDto.isPresent()) return wrapperResponse(new String[0]);
        return wrapperResponse(new Object[]{treeDto.get().toMap()});
    }

}
