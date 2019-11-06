package com.legooframework.model.covariant.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.SendMessageTemplate;
import com.legooframework.model.templatemgs.entity.SimpleMsgTemplateList;
import com.legooframework.model.templatemgs.entity.Touch90DefauteTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MsgTemplateProxyAction extends BaseEntityAction<EmptyEntity> {

    private static final Logger logger = LoggerFactory.getLogger(MsgTemplateProxyAction.class);

    public MsgTemplateProxyAction() {
        super(null);
    }

    public SimpleMsgTemplateList readDefTemplateByClassfies(Collection<String> classifies) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(classifies), "当前请求无法获取合法classifies");
        Map<String, Object> params = Maps.newHashMap();
        params.put("action", "readTemplateByClassfies");
        params.put("classifies", StringUtils.join(classifies, ','));
        Optional<JsonElement> jsonElement = super.post(null, "templatemgs.loadTemplateDefaults", params);
        return jsonElement.map(jsob -> SimpleMsgTemplateList.create(jsob.getAsString()))
                .orElseGet(SimpleMsgTemplateList::createEmpty);
    }

    public List<SendMessageTemplate> batchReplaceMemberTemplate(Integer companyId, Integer employeeId, List<String> payloads,
                                                                boolean encoding, boolean authorization, HttpServletRequest request) {

        String token = request == null ? WebUtils.SECURE_ANONYMOUS_TOKEN : request.getHeader(KEY_HEADER);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "当前请求无法获取合法token...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("employeeId", employeeId == null ? -1 : employeeId);
        params.put("encoding", true);
        params.put("authorization", authorization);
        List<String> encoding_list = Lists.newArrayList(payloads);
        if (!encoding) {
            encoding_list.clear();
            payloads.forEach(x -> {
                String[] args = StringUtils.splitByWholeSeparator(x, "||");
                encoding_list.add(String.format("%s||%s", args[0], WebUtils.encodeUrl(args[1])));
            });
        }
        List<JsonElement> jsonElements = Lists.newArrayList();
        if (encoding_list.size() <= 300) {
            params.put("payload", StringUtils.join(encoding_list, '@'));
            Optional<JsonElement> jsonElement = super.postWithToken(companyId, "templatemgs.batchReplaceMemberUrl",
                    token, params);
            jsonElement.ifPresent(jsonElements::add);
        } else if (payloads.size() <= 2000) {
            List<List<String>> list_list = Lists.partition(encoding_list, 300);
            list_list.forEach(list -> {
                params.put("payload", StringUtils.join(list, '@'));
                Optional<JsonElement> jsonElement = super.postWithToken(companyId, "templatemgs.batchReplaceMemberUrl",
                        token, params);
                jsonElement.ifPresent(jsonElements::add);
            });
        } else {
            List<List<String>> list_list = Lists.partition(encoding_list, 300);
            list_list.parallelStream().forEach(list -> {
                params.put("payload", StringUtils.join(list, '@'));
                Optional<JsonElement> jsonElement = super.postWithToken(companyId, "templatemgs.batchReplaceMemberUrl",
                        token, params);
                jsonElement.ifPresent(jsonElements::add);
            });
        }
        List<SendMessageTemplate> jobDetailTemplates = Lists.newArrayList();
        // detailId|memberId|channel|weixinId@deviceId|mobile|{encoding}memberName|{encoding}context|resulat|||
        // detailId|memberId|channel|weixinId@deviceId|mobile|{encoding}memberName|{encoding}context|resulat
        jsonElements.forEach(json -> {
            String[] args = StringUtils.split(json.getAsString(), "|||");
            Stream.of(args).forEach(c -> jobDetailTemplates.add(SendMessageTemplate.deCoding(c)));
        });
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchReplaceMemberTemplate(companyId:%s, employeeId:%s  ...) size is %s",
                    companyId, employeeId, jobDetailTemplates.size()));
        return jobDetailTemplates;
    }

    // 报文解析器
    // TemplateReaderController.readDefaultTemplate()
    public Touch90DefauteTemplate readTouch90Defaults(CrmStoreEntity store, String categories, HttpServletRequest request) {
        String token = request.getHeader(KEY_HEADER);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "当前请求无法获取合法token...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("storeId", store.getId());
        params.put("categories", categories);
        Optional<JsonElement> jsonElement = super.postWithToken(store.getCompanyId(), "templatemgs.loadTouch90Defaults", token, params);
        List<Map<String, Object>> mapList = Lists.newArrayListWithCapacity(12);
        if (jsonElement.isPresent()) {
            JsonArray array = jsonElement.get().getAsJsonArray();
            for (JsonElement element : array) {
                JsonObject item = element.getAsJsonObject();
                //subRuleId ||  categories || id || template
                Map<String, Object> map = Maps.newHashMap();
                map.put("subRuleId", item.get("subRuleId").getAsString());
                map.put("categories", item.get("categories").getAsString());
                map.put("id", item.get("id").getAsInt());
                JsonElement template = item.get("template");
                map.put("template", template == null || template.isJsonNull() ? null : item.get("template").getAsString());
                mapList.add(map);
            }
        }
        return new Touch90DefauteTemplate(store.getCompanyId(), store.getId(), mapList);
    }

}
