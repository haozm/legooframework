package com.legooframework.model.crmadapter.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legooframework.model.wechatcircle.entity.CircleUnReadDto;
import com.legooframework.model.wechatcircle.entity.WechatCircleSyncTime;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WechatCircleProxyAction extends BaseEntityAction<EmptyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(WechatCircleProxyAction.class);

    protected WechatCircleProxyAction() {
        super(null);
    }

    public List<WechatCircleSyncTime> batchSyncLastTime(List<String> weixinIds) {
        String wx_ids = StringUtils.join(weixinIds, ',');
        Map<String, Object> params = Maps.newHashMap();
        params.put("weixinIds", wx_ids);
        Optional<JsonElement> jsonElement = super.post(null, "wechatcircle.SyncLastTime", params);
        return deCodingSyncLastTime(jsonElement.orElse(null));
    }

    private List<WechatCircleSyncTime> deCodingSyncLastTime(JsonElement jsonElement) {
        if (jsonElement == null) return null;
        JsonArray json_res = jsonElement.getAsJsonArray();
        List<WechatCircleSyncTime> list = Lists.newArrayList();
        json_res.forEach(json -> {
            JsonObject obj = json.getAsJsonObject();
            String weixinId = obj.get("id").getAsString();
            String syncType = obj.get("syncType").getAsString();
            long startTime = obj.get("startTime").getAsLong();
            long lastTime = obj.get("lastTime").getAsLong();
            list.add(new WechatCircleSyncTime(weixinId, syncType, startTime, lastTime));
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("deCodingSyncLastTime() res is %s", list));
        return list;
    }

    public void wechatcircleUnread(CircleUnReadDto unReadDto) {
        Integer companyId = unReadDto.getSource().getCompanyId();
        Map<String, Object> params = unReadDto.enCoding();
        super.post(companyId, "wechatcircle.circleUnread", params);
    }

}
