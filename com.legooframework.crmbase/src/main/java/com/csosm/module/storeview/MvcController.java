package com.csosm.module.storeview;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.TreeNodeDto;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.storeview.entity.StoreTreeViewDto;
import com.csosm.module.storeview.entity.StoreViewEntity;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller(value = "storeViewController")
@RequestMapping("/storeview")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);


    @RequestMapping(value = "/load/{range}/recharge/tree.json")
    @ResponseBody
    public Map<String, Object> loadSmsRechargeTree(@PathVariable(value = "range") String range,
                                                   @RequestBody(required = false) Map<String, String> requestBody,
                                                   HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        int companyId = -1;
        if (StringUtils.equals(range, "all")) {
            companyId = MapUtils.getIntValue(requestBody, "companyId", -1);
            Preconditions.checkState(companyId != -1, "请指定需要加载的公司Id...");
        } else if (StringUtils.equals(range, "company")) {
            Optional<OrganizationEntity> company = loginUser.getCompany();
            Preconditions.checkState(company.isPresent());
            companyId = company.get().getId();
        } else {
            throw new IllegalArgumentException(String.format("非法的入参 %s ", range));
        }
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "Id=%s对应的公司不存在...", companyId);
        StoreTreeViewDto treeNode = getStoreViewService(request).loadSmsRechargeTree(company.get(), loginUser);
        return wrapperResponse(new StoreTreeViewDto[]{treeNode});
    }

    @RequestMapping(value = "/load/permission/tree.json")
    @ResponseBody
    public Map<String, Object> loadDataPermissionTree(@RequestBody(required = false) Map<String, String> requestBody,
                                                      HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        EmployeeEntity _emp = null;
        if (MapUtils.getIntValue(requestBody, "empId", -1) != -1) {
            Optional<EmployeeEntity> employee = getBean(EmployeeEntityAction.class, request)
                    .findByUserId(MapUtils.getIntValue(requestBody, "empId", -1), loginUser.getCompany().get());
            Preconditions.checkState(employee.isPresent(), "userId = %s 对应得人员非法...");
            _emp = employee.get();
        }
        StoreTreeViewDto treeNode = getStoreViewService(request)
                .loadDataPermissionTreeByUser(_emp == null ? loginUser.getEmployee() : _emp, loginUser);
        return wrapperResponse(new StoreTreeViewDto[]{treeNode});
    }


    /**
     * 获取用户可访问的组织架构树含门店
     *
     * @param request HttpServletRequest
     * @return Map<StringObject>
     */
    @RequestMapping(value = "/all/tree.json")
    @ResponseBody
    public Map<String, Object> loadUserOwnerOrgTreeWithStore(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前的登陆用户无公司组织信息...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<OrgTreeViewDto> treeDto = getStoreViewService(request).loadOrgTreeWithStoreByOrgId(loginUser);
        if (!treeDto.isPresent())
            return wrapperResponse(new String[0]);
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s elapsed %s ms", request.getRequestURI(), stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return wrapperResponse(new Object[]{treeDto.get().toMap()});
    }

    @RequestMapping(value = "/withEmps/tree.json")
    @ResponseBody
    public Map<String, Object> loadOrgTreeWithStoreAndEmps(
            @RequestBody(required = false) Map<String, String> requestBody, HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前的登陆用户无公司组织信息...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (requestBody == null) {
            Optional<OrgTreeViewDto> treeDto = getBean(BaseModelServer.class, request).loadRootTreeWithEmps(loginUser);
            return wrapperResponse(new Object[]{treeDto.get().toWithChildMap()});
        }

        Integer orgId = MapUtils.getInteger(requestBody, "orgId");
        Integer type = MapUtils.getInteger(requestBody, "type");

        if (type.intValue() == 1) {
            Optional<List<OrgTreeViewDto>> treeDtos = getBean(BaseModelServer.class, request)
                    .loadOrgTreeWithEmpsByOrgId(orgId, loginUser);
            List<Map<String, Object>> result = treeDtos.get().stream().map(TreeNodeDto::toWithChildMap).collect(Collectors.toList());
            return wrapperResponse(result);
        }
        if (type.intValue() == 2) {
            Optional<List<OrgTreeViewDto>> treeDtos = getBean(BaseModelServer.class, request)
                    .loadOrgTreeWithEmpByStoreId(orgId, loginUser);
            List<Map<String, Object>> result = treeDtos.get().stream().map(TreeNodeDto::toWithChildMap).collect(Collectors.toList());
            return wrapperResponse(result);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s elapsed %s ms", request.getRequestURI(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return wrapperResponse(new String[0]);
    }


    @RequestMapping(value = "/load/stores.json")
    @ResponseBody
    public Map<String, Object> loadAllSubStores(@RequestBody(required = false) Map<String, String> requestBody,
                                                HttpServletRequest request) {
        loadLoginUser(request);
        String nodeId = MapUtils.getString(requestBody, "nodeId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeId), "入参 nodeId 不可为空值...");
        Optional<List<StoreViewEntity>> list = getBean(StoreViewEntityAction.class, request).loadSubNodes(nodeId);
        Set<Integer> storeIds = Sets.newHashSet();
        if (list.isPresent()) {
            list.get().forEach(node -> {
                if (node.hasStores())
                    storeIds.addAll(node.getStoreIds());
            });
        }
        return wrapperResponse(CollectionUtils.isEmpty(storeIds) ? new String[0] : storeIds);
    }

    @RequestMapping(value = "/add/stores.json")
    @ResponseBody
    public Map<String, Object> addStoresAction(@RequestBody Map<String, String> reqMap, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), reqMap));
        LoginUserContext loginUser = loadLoginUser(request);
        if (Strings.isNullOrEmpty(MapUtils.getString(reqMap, "storeIds")))
            return wrapperEmptyResponse();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeId")), "缺少参数 %s ", "nodeId");
        List<Integer> int_ids = Stream.of(StringUtils.split(MapUtils.getString(reqMap, "storeIds"), ','))
                .mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
        getStoreViewService(request).addStoresToGroupNode(MapUtils.getString(reqMap, "nodeId"), int_ids, loginUser);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/remove/stores.json")
    @ResponseBody
    public Map<String, Object> removeStoresAction(@RequestBody Map<String, String> reqMap, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), reqMap));
        LoginUserContext loginUser = loadLoginUser(request);
        if (Strings.isNullOrEmpty(MapUtils.getString(reqMap, "storeIds")))
            return wrapperEmptyResponse();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeId")), "缺少参数 %s ", "nodeId");
        List<Integer> int_ids = Stream.of(StringUtils.split(MapUtils.getString(reqMap, "storeIds"), ','))
                .mapToInt(Integer::valueOf).boxed().collect(Collectors.toList());
        getStoreViewService(request).removeStoresFromGroupNode(MapUtils.getString(reqMap, "nodeId"), int_ids,
                loginUser);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/add/group.json")
    @ResponseBody
    public Map<String, Object> addSubNodeAction(@RequestBody Map<String, String> reqMap, HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), reqMap));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "pId")), "缺少参数 %s ", "pId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeName")), "缺少参数 %s ",
                "nodeName");
        StoreViewEntity entity = getStoreViewService(request).addSubGroupNode(MapUtils.getString(reqMap, "pId"),
                MapUtils.getString(reqMap, "nodeName"), MapUtils.getString(reqMap, "nodeDesc"), loginUser);
        return wrapperResponse(entity.buildTreeNode());
    }

    /**
     * 修改指定的分组节点信息
     */
    @RequestMapping(value = "/edit/group.json")
    @ResponseBody
    public Map<String, Object> editSubNodeAction(@RequestBody Map<String, String> reqMap, HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), reqMap));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeId")), "缺少参数 %s ", "nodeId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeName")), "缺少参数 %s ",
                "nodeName");
        getStoreViewService(request).editSubGroupNode(MapUtils.getString(reqMap, "nodeId"),
                MapUtils.getString(reqMap, "nodeName"), MapUtils.getString(reqMap, "nodeDesc"), loginUser);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/remove/group.json")
    @ResponseBody
    public Map<String, Object> removeNodeAction(@RequestBody Map<String, String> reqMap, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), reqMap));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(MapUtils.getString(reqMap, "nodeId")), "缺少参数 %s ", "nodeId");
        getStoreViewService(request).removeSubGroupNodeById(MapUtils.getString(reqMap, "nodeId"), loginUser);
        return wrapperEmptyResponse();
    }

    private StoreViewService getStoreViewService(HttpServletRequest request) {
        return getBean(StoreViewService.class, request);
    }

}
