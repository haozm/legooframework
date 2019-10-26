package com.csosm.module.material;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.material.entity.MaterialBlacklistEntity;
import com.csosm.module.material.entity.MaterialDetailAction;
import com.csosm.module.material.entity.MaterialDetailEntity;
import com.csosm.module.material.entity.MaterialGroupEntity;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.query.entity.PagingResult;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller(value = "materialController")
@RequestMapping("/material")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);
    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static String META_TYPES = "1:文本,3:图片,34:语音,43:视频,47:图片表情,49:网页信息,99:图文信息";

    @RequestMapping(value = "/load/types.json")
    @ResponseBody
    public Map<String, Object> loadMaterialTypes(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],loadMaterialTypes(requestBody=%s)", request.getRequestURI(), null));
        return wrapperResponse(Splitter.on(',').withKeyValueSeparator(':').split(META_TYPES));
    }

    @RequestMapping(value = "/single/add.json")
    @ResponseBody
    public Map<String, Object> createOneMaterial(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],createOneMaterial(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        int groupId = MapUtils.getIntValue(requestBody, "groupId");
        String context = MapUtils.getString(requestBody, "context");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(context), "内容不可以为空....");
        String deadline = MapUtils.getString(requestBody, "deadline");
        Date deadline_date = null;
        try {
            if (!Strings.isNullOrEmpty(deadline)) deadline_date = DAY_FORMAT.parse(deadline);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("非法的日期入参%s....", deadline));
        }
        Long id = getBean(MaterialDetailService.class, request).createByUser(userContext, groupId, context, deadline_date);
        return wrapperResponse(id);
    }

    @RequestMapping(value = "/find/byid.json")
    @ResponseBody
    public Map<String, Object> findById(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],findById(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        long materialId = MapUtils.getLongValue(requestBody, "id");
        Optional<MaterialDetailEntity> entity = getBean(MaterialDetailAction.class, request).findById(materialId);
        boolean effective = MapUtils.getBooleanValue(requestBody, "effective", false);
        Optional<MaterialBlacklistEntity> blacklist = getBean(MaterialDetailAction.class, request).findByUser(user);
        if (blacklist.isPresent() && entity.isPresent()) {
            if (blacklist.get().contains(entity.get())) entity.get().setBlacked();
        }
        if (entity.isPresent() && effective) {
            Preconditions.checkState(entity.get().isEnabled() && !entity.get().isBlacked(), "该素材处于停用状态，无法使用...");
            Preconditions.checkState(entity.get().isInEffectiveDate(), "该素材已经过期，无法获取...");
        }
        return wrapperResponse(entity.isPresent() ? entity.get().toViewBean() : null);
    }

    @RequestMapping(value = "/group/list.json")
    @ResponseBody
    public Map<String, Object> loadGroupList(@RequestBody(required = false) Map<String, String> requestBody,
                                             HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],loadGroupList(requestBody=%s)", request.getRequestURI(), null));
        Optional<List<MaterialGroupEntity>> entity = getBean(MaterialDetailAction.class, request)
                .loadAllGroups();
        List<Integer> _type = Lists.newArrayList(1);
        if (MapUtils.isNotEmpty(requestBody) && !Strings.isNullOrEmpty(MapUtils.getString(requestBody, "types"))) {
            String types = MapUtils.getString(requestBody, "types");
            String[] args = StringUtils.split(types, ',');
            _type.clear();
            for (String $it : args) _type.add(Integer.valueOf($it));
        }
        if (entity.isPresent()) {
            List<Map<String, Object>> list = Lists.newArrayList();
            for (MaterialGroupEntity $it : entity.get()) {
                if (_type.contains($it.getGroupType())) list.add($it.toViewBean());
            }
            return wrapperResponse(list);
        }
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/switch/status.json")
    @ResponseBody
    public Map<String, Object> switchStatus(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],switchStatus(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        long materialId = MapUtils.getLongValue(requestBody, "id");
        boolean enabled = MapUtils.getBooleanValue(requestBody, "enabled");
        getBean(MaterialDetailAction.class, request).switchStatus(materialId, userContext, enabled);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/switch/fans.json")
    @ResponseBody
    public Map<String, Object> switchFansStatus(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],switchFansStatus(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        long materialId = MapUtils.getLongValue(requestBody, "id");
        boolean enabled = MapUtils.getBooleanValue(requestBody, "enabled");
        Preconditions.checkState(!userContext.getEmployee().isAdmin(), "超级管理员不支持该操作...");
        getBean(MaterialDetailAction.class, request).switchFansStatus(materialId, userContext, enabled);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/edit/context.json")
    @ResponseBody
    public Map<String, Object> editContext(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],switchStatus(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        long materialId = MapUtils.getLongValue(requestBody, "id");
        String context = MapUtils.getString(requestBody, "context");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(context), "内容不可以为空....");
        String deadline = MapUtils.getString(requestBody, "deadline");
        Date deadline_date = null;
        try {
            if (!Strings.isNullOrEmpty(deadline)) deadline_date = DAY_FORMAT.parse(deadline);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("非法的日期入参%s....", deadline));
        }
        getBean(MaterialDetailAction.class, request).editInfo(materialId, userContext, context, deadline_date);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/page/list.json")
    @ResponseBody
    public Map<String, Object> materialList(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],materialList(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);

        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        List<String> orgIds = Lists.newArrayList();

        Map<String, Object> my_mate = Maps.newHashMap();
        // 公司超管
        if (userContext.getEmployee().isAdmin()) {
            my_mate.put("orgId", -1);
            my_mate.put("range", 1);
            my_mate.put("companyId", -1);
        } else if (userContext.getStore().isPresent()) { // 门店
            Preconditions.checkState(userContext.getCompany().isPresent());
            my_mate.put("orgId", userContext.getStore().get().getId());
            my_mate.put("range", 4);
            my_mate.put("companyId", userContext.getCompany().get().getId());
        } else if (userContext.getOrganization().isPresent()) { //中间状态
            Preconditions.checkState(userContext.getCompany().isPresent());
            my_mate.put("orgId", userContext.getOrganization().get().getId());
            my_mate.put("range", userContext.getOrganization().get().isCompany() ? 2 : 3);
            my_mate.put("companyId", userContext.getCompany().get().getId());
        }

        orgIds.add("1,-1");
        orgIds.add(String.format("2,%s", userContext.getEmployee().getCompanyId().or(-1)));

        Optional<MaterialBlacklistEntity> blacklist = userContext.getEmployee().isAdmin() ? Optional.<MaterialBlacklistEntity>absent() :
                getBean(MaterialDetailAction.class, request).findByUser(userContext);
        Map<String, Object> params = userContext.toMap();
        if (blacklist.isPresent()) {
            if (!blacklist.get().isBlackEmpty()) {
                params.put("blacklist", blacklist.get().getBlacklist());
            }
            if (!blacklist.get().isWhiteEmpty()) {
                params.put("whitelist", blacklist.get().getWhitelist());
            } else {
                params.put("whitelist", new int[]{-1});
            }
        } else {
            params.put("whitelist", new int[]{-1});
        }
        if (userContext.getEmployee().isAdmin()) {
            orgIds.add("1,-1");
        } else if (userContext.getStore().isPresent()) {
            orgIds.add(String.format("4,%s", userContext.getStore().get().getId()));
            if (userContext.getStore().get().getOrganizationId().isPresent()) {
                Optional<List<OrganizationEntity>> orgs = getBean(OrganizationEntityAction.class, request)
                        .loadAllSuperOrgs(userContext.getCompany().get().getId(), userContext.getStore().get().getOrganizationId().get(),
                                true);
                if (orgs.isPresent()) {
                    for (OrganizationEntity $it : orgs.get()) {
                        if ($it.isCompany()) {
                            orgIds.add(String.format("2,%s", $it.getId()));
                        } else {
                            orgIds.add(String.format("3,%s", $it.getId()));
                        }
                    }
                }
            }
        } else if (userContext.getOrganization().isPresent()) {
            OrganizationEntity org = userContext.getOrganization().get();
            if (org.isCompany()) {
                orgIds.add(String.format("2,%s", org.getId()));
            } else {
                Optional<List<OrganizationEntity>> orgs = getBean(OrganizationEntityAction.class, request)
                        .loadAllSuperOrgs(userContext.getCompany().get().getId(), org.getId(), true);
                if (orgs.isPresent()) {
                    for (OrganizationEntity $it : orgs.get()) {
                        if ($it.isCompany()) {
                            orgIds.add(String.format("2,%s", $it.getId()));
                        } else {
                            orgIds.add(String.format("3,%s", $it.getId()));
                        }
                    }
                }
            }
        }
        params.put("orgIds", orgIds);

        if (requestBody.containsKey("myrange")) {
            int my_range = MapUtils.getIntValue(requestBody, "myrange");
            if (6 == my_range) {
                params.put("my_mate", my_mate);
            } else {
                params.put("myrange", my_range);
            }
        }

        String types = MapUtils.getString(requestBody, "types");
        if (!Strings.isNullOrEmpty(types)) params.put("types", StringUtils.split(types, ','));
        if (requestBody.containsKey("enabled"))
            params.put("enabled", MapUtils.getBooleanValue(requestBody, "enabled"));
        if (requestBody.containsKey("groups"))
            params.put("groups", MapUtils.getIntValue(requestBody, "groups"));
        if (requestBody.containsKey("effective"))
            params.put("effective", MapUtils.getIntValue(requestBody, "effective") == 1);
        if (params.containsKey("groups") || params.containsKey("my_mate") || params.containsKey("myrange")
                || params.containsKey("types") ||
                params.containsKey("enabled") || params.containsKey("effective")) {
            params.put("where", true);
        } else {
            params.put("where", false);
        }
        if (requestBody.containsKey("order")) {
            params.put("order", MapUtils.getString(requestBody, "order"));
            Preconditions.checkArgument(StringUtils.containsAny("createTime,useSize,fansSize", MapUtils.getString(requestBody, "order"),
                    "排序条件默认支持 createTime,useSize,fansSize 三种"));
        }
        PagingResult page = queryEngineService(request).queryForPage("MaterialDetail", "list_4_mgn", pageNum, pageSize, params);
        if (page.getResultSet().isPresent()) {
            List<Map<String, Object>> maps = page.getResultSet().get();
            for (Map<String, Object> map : maps) {
                int enabled = MapUtils.getIntValue(map, "enabled");
                int blacked = MapUtils.getIntValue(map, "blacked");
                map.put("enabled", (1 == enabled && 0 == blacked));
                map.put("fansed", MapUtils.getIntValue(map, "fansed", 0) == 0 ? 0 : 1);
            }
        }
        return wrapperResponse(page.toMap());
    }

    @RequestMapping(value = "/load/talking.json")
    @ResponseBody
    public Map<String, Object> loadTalking(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],loadTalking(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
        int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
        Integer groupType = MapUtils.getIntValue(requestBody, "groupType", 2);
        String search = MapUtils.getString(requestBody, "search", null);
        PagingResult pagingResult = getBean(MaterialDetailService.class, request)
                .loadEnbaledTalking(userContext, groupType, search, pageNum, pageSize);
        return wrapperResponse(pagingResult.toMap());
    }

    @RequestMapping(value = "/words/replace.json")
    @ResponseBody
    public Map<String, Object> replaceWords(@RequestBody Map<String, String> http_request_map,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("replaceWords( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument(http_request_map.containsKey("content")
                        && http_request_map.containsKey("weixinId") && http_request_map.containsKey("materialId"),
                "请求参数中不能缺少content、weixinId、materialId");
        String content = MapUtils.getString(http_request_map, "content");
        String weixinId = MapUtils.getString(http_request_map, "weixinId");
        String materialId = MapUtils.getString(http_request_map, "materialId");
        String result = getBean(MaterialDetailService.class, request).replaceWords(content, userContext, weixinId, Long.parseLong(materialId));
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("content", result);
        return wrapperResponse(resultMap);
    }

    private QueryEngineService queryEngineService(HttpServletRequest request) {
        return getBean("queryEngineService", QueryEngineService.class, request);
    }
}
