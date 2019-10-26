package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;

import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.query.entity.PagingResult;
import com.csosm.module.webchat.entity.WechatAddFriendListAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

@Controller(value = "wechatFriendController")
@RequestMapping("/whfriend")
public class WechatFriendController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(WechatFriendController.class);

    @RequestMapping(value = "/member/list.json")
    @ResponseBody
    public Map<String, Object> queryMemberList(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,queryMemberList(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkState(user.getCompany().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        String storeIds = MapUtils.getString(requestBody, "storeIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "至少指定一家门店...");
        Optional<List<StoreEntity>> stores = getBean(BaseModelServer.class, request)
                .loadAllSubStoreByOrg(Integer.valueOf(storeIds.substring(4)), StringUtils.startsWith(storeIds, "org_"),
                        user.getCompany().get().getId());
        Preconditions.checkState(stores.isPresent(), "storeIds=%s 对应的实体门店不存在...", storeIds);
        List<Integer> st_ids = Lists.newArrayList();
        for (StoreEntity st : stores.get()) st_ids.add(st.getId());
        params.put("storeIds", st_ids);
        buildRange(requestBody, "lastVisitDays", 1, params);
        buildRange(requestBody, "joinDays", 0, params);
        if (!requestBody.containsKey("lastVisitDays"))
            buildRange(requestBody, "notVisitDays", 1, params);
        buildRange(requestBody, "rechargeAmount", 1, params);
        buildRange(requestBody, "c_py_buy_amount02", 1, params);
        buildRange(requestBody, "c_ty_buy_freq", 1, params);
        buildRange(requestBody, "c_ty_buy_avg_p_o_p02", 1, params);
        buildRange(requestBody, "joinDays", 1, params);
        buildRange(requestBody, "totalScore", 1, params);
        buildRange(requestBody, "addNums", 1, params);
        PagingResult pagingResult = queryEngineService.queryForPage("WechatAddFriend", "member_list",
                MapUtils.getIntValue(requestBody, "pageNum", 0),
                MapUtils.getIntValue(requestBody, "pageSize", 10), params);
        return wrapperResponse(pagingResult.toMap());
    }

    @RequestMapping(value = "/push/list.json")
    @ResponseBody
    public Map<String, Object> pushMemberList(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,pushMemberList(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkState(user.getCompany().isPresent());
        Map<String, Object> params = Maps.newHashMap();
        String storeIds = MapUtils.getString(requestBody, "storeIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "至少指定一家门店...");
        Optional<List<StoreEntity>> stores = getBean(BaseModelServer.class, request)
                .loadAllSubStoreByOrg(Integer.valueOf(storeIds.substring(4)), StringUtils.startsWith(storeIds, "org_"),
                        user.getCompany().get().getId());
        Preconditions.checkState(stores.isPresent(), "storeIds=%s 对应的实体门店不存在...", storeIds);
        List<Integer> st_ids = Lists.newArrayList();
        for (StoreEntity st : stores.get()) st_ids.add(st.getId());
        params.put("storeIds", st_ids);
        buildRange(requestBody, "createDate", 0, params);
        buildRange(requestBody, "pushDate", 0, params);
        String status = MapUtils.getString(requestBody, "status");
        if (!Strings.isNullOrEmpty(status)) params.put("status", StringUtils.split(status, ','));

        PagingResult pagingResult = queryEngineService.queryForPage("WechatAddFriend", "push_list",
                MapUtils.getIntValue(requestBody, "pageNum", 0),
                MapUtils.getIntValue(requestBody, "pageSize", 10), params);
        return wrapperResponse(pagingResult.toMap());
    }

    private void buildRange(Map<String, String> requestBody, String key, int type, Map<String, Object> param) {
        String val = MapUtils.getString(requestBody, key);
        if (Strings.isNullOrEmpty(val)) return;
        param.put(key, val);
        String[] args = StringUtils.split(val, ',');
        Preconditions.checkArgument(args.length == 2, "非法的范围取值:%s", val);
        if (1 == type) {
            param.put(String.format("%s_start", key), Integer.valueOf(args[0]));
            param.put(String.format("%s_end", key), Integer.valueOf(args[1]));
        } else if (0 == type) {
            param.put(String.format("%s_start", key), args[0]);
            param.put(String.format("%s_end", key), args[1]);
        }
    }

    @RequestMapping(value = "/bymembers/add.json")
    @ResponseBody
    public Map<String, Object> addByMember(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,addByMember(requestBody=%s)", request.getRequestURI(), requestBody));
//        LoginUserContext user = loadLoginUser(request);
//        Preconditions.checkState(user.getCompany().isPresent());
//        String memberIds = MapUtils.getString(requestBody, "memberIds");
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(memberIds), "至少指定一名成员...");
//        List<Integer> m_ids = Lists.newArrayList();
//        for (String $it : StringUtils.split(memberIds, ',')) m_ids.add(Integer.valueOf($it));
//        Optional<List<MemberEntity>> members = memberEntityAction.findByIds(m_ids, true, true);
//        Preconditions.checkState(members.isPresent(), "%s 对应的含有电话号码的账户不存在", memberIds);
//        // 检查门店是否绑定daV手机
//        Set<Integer> storeIds = Sets.newHashSet();
//        for (MemberEntity $it : members.get()) {
//            Preconditions.checkState($it.getStoreId().isPresent(), "会员 %s 无门店信息，无法执行该业务...", $it.getName());
//            storeIds.add($it.getStoreId().get());
//            Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class, request).findByIds(storeIds);
//            Preconditions.checkState(stores.isPresent() && storeIds.size() == stores.get().size(),
//                    "数据异常，会员对应的门店缺失...");
//            for (StoreEntity $st : stores.get()) {
//                Preconditions.checkState($st.getDeviceId().isPresent(), "会员%s所在的门店%s尚未绑定daV设备，无法执行该业务...");
//            }
//        }
//        wechatAddFriendListAction.batchInsertByMember(members.get(), user);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/byselect/add.json")
    @ResponseBody
    public Map<String, Object> addBySelect(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,addByMember(requestBody=%s)", request.getRequestURI(), requestBody));
//        LoginUserContext user = loadLoginUser(request);
//        Preconditions.checkState(user.getCompany().isPresent());
//        Map<String, Object> params = Maps.newHashMap();
//        String storeIds = MapUtils.getString(requestBody, "storeIds");
//        Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "至少指定一家门店...");
//        Optional<List<StoreEntity>> stores = getBean(BaseModelServer.class, request)
//                .loadAllSubStoreByOrg(Integer.valueOf(storeIds.substring(4)), StringUtils.startsWith(storeIds, "org_"),
//                        user.getCompany().get().getId());
//        Preconditions.checkState(stores.isPresent(), "storeIds=%s 对应的实体门店不存在...", storeIds);
//        for (StoreEntity $st : stores.get()) {
//            Preconditions.checkState($st.getDeviceId().isPresent(), "门店%s尚未绑定daV设备，无法执行该业务...");
//        }
//
//        Set<Integer> st_ids = Sets.newHashSet();
//        for (StoreEntity st : stores.get()) st_ids.add(st.getId());
//
//        params.put("storeIds", st_ids);
//        buildRange(requestBody, "lastVisitDays", 1, params);
//        buildRange(requestBody, "joinDays", 0, params);
//        if (!requestBody.containsKey("lastVisitDays"))
//            buildRange(requestBody, "notVisitDays", 1, params);
//        buildRange(requestBody, "rechargeAmount", 1, params);
//        buildRange(requestBody, "c_py_buy_amount02", 1, params);
//        buildRange(requestBody, "c_ty_buy_freq", 1, params);
//        buildRange(requestBody, "c_ty_buy_avg_p_o_p02", 1, params);
//        buildRange(requestBody, "joinDays", 1, params);
//        buildRange(requestBody, "totalScore", 1, params);
//        buildRange(requestBody, "addNums", 1, params);
//        wechatAddFriendListAction.batchInsertByQuery(user, params);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/today/pushed.json")
    @ResponseBody
    public Map<String, Object> loadTodayPusnList(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,loadTodayPusnList(requestBody=%s)", request.getRequestURI(), requestBody));
        super.loadLoginUser(request);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkNotNull(storeId, "入参门店ID不可以为空...");
        java.util.Optional<List<MemberEntity>> memebers = getBean(WechatFriendService.class, request)
                .loadTodayByStore(storeId);
        if (memebers.isPresent()) {
            List<Map<String, Object>> data = Lists.newArrayList();
            for (MemberEntity $it : memebers.get()) {
                Map<String, Object> dt = Maps.newHashMap();
                dt.put("name", $it.getName());
                dt.put("phoneNo", $it.getMobilephone());
                data.add(dt);
            }
            return wrapperResponse(data);
        }
        return wrapperResponse(new int[0]);
    }

    @RequestMapping(value = "/bindWeixin/callback.json")
    @ResponseBody
    public Map<String, Object> callback(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,callback(requestBody=%s)", request.getRequestURI(), requestBody));
        Preconditions.checkArgument(requestBody.containsKey("deviceId"), "请求参数中缺少deviceId");
        Preconditions.checkArgument(requestBody.containsKey("companyId"), "请求参数中缺少companyId");
        Preconditions.checkArgument(requestBody.containsKey("phoneNo"), "请求参数中缺少phoneNo");
        Preconditions.checkArgument(requestBody.containsKey("status"), "请求参数中缺少status");
        String deviceId = MapUtils.getString(requestBody, "deviceId");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        String phone = MapUtils.getString(requestBody, "phoneNo");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Integer status = MapUtils.getInteger(requestBody, "status");
        wechatService.modifyStatus(companyId, deviceId, weixinId, phone, status);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/config/info.json")
    @ResponseBody
    public Map<String, Object> loadConfigInformation(@RequestBody Map<String, String> requestBody,
                                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,loadConfigInformation(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("storeIds"), "请求参数缺少storeIds");
        String storeIdsStr = MapUtils.getString(requestBody, "storeIds");
        List<Integer> storeIds = getStoreIds(storeIdsStr);
        if (storeIds.isEmpty()) return wrapperEmptyResponse();
        Map<String, Object> info = wechatService.loadConfigInformation(storeIds, user);
        return wrapperResponse(info);
    }


    private List<Integer> getStoreIds(String storeIdsStr) {
        String[] storeIds = storeIdsStr.split(",");
        List<Integer> list = Lists.newArrayList();
        if (storeIds.length == 0) return list;
        for (int i = 0; i < storeIds.length; i++) {
            list.add(Integer.valueOf(storeIds[i]));
        }
        return list;
    }

    @RequestMapping(value = "/config/create.json")
    @ResponseBody
    public Map<String, Object> autoCreateConfig(@RequestBody Map<String, String> requestBody,
                                                HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,autoCreateConfig(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        checkAutoCreateConfigRequest(requestBody);
        String storeIdsStr = MapUtils.getString(requestBody, "storeIds");
        List<Integer> storeIds = getStoreIds(storeIdsStr);
        Preconditions.checkState(!storeIds.isEmpty(), "storeIds不能不为空");
        Integer suitType = MapUtils.getInteger(requestBody, "suitType");
        Integer enable = MapUtils.getInteger(requestBody, "enable");
        String content = MapUtils.getString(requestBody, "content");
        if (!Strings.isNullOrEmpty(content))
            Preconditions.checkArgument(content.length() < 50, "提示信息长度不能超过50");
        wechatService.createPushConfigs(storeIds, suitType, enable, content, user);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/{type}/statistics.json")
    @ResponseBody
    public Map<String, Object> count(@PathVariable String type, @RequestBody Map<String, String> requestBody,
                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,count(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("storeIds"), "请求参数缺少storeIds");
        String storeIdsStr = MapUtils.getString(requestBody, "storeIds");
        List<Integer> storeIds = getStoreIds(storeIdsStr);
        Preconditions.checkState(user.getCompany().isPresent(), "登录用户无公司信息");
        Integer companyId = user.getCompany().get().getId();
        String beginTime = MapUtils.getString(requestBody, "beginTime");
        String endTime = MapUtils.getString(requestBody, "endTime");
        if (storeIds.isEmpty()) return wrapperResponse(new String[0]);
        if (type.trim().equals("all")) return allStoreStatistics(storeIds, companyId);
        if (type.trim().equals("store")) return storeStatistics(storeIds, companyId, beginTime, endTime);
        if (type.trim().equals("items")) {
            int pageNum = MapUtils.getIntValue(requestBody, "pageNum", 0);
            int pageSize = MapUtils.getIntValue(requestBody, "pageSize", 10);
            Preconditions.checkState(storeIds.size() == 1, "请求参数storeIds只能设置一个值");
            return storeItemsStatistics(storeIds.get(0), companyId, beginTime, endTime, pageNum, pageSize);
        }
        return wrapperEmptyResponse();
    }

    private Map<String, Object> allStoreStatistics(List<Integer> storeIds, Integer companyId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("companyId", companyId);
        Optional<Map<String, Object>> optional = queryEngineService.queryForMap("WechatAddFriend", "add_member_all_statistics", params);
        if (!optional.isPresent()) return wrapperResponse(new String[0]);
        return wrapperResponse(optional.get());
    }

    private Map<String, Object> storeStatistics(List<Integer> storeIds, Integer companyId, String beginTime, String endTime) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeIds", storeIds);
        params.put("companyId", companyId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        Optional<List<Map<String, Object>>> optional = queryEngineService.queryForList("WechatAddFriend", "add_member_store_statistics", params);
        if (!optional.isPresent()) return wrapperResponse(new String[0]);
        return wrapperResponse(optional.get());
    }

    private Map<String, Object> storeItemsStatistics(Integer storeId, Integer companyId, String beginTime, String endTime, int pageNum, int pageSize) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", storeId);
        params.put("companyId", companyId);
        params.put("beginTime", beginTime);
        params.put("endTime", endTime);
        PagingResult pagingResult = queryEngineService.queryForPage("WechatAddFriend", "add_member_items_statistics", pageNum, pageSize,
                params);
        return wrapperResponse(pagingResult.toMap());
    }

    private void checkAutoCreateConfigRequest(Map<String, String> requestBody) {
        Preconditions.checkArgument(requestBody.containsKey("storeIds"), "请求参数缺少storeIds");
        Preconditions.checkArgument(requestBody.containsKey("suitType"), "请求参数缺少suitType");
        Preconditions.checkArgument(requestBody.containsKey("enable"), "请求参数缺少enable");
        Preconditions.checkArgument(requestBody.containsKey("content"), "请求参数缺少content");
    }


    @Resource
    private WechatAddFriendListAction wechatAddFriendListAction;
    @Resource
    private MemberEntityAction memberEntityAction;
    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;
    @Resource
    private WechatFriendService wechatService;
}
