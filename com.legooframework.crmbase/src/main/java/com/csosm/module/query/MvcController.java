package com.csosm.module.query;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.jdbc.sqlcfg.ColumnMeta;
import com.csosm.commons.mvc.BaseController;
import com.csosm.commons.mvc.ServletRequestHelper;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.query.entity.PagingResult;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller(value = "queryMvcController")
@RequestMapping(value = "/query")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/list.json")
    @ResponseBody
    public Map<String, Object> query4List(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[REQ][%s]->%s", request.getRequestURI(), requestBody));
        String modelName = requestBody.remove("modelName");
        String stmtId = requestBody.remove("stmtId");
        boolean need_meta = MapUtils.getBooleanValue(requestBody, "meta", false);
        requestBody.remove("meta");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(modelName), "缺少必要请求入参 modelName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "缺少必要请求入参 stmtId");
        loggerBegin(modelName, stmtId, requestBody, request);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Object> params = holdParams(requestBody, request);
        Optional<List<Map<String, Object>>> optional = queryEngineService.queryForList(modelName, stmtId, params);
        if (need_meta) {
            Optional<List<ColumnMeta>> listOptional = queryEngineService.getColumnMetas(modelName, stmtId);
            Map<String, Object> meta_header = Maps.newHashMap();
            if (listOptional.isPresent()) meta_header.put("meta", listOptional.get());
            if (logger.isDebugEnabled())
                logger.debug(String.format("[RSP][%s]->%s kill time %s", request.getRequestURI(), requestBody,
                        stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            return wrapperResponse(meta_header, optional.isPresent() ? optional.get() : null);
        }
        loggerEnd(modelName, stmtId, requestBody, request, stopwatch);
        return wrapperResponse(optional.isPresent() ? optional.get() : null);
    }

    @RequestMapping(value = "/{modelName}/{stmtId}/map.json")
    @ResponseBody
    public Map<String, Object> query4Map(@PathVariable String modelName, @PathVariable String stmtId,
                                         @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        loggerBegin(modelName, stmtId, requestBody, request);
        Map<String, Object> params = holdParams(requestBody, request);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Optional<Map<String, Object>> optional = queryEngineService.queryForMap(modelName, stmtId, params);
        loggerEnd(modelName, stmtId, requestBody, request, stopwatch);
        return wrapperResponse(optional.isPresent() ? optional.get() : null);
    }

    @RequestMapping(value = "/meta.json")
    @ResponseBody
    public Map<String, Object> loadMeta(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        // LoginUserContext loginUser = loadLoginUser(request);
        String modelName = MapUtils.getString(requestBody, "modelName");
        String stmtId = MapUtils.getString(requestBody, "stmtId");
        loggerBegin(modelName, stmtId, requestBody, request);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Preconditions.checkArgument(!Strings.isNullOrEmpty(modelName), "缺少请求入参 modelName");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "缺少请求入参 stmtId");
        Optional<List<ColumnMeta>> listOptional = queryEngineService.getColumnMetas(modelName, stmtId);
        loggerEnd(modelName, stmtId, requestBody, request, stopwatch);
        return wrapperResponse(listOptional.orNull());
    }

    @RequestMapping(value = "/{modelName}/{stmtId}/pages.json")
    @ResponseBody
    public Map<String, Object> query4Page(@PathVariable String modelName, @PathVariable String stmtId,
                                          @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        loggerBegin(modelName, stmtId, requestBody, request);
        Integer pageNum = MapUtils.getInteger(requestBody, "pageNum", 0);
        Integer pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        boolean meta = MapUtils.getBoolean(requestBody, "meta", false);
        requestBody.remove("pageNum");
        requestBody.remove("pageSize");
        Optional<List<ColumnMeta>> metaOptional = Optional.absent();
        if (meta) metaOptional = queryEngineService.getColumnMetas(modelName, stmtId);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Object> params = holdParams(requestBody, request);
        PagingResult pagingResult = queryEngineService.queryForPage(modelName, stmtId, pageNum, pageSize, params);
        loggerEnd(modelName, stmtId, requestBody, request, stopwatch);
        return metaOptional.isPresent() ? wrapperResponse(pagingResult.toMap(metaOptional.get())) :
                wrapperResponse(pagingResult.toMap());
    }

    @RequestMapping(value = "/{modelName}/{stmtId}/list.json")
    @ResponseBody
    public Map<String, Object> queryAssignList(@PathVariable String modelName, @PathVariable String stmtId,
                                               @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        loggerBegin(modelName, stmtId, requestBody, request);
        boolean meta = MapUtils.getBoolean(requestBody, "meta", false);
        Optional<List<ColumnMeta>> metaOptional = Optional.absent();
        if (meta) metaOptional = queryEngineService.getColumnMetas(modelName, stmtId);
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<String, Object> params = holdParams(requestBody, request);
        Optional<List<Map<String, Object>>> optional = queryEngineService.queryForList(modelName, stmtId, params);
        loggerEnd(modelName, stmtId, requestBody, request, stopwatch);
        return wrapperResponse(metaOptional.isPresent() ? metaOptional.get() : null,
                optional.isPresent() ? optional.get() : null);
    }


    private void loggerBegin(String modelName, String stmtId, Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[REQ][%s][%s.%s]->%s", modelName, stmtId, request.getRequestURI(), requestBody));
    }

    private void loggerEnd(String modelName, String stmtId, Map<String, String> requestBody, HttpServletRequest request,
                           Stopwatch stopwatch) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[RSP][%s][%s.%s]->%s kill time %s", modelName, stmtId, request.getRequestURI(),
                    requestBody, stopwatch.elapsed(TimeUnit.MILLISECONDS)));
    }


    private Map<String, Object> holdParams(Map<String, String> requestBody, HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "员工所在公司为空,无法执行后续操作...");
        Map<String, Object> params = loginUser.toMap();
        Optional<Map<String, Object>> dynamic_query_map = ServletRequestHelper.parseQueryParams(requestBody);

        params.put("has_dynamic_params", dynamic_query_map.isPresent());
        if (dynamic_query_map.isPresent()) params.putAll(dynamic_query_map.get());
        if (MapUtils.getInteger(params, "storeId") != null) {
            Integer storeId = MapUtils.getInteger(params, "storeId");
            StoreEntity store = getBean(StoreEntityAction.class, request).loadById(storeId);
            params.put("MSG_COM_STORE", String.format("MSG_%s_%s", loginUser.getCompany().get().getId(), store.getId()));
        }
        return params;
    }

    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;

}
