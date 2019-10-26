package com.legooframework.model.covariant.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.CovariantService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController(value = "covariantMvcController")
@RequestMapping(value = "/covariant")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/cache/clean.json")
    @ResponseBody
    public JsonMessage cacheClean(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("cacheClean(url=%s)", request.getRequestURI()));
        LoginContextHolder.setAnonymousCtx();
        String cacheName = request.getParameter("cache");
        getBean(Constant.CACHE_MANAGER, CaffeineCacheManager.class, request).clearByCache(Constant.CACHE_ENTITYS);
        LoginContextHolder.clear();
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/enum/{enumType}/list.json")
    @ResponseBody
    public JsonMessage enumTypeList(@PathVariable(value = "enumType") String enumType, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("enumTypeList(url=%s)", request.getRequestURI()));
        List<Map<String, Object>> mapList = Lists.newArrayList();
        if (StringUtils.equals("BusinessType", enumType)) {
            Stream.of(BusinessType.values()).forEach(bt -> {
                Map<String, Object> param = Maps.newHashMap();
                param.put("value", bt.getValue());
                param.put("desc", bt.getDesc());
                mapList.add(param);
            });
        } else if (StringUtils.equals("ComRoleType", enumType)) {
            Stream.of(RoleType.get4Company()).forEach(bt -> {
                Map<String, Object> param = Maps.newHashMap();
                param.put("value", bt.getValue());
                param.put("desc", bt.getDesc());
                mapList.add(param);
            });
        } else if (StringUtils.equals("StoreRoleType", enumType)) {
            Stream.of(RoleType.get4Store()).forEach(bt -> {
                Map<String, Object> param = Maps.newHashMap();
                param.put("value", bt.getValue());
                param.put("desc", bt.getDesc());
                mapList.add(param);
            });
        } else if (StringUtils.equals("SendChannel", enumType)) {
            Stream.of(SendChannel.values()).forEach(bt -> {
                Map<String, Object> param = Maps.newHashMap();
                param.put("value", bt.getValue());
                param.put("desc", bt.getDesc());
                mapList.add(param);
            });
        } else {
            throw new IllegalArgumentException(String.format("非法的入参 enumType = %s", enumType));
        }
        return JsonMessageBuilder.OK().withPayload(mapList).toMessage();
    }

    @RequestMapping(value = "/presend/smses.json")
    @ResponseBody
    public JsonMessage preSendSms(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("preSendSms(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            String empIds = MapUtils.getString(requestBody, "empIds", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(empIds), "待发送的人员ID不可以为空...");
            Integer memberId = MapUtils.getInteger(requestBody, "memberId", 0);
            Preconditions.checkArgument(memberId != 0, "待发送的人员ID不可以为空...");
            List<Integer> empids = Stream.of(StringUtils.split(empIds, ',')).mapToInt(Integer::parseInt)
                    .boxed().collect(Collectors.toList());
            String content = MapUtils.getString(requestBody, "content", null);
            int businessType = MapUtils.getInteger(requestBody, "businessType", 0);
            getBean(CovariantService.class, request)
                    .preSendSmsByStore(user.getStoreId().orElse(0), memberId, empids, BusinessType.paras(businessType),
                            content);
        } finally {
            LoginContextHolder.clear();
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @RequestMapping(value = "/preview/smses.json")
    @ResponseBody
    public JsonMessage preViewSms(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("preViewSms(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer memberId = MapUtils.getInteger(requestBody, "memberId", 0);
            Preconditions.checkArgument(memberId != 0, "待发送的人员ID不可以为空...");
            String content = MapUtils.getString(requestBody, "content", null);
            String res = getBean(CovariantService.class, request).preViewSmsByStore(memberId, content);
            return JsonMessageBuilder.OK().withPayload(res).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/templet/{useType}/enabled/list.json")
    @ResponseBody
    public JsonMessage templetMsgByStore(@PathVariable String useType, @RequestBody Map<String, Object> requestBody,
                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("templetMsgByStore(url=%s,requestBody=%s)", request.getRequestURI(), requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Map<String, Object> params = user.toViewMap();
            if (StringUtils.equals("birthdaycare", useType)) {
                params.put("useType", 4);
            } else {
                params.put("useType", -1);
            }
            Optional<List<Map<String, Object>>> list = getJdbcQuery(request).queryForList("MsgTemplateEnity",
                    "quert4EnabledList", params);
            return JsonMessageBuilder.OK().withPayload(list).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }


    @RequestMapping(value = "/task/todo/pages.json")
    @ResponseBody
    public JsonMessage taskcareTodoList(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("taskcareTodoList(requestBody=%s, url=%s)", requestBody, request.getRequestURI()));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Map<String, Object> params = user.toViewMap();
            int pageNum = MapUtils.getInteger(requestBody, "int_search_pageNum", 1);
            int pageSize = MapUtils.getInteger(requestBody, "int_search_pageSize", 20);
            params.put("pageNum", pageNum);
            params.put("pageSize", pageSize);
            String task_code = MapUtils.getString(requestBody, "str_search_task_code", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(task_code), "参数 str_search_task_code 值异常...");
            params.put("task_code", task_code);

            int subtask_code = MapUtils.getIntValue(requestBody, "int_search_subtask_code", 1);
            params.put("date_start", subtask_code == 100 ? MapUtils.getString(requestBody, "date_start") : "");
            params.put("date_end", subtask_code == 100 ? MapUtils.getString(requestBody, "date_end") : "");
            params.put("subtask_code", subtask_code);
            if (user.isShoppingGuide()) {
                params.put("right_level", 0);
            } else {
                params.put("right_level", 1);
            }
            int subtaskstate_code = MapUtils.getInteger(requestBody, "int_search_subtaskstate_code", 1);
            params.put("subtaskstate_code", subtaskstate_code);
            String search_keywords = MapUtils.getString(requestBody, "str_search_keywords", null);
            params.put("keywords", Strings.isNullOrEmpty(search_keywords) ? "" : search_keywords);
            Optional<String> totalJson = getJdbcQuery(request).queryForObject("DBStorage", "GetTaskNums", params, String.class);
            Preconditions.checkState(totalJson.isPresent(), "执行查询（DBStorage,GetTaskNums）返回数据异常");
            CallStorageData storageData = new CallStorageData(task_code);
            storageData.setTotal(totalJson.get());
            if (storageData.hasData(String.valueOf(subtaskstate_code))) {
                Optional<String> dataJson = getJdbcQuery(request).queryForObject("DBStorage", "GetTaskList", params, String.class);
                Preconditions.checkState(dataJson.isPresent(), "执行查询（DBStorage,GetTaskList）返回数据异常");
                storageData.setDatas(dataJson.get());
            }
            return JsonMessageBuilder.OK().withPayload(storageData.toData(String.valueOf(subtaskstate_code))).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    private UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

    private JdbcQuerySupport getJdbcQuery(HttpServletRequest request) {
        return getBean("covariantJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}

