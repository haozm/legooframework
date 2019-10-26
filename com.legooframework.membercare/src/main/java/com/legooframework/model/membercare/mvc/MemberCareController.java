package com.legooframework.model.membercare.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.Authenticationor;
import com.legooframework.model.crmadapter.service.CrmPermissionHelper;
import com.legooframework.model.membercare.entity.BusinessType;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/care")
public class MemberCareController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MemberCareController.class);

    @RequestMapping(value = "/touched90/detail/byid.json")
    @ResponseBody
    public JsonMessage loadTouch90Detail(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Detail(requestBody=%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        String taskId = MapUtils.getString(requestBody, "taskId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(taskId), "参数 taskId=‘’ 不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("taskId", taskId);
        params.putAll(user.toParams());
        Optional<List<Map<String, Object>>> list = getQuerySupport(request).queryForList("MemberCare", "touch90_detail", params);
        if (!list.isPresent()) return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(list.get()).toMessage();
    }

    /**
     * 加载90 节点明细
     *
     * @param requestBody 请求大侠
     * @param request     请求夫
     * @return 请求
     */
    @RequestMapping(value = "/load/touched90/list.json")
    public JsonMessage loadTouch90List(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90Detail(url=%s,requestBody=%s)", request.getRequestURL(), requestBody));
        LoginContext user = LoginContextHolder.get();
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);
        Map<String, Object> params = Maps.newHashMap();
        params.putAll(user.toParams());
        Integer pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
        Integer pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        params.put("businessType", BusinessType.TOUCHED90.toString());
        params.putAll(requestBody);
        params.put("storeId", authenticationor.getStore().getId());
        PagingResult pg = getQuerySupport(request).queryForPage("MemberCare", "touch90_list", pageNum, pageSize, params);
        return JsonMessageBuilder.OK().withPayload(pg.toData()).toMessage();
    }

    /**
     * 获取 90 可支配的节点
     *
     * @param requestBody 大将东区
     * @param request     大鱼海棠
     * @return 长子
     */
    @RequestMapping(value = "/load/touched90/total.json")
    public JsonMessage loadTouch90StartingNum(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadTouch90StartingNum(requestBody=%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Authenticationor authenticationor = getBean(CrmPermissionHelper.class, request).authentication(requestBody);

        Map<String, Object> params = Maps.newHashMap();
        params.putAll(user.toParams());
        params.put("businessType", BusinessType.TOUCHED90.toString());
        if (authenticationor.hasStore()) {
            params.put("storeId", authenticationor.getStore().getId());
        } else {
            params.put("storeIds", authenticationor.getOptStoreIds().orElse(Lists.newArrayList(-1)));
        }
        Optional<List<Map<String, Object>>> list = getQuerySupport(request).queryForList("MemberCare", "totalStartingNums", params);
        Map<String, Integer> resulate = Maps.newHashMap();
        resulate.put("Create", 0);
        resulate.put("Starting", 0);
        resulate.put("Finished", 0);
        resulate.put("Stoped", 0);
        resulate.put("Canceled", 0);
        resulate.put("Expired", 0);
        resulate.put("Exceptioned", 0);
        list.ifPresent(x -> x.forEach(m -> resulate.put(MapUtils.getString(m, "taskStatus"), MapUtils.getIntValue(m, "num"))));
        return JsonMessageBuilder.OK().withPayload(resulate).toMessage();
    }

    JdbcQuerySupport getQuerySupport(HttpServletRequest request) {
        return getBean("careJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}

