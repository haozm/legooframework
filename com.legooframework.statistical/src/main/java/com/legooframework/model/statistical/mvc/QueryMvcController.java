package com.legooframework.model.statistical.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntityAction;
import com.legooframework.model.statistical.entity.QueryDTO;
import com.legooframework.model.statistical.service.StatisticalService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController(value = "staQueryController")
@RequestMapping(value = "/query")
public class QueryMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(QueryMvcController.class);

    /**
     * @param requestBody 请求
     * @param request     HttpServletRequest
     * @return JsonMessage
     */
    @PostMapping(value = "/{queryType}/{companyId}/data.json")
    @ResponseBody
    public JsonMessage statisticalQuery(@PathVariable(value = "queryType") String queryType,
                                        @PathVariable(value = "companyId") int companyId,
                                        @RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        if (logger.isDebugEnabled())
            logger.debug(String.format("statisticalQuery(requestBody=%s,url=%s) start", requestBody, request.getRequestURL().toString()));
        String stmtId = request.getParameter("stm");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(stmtId), "参数 stm=? 不可以为空值...");
        String rid = request.getParameter("rid");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(rid), "参数 rid=? 不可以为空值...");
        String layout = request.getParameter("pt");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(layout), "参数 pt=? 不可以为空值...");
        Integer userId = MapUtils.getInteger(requestBody, "int_search_userId");
        UserAuthorEntity user = getBean(UserAuthorEntityAction.class, request).loadUserById(userId, companyId);
        try {
            QueryDTO query = new QueryDTO(queryType, companyId, stmtId, rid, layout, requestBody);
            if (query.isSummaryQuery()) {
                Map<String, Map<String, Object>> res = getBean(StatisticalService.class, request).query4Summary(user, query);
                return JsonMessageBuilder.OK().withPayload(res).toMessage();
            } else if (query.isSubSummaryQuery()) {
                Optional<List<Map<String, Object>>> maplist = getBean(StatisticalService.class, request)
                        .query4Detail(user, query);
                return JsonMessageBuilder.OK().withPayload(maplist.orElse(null)).toMessage();
            } else {
                return JsonMessageBuilder.ERROR("9999", String.format("非法的入参 %s", queryType)).toMessage();
            }
        } catch (Exception e) {
            logger.error("statisticalQuery(%s) has error", e);
            return JsonMessageBuilder.ERROR("9999", "请求数据异常").toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

}
