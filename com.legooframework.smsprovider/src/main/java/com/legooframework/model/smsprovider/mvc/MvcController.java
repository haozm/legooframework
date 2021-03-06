package com.legooframework.model.smsprovider.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.StoEntityAction;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.smsprovider.entity.SMSSettingEntity;
import com.legooframework.model.smsprovider.entity.SMSSettingEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController(value = "smsproviderController")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/smsprovider/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("smsProviderBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/check/sms/prefix.json")
    public JsonMessage checkSmsPrefix(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        try {
            Integer storeId = user.getStoreId().orElse(MapUtils.getInteger(requestBody, "storeId", -1));
            String prefix = MapUtils.getString(requestBody, "smsPrefix");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以未空...");
            Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
            StoEntity store = getBean(StoEntityAction.class, request).loadById(storeId);
            Optional<List<SMSSettingEntity>> list = getBean(SMSSettingEntityAction.class, request)
                    .checkSmsPrefix(store, prefix);
            return JsonMessageBuilder.OK().withPayload(list.map(List::size).orElse(0)).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/edit/sms/prefix.json")
    public JsonMessage settingSmsPrefix(@RequestBody(required = false) Map<String, Object> requestBody,
                                        HttpServletRequest request) throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        try {
            Integer storeId = user.getStoreId().orElse(MapUtils.getInteger(requestBody, "storeId", -1));
            String prefix = MapUtils.getString(requestBody, "smsPrefix");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(prefix), "入参 smsPrefix 不可以为空...");
            Preconditions.checkState(prefix.length() <= 13, "短信前缀最大长度需>=13");
            StoEntity store = getBean(StoEntityAction.class, request).loadById(storeId);
            getBean(SMSSettingEntityAction.class, request).changeSmsPrefix(store, prefix);
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param requestBody 请求负载
     * @param request     请求
     * @return EEE
     */
    @PostMapping(value = "/smssetting/load/sms/prefix.json")
    public JsonMessage loadSmsPreix(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) throws Exception {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        UserAuthorEntity user = loadLoginUser(requestBody, request);
        try {
            Integer storeId = user.getStoreId().orElse(MapUtils.getInteger(requestBody, "storeId", -1));
            if (-1 != storeId) {
                SMSSettingEntity smsSetting = getBean(SMSSettingEntityAction.class, request)
                        .loadByStoreId(user.getCompanyId(), storeId);
                return JsonMessageBuilder.OK().withPayload(smsSetting == null ? null : smsSetting.toViewMap()).toMessage();
            } else if (user.getSubStoreIds().isPresent()) {
                List<SMSSettingEntity> smsSettings = getBean(SMSSettingEntityAction.class, request)
                        .loadByStoreIds(user.getCompanyId(), user.getSubStoreIds().get().toArray(new Integer[0]));
                if (CollectionUtils.isNotEmpty(smsSettings)) {
                    List<Map<String, Object>> params = smsSettings.stream().map(SMSSettingEntity::toViewMap)
                            .collect(Collectors.toList());
                    return JsonMessageBuilder.OK().withPayload(params).toMessage();
                }
            }
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    private UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

}
