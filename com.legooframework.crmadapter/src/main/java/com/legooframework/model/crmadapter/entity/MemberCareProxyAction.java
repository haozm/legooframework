package com.legooframework.model.crmadapter.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legooframework.model.membercare.entity.TaskCareDetailRule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MemberCareProxyAction extends BaseEntityAction<EmptyEntity> {

    public MemberCareProxyAction() {
        super(null);
    }

    private static final Comparator<TaskCareDetailRule> TOUCH90_DETAIL_ORDERING = Comparator
            .comparingLong(x -> x == null ? 0 : x.getDelay().toHours());

    public Optional<List<TaskCareDetailRule>> readTouch90Rule(Integer companyId, Integer storeId, HttpServletRequest request) {
        String token = request.getHeader(KEY_HEADER);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "当前请求无法获取合法token...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", storeId);
        Optional<JsonElement> jsonElement = super.postWithToken(companyId, "membercare.loadTouch90Rule", token,
                MapUtils.isEmpty(params) ? null : params);
        List<TaskCareDetailRule> list = Lists.newArrayListWithCapacity(10);
        if (jsonElement.isPresent()) {
            JsonObject jsonObject = jsonElement.get().getAsJsonObject();
            JsonArray array = jsonObject.getAsJsonArray("ruleDetail");
            for (JsonElement element : array) {
                JsonObject item = element.getAsJsonObject();
                // TODO
                // list.add(TaskCareDetailRule.create(item.get("delay").getAsString(), item.get("expired").getAsString()));
            }
        }
        if (CollectionUtils.isNotEmpty(list)) list.sort(TOUCH90_DETAIL_ORDERING);
        return Optional.ofNullable(CollectionUtils.isEmpty(list) ? null : list);
    }

}
