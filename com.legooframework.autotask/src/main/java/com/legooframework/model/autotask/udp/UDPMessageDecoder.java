package com.legooframework.model.autotask.udp;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderAdapter;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UDPMessageDecoder extends ProtocolDecoderAdapter {

    private static final Logger logger = LoggerFactory.getLogger(UDPMessageDecoder.class);

    @Override
    public void decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput output) throws Exception {
        String json_data = buffer.getString(Charsets.UTF_8.newDecoder());
        if (logger.isTraceEnabled()) logger.trace("[REV-JSON]" + json_data);
        UDPMessage udp_message;
        try {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(json_data).getAsJsonObject();
            String tag = getString(jsonObject, "tag");
            String fromDevicesId = getString(jsonObject, "fromDevicesId");
            String toDevicesId = getString(jsonObject, "toDevicesId");
            String action = getString(jsonObject, "action");
            String ruleId = getString(jsonObject, "ruleId");
            int len = getInt(jsonObject, "len");
            udp_message = new UDPMessage(tag, fromDevicesId, toDevicesId, action, ruleId, len);
            JsonElement jsonElement = jsonObject.get("value");
            if (StringUtils.equals("msg", tag) || StringUtils.equals("hismsg", tag) ||
                    StringUtils.equals("ack", tag) || StringUtils.equals("delete_msg", tag) ||
                    StringUtils.equals("claim", tag) || StringUtils.equals("ackmsg", tag) ||
                    StringUtils.equals("single_contact", tag) || StringUtils.equals("contact_finish", tag)) {
                Map<String, Object> map = Maps.newHashMap();
                JsonObject _jo = jsonElement.getAsJsonObject();
                Set<String> keys = _jo.keySet();
                keys.forEach(x -> map.put(x, getString(_jo, x)));
                if (StringUtils.equals("hismsg", tag) || StringUtils.equals("delete_msg", tag)) {
                    map.put("companyId", getString(jsonObject, "companyid"));
                    map.put("storeId", getString(jsonObject, "storeid"));
                }
                udp_message.setValue(map);
            } else if (StringUtils.equals("unclaim_msg", tag)) {
                JsonArray _jas = jsonElement.getAsJsonObject().getAsJsonArray("listuserinfo");
                List<Map<String, Object>> new_list = Lists.newArrayList();
                for (int i = 0; i < _jas.size(); i++) {
                    JsonElement $it = _jas.get(i);
                    new_list.add(jsonToMap($it.getAsJsonObject()));
                }
                udp_message.setValues(new_list);
            } else {
                logger.warn(String.format("尚不支持的Tag=%s解析[%s]", tag, json_data));
                udp_message = new UDPMessage("bad", fromDevicesId, toDevicesId,
                        action, ruleId, len);
            }
        } catch (Exception e) {
            logger.error("decode msg [%s] from upd has error", e);
            udp_message = new UDPMessage("bad", "", "", "", "", 0);
        }
        output.write(udp_message);
    }

    private String getString(JsonObject jsonObject, String property) {
        JsonElement jsonElement = jsonObject.get(property);
        if (jsonElement == null || jsonElement.isJsonNull()) return null;
        return jsonElement.getAsString();
    }

    private int getInt(JsonObject jsonObject, String property) {
        JsonElement jsonElement = jsonObject.get(property);
        if (jsonElement == null || jsonElement.isJsonNull()) return 0;
        return jsonElement.getAsInt();
    }

    private Map<String, Object> jsonToMap(JsonObject jsonObject) {
        Map<String, Object> map = Maps.newHashMap();
        Set<String> keys = jsonObject.keySet();
        keys.forEach(x -> map.put(x, getString(jsonObject, x)));
        return map;
    }
}
