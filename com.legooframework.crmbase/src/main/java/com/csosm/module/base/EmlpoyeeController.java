package com.csosm.module.base;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.csosm.module.base.entity.EmployeeEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller(value = "emlpoyeeController")
@RequestMapping("/employee")
public class EmlpoyeeController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(EmlpoyeeController.class);

    @RequestMapping(value = "/store/shoppingguides.json")
    @ResponseBody
    public Map<String, Object> loadShoppingGuide(@RequestBody(required = false) Map<String, String> http_request_map,
                                                 HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadShoppingGuide(http_request_map=%s)", http_request_map));

        LoginUserContext loginUser = loadLoginUser(request);
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        if (null == storeId) {
            Optional<StoreEntity> store = loginUser.getStore();
            Preconditions.checkState(store.isPresent(), "当前用户无门店信息，无法获取导购.");
            storeId = store.get().getId();
        }
        Optional<List<EmployeeEntity>> ems = getBean(EmployeeServer.class, request).loadEnabledShoppingGuides(storeId,
                loginUser);
        if (ems.isPresent()) {
            List<Map<String, Object>> views = Lists.newArrayListWithCapacity(ems.get().size());
            for (EmployeeEntity $it : ems.get()) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("id", $it.getId());
                map.put("name", $it.isEnabled() ? $it.getUserName() : String.format("%s[无效]", $it.getUserName()));
                views.add(map);
            }
            return wrapperResponse(views);
        }
        return wrapperEmptyResponse();
    }

    private class ParamsHolder {

        private final Map<String, Object> params;

        public ParamsHolder(Map<String, Object> params) {
            this.params = params;
        }

        private Integer checkAndGetEmpId() {
            Preconditions.checkArgument(params.containsKey("empId"), "请求参数缺少职员编号[empId]");
            return MapUtils.getInteger(params, "empId");
        }

        private String checkAndGetUserName() {
            Preconditions.checkArgument(params.containsKey("userName"), "请求参数缺少职员编号[userName]");
            return MapUtils.getString(params, "userName");
        }

        private Integer checkAndGetSex() {
            Preconditions.checkArgument(params.containsKey("sex"), "请求参数缺少职员性别[sex]");
            return MapUtils.getInteger(params, "sex");
        }

        private String getPhoneNo() {
            return MapUtils.getString(params, "phoneNo");
        }

        private List<Integer> checkAndGetRoleIds() {
            Preconditions.checkArgument(params.containsKey("roleIds"), "请求参数缺少职员角色[roleIds]");
            String roleIds = MapUtils.getString(params, "roleIds");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(roleIds.trim()), "请指定至少一个角色");
            return Splitter.on(",").splitToList(roleIds).stream().map(x -> Integer.parseInt(x))
                    .collect(Collectors.toList());
        }

        private List<Integer> checkAndGetEmpIds() {
            Preconditions.checkArgument(params.containsKey("empIds"), "请求参数缺少职员编号[empIds]");
            String empIds = MapUtils.getString(params, "empIds");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(empIds.trim()), "请求参数[empIds]无数据");
            return Splitter.on(",").splitToList(empIds).stream().map(x -> Integer.parseInt(x))
                    .collect(Collectors.toList());
        }

        private String checkAndGetLoginName() {
            Preconditions.checkArgument(params.containsKey("loginName"), "请求参数缺少职员登录名称[loginName]");
            return MapUtils.getString(params, "loginName");
        }

        private Integer checkAndGetStoreId() {
            Preconditions.checkArgument(params.containsKey("storeId"), "请求参数缺少门店编号[storeId]");
            return MapUtils.getInteger(params, "storeId");
        }

        private String getSearch() {
            return MapUtils.getString(params, "search");
        }

        private Optional<Integer> getStoreId() {
            if (!params.containsKey("storeId"))
                return Optional.absent();
            Integer storeId = MapUtils.getInteger(params, "storeId");
            if (null == storeId)
                return Optional.absent();
            return Optional.of(storeId);
        }

        private Optional<Integer> getOrgId() {
            if (!params.containsKey("orgId"))
                return Optional.absent();
            Integer orgId = MapUtils.getInteger(params, "orgId");
            if (null == orgId)
                return Optional.absent();
            return Optional.of(orgId);
        }

        private String checkAndGetNewPwd() {
            Preconditions.checkArgument(params.containsKey("newPassword"), "请求参数缺少新密码[newPassword]");
            return MapUtils.getString(params, "newPassword");
        }

        private String checkAndGetOldPwd() {
            Preconditions.checkArgument(params.containsKey("oldPassword"), "请求参数缺少新密码[oldPassword]");
            return MapUtils.getString(params, "oldPassword");
        }

        private String checkAndGetConfirmPwd() {
            Preconditions.checkArgument(params.containsKey("confirmPassword"), "请求参数缺少确认密码[oldPassword]");
            return MapUtils.getString(params, "confirmPassword");
        }

    }

    @RequestMapping(value = "load_emps/list.json")
    @ResponseBody
    public Map<String, Object> listEmployee(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveEmployee(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        Map<String, Object> params = Maps.newHashMap();
        Preconditions.checkState(user.getCompany().isPresent(), "当前登录用户无公司信息");
        params.put("companyId", user.getCompany().get().getId());
        if (requestBody.containsKey("orgId")) params.put("orgId", MapUtils.getInteger(requestBody, "orgId"));
        if (requestBody.containsKey("storeId")) params.put("storeId", MapUtils.getInteger(requestBody, "storeId"));
        if (requestBody.containsKey("search")) params.put("search", MapUtils.getString(requestBody, "search"));
        Optional<List<Map<String, Object>>> empsOpt = getBean(QueryEngineService.class, request).queryForList("employee", "load_emps", params);
        if (!empsOpt.isPresent()) return wrapperResponse(new String[0]);
        List<Map<String, Object>> result = empsOpt.get().stream().map(x -> {
            if (MapUtils.getIntValue(x, "enable") == 1) x.put("enable", true);
            if (MapUtils.getIntValue(x, "enable") == 2) x.put("enable", false);
            return x;
        }).collect(Collectors.toList());
        return wrapperResponse(result);
    }

    /**
     * 新增职员
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/{channel}/save.json")
    @ResponseBody
    public Map<String, Object> saveEmployee(@PathVariable(value = "channel") String channel,
                                            @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("saveEmployee(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        String loginName = holder.checkAndGetLoginName();
        String userName = holder.checkAndGetUserName();
        String phoneNo = holder.getPhoneNo();
        Integer sex = holder.checkAndGetSex();
        List<Integer> roleIds = holder.checkAndGetRoleIds();

        TransactionStatus status = startTx("saveEmployee");
        try {
            if (holder.getOrgId().isPresent()) {
                getBean(EmployeeServer.class, request).saveOrgEmployee(user, holder.getOrgId().get(), loginName,
                        userName, phoneNo, sex, roleIds);
            } else if (holder.getStoreId().isPresent()) {
                getBean(EmployeeServer.class, request).saveStoreEmployee(user, holder.getStoreId().get(), loginName,
                        userName, phoneNo, sex, roleIds);
            }
            commitTx(status);
        } catch (Exception e) {
            rollbackTx(status);
            throw e;
        }
        return wrapperEmptyResponse();
    }

    /**
     * 编辑职员信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/{channel}/edit.json")
    @ResponseBody
    public Map<String, Object> editEmployee(@PathVariable(value = "channel") String channel,
                                            @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("editEmployee(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        Integer empId = holder.checkAndGetEmpId();
        String userName = holder.checkAndGetUserName();
        String phoneNo = holder.getPhoneNo();
        Integer sex = holder.checkAndGetSex();
        List<Integer> roleIds = holder.checkAndGetRoleIds();

        TransactionStatus status = startTx("editEmployee");
        try {
            if (holder.getOrgId().isPresent()) {
                getBean(EmployeeServer.class, request).editOrgEmployee(user, holder.getOrgId().get(), empId, userName,
                        phoneNo, sex, roleIds);
            } else if (holder.getStoreId().isPresent()) {
                getBean(EmployeeServer.class, request).editStoreEmployee(user, holder.getStoreId().get(), empId,
                        userName, phoneNo, sex, roleIds);
            }
            commitTx(status);
        } catch (Exception e) {
            rollbackTx(status);
            throw e;
        }

        return wrapperEmptyResponse();
    }

    /**
     * 批量启用职员
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/{channel}/{employeeState}/state.json")
    @ResponseBody
    public Map<String, Object> changeStatsEmployees(@PathVariable(value = "channel") String channel,
                                                    @PathVariable(value = "employeeState") String employeeState,
                                                    @RequestBody Map<String, Object> requestBody,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("enableEmployees(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        List<Integer> empIds = holder.checkAndGetEmpIds();
        if (employeeState.equals("true")) {
            getBean(EmployeeEntityAction.class, request).enableEmployees(empIds, user.getCompany().get());
        } else {
            getBean(EmployeeEntityAction.class, request).disableEmployees(empIds, user.getCompany().get());
        }
        return wrapperEmptyResponse();
    }

    /**
     * 批量重置职员密码
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/{channel}/resetPwd.json")
    @ResponseBody
    public Map<String, Object> resetEmployeesPwd(@PathVariable(value = "channel") String channel,
                                                 @RequestBody Map<String, Object> requestBody,
                                                 HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("resetEmployeesPwd(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        List<Integer> empIds = holder.checkAndGetEmpIds();
        getBean(EmployeeEntityAction.class, request).resetPassword(empIds, user.getCompany().get());
        return wrapperEmptyResponse();
    }

    /**
     * 重新设置密码
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/{channel}/password/change.json")
    @ResponseBody
    public Map<String, Object> changePassword(@PathVariable(value = "channel") String channel,
                                              @RequestBody Map<String, Object> requestBody,
                                              HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("changePassword(%s,url=%s)", "null", request.getRequestURL()));
        LoginUserContext user = loadLoginUser(request);
        ParamsHolder holder = new ParamsHolder(requestBody);
        String oldPwd = holder.checkAndGetOldPwd();
        String newPwd = holder.checkAndGetNewPwd();
        String confirmPwd = holder.checkAndGetConfirmPwd();
        getBean(EmployeeEntityAction.class, request).changePassword(holder.checkAndGetEmpId(), oldPwd, newPwd, confirmPwd,
                user.getCompany().get());
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/{channel}/manager/list.json")
    @ResponseBody
    public Map<String, Object> listManagers(@PathVariable(value = "channel") String channel,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]", request.getRequestURI()));
        LoginUserContext loginUser = loadLoginUser(request);
        Integer orgId = null;
        if (loginUser.getCompany().isPresent())
            orgId = loginUser.getCompany().get().getId();
        if (loginUser.getOrganization().isPresent())
            orgId = loginUser.getOrganization().get().getId();
        Preconditions.checkState(null != orgId, "登录用户无组织信息");
        Map<String, Object> params = Maps.newHashMap();
        params.put("orgId", orgId);
        Optional<List<Map<String, Object>>> managersOpt = getBean(QueryEngineService.class, request)
                .queryForList("employee", "load_managers", params);
        if (!managersOpt.isPresent())
            return wrapperResponse(new String[0]);
        return wrapperResponse(managersOpt.get());
    }

}
