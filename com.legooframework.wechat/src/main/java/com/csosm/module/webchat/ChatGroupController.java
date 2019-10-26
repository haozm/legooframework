package com.csosm.module.webchat;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.BaseModelServer;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.webchat.entity.ChatRoomContactEntity;
import com.csosm.module.webchat.entity.ChatRoomContactEntityAction;
import com.csosm.module.webchat.entity.ChatRoomDto;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller(value = "chatGroupController")
@RequestMapping("/chatgroup")
public class ChatGroupController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ChatGroupController.class);

    @RequestMapping(value = "/bystores/list.json")
    @ResponseBody
    public Map<String, Object> listByStores(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,listByStores(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        Preconditions.checkState(userContext.getCompany().isPresent());
        String store_ids = MapUtils.getString(requestBody, "storeIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(store_ids), "至少指定一家门店Id...");
        // Optional<List<StoreEntity>> loadAllSubStoreByOrg(Integer id, boolean isOrg, Integer companyId)
        Optional<List<StoreEntity>> stores = getBean(BaseModelServer.class, request)
                .loadAllSubStoreByOrg(Integer.valueOf(store_ids.substring(4)), StringUtils.startsWith(store_ids, "org_"),
                        userContext.getCompany().get().getId());
        Preconditions.checkState(stores.isPresent(), "storeIds=%s 对应的实体门店不存在...", store_ids);
        Optional<List<ChatRoomContactEntity>> chatrooms = chatRoomContactEntityAction.findAllByStores(stores.get());
        if (chatrooms.isPresent()) {
            List<Map<String, String>> rooms = Lists.newArrayList();
            for (ChatRoomContactEntity $it : Sets.newHashSet(chatrooms.get())) {
                ChatRoomDto cur = $it.toChatRoomDto();
                rooms.add(cur.toTreeNode());
            }
            return wrapperResponse(rooms);
        }
        return wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/total/amount.json")
    @ResponseBody
    public Map<String, Object> totalAmount(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,totalAmount(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        Optional<OrganizationEntity> company = userContext.getCompany();
        Preconditions.checkState(company.isPresent(), "当前登陆用户无公司信息...");
        String names = MapUtils.getString(requestBody, "names");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(names), "至少指定一个聊天群组...");
        List<String> ids = Lists.newArrayList(StringUtils.split(names, ','));
        Optional<List<ChatRoomContactEntity>> chat_rooms_opt = chatRoomContactEntityAction.findAllByNames(ids, company.get());
        Map<String, Object> amount = Maps.newHashMap();
        amount.put("chatRoomSize", 0);
        amount.put("allUserSize", 0);
        amount.put("distinctUserSize", 0);
        amount.put("dayAddSize", 0);
        amount.put("weekAddSize", 0);
        amount.put("monthAddSize", 0);
        amount.put("dayDelSize", 0);
        amount.put("weekDelSize", 0);
        amount.put("monthDelSize", 0);
        amount.put("activeSize", 0);
        Set<ChatRoomContactEntity.Member> set = Sets.newHashSet();
        if (chat_rooms_opt.isPresent()) {
            List<ChatRoomContactEntity> chat_rooms = chat_rooms_opt.get();
            amount.put("chatRoomSize", chat_rooms.size());
            List<String> roomNames = Lists.newArrayList();
            for (ChatRoomContactEntity $it : chat_rooms) {
                amount.put("allUserSize", MapUtils.getIntValue(amount, "allUserSize") + $it.getSize());
                set.addAll($it.getMembers());
                roomNames.add($it.getId());
            }
            amount.put("distinctUserSize", set.size());
            Map<String, Object> params = Maps.newHashMap();
            params.put("roomNames", roomNames);
            Optional<List<Map<String, Object>>> amount_maps = queryEngineService
                    .queryForList("WxChatGroup", "amount_member_nums", params);
            if (amount_maps.isPresent()) {
                for (Map<String, Object> $it : amount_maps.get()) {
                    String ranges = MapUtils.getString($it, "ranges");
                    if ("days".equals(ranges)) {
                        amount.put("dayAddSize", MapUtils.getIntValue(amount, "dayAddSize") + MapUtils.getIntValue($it, "addSize"));
                        amount.put("dayDelSize", MapUtils.getIntValue(amount, "dayDelSize") + MapUtils.getIntValue($it, "removeSize"));
                        amount.put("dayActiveSize", MapUtils.getFloatValue(amount, "dayActiveSize") + MapUtils.getFloatValue($it, "activeSize"));
                    } else if ("weeks".equals(ranges)) {
                        amount.put("weekAddSize", MapUtils.getIntValue(amount, "weekAddSize") + MapUtils.getIntValue($it, "addSize"));
                        amount.put("weekDelSize", MapUtils.getIntValue(amount, "weekDelSize") + MapUtils.getIntValue($it, "removeSize"));
                        amount.put("weekActiveSize", MapUtils.getFloatValue(amount, "weekActiveSize") + MapUtils.getFloatValue($it, "activeSize"));
                    } else if ("months".equals(ranges)) {
                        amount.put("monthAddSize", MapUtils.getIntValue(amount, "monthAddSize") + MapUtils.getIntValue($it, "addSize"));
                        amount.put("monthDelSize", MapUtils.getIntValue(amount, "monthDelSize") + MapUtils.getIntValue($it, "removeSize"));
                        amount.put("monthActiveSize", MapUtils.getFloatValue(amount, "monthActiveSize") + MapUtils.getFloatValue($it, "activeSize"));
                    }
                }
            }
        }
        return wrapperResponse(amount);
    }

    @RequestMapping(value = "/total/list.json")
    @ResponseBody
    public Map<String, Object> totalList(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,totalList(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        Optional<OrganizationEntity> company = userContext.getCompany();
        Preconditions.checkState(company.isPresent(), "当前登陆用户无公司信息...");
        String names = MapUtils.getString(requestBody, "names");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(names), "至少指定一个聊天群组...");
        List<String> ids = Lists.newArrayList(StringUtils.split(names, ','));
        String data_rangs = MapUtils.getString(requestBody, "dateRange");
        Map<String, Object> params = Maps.newHashMap();
        params.put("chatRoomNames", ids);
        if (!Strings.isNullOrEmpty(data_rangs)) {
            String[] dates = StringUtils.split(data_rangs, ',');
            params.put("date_start", dates[0]);
            params.put("date_end", dates[1]);
        } else {
            params.put("date_start", DateTime.now().toString("yyyy-MM-dd"));
            params.put("date_end", DateTime.now().toString("yyyy-MM-dd"));
        }
        Optional<List<Map<String, Object>>> list = queryEngineService.queryForList("WxChatGroup", "amount_list", params);
        if (!list.isPresent()) return wrapperResponse(new int[0]);
        return wrapperResponse(list.get());
    }

    @RequestMapping(value = "/total/detail.json")
    @ResponseBody
    public Map<String, Object> totalDetail(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=%s,totalDetail(requestBody=%s)", request.getRequestURI(), requestBody));
        LoginUserContext userContext = loadLoginUser(request);
        Optional<OrganizationEntity> company = userContext.getCompany();
        Preconditions.checkState(company.isPresent(), "当前登陆用户无公司信息...");
        Map<String, Object> params = Maps.newHashMap();
        String chatroomname = MapUtils.getString(requestBody, "name");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(chatroomname), "需要选择一个群聊天室...");
        params.put("chatroomname", chatroomname);
        String data_rangs = MapUtils.getString(requestBody, "dateRange");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(data_rangs), "需指定日期范围...");
        String[] dates = StringUtils.split(data_rangs, ',');
        params.put("date_start", dates[0]);
        params.put("date_end", dates[1]);
        Optional<List<Map<String, Object>>> list = queryEngineService.queryForList("WxChatGroup", "amount_detail", params);
        if (!list.isPresent()) return wrapperResponse(new int[0]);
        return wrapperResponse(list.get());
    }

    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;
    @Resource
    ChatRoomContactEntityAction chatRoomContactEntityAction;
}
