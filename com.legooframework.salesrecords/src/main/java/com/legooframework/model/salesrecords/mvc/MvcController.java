package com.legooframework.model.salesrecords.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.salesrecords.entity.SaleAlloctRuleEntity;
import com.legooframework.model.salesrecords.entity.SaleAlloctRuleEntityAction;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;
import com.legooframework.model.salesrecords.service.SaleRecordService;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDate;
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

@RestController
@RequestMapping(value = "/sale")
public class MvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MvcController.class);

    @RequestMapping(value = "/welcome.json")
    @ResponseBody
    public JsonMessage welcome(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("welcome(url=%s)", request.getRequestURI()));
        Bundle bundle = getBean("salesRecordsBundle", Bundle.class, request);
        return JsonMessageBuilder.OK().withPayload(bundle.toDesc()).toMessage();
    }

    @RequestMapping(value = "/detail/alloct/byemp.json")
    public JsonMessage detailAlloctByEmpPage(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("detailAlloctByEmpPage(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
            int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
            Map<String, Object> params = user.toViewMap();
            int cross = MapUtils.getIntValue(requestBody, "cross", -1);
            if (cross != -1) params.put("cross", cross);
            int orderType = MapUtils.getIntValue(requestBody, "orderType", -1);
            if (orderType != -1) params.put("orderType", orderType);
            String keyword = MapUtils.getString(requestBody, "keyword", null);
            if (!Strings.isNullOrEmpty(keyword)) params.put("keyword", String.format("%%%s%%", keyword));
            params.put("employeeId", MapUtils.getInteger(requestBody, "employeeId", 0));
            String start_date = MapUtils.getString(requestBody, "start");
            String end_date = MapUtils.getString(requestBody, "end");
            if (Strings.isNullOrEmpty(start_date) || Strings.isNullOrEmpty(end_date)) {
                LocalDate now = LocalDate.now();
                params.put("startTime", now.dayOfMonth().withMinimumValue());
                params.put("endTime", now.dayOfMonth().withMaximumValue());
            } else {
                params.put("startTime", start_date);
                params.put("endTime", end_date);
            }
            PagingResult page = getJdbcQuerySupport(request).queryForPage("SaleAlloctResultEntity", "alloct4Detail",
                    pageNum, pageSize, params);
            return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/summary/alloct/byemp.json")
    public JsonMessage summaryAlloctByEmpPage(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("summaryAlloctByemp(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
            int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
            Map<String, Object> params = user.toViewMap();
            params.put("storeId", MapUtils.getInteger(requestBody, "storeId", -1));
            params.put("employeeId", MapUtils.getInteger(requestBody, "employeeId", -1));
            String start_date = MapUtils.getString(requestBody, "start");
            String end_date = MapUtils.getString(requestBody, "end");
            if (Strings.isNullOrEmpty(start_date) || Strings.isNullOrEmpty(end_date)) {
                LocalDate now = LocalDate.now();
                params.put("startTime", now.dayOfMonth().withMinimumValue());
                params.put("endTime", now.dayOfMonth().withMaximumValue());
            } else {
                params.put("startTime", start_date);
                params.put("endTime", end_date);
            }
            PagingResult page = getJdbcQuerySupport(request).queryForPage("SaleAlloctResultEntity", "summaryByemp",
                    pageNum, pageSize, params);
            return JsonMessageBuilder.OK().withPayload(page.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/alloct/store/byperiod.json")
    public JsonMessage alloctByStoreWithPeriod(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("alloctByStoreWithPeriod(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Integer storeId = null;
            if (user.isStoreManager()) {
                storeId = user.getStoreId().orElse(0);
            } else if (user.isAdmin()) {
                storeId = MapUtils.getInteger(requestBody, "storeId", null);
            }
            StoEntity store = getBean(StoEntityAction.class, request).loadById(storeId);
            String start_str = MapUtils.getString(requestBody, "start", null);
            String end_str = MapUtils.getString(requestBody, "end", null);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(start_str), "参数 start 不可为空值");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(end_str), "参数 end 不可为空值");
            LocalDate start = DateTimeUtils.parseYYYYMMDD(start_str);
            LocalDate end = DateTimeUtils.parseYYYYMMDD(end_str);
            getBean(SaleRecordService.class, request).alloctSaleOrder4StoreWithPeriod(store, start, end);
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/alloct/reader/rule.json")
    public JsonMessage readerAlloctRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("readerAlloctRule(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            Integer storeId = null;
            if (user.isStoreManager()) {
                storeId = user.getStoreId().orElse(0);
            } else if (user.isAdmin()) {
                storeId = MapUtils.getInteger(requestBody, "storeId", null);
            }
            Optional<SaleAlloctRuleEntity> optional;
            if (storeId == null) {
                OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
                optional = getBean(SaleAlloctRuleEntityAction.class, request).findByCompany(company);
            } else {
                StoEntity store = getBean(StoEntityAction.class, request).loadById(storeId);
                optional = getBean(SaleAlloctRuleEntityAction.class, request).findByStore(store);
            }
            Map<String, Object> params = optional.map(SaleAlloctRuleEntity::toViewMap).orElse(null);
            return JsonMessageBuilder.OK().withPayload(params).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/alloct/write/rule.json")
    public JsonMessage writeAlloctRule(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("writeAlloctRule(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int auto_run = MapUtils.getIntValue(requestBody, "auto_run", 1);
            int coverted = MapUtils.getIntValue(requestBody, "coverted", 0);
            String member_rule_str = MapUtils.getString(requestBody, "member_rule", null);
            String no_member_rule_str = MapUtils.getString(requestBody, "no_member_rule", null);
            String crs_member_rule_str = MapUtils.getString(requestBody, "crs_member_rule", null);
            String crs_no_member_rule_str = MapUtils.getString(requestBody, "crs_no_member_rule", null);
            List<List<SaleAlloctRuleEntity.Rule>> memberRule = SaleAlloctRuleEntity.decodingRule(member_rule_str);
            List<List<SaleAlloctRuleEntity.Rule>> noMemberRule = SaleAlloctRuleEntity.decodingRule(no_member_rule_str);
            List<List<SaleAlloctRuleEntity.Rule>> crossMemberRule = SaleAlloctRuleEntity.decodingRule(crs_member_rule_str);
            List<List<SaleAlloctRuleEntity.Rule>> crossNoMemberRule = SaleAlloctRuleEntity.decodingRule(crs_no_member_rule_str);
            if (user.isStoreManager()) {
                StoEntity store = getBean(StoEntityAction.class, request).loadById(user.getStoreId().orElse(0));
                getBean(SaleAlloctRuleEntityAction.class, request).insert4Store(store, auto_run == 1, memberRule, noMemberRule);
            } else if (user.isAdmin()) {
                OrgEntity company = getBean(OrgEntityAction.class, request).loadComById(user.getCompanyId());
                getBean(SaleAlloctRuleEntityAction.class, request).insert4Company(company, auto_run == 1, memberRule, noMemberRule,
                        crossMemberRule, crossNoMemberRule, coverted == 1);
            } else {
                throw new RuntimeException("当前账户无权限操作....");
            }
            return JsonMessageBuilder.OK().toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }


    @RequestMapping(value = "/90days/bymember.json")
    public JsonMessage loadSaleRecodesByMember(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSaleRecodesByMember(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            loadLoginUser(requestBody, request);
            Integer memberId = MapUtils.getInteger(requestBody, "memberId");
            Optional<MemberEntity> member = getBean(MemberEntityAction.class, request).findById(memberId);
            Preconditions.checkState(member.isPresent(), "id =%s 对应的会员不存在...");
            Optional<List<SaleRecordEntity>> saleRecords = getBean(SaleRecordEntityAction.class, request)
                    .loadMemberBy90Days(member.get());
            if (!saleRecords.isPresent()) return JsonMessageBuilder.OK().toMessage();
            List<Map<String, Object>> params = Lists.newArrayList();
            for (SaleRecordEntity sa : saleRecords.get()) {
                params.add(sa.toViewMap());
            }
            return JsonMessageBuilder.OK().withPayload(params).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/goods/list.json")
    public JsonMessage loadSaleGoodsList(@RequestBody(required = false) Map<String, Object> requestBody,
                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSaleGoodsList(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
            int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
            Map<String, Object> params = Maps.newHashMap();
            params.put("companyId", user.getCompanyId());
            if (MapUtils.isNotEmpty(requestBody))
                params.putAll(requestBody);
            PagingResult pagingResult = getJdbcQuerySupport(request).queryForPage("SaleGoodsEntity", "goodsList", pageNum,
                    pageSize, params);
            return JsonMessageBuilder.OK().withPayload(pagingResult.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/load/records.json")
    public JsonMessage loadSaleRecords(@RequestBody(required = false) Map<String, Object> requestBody,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSaleRecords(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            UserAuthorEntity user = loadLoginUser(requestBody, request);
            int pageNum = MapUtils.getInteger(requestBody, "pageNum", 1);
            int pageSize = MapUtils.getInteger(requestBody, "pageSize", 20);
            Map<String, Object> params = Maps.newHashMap();
            if (MapUtils.isNotEmpty(requestBody))
                params.putAll(requestBody);
            params.putAll(user.toParamMap());
            PagingResult pagingResult = getJdbcQuerySupport(request).queryForPage("SaleRecordEntity", "salerecord", pageNum,
                    pageSize, params);
            return JsonMessageBuilder.OK().withPayload(pagingResult.toData()).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    @RequestMapping(value = "/load/details.json")
    public JsonMessage loadSaleRecordDetail(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSaleRecordDetail(requestBody=%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        try {
            String saleRecordId = MapUtils.getString(requestBody, "saleRecordId");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(saleRecordId), "入参 saleRecordId 不可以为空值...");
            Map<String, Object> params = Maps.newHashMap();
            params.put("saleRecordId", saleRecordId);
            Optional<List<Map<String, Object>>> resulate = getJdbcQuerySupport(request).queryForList("SaleRecordEntity",
                    "saledetails", params);
            return JsonMessageBuilder.OK().withPayload(resulate.orElse(null)).toMessage();
        } finally {
            LoginContextHolder.clear();
        }
    }

    private JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("salesRecordsJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

    private UserAuthorEntity loadLoginUser(Map<String, Object> requestBody, HttpServletRequest request) {
        Integer userId = MapUtils.getInteger(requestBody, "userId", 0);
        Preconditions.checkArgument(userId != 0, "登陆用户userId值非法...");
        return getBean(UserAuthorEntityAction.class, request).loadUserById(userId, null);
    }

}

