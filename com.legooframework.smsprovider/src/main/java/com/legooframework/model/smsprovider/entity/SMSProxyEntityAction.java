package com.legooframework.model.smsprovider.entity;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.legooframework.model.core.base.entity.EmptyEntity;
import com.legooframework.model.core.base.entity.HttpBaseEntityAction;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsgateway.entity.SendMsg4FinalEntity;
import com.legooframework.model.smsgateway.entity.SendMsg4SendEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

public class SMSProxyEntityAction extends HttpBaseEntityAction<EmptyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(SMSProxyEntityAction.class);

    private final String domian;
    private final String sendHttpApi;
    private final String syncHttpApi;

    public SMSProxyEntityAction(String domian) {
        this.domian = domian;
        this.sendHttpApi = String.format("http://%s/smsresult/api/smses/batch/sending.json", this.domian);
        this.syncHttpApi = String.format("http://%s/smsresult/api/smses/state/fetching.json", this.domian);
    }

    public Optional<List<SendMsg4FinalEntity>> syncSmsState(List<String> smsIds) {
        if (CollectionUtils.isEmpty(smsIds)) return Optional.empty();
        Map<String, Object> params = Maps.newHashMap();
        params.put("payload", Joiner.on(',').join(smsIds));
        try {
            Optional<String> response = super.post(syncHttpApi, params, 0);
            if (!response.isPresent()) return Optional.empty();
            Optional<JsonElement> jsonElement = WebUtils.parseJson(response.get());
            if (!jsonElement.isPresent()) return Optional.empty();
            String rsp_payload = jsonElement.get().getAsString();
            String[] args = StringUtils.splitByWholeSeparator(rsp_payload, "|||");
            List<SendMsg4FinalEntity> res_list = Lists.newArrayListWithCapacity(args.length);
            for (String str : args) {
                // 1858882831238231|28372e35-ed81-4867-97fd-c94855b693b8|4|2|2019-05-22 18:33:00|error:NOTEXITS
                String[] arg = StringUtils.split(str, '|');
                res_list.add(SendMsg4FinalEntity.create(arg[1], Integer.parseInt(arg[2]), Integer.parseInt(arg[3]), arg[4], arg[5]));
            }
            return Optional.of(res_list);
        } catch (Exception e) {
            logger.error("syncSmsState(...) has error", e);
            throw new RuntimeException(e);
        }
    }

    public SendMsg4SendEntity sendSingleSms(String smsId, String mixed, String context) {
        SendMsg4SendEntity result = new SendMsg4SendEntity(smsId);
        StringJoiner joiner = new StringJoiner("|");
        joiner.add(mixed).add("1").add(WebUtils.encodeUrl(context));
        Map<String, Object> params = Maps.newHashMap();
        params.put("payload", joiner.toString());
        try {
            Optional<String> response = super.post(sendHttpApi, params, 0);
            Preconditions.checkState(response.isPresent(), "网关无数据返回...");
            Optional<JsonElement> jsonElement = WebUtils.parseJson(response.get());
            Preconditions.checkState(jsonElement.isPresent(), "网关无数据返回...");
            String rsp_payload = jsonElement.get().getAsString();
            this.docodingSendRes(rsp_payload, result);
        } catch (Exception e) {
            result.errorByException(e);
        }
        return result;
    }

    private void docodingSendRes(String payload, SendMsg4SendEntity sendEntity) {
        try {
            String[] args = StringUtils.split(payload, '|');
            Preconditions.checkState(args.length == 3, "错误的返回报文%s", payload);
            if (StringUtils.equals("0000", args[1])) {
                sendEntity.finshedSend();
            } else {
                sendEntity.errorBySending(args[2]);
            }
        } catch (Exception e) {
            sendEntity.errorByException(e);
        }
    }


}
