package com.legooframework.model.organization.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LegooRole;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.organization.entity.EmployeeEntity;
import com.legooframework.model.organization.service.EmployeeService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController(value = "empController")
@RequestMapping(value = "/emp")
public class EmployeeController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    /**
     * 根据门店ID查询该门店下的职员
     *
     * @param datas
     * @param request
     * @return
     */
    @PostMapping(value = "/query/employees.json")
    public JsonMessage queryEmployees(@RequestBody Map<String, Object> datas, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "storeId");
        Long storeId = MapUtils.getLong(datas, "storeId", null);
        List<EmployeeEntity> emps = empService.loadEmployeesByStoreId(storeId);
        return JsonMessageBuilder.OK().withPayload(emps).toMessage();
    }

    /**
     * 新增职员基本信息
     *
     * @param type
     * @param datas
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/add/employee.json")
    public JsonMessage addEmployee(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        LoginContext loginUser = getLoginContext();
        loginUser.getRoles();
        String type = MapUtils.getString(datas, "type", null);
        Preconditions.checkArgument(type.equals("store") || type.equals("company"), "入参type必须是store或company");
        String workNo = MapUtils.getString(datas, "workNo", null);
        String userName = MapUtils.getString(datas, "userName", null);
        String userSex = MapUtils.getString(datas, "userSex", null);
        KvDictDto userSexDto = new KvDictDto(userSex, "职员性别", 0, "SEX");
        String birthday = MapUtils.getString(datas, "userBirthday", null);
        Date userBirthday = null;
        if (!Strings.isNullOrEmpty(birthday))
            userBirthday = Date.from(LocalDate.parse(birthday).atStartOfDay(ZoneId.systemDefault()).toInstant());
        String userRemark = MapUtils.getString(datas, "userRemark", null);
        String phoneNo = MapUtils.getString(datas, "phoneNo", null);
        String employeeTimeStr = MapUtils.getString(datas, "employeeTime", null);
        Date employeeTime = null;
        if (!Strings.isNullOrEmpty(employeeTimeStr))
            employeeTime = Date.from(LocalDate.parse(employeeTimeStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Long orgId = MapUtils.getLong(datas, "orgId", null);
        Objects.requireNonNull(orgId);
        String roleNosStr = MapUtils.getString(datas, "roleNos", null);
        Preconditions.checkArgument(StringUtils.isNotBlank(roleNosStr), "职员角色不能为空");
        String[] roleNos = roleNosStr.split(",");
        Long empId = null;
        if (type.equals("store")) {
            Long storeId = MapUtils.getLong(datas, "storeId", null);
            Objects.requireNonNull(storeId);
            empId = empService.addStoreEmployee(workNo, userName, userSexDto, userBirthday, userRemark, phoneNo,
                    employeeTime, orgId, storeId, loginUser, roleNos);
        } else if (type.equals("company")) {
            empId = empService.addCompanyEmployee(workNo, userName, userSexDto, userBirthday, userRemark, phoneNo,
                    employeeTime, orgId, loginUser, roleNos);
        }
        Preconditions.checkState(empId != null, "新增职员信息出错");
        return JsonMessageBuilder.OK().withPayload(empId).toMessage();
    }

    /**
     * 入参不存在校验
     */
    private void requestArgsNxCheck(Map<String, Object> datas, String... args) {
        Preconditions.checkArgument(!datas.isEmpty(), "请求参数不能为空");
        Arrays.stream(args)
                .forEach(x -> Preconditions.checkArgument(datas.containsKey(x), String.format("请求参数需包含%s", x)));
    }

    /**
     * 删除职员
     *
     * @param datas
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/remove/employee.json")
    public JsonMessage removeEmployee(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId");
        Long empId = MapUtils.getLong(datas, "empId", null);
        empService.removeEmployee(empId);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 编辑职员基本信息
     *
     * @param datas
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/edit/employee.json")
    public JsonMessage editEmployee(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId", "userName", "userBirthday", "phoneNo", "userRemark", "employeeTime");
        Long empId = MapUtils.getLong(datas, "empId", null);
        Objects.requireNonNull(empId);
        String userName = MapUtils.getString(datas, "userName", null);
        String birthday = MapUtils.getString(datas, "userBirthday", null);
        Date userBirthday = Date.from(LocalDate.parse(birthday).atStartOfDay(ZoneId.systemDefault()).toInstant());
        String phoneNo = MapUtils.getString(datas, "phoneNo", null);
        String userRemark = MapUtils.getString(datas, "userRemark", null);
        String employeeTimeStr = MapUtils.getString(datas, "employeeTime", null);
        String roleNosStr = MapUtils.getString(datas, "roleNos", null);
        String[] roleNos = null;
        if (roleNosStr != null)
            roleNos = roleNosStr.split(",");
        Date employeeTime = null;
        if (employeeTimeStr != null)
            employeeTime = Date.from(LocalDate.parse(employeeTimeStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
        empService.editEmployee(empId, userName, userBirthday, userRemark, phoneNo, employeeTime, roleNos);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 设置职员为工作状态
     *
     * @param datas
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/inservice/employee.json")
    public JsonMessage inServiceAction(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId");
        Long empId = MapUtils.getLong(datas, "empId", null);
        empService.inServiceEmployee(empId);
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/invacation/employee.json")
    public JsonMessage inVacationAction(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId");
        Long empId = MapUtils.getLong(datas, "empId", null);
        empService.inVacationEmployee(empId);
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/quit/employee.json")
    public JsonMessage quitAction(@RequestBody Map<String, Object> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId");
        Long empId = MapUtils.getLong(datas, "empId", null);
        empService.quitEmployee(empId);
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/assign/employee.json")
    public JsonMessage assginEmployee(@RequestBody Map<String, Object> datas, HttpServletRequest request)
            throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        requestArgsNxCheck(datas, "empId", "orgId", "storeId");
        Long empId = MapUtils.getLong(datas, "empId", null);
        Long orgId = MapUtils.getLong(datas, "orgId", null);
        Long storeId = MapUtils.getLong(datas, "storeId", null);
        empService.assignToOrg(orgId, storeId, empId);
        return JsonMessageBuilder.OK().toMessage();
    }


    @PostMapping(value = "/loadAll.json")
    public JsonMessage loadAllEmployee(@RequestBody Map<String, Object> datas, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        int pageNum = MapUtils.getIntValue(datas, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(datas, "pageSize", 0);
        datas.remove("pageNum");
        datas.remove("pageSize");
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        if (MapUtils.isNotEmpty(datas)) {
            params.put("has_dynamic_params", true);
            params.putAll(datas);
        }
        PagingResult paging = getQuerySupport(request).queryForPage("employee", "loadByStores", pageNum, pageSize,
                params);
        List<LegooRole> roles = empService.getAllRoles();
        Optional<List<Map<String, Object>>> pageResOpt = paging.getResultSet();
        if (pageResOpt.isPresent()) {
            pageResOpt.get().stream().forEach(x -> {
                String employeeRole = MapUtils.getString(x, "roleNos");
                if (!Strings.isNullOrEmpty(employeeRole)) {
                    String[] roleNos = employeeRole.split(",");
                    if (empService.checkEditable(roles, roleNos)) {
                        x.put("editable", 1);
                    } else {
                        x.put("editable", 0);
                    }

                }

            });
        }
        return JsonMessageBuilder.OK().withPayload(paging.toData()).toMessage();
    }

    @Resource
    private EmployeeService empService;

    private JdbcQuerySupport getQuerySupport(HttpServletRequest request) {
        return getBean("orgJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
