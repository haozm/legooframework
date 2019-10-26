package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.RoleEntity;
import com.csosm.module.base.entity.RoleEntityAction;
import com.csosm.module.menu.SecAccessService;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController(value = "RoleController")
@RequestMapping("/role")
public class RoleController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @RequestMapping(value = "/{channel}/enabled/list.json")
    public Map<String, Object> loadRoles(@PathVariable(name = "channel") String channel, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadRoles(http_request_map=%s)", request.getRequestURL()));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆账户无公司信息，无法执行后续操作...");
        Preconditions.checkArgument(StringUtils.equals(channel, "mobile") || StringUtils.equals(channel, "web"),
                "非法的请求...");
        List<RoleEntity> roles = getBean(RoleEntityAction.class, request).loadEnlabedRole(loginUser.getCompany().get());
        List<Map<String, Object>> mapList = Lists.newArrayList();
        roles.forEach(r -> {
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", r.getId());
            map.put("name", r.getName());
            map.put("desc", r.getDesc());
            map.put("resources", r.getResources());
            mapList.add(map);
        });
        return wrapperResponse(mapList);
    }

    @RequestMapping(value = "/{channel}/find/byid.json")
    @SuppressWarnings("unchecked")
    public Map<String, Object> findRoleById(@PathVariable(name = "channel") String channel,
                                            @RequestBody Map<String, String> requestBody,
                                            HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findRoleById(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆账户无公司信息，无法执行后续操作...");
        Preconditions.checkArgument(StringUtils.equals(channel, "mobile") || StringUtils.equals(channel, "web"),
                "非法的请求...");
        Integer roleId = MapUtils.getInteger(requestBody, "roleId");
        Preconditions.checkNotNull(roleId, "入参 roleId 不可以为空...");
        Optional<RoleEntity> roles = getBean(RoleEntityAction.class, request).findById(loginUser.getCompany().get(), roleId);
        Map<String, Object> map = Maps.newHashMap();
        roles.ifPresent(r -> {
            map.put("id", r.getId());
            map.put("name", r.getName());
            map.put("desc", r.getDesc());
            map.put("resources", r.getResources());
        });
        Optional<Set<String>> resourcesOpt = (Optional<Set<String>>)MapUtils.getObject(map, "resources");
        if (null != resourcesOpt && resourcesOpt.isPresent()) {
            Set<String> resourceIds = resourcesOpt.get();
            if (CollectionUtils.isNotEmpty(resourceIds)) {
                Multimap<String, String> sou = ArrayListMultimap.create();
                resourceIds.forEach(s -> sou.put(StringUtils.split(s, '_')[0], s));
                map.put("resources", sou.asMap());
            }
        }

        return MapUtils.isNotEmpty(map) ? wrapperResponse(map) : wrapperErrorResponse(new NullPointerException(),
                "不存在对应的角色...");
    }

    @RequestMapping(value = "/{channel}/authorized.json")
    public Map<String, Object> authorized(@PathVariable(name = "channel") String channel,
                                          @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("authorized(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(StringUtils.equals(channel, "mobile") || StringUtils.equals(channel, "web"),
                "非法的请求...");
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆账户无公司信息，无法执行后续操作...");
        Integer roleId = MapUtils.getInteger(requestBody, "roleId");
        String resourceIds = MapUtils.getString(requestBody, "resourceIds");
        Preconditions.checkArgument(roleId != null, "入参角色ID 不可以为空值...");
        String[] args = StringUtils.isEmpty(resourceIds) ? null : StringUtils.split(resourceIds, ',');
        getBean(SecAccessService.class, request).authorized(loginUser.getCompany().get(), roleId, args);
        return wrapperEmptyResponse();
    }

}
