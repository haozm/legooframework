package com.legooframework.model.statistical.mvc;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController(value = "SalesMvcController")
@RequestMapping(value = "/sales")
public class SalesMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SalesMvcController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("statisticalBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    @RequestMapping(value = "/{companyId}/pages.json")
    @ResponseBody
    public JsonMessage querySales(@PathVariable(value = "companyId") int companyId,
                                  @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],querySales(requestBody=%s)", request.getRequestURI(), requestBody));
        Integer pageNum = MapUtils.getInteger(requestBody, "pageNum", 0);
        Integer pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
        Integer userId = MapUtils.getInteger(requestBody, "userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        requestBody.putAll(user.toViewMap());
        PagingResult page = queryAction.queryForPage("empSales", "sales",
                pageNum, pageSize, requestBody);
        return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
    }

    @RequestMapping(value = "/{companyId}/details.json")
    @ResponseBody
    public JsonMessage queryDetails(@PathVariable(value = "companyId") int companyId, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],details(requestBody=%s)", request.getRequestURI(), requestBody));
        Integer userId = MapUtils.getInteger(requestBody, "userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        requestBody.putAll(user.toViewMap());
        Optional<Map<String, Object>> result = queryAction.queryForMap("empSales", "salesDetail", requestBody);
        if (!result.isPresent())
            return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(result.get()).toMessage();
    }

    @RequestMapping(value = "/{companyId}/goods.json")
    @ResponseBody
    public JsonMessage queryGoods(@PathVariable(value = "companyId") int companyId, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],statistics(requestBody=%s)", request.getRequestURI(), requestBody));
        Integer userId = MapUtils.getInteger(requestBody, "userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        requestBody.putAll(user.toViewMap());
        Optional<List<Map<String, Object>>> result = queryAction.queryForList("empSales", "salesGoods", requestBody);
        if (!result.isPresent())
            return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(result.get()).toMessage();
    }

    @RequestMapping(value = "/{companyId}/total.json")
    @ResponseBody
    public JsonMessage queryTotal(@PathVariable(value = "companyId") int companyId, @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("URL=[%s],statistics(requestBody=%s)", request.getRequestURI(), requestBody));
        Integer userId = MapUtils.getInteger(requestBody, "userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        requestBody.putAll(user.toViewMap());
        Optional<Map<String, Object>> result = queryAction.queryForMap("empSales", "sales_total", requestBody);
        if (!result.isPresent())
            return JsonMessageBuilder.OK().toMessage();
        return JsonMessageBuilder.OK().withPayload(result.get()).toMessage();
    }

    @Autowired
    private JdbcQuerySupport queryAction;

}
