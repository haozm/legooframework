package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.commons.mvc.BetweenDayDto;
import com.csosm.module.base.entity.*;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.storeview.StoreViewService;
import com.csosm.module.storeview.entity.StoreTreeViewDto;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Controller(value = "baseController")
@RequestMapping("/base")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/login/user.json")
    @ResponseBody
    public Map<String, Object> loadLoginUserInfo(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Map<String, Object> map = loginUser.toMap();
        map.remove("SUB_STORES");
        map.remove("ALL_STORES");
        return wrapperResponse(map);
    }

    /**
     * 获取用户可访问的组织架构树含门店
     *
     * @param request HttpServletRequest
     * @return Map<StringObject>
     */
    @RequestMapping(value = "/org-store/tree.json")
    @ResponseBody
    public Map<String, Object> loadUserOwnerOrgTreeWithStore(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前的登陆用户无公司组织信息...");
        String hasStore = request.getParameter("hasStore");
        // 默认为 true
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (getBean(StoreViewEntityAction.class, request).hasStoreView(loginUser.getEmployee())) {
            StoreTreeViewDto treeNode = getBean(StoreViewService.class, request)
                    .loadDataPermissionTreeByUser(loginUser.getEmployee(), loginUser);
            return wrapperResponse(new Object[]{treeNode.toMap()});
        }
        if (Strings.isNullOrEmpty(hasStore) || StringUtils.equals("true", hasStore)) {
            Optional<OrgTreeViewDto> treeDto = baseAdapterServer.loadOrgTreeWithStoreByOrgId(
                    loginUser.getOrganization().isPresent() ? loginUser.getOrganization().get().getId()
                            : loginUser.getCompany().get().getId(), loginUser);
            if (!treeDto.isPresent())
                return wrapperResponse(new String[0]);
            if (logger.isDebugEnabled())
                logger.debug(String.format("%s elapsed %s ms", request.getRequestURI(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return wrapperResponse(new Object[]{treeDto.get().toMap()});
        } else {
            Optional<OrgTreeViewDto> treeDto = baseAdapterServer
                    .loadOrgTreeWithoutStoreByOrgId(loginUser.getOrganization().isPresent() ? loginUser.getOrganization().get().getId()
                            : loginUser.getCompany().get().getId(), loginUser);
            if (!treeDto.isPresent())
                return wrapperResponse(new String[0]);
            if (logger.isDebugEnabled())
                logger.debug(String.format("%s elapsed %s ms", request.getRequestURI(),
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return wrapperResponse(new Object[]{treeDto.get().toMap()});
        }
    }

    @RequestMapping(value = "/org-report/tree.json")
    @ResponseBody
    public Map<String, Object> loadOrgTree4ReportAction(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();
        String btn_date_val = request.getParameter("btn_date");
        BetweenDayDto betweenDateDto = BetweenDayDto.withStartAndEnd(DateTime.now().plusDays(-2),
                DateTime.now().plusDays(-1));
        if (!Strings.isNullOrEmpty(btn_date_val))
            betweenDateDto = BetweenDayDto.withStartEnd(btn_date_val);
        OrgTreeViewDto treeView_opt = baseAdapterServer.loadOrgTree4Report(betweenDateDto, loginUser);
        stopwatch.stop();
        // 增加时间间隔监控输出日志
        if (logger.isDebugEnabled())
            logger.debug(String.format("%s elapsed %s ms", request.getRequestURI(),
                    stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        return wrapperResponse(treeView_opt == null ? new String[0] : new Object[]{treeView_opt});
    }

    // 加载当前配置运行的cache list
    @RequestMapping(value = "/{orgId}/exits.json")
    @ResponseBody
    public Map<String, Object> exitsOrgAction(@PathVariable int orgId, HttpServletRequest request) {
        OrganizationEntityAction action = getBean(OrganizationEntityAction.class, request);
        Optional<OrganizationEntity> opt = action.findCompanyById(orgId);
        return wrapperResponse(opt.isPresent());
    }

    // 动态输出 会员卡列表
    @RequestMapping(value = "/member/card/types.json")
    @ResponseBody
    public Map<String, Object> loadCardTypesAction(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Optional<List<Map<String, Object>>> card_types = queryEngineService.queryForList("member", "loadAllCardTypes",
                loginUser.toMap());
        if (card_types.isPresent())
            return wrapperResponse(card_types.get());
        return wrapperEmptyResponse();
    }

    // 获取当前登录用户所在门店的店长以及导购名单
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

    @RequestMapping(value = "/global/crmbase.json")
    @ResponseBody
    public Map<String, Object> globalConfig(HttpServletRequest request) {
        Properties prop = new Properties();
        try {
            prop.load(MvcController.class.getClassLoader().getResourceAsStream("jdbc.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
        String uploadUrl = prop.getProperty("upload.url");
        Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("uploadUrl", uploadUrl);
        return wrapperResponse(retMap);
    }

    private Map<String, Object> getDataWord(Map<String, Object> requestBody) {
        requestBody.remove("comName");
        requestBody.remove("comShortName");
        requestBody.remove("loginName");
        if (requestBody.containsKey("companyId")) {
            requestBody.remove("companyId");
        }
        if (requestBody.containsKey("linkMan"))
            requestBody.remove("linkMan");
        if (requestBody.containsKey("linkPhone"))
            requestBody.remove("linkPhone");
        if (requestBody.containsKey("industryType"))
            requestBody.remove("industryType");
        if (requestBody.containsKey("nintyServiceDate"))
            requestBody.remove("nintyServiceDate");
        return requestBody;
    }

    @RequestMapping(value = "/web/company/register.json")
    public Map<String, Object> registerCompany(@RequestBody Map<String, Object> requestBody,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]registerCompany( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("comName"), "请求参数缺少公司名称[comName]");
        Preconditions.checkArgument(requestBody.containsKey("comShortName"), "请求参数缺少公司简称[comShortName]");
        Preconditions.checkArgument(requestBody.containsKey("loginName"), "请求参数缺少公司登录账号[loginName]");
        String comName = MapUtils.getString(requestBody, "comName");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "公司ID不可以为空值...");
        String comShortName = MapUtils.getString(requestBody, "comShortName");
        String loginName = MapUtils.getString(requestBody, "loginName");
        String linkMan = MapUtils.getString(requestBody, "linkMan");
        String linkPhone = MapUtils.getString(requestBody, "linkPhone");
        Integer industryType = MapUtils.getInteger(requestBody, "industryType");
        TransactionStatus startTx = startTx("registerCompany");
        try {
            getBean(BaseModelServer.class, request).registerCompany(user, companyId, comName, comShortName,
                    loginName, linkMan, linkPhone, industryType, getDataWord(requestBody));
        } catch (Exception e) {
            rollbackTx(startTx);
            throw e;
        }
        commitTx(startTx);
        return wrapperEmptyResponse();
    }

    @Resource
    private BaseModelServer baseAdapterServer;
    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;
}
