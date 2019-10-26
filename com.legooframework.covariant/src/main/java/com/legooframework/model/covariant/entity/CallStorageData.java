package com.legooframework.model.covariant.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class CallStorageData {

    private final String taskCode;
    private int totalCount = 0, substateCount = 0, count = 0;
    private List<Map<String, Object>> resultSet;
    private Map<String, Integer> countMap = Maps.newHashMap();

    public CallStorageData(String taskCode) {
        this.taskCode = taskCode;
    }

    public void setTotal(String jsonString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonString), "待解析的JSON字符串不可以为空值...");
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
        if (jsonObject.isJsonNull()) throw new RuntimeException(String.format("解析Json=%s 发生异常", jsonString));
        String code = jsonObject.get("code").getAsString();
        if (!StringUtils.equals("0000", code)) throw new RuntimeException(String.format("解析Json=%s 发生异常", jsonString));
        JsonArray jsonArray = jsonObject.get("data").getAsJsonArray();
        if (jsonArray.isJsonNull()) return;
        jsonObject = jsonArray.iterator().next().getAsJsonObject();
        this.totalCount = Integer.parseInt(jsonObject.get("total_count").getAsString());
        this.substateCount = Integer.parseInt(jsonObject.get("default_substate_count").getAsString());
        jsonArray = jsonObject.get("allstate_count").getAsJsonArray();
        if (!jsonArray.isJsonNull()) {
            for (JsonElement $it : jsonArray) {
                for (Map.Entry<String, JsonElement> entry : $it.getAsJsonObject().entrySet()) {
                    this.countMap.put(entry.getKey(),
                            Integer.parseInt(entry.getValue().isJsonNull() ? "0" : entry.getValue().getAsString()));
                }
            }
        }
    }

    public void setDatas(String jsonString) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonString), "待解析的JSON字符串不可以为空值...");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(jsonString);
        JsonArray jsonArray = null;
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = parser.parse(jsonString).getAsJsonObject();
            if (jsonObject.isJsonNull()) throw new RuntimeException("解析Json_Date 发生异常,jsonString={}");
            String code = jsonObject.get("code").getAsString();
            if (!StringUtils.equals("0000", code))
                throw new RuntimeException(String.format("解析Json_data 发生异常 code=%s", code));
            jsonArray = jsonObject.get("data").getAsJsonArray();
        } else if (jsonElement.isJsonArray()) {
            jsonArray = jsonElement.getAsJsonArray();
        } else if (jsonElement.isJsonNull()) {
            return;
        }
        if (jsonArray == null || jsonArray.isJsonNull()) return;
        this.resultSet = Lists.newArrayList();
        for (JsonElement $it : jsonArray) {
            Map<String, Object> params = Maps.newHashMap();
            for (Map.Entry<String, JsonElement> entry : $it.getAsJsonObject().entrySet()) {
                params.put(entry.getKey(), entry.getValue().isJsonNull() ? null : entry.getValue().getAsString());
            }
            this.resultSet.add(params);
        }
    }

    public boolean hasData(String subtaskstate_code) {
        return MapUtils.getInteger(this.countMap, subtaskstate_code, 0) != 0;
    }

    public Map<String, Object> toData(String subtaskstate_code) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("total", MapUtils.getInteger(this.countMap, subtaskstate_code, 0));
        map.put("data", CollectionUtils.isEmpty(resultSet) ? null : resultSet);
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("taskCode", taskCode)
                .add("totalCount", totalCount)
                .add("substateCount", substateCount)
                .add("substateCount", substateCount)
                .toString();
    }
}
