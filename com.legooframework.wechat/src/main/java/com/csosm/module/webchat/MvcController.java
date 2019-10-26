package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.mvc.BaseController;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.*;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.query.entity.PagingResult;
import com.csosm.module.webchat.concurrent.MatchResultSet;
import com.csosm.module.webchat.concurrent.WechatMatchMemberDto;
import com.csosm.module.webchat.entity.*;
import com.csosm.module.webchat.group.WeixinGroupFactory;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller(value = "webChatController")
@RequestMapping("/webchat")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);
    private static final DateFormat DAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @RequestMapping(value = "/upload/4save.json")
    @ResponseBody
    public Map<String, Object> upload4Save(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        loadLoginUser(request);
        String json_data = MapUtils.getString(requestBody, "data");
        if (!Strings.isNullOrEmpty(json_data)) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(json_data).getAsJsonObject();
            String weixinId = MyWebUtil.getString(jsonObject, "weixinId");
            String deviceId = MyWebUtil.getString(jsonObject, "deviceId");
            JsonArray json_array = jsonObject.getAsJsonArray("datas");
            List<Map<String, Object>> new_list = Lists.newArrayList();
            for (int i = 0; i < json_array.size(); i++) {
                JsonElement $it = json_array.get(i);
            }
        }
        return wrapperResponse(null);
    }

//    @RequestMapping(value = "/login.json")
//    @ResponseBody
//    public Map<String, Object> loginAction(@RequestBody Map<String, String> http_request_map,
//                                           HttpServletRequest request) {
//        String login_name = MapUtils.getString(http_request_map, "username");
//        String login_pwd = MapUtils.getString(http_request_map, "password");
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("loginAction(username=%s,password=******)", login_name));
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(login_name), "用户名 username 非法。");
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(login_name), "密码 password 非法。");
//        LoginUserContext userContext = baseAdapterServer.loginAction(login_name, login_pwd, request);
//        Map<String, Object> res_map = userContext.toMap();
//        Optional<StoreEntity> store_opt = userContext.getStore();
//        res_map.put("DEVICE_ID", -99999L);
//        res_map.put("COMPANY_ID", userContext.getCompany().isPresent() ? userContext.getCompany().get().getId() : null);
//        Map<String, Object> ip_mapping_params = Maps.newHashMap();
//        ip_mapping_params.put("companyId", userContext.getCompany().isPresent() ? userContext.getCompany().get().getId() : -1);
//        Optional<Map<String, Object>> ip_mapping = queryEngineService.queryForMap("webchat",
//                "loadipmapping", ip_mapping_params);
//        res_map.put("IP_TABLE", ip_mapping.isPresent() ? ip_mapping.get() : null);
//        if (store_opt.isPresent()) {
//            Map<String, Object> params = Maps.newHashMap();
//            params.put("storeId", store_opt.get().getId());
//            Optional<String> tablename = queryEngineService
//                    .queryForObject("webchat", "contactsTable", params, String.class);
//            res_map.put("DEVICE_ID", tablename.isPresent() ? tablename.get() : -99999L);
//        }
//        return wrapperResponse(res_map);
//    }

    @RequestMapping(value = "/{storeId}/contacts.json")
    @ResponseBody
    public Map<String, Object> loadContacts(@PathVariable Integer storeId, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadContacts(storeId=%s)", storeId));
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "无法匹配%s对应得门店.");
        // Preconditions.checkState(store.get().hasDevice(), "门店%s尚未绑定微信终端设备.", store.get().getName());
        Map<String, Object> params = Maps.newHashMap();
        params.put("tablename", store.get().getContactTableName());
        params.put("storeId", store.get().getId());
        Optional<List<Map<String, Object>>> contacts_opt = queryEngineService.queryForList("webchat", "contacts",
                params);
        return wrapperResponse(contacts_opt.isPresent() ? contacts_opt.get() : new String[0]);
    }

    @RequestMapping(value = "/unbild/contacts.json")
    @ResponseBody
    public Map<String, Object> loadContact4Seach(@RequestBody Map<String, Object> http_request_map,
                                                 HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadContact4Seach(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "入参 storeId  不可以为空...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "无法匹配%s对应得门店.");
        Preconditions.checkState(store.get().hasDevice(), "门店%s尚未绑定微信终端设备.", store.get().getName());
        Map<String, Object> params = Maps.newHashMap();
        params.put("tablename", store.get().getContactTableName());
        params.put("storeId", store.get().getId());
        if (MapUtils.isNotEmpty(http_request_map)
                && !Strings.isNullOrEmpty(MapUtils.getString(http_request_map, "search"))) {
            params.put("where", true);
            params.putAll(http_request_map);
        } else {
            params.put("where", false);
        }
        Optional<List<Map<String, Object>>> contacts_opt = queryEngineService.queryForList("webchat", "search_unbild",
                params);
        return wrapperResponse(contacts_opt.isPresent() ? contacts_opt.get() : new String[0]);
    }

    private static class Conditions {

        private Map<String, Object> reqMap;

        private Map<String, Object> params;

        private final StoreEntity store;

        Conditions(StoreEntity store, Map<String, Object> reqMap, Map<String, Object> params) {
            this.store = store;
            this.reqMap = reqMap;
            this.params = params;
        }

        private void fillBaseCondition() {
            this.params.put("tablename", this.store.getContactTableName());
            this.params.put("storeId", this.store.getId());
            Preconditions.checkState(this.store.getDeviceIds().isPresent(), "当前门店%s无设备信息...,无法执行后续操作",
                    store.getName());
            this.params.put("deviceIds", this.store.getDeviceIds().get());
        }

        private void fillAllCondition() {
            if (!params.containsKey("all")) return;
            boolean alled = MapUtils.getBooleanValue(this.reqMap, "all", false);
            this.params.put("all", alled);
        }

        private void fillBildFlagCondition() {
            if (!this.reqMap.containsKey("bildFlag")) return;
            Integer bildFlag = MapUtils.getInteger(this.reqMap, "bildFlag");
            if (null != bildFlag) {
                Preconditions.checkArgument(bildFlag == 0 || bildFlag == 1 || bildFlag == 2, "非法的 bildFlag 取值 %s",
                        bildFlag);
                if (bildFlag == 1 || bildFlag == 2)
                    this.params.put("bildFlag", bildFlag);
            }
        }

        private void fillOperFlagCondition() {
            if (!this.reqMap.containsKey("operFlags")) return;
            String operFlags = MapUtils.getString(this.reqMap, "operFlags");
            if (!Strings.isNullOrEmpty(operFlags)) {
                List<String> operFlags_vals = Lists.newArrayList();
                String[] args = StringUtils.split(operFlags, ',');
                for (String $it : args) {
                    if ("1".equals($it)) {
                        operFlags_vals.add("add");
                    } else if ("2".equals($it)) {
                        operFlags_vals.add("delete_contact");
                    } else if ("3".equals($it)) {
                        operFlags_vals.add("passive_delete_contact");
                    } else if ("4".equals($it)) {
                        operFlags_vals.add("black_contact");
                    }
                }
                this.params.put("operFlags", operFlags_vals);
            }
        }

        private void fillGroupCondition() {
            if (!this.reqMap.containsKey("groupId"))
                return;
            String groupId = MapUtils.getString(this.reqMap, "groupId");
            if (WeixinGroupFactory.isAllFriendGroup(groupId))
                return;
            if (WeixinGroupFactory.isNewFriendGroup(groupId)) {
                this.params.put("newAddFriendGroupId", groupId);
                this.params.put("beforeDays", store.getBeforeDays());
                return;
            }
            if (WeixinGroupFactory.isLabelGroup(groupId)) {
                this.params.put("labelGroupId", groupId);
                this.params.put("labelDeviceId", WeixinGroupFactory.getDeviceId(groupId));
                this.params.put("labelId", WeixinGroupFactory.getLabelId(groupId));
                return;
            }
            this.params.put("commonGroupId", groupId);
        }

        private void fillGroupsCondition() {
            if (!this.reqMap.containsKey("groupIds"))
                return;
            String groupIdsStr = MapUtils.getString(this.reqMap, "groupIds");
            String[] groupIds = StringUtils.split(groupIdsStr, ',');
            if (groupIds.length == 0)
                return;
            List<String> commonGroupIds = Lists.newArrayList();
            Map<String, Object> labelGroupMap = Maps.newIdentityHashMap();
            for (int i = 0; i < groupIds.length; i++) {
                String groupId = groupIds[i];
                if (WeixinGroupFactory.isLabelGroup(groupId)) {
                    labelGroupMap.put(WeixinGroupFactory.getDeviceId(groupId), WeixinGroupFactory.getLabelId(groupId));
                } else {
                    commonGroupIds.add(groupId);
                }
            }
            if (!commonGroupIds.isEmpty())
                this.params.put("commonGroupIds", commonGroupIds);
            if (!labelGroupMap.isEmpty())
                this.params.put("labelGroupMap", labelGroupMap);
        }

        private void fillSearchCondition() {
            if (!this.reqMap.containsKey("search")) return;
            String search = MapUtils.getString(this.reqMap, "search");
            if (!Strings.isNullOrEmpty(search))
                this.params.put("search", search);
        }

        private void fillDateCondition() {
            if (this.reqMap.containsKey("beginTime") && this.reqMap.containsKey("endTime")) {
                String beginTime = MapUtils.getString(this.reqMap, "beginTime", null);
                String endTime = MapUtils.getString(this.reqMap, "endTime", null);
                this.params.put("beginTime", beginTime);
                this.params.put("endTime", endTime);
            }
        }

        private void fillLabelIdsCondition() {
            if (!this.reqMap.containsKey("labelIds")) return;
            String lableIdsStr = MapUtils.getString(this.reqMap, "labelIds");
            if (!Strings.isNullOrEmpty(lableIdsStr))
                params.put("labelIds", Lists.newArrayList(lableIdsStr.split(",")));
        }

        public Map<String, Object> conditionToMap() {
            fillBildFlagCondition();
            fillAllCondition();
            fillOperFlagCondition();
            fillSearchCondition();
            fillGroupCondition();
            fillGroupsCondition();
            fillBaseCondition();
            fillDateCondition();
            fillLabelIdsCondition();
            return this.params;
        }
    }

    @RequestMapping(value = "/with/member/list.json")
    @ResponseBody
    public Map<String, Object> loadWebchatWithMemberList(@RequestBody Map<String, Object> http_request_map,
                                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,loadContactList(http_request_map=%s)", request.getRequestURI(),
                    http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        StoreEntity store = null;
        if(http_request_map.containsKey("storeId")) {
        	Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
            Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
            Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class, request).findById(storeId);
            Preconditions.checkState(storeOpt.isPresent(), String.format("无法匹配%s对应得门店.", storeId));
            store = storeOpt.get();
        }else {
        	Optional<StoreEntity> storeOpt = userContext.getStore();
        	if(!storeOpt.isPresent()) return wrapperResponse(new String[0]);
        	Preconditions.checkState(null != store, "当前用户及请求参数无门店信息");
        }
        Map<String, Object> params = userContext.toMap();
        Integer pageNum = MapUtils.getInteger(http_request_map, "pageNum",1);
        Integer pageSize = MapUtils.getInteger(http_request_map, "pageSize",20);
        Conditions conditions = new Conditions(store, http_request_map, params);
        PagingResult pagingResult = queryEngineService.queryForPage("webchat", "contactslist", pageNum,
        		pageSize, conditions.conditionToMap());
        return wrapperResponse(pagingResult.toMap());
    }

    
    // 获取当前登录用户 所在门店的微信通信录,用于PC端页面管理
    @RequestMapping(value = "/store/all/contacts.json")
    @ResponseBody
    public Map<String, Object> loadContactsByStore(@RequestBody Map<String, Object> http_request_map,
                                                   HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadContactsByStore(http_request_map=%s)", ""));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);

        Optional<List<WebChatUserEntity>> webchat_user = getBean(WebChatUserAction.class, request)
                .loadAllByStore(store.get(), http_request_map);

        List<Map<String, Object>> list = Lists.newArrayList();
        if (webchat_user.isPresent()) {
            for (WebChatUserEntity $it : webchat_user.get()) list.add($it.toViewMap());
        }
        return wrapperResponse(list);
    }

    // 获取当前登录用户 所在门店的未绑定的微信用户列表
    @RequestMapping(value = "/store/unbild/contacts.json")
    @ResponseBody
    public Map<String, Object> loadUnbildContactsByStore(@RequestBody Map<String, Object> http_request_map,
                                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUnbildContactsByStore(http_request_map=%s)", http_request_map));
        loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);
        Map<String, Object> params = null;
        if (!Strings.isNullOrEmpty(MapUtils.getString(http_request_map, "searchs"))) {
            params = Maps.newHashMap();
            params.put("searchs", MapUtils.getString(http_request_map, "searchs"));
        }
        Optional<List<WebChatUserEntity>> webchat_user = getBean(WebChatUserAction.class, request)
                .loadAllByStore(store.get(), params, false);

        List<Map<String, Object>> list = Lists.newArrayList();
        if (webchat_user.isPresent())
            for (WebChatUserEntity $it : webchat_user.get()) list.add($it.toViewMap());
        return wrapperResponse(list);
    }

    @RequestMapping(value = "/store/build/contacts.json")
    @ResponseBody
    public Map<String, Object> loadBildContactsByStore(@RequestBody Map<String, Object> http_request_map,
                                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBildContactsByStore(http_request_map=%s)", ""));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);

        Optional<List<WebChatUserEntity>> webchat_user = getBean(WebChatUserAction.class, request)
                .loadAllByStore(store.get(), http_request_map, true);

        List<Map<String, Object>> list = Lists.newArrayList();
        if (webchat_user.isPresent())
            for (WebChatUserEntity $it : webchat_user.get()) list.add($it.toViewMap());
        return wrapperResponse(list);
    }

    @RequestMapping(value = "/single/match/members.json")
    @ResponseBody
    public Map<String, Object> loadSingleMatchMember(@RequestBody Map<String, String> http_request_map,
                                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSingleMatchMember(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);

        String weixinId = MapUtils.getString(http_request_map, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "缺少入参 weixinId");
        List<String> weixinIds = Lists.newArrayList();
        weixinIds.add(weixinId);
        List<MatchResultSet> matchResultSet = getBean(WebChatBindMemberServer.class, request)
                .batchMatchMembersByStore(store.get(), weixinIds, userContext);
        return wrapperResponse(matchResultSet.get(0).toSignleMaps());
    }

    // 批量微信绑定会员列表输出操作
    @RequestMapping(value = "/batch/build/match.json")
    @ResponseBody
    public Map<String, Object> batchBuildMatchedMember(@RequestBody Map<String, String> http_request_map,
                                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchBuildMatchedMember(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);

        String matched = MapUtils.getString(http_request_map, "matched");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(matched), "缺少入参 matched");
        getBean(WebChatBindMemberServer.class, request).batchBildMembers(matched, store.get(), userContext);
        return wrapperEmptyResponse();
    }

    // 逐条解除绑定
    @RequestMapping(value = "/batch/unbuild/match.json")
    @ResponseBody
    public Map<String, Object> unBildMembers(@RequestBody Map<String, String> http_request_map,
                                             HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("unBildMembers(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        http_request_map.remove("storeId");
        Preconditions.checkNotNull(storeId, "门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);

        String weixinId = MapUtils.getString(http_request_map, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "缺少入参 weixinId");
        getBean(WebChatBindMemberServer.class, request).unBildMembers(weixinId, store.get(), userContext);
        return wrapperEmptyResponse();
    }

    /**
     * 未绑定微信的好友 通过 分页 请求批量匹配 结果进行展示
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/batch/match/members.json")
    @ResponseBody
    public Map<String, Object> loadBatchMatchMember(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBatchMatchMember(http_request_map=%s)", requestBody));
        LoginUserContext userContext = loadLoginUser(request);

        Integer pageNum = MapUtils.getInteger(requestBody, "pageNum", 0);
        Integer pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkNotNull(storeId, "门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);
        Preconditions.checkState(store.get().hasDevice(), "当前门店尚未绑定微信设备...无法执行后续操作...");
        Map<String, Object> params = Maps.newHashMap();
        String searchs = MapUtils.getString(requestBody, "searchs", null);
        params.put("storeId", store.get().getId());
        params.put("table_name", store.get().getContactTableName());
        params.put("companyId", store.get().getCompanyId().or(-1));
        if (!Strings.isNullOrEmpty(searchs)) {
            params.put("searchs", String.format("%%%s%%", searchs));
        }

        PagingResult pagingResult = queryEngineService.queryForPage("webchat", "load_unbild_store", pageNum, pageSize, params);

        if (pagingResult.getCount() == 0) {
            if (logger.isDebugEnabled())
                logger.debug("当前未关联数量未 0 个...  中止请求....");
            return wrapperResponse(PagingResult.emptyPagingResult("webchat", "load_unbild_store").toMap());
        }

        Optional<List<Map<String, Object>>> datas = pagingResult.getResultSet();
        if (!datas.isPresent())
            return wrapperResponse(PagingResult.emptyPagingResult("webchat", "load_unbild_store").toMap());
        List<String> weixinIds = Lists.newArrayList();
        for (Map<String, Object> map : datas.get()) weixinIds.add(MapUtils.getString(map, "weixinId"));
        if (logger.isDebugEnabled())
            logger.debug(String.format("Total %s weixiner canyu this batch mached...", weixinIds.size()));
        List<MatchResultSet> maps = getBean(WebChatBindMemberServer.class, request)
                .batchMatchMembersByStore(store.get(), weixinIds, userContext);
        List<Map<String, Object>> _temp_map = Lists.newArrayList();
        for (MatchResultSet $it : maps) _temp_map.add($it.toBatchMaps());
        PagingResult pg = new PagingResult(pagingResult.getModel(), pagingResult.getStmtId(), pagingResult.getCount(),
                pagingResult.getPageNum(), pagingResult.getPageSize(), _temp_map);
        return wrapperResponse(pg.toMap());
    }

    @RequestMapping(value = "/unbind/wechat/search.json")
    @ResponseBody
    public Map<String, Object> loadUnbindSearch(@RequestBody Map<String, String> http_request_map,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUnbindSearch(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Optional<StoreEntity> store = userContext.getStore();
        Preconditions.checkState(store.isPresent(), "当前用户无门店信息，禁止后续操作...");

        String search = MapUtils.getString(http_request_map, "search");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(search), "搜索内容不可以为空值...");
        Map<String, Object> param = Maps.newHashMap();
        param.put("searchs", search);
        Optional<List<WebChatUserEntity>> maps = getBean(WebChatUserAction.class, request)
                .loadAllByStore(store.get(), param, false);
        if (!maps.isPresent()) return wrapperEmptyResponse();
        List<Map<String, Object>> list = Lists.newArrayList();
        for (WebChatUserEntity $it : maps.get()) {
            list.add($it.toViewMap());
        }
        return wrapperResponse(list);
    }

    @RequestMapping(value = "/batch/match/wechat.json")
    @ResponseBody
    public Map<String, Object> loadBatchMatchWechat(@RequestBody Map<String, String> http_request_map,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadBatchMatchWechat(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getStore().isPresent(), "当前用户无门店信息，无法执行该操作...");
        Integer memberId = MapUtils.getInteger(http_request_map, "memberId");
        Preconditions.checkNotNull(memberId, "会员ID可以为空值...");
        Optional<List<WechatMatchMemberDto>> wechats = getBean(WebChatBindMemberServer.class, request)
                .memberMatchWechats(userContext.getStore().get(), memberId);
        if (!wechats.isPresent()) return wrapperEmptyResponse();
        List<Map<String, Object>> list = Lists.newArrayList();
        for (WechatMatchMemberDto $it : wechats.get()) {
            Map<String, Object> res = $it.getWebChatUser().toViewMap();
            res.put("distance", $it.getDistance());
            list.add(res);
        }
        return wrapperResponse(list);
    }

    @RequestMapping(value = "/message/batch/send.json")
    @ResponseBody
    public Map<String, Object> batchSendMsgAction(@RequestBody Map<String, String> http_request_map,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSendMsgAction(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkNotNull(storeId, "门店ID不可以为空");
        String weixinIds = MapUtils.getString(http_request_map, "weixinIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinIds), "缺少入参 weixinIds");
        List<String> wxids = Splitter.on(',').splitToList(weixinIds);
        String msgtext = MapUtils.getString(http_request_map, "msgtext");
        String imageUrls = MapUtils.getString(http_request_map, "imageUrls");
        Long msgTempId = MapUtils.getLong(http_request_map, "msgTempId");
        String[] imgs = Strings.isNullOrEmpty(imageUrls) ? null : StringUtils.split(imageUrls, ',');
        if (Strings.isNullOrEmpty(msgtext) && Strings.isNullOrEmpty(imageUrls))
            throw new IllegalArgumentException("消息内容缺少文本或者图片信息，请检查发送内容.");
        getBean(WebChatSendLogServer.class, request).sendWebChatMsg(msgtext, imgs, wxids, storeId, msgTempId, userContext);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/message/resend/intoday.json")
    @ResponseBody
    public Map<String, Object> reSendByFaileInToday(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("reSendByFaileInToday(url=%s)", request.getRequestURL()));
        LoginUserContext userContext = loadLoginUser(request);
        getBean(SendMsgEntityAction.class, request).sendByFailsInToday(userContext);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/load/send/detail.json")
    @ResponseBody
    public Map<String, Object> loadSendDetailAction(@RequestBody Map<String, String> http_request_map,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSendDetailAction(http_request_map=%s)", http_request_map));
        loadLoginUser(request);
        String batchNo = MapUtils.getString(http_request_map, "batchNo");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(batchNo), "缺少入参 batchNo");
        Map<String, Object> params = Maps.newHashMap();
        params.put("batchNo", batchNo);
        int flag = MapUtils.getIntValue(http_request_map, "flag");
        Preconditions.checkArgument(flag == -2 || flag == 1, "缺少入参 flag 取值信息为(1,-2)");
        params.put("flag", flag);

        // int pageNum, int pageSize
        String pageNum = MapUtils.getString(http_request_map, "pageNum");
        String pageSize = MapUtils.getString(http_request_map, "pageSize");

        PagingResult pagingResult = queryEngineService.queryForPage("webchat", "msgdetail", Integer.valueOf(pageNum),
                Integer.valueOf(pageSize), params);

        return wrapperResponse(pagingResult.toMap());
    }

    @RequestMapping(value = "/send/friend.json")
    @ResponseBody
    public Map<String, Object> sendFriendAction(@RequestBody Map<String, String> http_request_map,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("sendFriendAction(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);

        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkNotNull(storeId, "待加载的门店ID不可以为空....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);
        Preconditions.checkState(store.get().hasDevice(), "门店%s尚未绑定微信终端设备.",
                store.get().getName());
        String msgtxt = MapUtils.getString(http_request_map, "msgtxt");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(msgtxt), "缺少入参 msgtxt");
        getBean(SendMsgEntityAction.class, request).sendFrieds(msgtxt, userContext);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/hischat/page.json")
    @ResponseBody
    public Map<String, Object> hischatQueryAction(@RequestBody Map<String, String> http_request_map,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("hischatQueryAction(http_request_map=%s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getCompany().isPresent(), "当前公司无公司属性...");
        Preconditions.checkState(userContext.getMaxPowerRole().isPresent(), "当前登陆账号无角色绑定，无法执行该操作...");
        StoreEntity store = null;
        if (!userContext.hasDianZhangRole()) {
            Integer storeId = MapUtils.getInteger(http_request_map, "storeIds");
            Preconditions.checkNotNull(storeId, "入参 storeIds 不可以未空，请指定门店...");
            Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class, request).findById(storeId);
            Preconditions.checkState(storeOpt.isPresent(), "id=%s对应的门店实体不存在....", storeId);
            Preconditions.checkState(storeOpt.get().hasDevice(), "该门店尚未绑定设备");
            Optional<WxMsgWhiteListEntity> whiteList = getBean(WxMsgWhiteListAction.class, request).findByStore(storeOpt.get());
            if (!whiteList.isPresent() || !whiteList.get().isPassLoginUser(userContext)) {
                return wrapperResponse(null);
            }
            store = storeOpt.get();
        } else {
            Preconditions.checkState(userContext.getStore().isPresent(), "当前账户为店长角色，但未指定归属门店....");
            store = userContext.getStore().get();
        }

        int chatType = MapUtils.getIntValue(http_request_map, "chatType");
        Preconditions.checkArgument(chatType == 1 || chatType == 2, "非法的chatTypeType 取值....,合法取值为(1,2)");
        String pageNum = http_request_map.remove("pageNum");
        String pageSize = http_request_map.remove("pageSize");
        Preconditions.checkArgument(NumberUtils.isNumber(pageNum), "非法的入参 pageNum=%s", pageNum);
        Preconditions.checkArgument(NumberUtils.isNumber(pageSize), "非法的入参 pageSize=%s", pageSize);

        Optional<Set<Integer>> mystoreIds = userContext.getSubStoreIds();
        if (!mystoreIds.isPresent()) {
            if (logger.isWarnEnabled())
                logger.warn(String.format("当前登陆用户%s 名下无管辖门店，无法执行该操作...", userContext.getUsername()));
            return wrapperResponse(PagingResult.toEmpty());
        }

        Map<String, Object> params = userContext.toMap();
        params.put("companyId", userContext.getCompany().get().getId());
        params.put("strDate", null);
        params.put("chatType", chatType);
        params.put("keywords", null);
        params.put("storeIds", new Integer[]{store.getId()});
        // 替换系统变量-适应选择不同的门店
        params.put("STORE_DEVICEID", store.getContactTableName() != null ? store.getContactTableName() : "NO_DEVICE");
        params.put("MSG_COM_STORE", String.format("MSG_%s_%s", userContext.getCompany().get().getId(), store.getId()));

        String actionTag = MapUtils.getString(http_request_map, "actionTag");
        if (!Strings.isNullOrEmpty(actionTag))
            params.put("actionTag", actionTag);

        String strDate = MapUtils.getString(http_request_map, "strDate");
        String endDate = MapUtils.getString(http_request_map, "endDate");

        if (!Strings.isNullOrEmpty(strDate)) {
            params.put("strDate", strDate);
            params.put("endDate", endDate);
        }

        if (!Strings.isNullOrEmpty(MapUtils.getString(http_request_map, "keywords"))) {
            params.put("keywords", String.format("%%%s%%", MapUtils.getString(http_request_map, "keywords")));
        }

        PagingResult pagingResult = queryEngineService.queryForPage("hischat", "detail",
                Integer.valueOf(pageNum), Integer.valueOf(pageSize), params);

        return wrapperResponse(pagingResult.toMap());
    }

    @RequestMapping(value = "/manager/amount.json")
    @ResponseBody
    public Map<String, Object> wechatMsgAmount(@RequestBody Map<String, String> http_request_map,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("wechatMsgAmount( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Map<String, Object> params = userContext.toMap();
        Preconditions.checkArgument(http_request_map.containsKey("storeIds"), "请求参数缺少 storeIds");
        String storeIdsStr = MapUtils.getString(http_request_map, "storeIds");
        if (Strings.isNullOrEmpty(storeIdsStr.trim())) return wrapperEmptyResponse();
        List<Integer> storeIds = Lists.newArrayList();
        for (String storeId : StringUtils.split(storeIdsStr, ','))
            storeIds.add(Integer.parseInt(storeId));
        params.put("storeIds", storeIds);
        Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class, request).findByIds(storeIds);
        Preconditions.checkState(stores.isPresent(), String.format("门店IDS[%s] 无门店信息", storeIds));
        List<String> tableNames = Lists.newArrayList();
        for (StoreEntity store : stores.get())
            tableNames.add(store.getContactTableName());
        params.put("tableNames", tableNames);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("webchat", "amount_manage", params);
        Map<String, Object> amount = Maps.newHashMap();
        amount.put("weixinAmount", 0);
        amount.put("memberAmount", 0);
        amount.put("unbildWxAmount", 0);
        amount.put("bildWxAmount", 0);
        amount.put("deleteWxAmount", 0);

        if (query_res.isPresent())
            for (Map<String, Object> $it : query_res.get()) {
                if (StringUtils.equals("weixin", MapUtils.getString($it, "type"))) {
                    amount.put("weixinAmount", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("member", MapUtils.getString($it, "type"))) {
                    amount.put("memberAmount", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("bilds", MapUtils.getString($it, "type"))) {
                    amount.put("bildWxAmount", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("delete", MapUtils.getString($it, "type"))) {
                    amount.put("deleteWxAmount", MapUtils.getIntValue($it, "amount", 0));
                }
            }
        amount.put("unbildWxAmount", MapUtils.getIntValue(amount, "memberAmount", 0) -
                MapUtils.getIntValue(amount, "bildWxAmount", 0));
        return wrapperResponse(amount);
    }

    /**
     * 好友统计
     *
     * @param http_request_map
     * @param request
     * @return
     */
    @RequestMapping(value = "/friend/statistic.json")
    @ResponseBody
    public Map<String, Object> wechatFriendAllCount(@RequestBody Map<String, String> http_request_map,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("wechatFriendAllCount( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument((http_request_map.containsKey("beginTime")
                && http_request_map.containsKey("endTime")), "入参beginTime或endTime不能为空");
        Map<String, Object> params = tranToFriendStatisticMap(http_request_map, userContext);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("webchat", "friend_all_count", params);
        if (!query_res.isPresent()) return wrapperEmptyResponse();
        return wrapperResponse(query_res.get());
    }

    @RequestMapping(value = "/userinfo/bymember.json")
    @ResponseBody
    public Map<String, Object> loadWechatByMember(@RequestBody Map<String, String> requestBody,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadWechatByMember( http_request_map = %s)", request.getRequestURI(), requestBody));
        loadLoginUser(request);
        int storeId = MapUtils.getIntValue(requestBody, "storeId", -1);
        int memberId = MapUtils.getIntValue(requestBody, "memberId", -1);
        Preconditions.checkState(storeId != -1, "请指定门店ID");
        Preconditions.checkState(memberId != -1, "请指定会员ID");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent());
        java.util.Optional<MemberEntity> member = getBean(MemberEntityAction.class, request).findMemberById(store.get(), memberId);
        Preconditions.checkState(member.isPresent(), "id=%s 对应的会员不存在...", memberId);
        Preconditions.checkState(member.get().getStoreId().isPresent(), "会员信息异常,会员无门店信息");
        if (member.get().getStoreId().get().equals(storeId)) {
            Optional<WebChatUserEntity> webChat = getBean(WebChatUserAction.class, request)
                    .findByMember(store.get(), member.get());
            if (webChat.isPresent()) {
                Optional<WebChatUserEntity> orginWeixin = getBean(WebChatUserAction.class, request).findOrginById(store.get(), webChat.get().getUserName());
                Map<String, Object> map = Maps.newHashMap();
                map.put("weixinId", webChat.isPresent() ? webChat.get().getUserName() : "");
                map.put("nickName", webChat.get().getNickName().isPresent() ? webChat.get().getNickName().get() : "");
                if (orginWeixin.isPresent()) {
                    map.put("wxNickName", orginWeixin.get().getNickName().isPresent() ? orginWeixin.get().getNickName().get() : "");
                } else {
                    map.put("wxNickName", "");
                }
                map.put("iconUrl", webChat.get().getIconUrl().isPresent() ? webChat.get().getIconUrl().get() : "");
                map.put("conRemark", webChat.get().getConRemark().isPresent() ? webChat.get().getConRemark().get() : "");
                return wrapperResponse(map);
            }
        }
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/member/byweixin.json")
    @ResponseBody
    public Map<String, Object> loadMemberByWeixinId(@RequestBody Map<String, String> requestBody,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadMemberByWeixinId( http_request_map = %s)", request.getRequestURI(), requestBody));
        loadLoginUser(request);
        Integer storeId = MapUtils.getIntValue(requestBody, "storeId");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Preconditions.checkNotNull(storeId, "请指定门店ID");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "请指定会员ID");
        java.util.Optional<MemberEntity> member = getBean(WebChatBindMemberServer.class, request).loadMemberByWeixinId(storeId, weixinId);
        return wrapperResponse(member.isPresent() ? member.get().toViewMap() : null);
    }

    /**
     * 将请求参数变换好友统计需要的MAP
     *
     * @param http_request_map
     * @param userContext
     * @return
     */
    private Map<String, Object> tranToFriendStatisticMap(Map<String, String> http_request_map, LoginUserContext userContext) {
        Map<String, Object> params = userContext.toMap();
        String storeIds = MapUtils.getString(http_request_map, "storeIds");
        if (!Strings.isNullOrEmpty(storeIds))
            params.put("storeIds", Lists.newArrayList(storeIds.split(",")));
        String beginTime = MapUtils.getString(http_request_map, "beginTime");
        String endTime = MapUtils.getString(http_request_map, "endTime");
        checkTime(beginTime, endTime);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        return params;
    }

    /**
     * 检验时间
     *
     * @param beginTime
     * @param endTime
     */
    private void checkTime(String beginTime, String endTime) {
        try {
            Date beginDate = DAY_FORMAT.parse(beginTime);
            Date endDate = DAY_FORMAT.parse(endTime);
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTime(beginDate);
            lastDate.add(Calendar.DATE, 31);
            Preconditions.checkState(endDate.before(lastDate.getTime()), "查询时间跨度不能超过30天");
        } catch (ParseException e) {
            throw new IllegalStateException("时间格式不正确");
        }
    }

    /**
     * 好友详细统计
     *
     * @param http_request_map
     * @param request
     * @return
     */
    @RequestMapping(value = "/friend/item.json")
    @ResponseBody
    public Map<String, Object> wechatFriendDetailCount(@RequestBody Map<String, String> http_request_map,
                                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("wechatFriendAllCount( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument((http_request_map.containsKey("beginTime")
                && http_request_map.containsKey("endTime")), "入参beginTime或endTime不能为空");
        Map<String, Object> params = tranToFriendStatisticMap(http_request_map, userContext);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("webchat", "friend_detail_count", params);
        if (!query_res.isPresent()) return wrapperEmptyResponse();
        return wrapperResponse(query_res.get());
    }

    @RequestMapping(value = "/load/withmember.json")
    @ResponseBody
    public Map<String, Object> loadWeixinwithMember(@RequestBody Map<String, String> http_request_map,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("LoadWeixinwithMember( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkArgument(http_request_map.containsKey("storeId") && http_request_map.containsKey("weixinId"),
                "请求参数不能缺少storeId或weixinId");
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkArgument(storeId != null, "storeId不能为空");
        String weixinId = MapUtils.getString(http_request_map, "weixinId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(weixinId), "weixinId不能为空");
        StoreEntity store = getBean(StoreEntityAction.class, request).loadById(storeId);
        Optional<WebChatUserEntity> weixinOpt = getBean(WebChatUserAction.class, request).findOrginById(store, weixinId);
        Preconditions.checkState(weixinOpt.isPresent(), String.format("该门店[%s]无微信[%s]", storeId, weixinId));
        WebChatUserEntity weixin = weixinOpt.get();
        Map<String, Object> resultMap = Maps.newHashMap();
        resultMap.put("weixin", weixin.toViewMap());
        if (!weixin.hasMember()) return wrapperResponse(resultMap);
        java.util.Optional<MemberEntity> memberOpt = getBean(WebChatBindMemberServer.class, request).loadMemberByWeixinId(storeId, weixinId);
        Preconditions.checkState(memberOpt.isPresent(), String.format("门店[%s]微信账号[%s] 无绑定会员", storeId, weixinId));
        resultMap.put("member", memberOpt.get().toViewMap());
        return wrapperResponse(resultMap);
    }

    /**
     * @param http_request_map
     * @param request
     * @return
     */
    @RequestMapping(value = "/load/signEmployee.json")
    @ResponseBody
    public Map<String, Object> loadSignEmployee(@RequestBody Map<String, String> http_request_map,
                                                HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        String weixinId = MapUtils.getString(http_request_map, "weixinId", null);
        Optional<EmployeeEntity> employeeOpt = getBean(WechatFriendService.class, request).findBeSignedEmployee(loginUser, weixinId);
        if (!employeeOpt.isPresent()) return wrapperEmptyResponse();
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", employeeOpt.get().getId());
        map.put("name", employeeOpt.get().getUserName());
        return wrapperResponse(map);
    }

    @Resource
    private QueryEngineService queryEngineService;
    @Resource
    private BaseModelServer baseAdapterServer;

}
