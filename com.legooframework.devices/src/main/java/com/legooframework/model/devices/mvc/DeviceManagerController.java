package com.legooframework.model.devices.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController(value = "deviceManagerController")
@RequestMapping(value = "/device")
public class DeviceManagerController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceManagerController.class);

    /**
     * 获取指定门店的 设备在线状态 目前仅支持 一个门店一台设备的情况
     *
     * @return JsonMessage
     */
    @RequestMapping(value = "/bystore/status.json")
    @ResponseBody
    public JsonMessage loadStatusByStore(@RequestBody Map<String, String> http_request_map,
                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadStatusByStore( http_request_map = %s)", http_request_map));
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空值...");
        params.put("storeId", storeId);
        Map<String, Object> data = Maps.newHashMap();
        data.put("deviceOnline", false);
        data.put("weixinOnline", false);
        Optional<Map<String, Object>> query_res = getJdbcQuerySupport(request).queryForMap("device",
                "status_bystore", params);
        query_res.ifPresent(data::putAll);
        return JsonMessageBuilder.OK().withPayload(data).toMessage();
    }

    @RequestMapping(value = "/manager/amount.json")
    @ResponseBody
    public JsonMessage deviceMsgAmount(@RequestBody Map<String, String> http_request_map,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgAmount( http_request_map = %s)", http_request_map));
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        String storeIds = MapUtils.getString(http_request_map, "storeIds");
        params.put("storeIds", Strings.isNullOrEmpty(storeIds) ? StringUtils.split(storeIds, ',') : null);

        Optional<List<Map<String, Object>>> query_res = getJdbcQuerySupport(request)
                .queryForList("device", "amount_manage", params);

        Map<String, Object> amount = Maps.newHashMap();
        amount.put("allDevice", 0);
        amount.put("onlineDevice", 0);

        amount.put("allWeixin", 0);
        amount.put("onlineWeixin", 0);

        amount.put("allSubDevice", 0);
        amount.put("onlineSubDevice", 0);

        if (query_res.isPresent())
            for (Map<String, Object> $it : query_res.get()) {
                if (StringUtils.equals("device", MapUtils.getString($it, "subType"))) {
                    amount.put("allDevice", MapUtils.getIntValue(amount, "allDevice", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineDevice", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("weixin", MapUtils.getString($it, "subType"))) {
                    amount.put("allWeixin", MapUtils.getIntValue(amount, "allWeixin", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineWeixin", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("subDevice", MapUtils.getString($it, "subType"))) {
                    amount.put("allSubDevice", MapUtils.getIntValue(amount, "allSubDevice", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineSubDevice", MapUtils.getIntValue($it, "amount", 0));
                }
            }
        return JsonMessageBuilder.OK().withPayload(amount).toMessage();
    }

    @RequestMapping(value = "/manager/sublist.json")
    @ResponseBody
    public JsonMessage deviceMsgSubList(@RequestBody Map<String, String> http_request_map,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgSubList( http_request_map = %s)", http_request_map));
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkNotNull(storeId, "门店storeId不可以空值...");
        params.put("storeId", storeId);
        Optional<List<Map<String, Object>>> query_res = getJdbcQuerySupport(request)
                .queryForList("device", "sub_mnglist", params);
        return JsonMessageBuilder.OK().withPayload(query_res.isPresent() ? query_res.get() : new String[0]).toMessage();
    }

    @RequestMapping(value = "/manager/list.json")
    @ResponseBody
    public JsonMessage deviceMsgList(@RequestBody Map<String, String> http_request_map,
                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgList( http_request_map = %s)", http_request_map));
        LoginContext loginContext = getLoginContext();
        Map<String, Object> params = loginContext.toParams();
        String storeIds = MapUtils.getString(http_request_map, "storeIds");
        params.put("storeIds", !Strings.isNullOrEmpty(storeIds) ? StringUtils.split(storeIds, ',') : null);
        Integer deviceOnline = MapUtils.getInteger(http_request_map, "deviceOnline");
        if (null != deviceOnline) params.put("deviceOnline", deviceOnline);
        Integer weixinOnline = MapUtils.getInteger(http_request_map, "weixinOnline");
        if (null != weixinOnline) params.put("weixinOnline", weixinOnline);
        Optional<List<Map<String, Object>>> query_res = getJdbcQuerySupport(request)
                .queryForList("device", "mnglist", params);
        return JsonMessageBuilder.OK().withPayload(query_res.isPresent() ? query_res.get() : new String[0]).toMessage();
    }


    private JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("deviceJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

}
