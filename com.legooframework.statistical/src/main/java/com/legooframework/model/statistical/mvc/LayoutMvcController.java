package com.legooframework.model.statistical.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.statistical.service.StatisticalService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController(value = "statisticalController")
@RequestMapping(value = "/layout")
public class LayoutMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LayoutMvcController.class);

    /**
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/load/homepage.json")
    @ResponseBody
    public JsonMessage loadHomepage(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadHomepage(requestBody=%s,payload=...) start", requestBody));
        Integer userId = MapUtils.getInteger(requestBody, "int_search_userId");
        String layoutType = MapUtils.getString(requestBody, "str_pageType", "HOMEPAGE");
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
            Map<String, Object> homepage = getBean(StatisticalService.class, request).loadHomePage(user, layoutType);
            return JsonMessageBuilder.OK().withPayload(homepage).toMessage();
        } catch (Exception e) {
            logger.error("loadLayout(%s) has error", e);
            return JsonMessageBuilder.ERROR("9999", "请求数据异常").toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    /**
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/load/{companyId}/subpage.json")
    @ResponseBody
    public JsonMessage loadSubpage(@PathVariable(value = "companyId") int companyId,
                                   @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSubpage(requestBody=%s,payload=...) start", requestBody));
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        Integer userId = MapUtils.getInteger(requestBody, "int_search_userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        String layoutType = request.getParameter("pt");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(layoutType), "参数 pt=? 不可以为空值...");
        String rid = request.getParameter("rid");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(rid), "参数 rid=? 不可以为空值...");
        try {
            Map<String, Object> subpage = getBean(StatisticalService.class, request).loadSubPage(user, layoutType, rid);
            return JsonMessageBuilder.OK().withPayload(subpage).toMessage();
        } catch (Exception e) {
            logger.error("loadLayout(%s) has error", e);
            return JsonMessageBuilder.ERROR("9999", "请求数据异常").toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

}
