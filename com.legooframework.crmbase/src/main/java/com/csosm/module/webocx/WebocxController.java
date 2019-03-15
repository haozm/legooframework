package com.csosm.module.webocx;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.RoleEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.query.entity.PagingResult;
import com.csosm.module.webocx.entity.GroupAuthorEntity;
import com.csosm.module.webocx.entity.GroupAuthorEntityAction;
import com.csosm.module.webocx.entity.LegooWebOcxRepository;
import com.csosm.module.webocx.entity.PageDefinedDto;
import com.csosm.module.webocx.service.LegooWebOcxService;
import com.csosm.module.webocx.service.MemberGroupInfo;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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

@Controller
@RequestMapping(value = "/webocx")
public class WebocxController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WebocxController.class);

    @RequestMapping(value = "/load/config.json")
    @ResponseBody
    public Map<String, Object> loadModelById(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        String modelId = MapUtils.getString(requestBody, "modelId");
        String type = MapUtils.getString(requestBody, "type");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(modelId), "请求配置模型ID不可以为空值...");
        Optional<Map<String, Object>> model = getBean(LegooWebOcxService.class, request).loadOcxById(modelId, user);
        Preconditions.checkState(model.isPresent(), "Id=%s 对应的配置项不存在...", modelId);
        return wrapperResponse(model.get());
    }

    @RequestMapping(value = "/{model}/{stmtId}/list.json")
    @ResponseBody
    public Map<String, Object> queryForList(@PathVariable String model, @PathVariable String stmtId,
                                            @RequestBody(required = false) Map<String, Object> requestBody,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Map<String, Object> params = user.toMap();
        if (MapUtils.isNotEmpty(requestBody)) params.putAll(requestBody);
        Optional<List<Map<String, Object>>> mapList = querySupport(request).queryForList(model, stmtId, requestBody);
        return mapList.isPresent() ? wrapperResponse(mapList.get()) : wrapperEmptyResponse();
    }

    @RequestMapping(value = "/{model}/{stmtId}/pages.json")
    @ResponseBody
    public Map<String, Object> queryForPages(@PathVariable String model, @PathVariable String stmtId,
                                             @RequestBody(required = false) Map<String, String> requestBody,
                                             HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Map<String, Object> params = user.toMap();
        if (MapUtils.isNotEmpty(requestBody)) params.putAll(requestBody);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        requestBody.remove("pageNum");
        requestBody.remove("pageSize");
        PagingResult paging = querySupport(request).queryForPage(model, stmtId, pageNum, pageSize, params);
        return wrapperResponse(paging.toMap());
    }

    @RequestMapping(value = "/statistical/list.json")
    @ResponseBody
    public Map<String, Object> statisticalByGroup(@RequestBody Map<String, String> requestBody,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] statisticalByGroup(RequestBody->%s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前公司无公司信息，数据异常...");
        OrganizationEntity company = loginUser.getCompany().get();
        String groupName = MapUtils.getString(requestBody, "groupName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupName), "参数 groupName 不可以为空值...");
        Optional<RoleEntity> role = loginUser.getMaxPowerRole();
        if (role.isPresent() && (role.get().isStoreManager() || role.get().isShoppingGuide())) {
            Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息...");
            Optional<List<MemberGroupInfo>> list = getBean(LegooWebOcxService.class, request)
                    .statisticalByGroup(groupName, company, loginUser.getStore().get(), loginUser);
            if (!list.isPresent()) return wrapperEmptyResponse();
            List<Map<String, Object>> res = Lists.transform(list.get(), new Function<MemberGroupInfo, Map<String, Object>>() {
                @Override
                public Map<String, Object> apply(MemberGroupInfo memberGroupInfo) {
                    return memberGroupInfo.toViewMap();
                }
            });
            return CollectionUtils.isNotEmpty(res) ? wrapperResponse(res) : wrapperEmptyResponse();
        } else if (role.isPresent() && role.get().isLeader()) {
            return wrapperEmptyResponse();
        }
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/statistical/detail.json")
    @ResponseBody
    public Map<String, Object> statisticalByGroupDetail(@RequestBody Map<String, String> requestBody,
                                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        String groupId = MapUtils.getString(requestBody, "groupId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(groupId), "参数 groupId 不可以为空值...");
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 1);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 20);
        requestBody.remove("pageNum");
        requestBody.remove("pageSize");
        Optional<PageDefinedDto> item = getBean(LegooWebOcxService.class, request).loadByGroupId(groupId, loginUser);
        if (logger.isDebugEnabled() && !item.isPresent())
            logger.debug(String.format("groupId=%s 对应的 分组信息  不存在或者授权失败....", groupId));
        if (!item.isPresent()) return wrapperResponse(PagingResult.emptyPagingResult("", ""));
        Map<String, Object> params = loginUser.toMap();
        if (MapUtils.isNotEmpty(requestBody)) {
            // 排除 空字符串
            Set<String> keys = Sets.newHashSet(requestBody.keySet());
            for (String key : keys) {
                if (Strings.isNullOrEmpty(MapUtils.getString(requestBody, key))) requestBody.remove(key);
            }
            params.putAll(requestBody);
        }
        item.get().getPageDefined().holdParam(params);
        PagingResult paging = querySupport(request).queryForPage(item.get().getSqlModel(), item.get().getSqlStmtId(),
                pageNum, pageSize, params);
        return wrapperResponse(paging.toMap());
    }

    @RequestMapping(value = "/mgn/group/switched.json")
    @ResponseBody
    public Map<String, Object> manage4GroupDetailSwitched(@RequestBody Map<String, String> requestBody,
                                                          HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前公司无公司信息，数据异常...");
        String groupId = MapUtils.getString(requestBody, "fullName");
        boolean enabled = MapUtils.getBoolean(requestBody, "enabled");
        Optional<PageDefinedDto> pgs = getBean(LegooWebOcxRepository.class, request).findByFullName(groupId);
        Preconditions.checkState(pgs.isPresent(), "Id=%s 对应的查询分组不存在...", groupId);
        OrganizationEntity company = loginUser.getCompany().get();
        if (loginUser.getMaxPowerRole().isPresent() && loginUser.getMaxPowerRole().get().isStoreManager()) {
            Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆账号无门店信息，数据异常...");
            StoreEntity store = loginUser.getStore().get();
            getBean(GroupAuthorEntityAction.class, request).saveOrUpdate(store, enabled, pgs.get(), loginUser);
        } else if (loginUser.getMaxPowerRole().isPresent() && loginUser.getMaxPowerRole().get().isLeader()) {
            getBean(GroupAuthorEntityAction.class, request).saveOrUpdate(company, enabled, pgs.get(), loginUser);
        } else {
            throw new RuntimeException("权限不足，无法执行该操作...");
        }
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/mgn/group/list.json")
    @ResponseBody
    public Map<String, Object> manage4GroupList(@RequestBody Map<String, String> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        String groupName = MapUtils.getString(requestBody, "groupName");
        Preconditions.checkState(loginUser.getCompany().isPresent(), "登陆用户数据异常.....无法获取公司信息...");
        OrganizationEntity company = loginUser.getCompany().get();
        Optional<RoleEntity> role = loginUser.getMaxPowerRole();
        if (!role.isPresent()) return wrapperEmptyResponse();
        Optional<List<GroupAuthorEntity>> author = Optional.absent();
        Optional<List<PageDefinedDto>> _pages = Optional.absent();
        if (role.get().isStoreManager()) {
            Preconditions.checkState(loginUser.getStore().isPresent(), "当前门店尚未分配门店信息，无法继续后续操作...");
            _pages = getBean(LegooWebOcxRepository.class, request)
                    .loadStorePages(groupName, company, loginUser.getStore().get());
            if (!_pages.isPresent()) return wrapperEmptyResponse();
            author = getBean(GroupAuthorEntityAction.class, request)
                    .findAllByStore(loginUser.getStore().get());

        } else if (role.get().isLeader()) {
            _pages = getBean(LegooWebOcxRepository.class, request)
                    .loadCompanyPages(groupName, company);
            if (!_pages.isPresent()) return wrapperEmptyResponse();
            author = getBean(GroupAuthorEntityAction.class, request)
                    .findAllByCom(company);
        }
        List<Map<String, Object>> list = Lists.newArrayList();
        for (PageDefinedDto $it : _pages.get()) {
            Map<String, Object> map = $it.toViewMap();
            map.put("enabled", true);
            if (author.isPresent()) {
                for (GroupAuthorEntity au : author.get()) {
                    if (au.equalsPage($it)) map.put("enabled", au.isEnabled());
                }
            }
            list.add(map);
        }
        return wrapperResponse(list);
    }

    private QueryEngineService querySupport(HttpServletRequest request) {
        return getBean("queryEngineService", QueryEngineService.class, request);
    }

}
