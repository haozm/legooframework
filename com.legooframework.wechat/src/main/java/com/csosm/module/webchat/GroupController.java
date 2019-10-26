package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.EmployeeServer;
import com.csosm.module.base.entity.EmployeeEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.webchat.entity.WebChatUserAction;
import com.csosm.module.webchat.entity.WebChatUserEntity;
import com.csosm.module.webchat.group.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller(value = "groupController")
@RequestMapping("/webchat/group")
public class GroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(GroupController.class);

    /**
     * 新增分组
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/addGroup.json")
    @ResponseBody
    public Map<String, Object> addGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addGroup(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        Preconditions.checkState(store.hasDevice(), "门店%s尚未绑定微信终端设备.", store.getName());
        WeixinGroupEntity group = getBean(WeixinGroupAction.class, request).addUserDefinedGroup(store,
                handler.getExistGroupName());
        Map<String, Object> groupMap = Maps.newHashMap();
        groupMap.put("groupId", group.getId());
        groupMap.put("groupName", group.getName());
        return wrapperResponse(groupMap);
    }

    /**
     * 修改分组名称
     *
     * @param params
     * @param request
     * @return
     */
    @RequestMapping(value = "/changeGroupName.json")
    @ResponseBody
    public Map<String, Object> changeGroupName(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("changeGroupName(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).modifyGroupName(handler.getExistGroupId(),
                handler.getExistGroupName(), store);
        return wrapperEmptyResponse();
    }

    /**
     * 移除分组
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/removeGroup.json")
    @ResponseBody
    public Map<String, Object> removeGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("removeGroup(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).removeGroup(handler.getExistGroupId(), store);
        return wrapperEmptyResponse();
    }

    /**
     * 添加多个微信好友
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/addFriends.json")
    @ResponseBody
    public Map<String, Object> addFriends(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addFriends(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        StoreEntity store = getAndCheckStore(params, userContext);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        getBean(WeixinGroupService.class, request).addFriends(handler.getExistGroupId(), handler.getFriendIds(), store);
        return wrapperEmptyResponse();
    }

    /**
     * 移除微信好友
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/removeFriends.json")
    @ResponseBody
    public Map<String, Object> removeFriends(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("removeFriends(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).removeFriends(handler.getExistGroupId(), handler.getFriendIds(),
                store);
        return wrapperEmptyResponse();
    }

    /**
     * 将分组授权给导购
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/grantToGuide.json")
    @ResponseBody
    public Map<String, Object> grantGroup(@RequestBody(required = false) Map<String, Object> httpParam,
                                          HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("grantToGuide(http_request_map=%s)", httpParam));
        LoginUserContext userContext = loadLoginUser(request);
        StoreEntity store = getAndCheckStore(httpParam, userContext);
        GroupParamsHanler handler = new GroupParamsHanler(httpParam, userContext);
        if (handler.getGroups().isEmpty())
            return wrapperEmptyResponse();
        for (Map<String, Object> group : handler.getGroups()) {
            getBean(WeixinGroupService.class, request).grantGroupToEmployees(userContext, store,
                    MapUtils.getString(group, "groupId"), Lists.newArrayList((String[]) group.get("guideIds")));
        }
        return wrapperEmptyResponse();
    }

    /**
     * 一键分配导购组
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/autoAssign.json")
    @ResponseBody
    public Map<String, Object> autoAssignGroup(@RequestBody(required = false) Map<String, Object> params,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("autoAssign(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).addGuideGroups(store, userContext);
        return wrapperEmptyResponse();
    }

    private List<Map<String, Object>> getStoreGroupsMap(StoreEntity store, HttpServletRequest request) {
        List<WeixinGroupEntity> storeGroups = getBean(WeixinGroupAction.class, request).loadGroupsForStore(store);
        List<Map<String, Object>> storeGroupsMaps = Lists.newArrayList();
        for (WeixinGroupEntity group : storeGroups)
            storeGroupsMaps.add(group.toViewMap());
        return storeGroupsMaps;
    }

    private List<Map<String, Object>> getLabelGroupsMap(StoreEntity store, HttpServletRequest request) {
        List<WeixinGroupEntity> labelGroups = getBean(WeixinGroupService.class, request).loadGroupsForWeixin(store);
        List<Map<String, Object>> labelGroupsMaps = Lists.newArrayList();
        for (WeixinGroupEntity group : labelGroups)
            labelGroupsMaps.add(group.toViewMap());
        return labelGroupsMaps;
    }

    /**
     * 分组转换
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/toGroup.json")
    @ResponseBody
    public Map<String, Object> toGroup(@RequestBody(required = false) Map<String, Object> params,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("toGroup(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument(params.containsKey("orgin") && params.containsKey("dest"), "请求参数缺少原分组或目标分组ID");
        String orgin = MapUtils.getString(params, "orgin");
        String dest = MapUtils.getString(params, "dest");
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).toGroup(store, orgin, dest);
        return wrapperEmptyResponse();
    }

    /**
     * 没有好友列表的分组信息
     *
     * @param params
     * @param request
     * @return
     */
    @RequestMapping(value = "/{type}/list.json")
    @ResponseBody
    public Map<String, Object> listNoMemberGroup(@RequestBody(required = false) Map<String, Object> params,
                                                 @PathVariable String type, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("listNoMemberGroup(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument(userContext.getStore().isPresent(), "登录用户无门店信息");
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext, store);
        getBean(WeixinGroupService.class, request).addAndGrantedGuideGroupIfAbsent(userContext, store);
        if (type.trim().equals("store")) {
            return wrapperResponse(getStoreGroupsMap(store, request));
        } else if (type.trim().equals("label")) {
            return wrapperResponse(getLabelGroupsMap(store, request));
        } else if (type.trim().equals("all")) {
            List<Map<String, Object>> retMaps = getStoreGroupsMap(store, request);
            retMaps.addAll(getLabelGroupsMap(store, request));
            return wrapperResponse(retMaps);
        } else if (type.trim().equals("pc-tree")) {
            List<Map<String, Object>> retMaps = Lists.newArrayList();
            Map<String, Object> storeGroupsMap = Maps.newHashMap();
            Map<String, Object> labelGroupsMap = Maps.newHashMap();
            List<Map<String, Object>> storeGroupsMaps = getStoreGroupsMap(store, request);
            if (!storeGroupsMaps.isEmpty()) {
                storeGroupsMap.put("id", "store");
                storeGroupsMap.put("label", "普通组");
                storeGroupsMap.put("type", "group");
                storeGroupsMap.put("children", storeGroupsMaps);
                retMaps.add(storeGroupsMap);
            }
            labelGroupsMap.put("id", "label");
            labelGroupsMap.put("label", "标签组");
            labelGroupsMap.put("type", "group");
            labelGroupsMap.put("children", new String[0]);
            retMaps.add(labelGroupsMap);
            return wrapperResponse(retMaps);
        } else if (type.trim().equals("tree")) {
            List<Map<String, Object>> retMaps = Lists.newArrayList();
            Map<String, Object> storeGroupsMap = Maps.newHashMap();
            Map<String, Object> labelGroupsMap = Maps.newHashMap();
            List<Map<String, Object>> storeGroupsMaps = getStoreGroupsMap(store, request);
            List<Map<String, Object>> labelGroupsMaps = getLabelGroupsMap(store, request);
            if (!storeGroupsMaps.isEmpty()) {
                storeGroupsMap.put("id", "store");
                storeGroupsMap.put("label", "普通组");
                storeGroupsMap.put("type", "group");
                storeGroupsMap.put("children", storeGroupsMaps);
                retMaps.add(storeGroupsMap);
            }
            if (!labelGroupsMaps.isEmpty()) {
                labelGroupsMap.put("id", "label");
                labelGroupsMap.put("label", "标签组");
                labelGroupsMap.put("type", "group");
                labelGroupsMap.put("children", labelGroupsMaps);
                retMaps.add(labelGroupsMap);
            }
            return wrapperResponse(retMaps);
        }
        return wrapperResponse(new String[0]);
    }

    /**
     * 加载登录用户拥有的分组列表
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/queryGuideRoles.json")
    @ResponseBody
    public Map<String, Object> queryGuideRoles(@RequestBody(required = false) Map<String, Object> params,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryGuideRoles(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        StoreEntity store = getAndCheckStore(params, userContext);
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext, store);
        getBean(WeixinGroupService.class, request).addAndGrantedGuideGroupIfAbsent(userContext, store);
        List<GroupEmployeesDTO> GroupEmployeesDtos = getBean(WeixinGroupAction.class, request)
                .loadGrantedGroupWithEmployees(store);
        Optional<List<EmployeeEntity>> empsOpt = getBean(EmployeeServer.class, request)
                .loadEnabledShoppingGuides(store.getId(), userContext);
        return wrapperResponse(GroupResultHandler.createGuideRolesResult(GroupEmployeesDtos, empsOpt));
    }

    /**
     * 获取门店
     *
     * @param params
     * @param userContext
     * @return
     */
    private StoreEntity getAndCheckStore(Map<String, Object> params, LoginUserContext userContext) {
        StoreEntity store = null;
        if (params == null || params.isEmpty() || !params.containsKey("storeId")) {
            store = userContext.getStore().isPresent() ? userContext.getStore().get() : null;
        }
        if (params != null && params.containsKey("storeId")) {
            Integer storeId = MapUtils.getInteger(params, "storeId");
            Optional<StoreEntity> storeOpt = storeAction.findById(storeId);
            store = storeOpt.isPresent() ? storeOpt.get() : null;
        }
        Preconditions.checkState(store != null, "登录用户未选择门店");
        return store;
    }

    /**
     * 查询好友列表
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/queryAllFriends.json")
    @ResponseBody
    public Map<String, Object> queryAllFriends(@RequestBody(required = false) Map<String, Object> params,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("queryAllFriends(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        List<MemberDTO> members = getBean(WeixinGroupService.class, request).searchFriend(handler.getExistGroupId(),
                params, store);
        List<Map<String, Object>> children = Lists.newArrayList();
        for (MemberDTO member : members) {
            children.add(member.toMap());
        }
        return wrapperResponse(children);
    }

    /**
     * 查询好友列表
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "search/friends.json")
    @ResponseBody
    public Map<String, Object> loadAllFriends(@RequestBody(required = false) Map<String, Object> params,
                                              HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("search-friends(http_request_map=%s)", params));
        LoginUserContext userContext = loadLoginUser(request);
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        Set<MemberDTO> members = getBean(WeixinGroupService.class, request).listFriends(handler.getExistGroupIds(), store);
        List<Map<String, Object>> children = Lists.newArrayList();
        for (MemberDTO member : members) {
            children.add(member.toMap());
        }
        return wrapperResponse(children);
    }

    /**
     * 加载登录用户拥有的分组列表
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/queryGroupList.json")
    @ResponseBody
    public Map<String, Object> queryStoreGroupList(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent(), "登录用户无门店信息");
        Preconditions.checkState(userContext.getCompany().isPresent(), "登录用户无公司信息");
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext,
                userContext.getExitsStore());
        getBean(WeixinGroupService.class, request).addAndGrantedGuideGroupIfAbsent(userContext,
                userContext.getExitsStore());
        List<GroupMemberDTO> groupMemberDtos = getBean(WeixinGroupService.class, request)
                .loadGrantedGroupsWithMembersForGuide(userContext, params);
        List<Map<String, Object>> resMaps = Lists.newArrayList();
        for (GroupMemberDTO dto : groupMemberDtos)
            resMaps.add(dto.toMap());
        return wrapperResponse(resMaps);
    }

    /**
     * 获取授权给当前登录用户的分组
     *
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/queryNoMemberGroups.json")
    @ResponseBody
    public Map<String, Object> queryNoMemberGroups(HttpServletRequest request) {
        LoginUserContext userContext = loadLoginUser(request);
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext,
                userContext.getExitsStore());
        getBean(WeixinGroupService.class, request).addAndGrantedGuideGroupIfAbsent(userContext, userContext.getExitsStore());
        List<Map<String, Object>> resultMaps = Lists.newArrayList();
        List<WeixinGroupEntity> groups = getBean(WeixinGroupService.class, request).loadNoMemberGroups(userContext);
        for (WeixinGroupEntity group : groups) {
            resultMaps.add(group.toViewMap());
        }
        return wrapperResponse(resultMaps);
    }

    /**
     * 获取授权给当前登录用户的分组
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/query/friend/groups.json")
    @ResponseBody
    public Map<String, Object> queryFriendGroups(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        LoginUserContext userContext = loadLoginUser(request);
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext,
                userContext.getExitsStore());
        getBean(WeixinGroupService.class, request).addAndGrantedGuideGroupIfAbsent(userContext, userContext.getExitsStore());
        List<Map<String, Object>> resultMaps = Lists.newArrayList();
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        StoreEntity store = getAndCheckStore(params, userContext);
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class, request).findById(store, handler.getFriendId());
        Preconditions.checkArgument(weixinOpt.isPresent(), String.format("微信好友[%s]不存在", handler.getFriendId()));
        List<WeixinGroupEntity> groups = getBean(WeixinGroupService.class, request).loadNoMemberGroups(userContext);
        for (WeixinGroupEntity group : groups) {
            Map<String, Object> viewMap = group.toViewMap();
            if (getBean(WeixinGroupAction.class, request).existInGrantableGroup(userContext.getExitsStore(),
                    group, weixinOpt.get())) {
                viewMap.put("existFriend", 1);
            } else {
                viewMap.put("existFriend", 0);
            }
            resultMaps.add(viewMap);
        }
        return wrapperResponse(resultMaps);
    }

    /**
     * 重置分组好友
     *
     * @param params
     * @param request
     * @return OK
     */
    @RequestMapping(value = "/reset/friend/groups.json")
    @ResponseBody
    public Map<String, Object> resetFriendGroups(@RequestBody(required = false) Map<String, Object> params, HttpServletRequest request) {
        LoginUserContext userContext = loadLoginUser(request);
        getBean(WeixinGroupService.class, request).grantAllFriendGroupIfAbsent(userContext,
                userContext.getExitsStore());
        GroupParamsHanler handler = new GroupParamsHanler(params, userContext);
        getBean(WeixinGroupService.class, request).resetGroupsAndFriends(userContext, handler.getExistGroupIds(), handler.getFriendId());
        return wrapperEmptyResponse();
    }

    @Resource
    private StoreEntityAction storeAction;
    @Resource
    private QueryEngineService queryEngineService;
}
