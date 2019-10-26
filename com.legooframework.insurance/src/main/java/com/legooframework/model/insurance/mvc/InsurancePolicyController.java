package com.legooframework.model.insurance.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.insurance.entity.MemberEntity;
import com.legooframework.model.insurance.entity.MemberEntityAction;
import com.legooframework.model.insurance.service.InsurancePolicyService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/insurance")
public class InsurancePolicyController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(InsurancePolicyController.class);

    @PostMapping(value = "/load/list.json")
    public JsonMessage loadPolicyDetails(@RequestBody(required = false) Map<String, Object> requestBody,
                                         HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadPolicyDetails(%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        Map<String, Object> params = Maps.newHashMap();
        int pageNum = MapUtils.getInteger(requestBody, "page", 1);
        int pageSize = MapUtils.getInteger(requestBody, "limit", 20);
        if (MapUtils.isNotEmpty(requestBody)) params.putAll(requestBody);
        PagingResult pages = getJdbcQuerySupport(request).queryForPage("InsurancePolicy", "details", pageNum, pageSize,
                params);
        return JsonMessageBuilder.OK().withPayload(pages.toData()).toMessage();
    }

    @PostMapping(value = "/member/detail.json")
    public JsonMessage loadPolicyMemberDetails(@RequestBody(required = false) Map<String, Object> requestBody,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadPolicyMemberDetails(%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        Map<String, Object> params = Maps.newHashMap();
        int insuranceId = MapUtils.getInteger(requestBody, "insuranceId", -1);
        if (insuranceId == -1) return JsonMessageBuilder.OK().withPayload(new String[0]).toMessage();
        params.put("insuranceId", insuranceId);
        Optional<List<Map<String, Object>>> list = getJdbcQuerySupport(request)
                .queryForList("InsurancePolicy", "members", params);
        return JsonMessageBuilder.OK().withPayload(list.orElse(null)).toMessage();
    }

    @PostMapping(value = "/submit/policy.json")
    public JsonMessage submitPolicy(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) throws ParseException {
        if (logger.isDebugEnabled())
            logger.debug(String.format("submitPolicy(%s)", requestBody));
        TransactionStatus tx = startTx(request, null);
        try {
            LoginContextHolder.setAnonymousCtx();
            Integer defrayerId = MapUtils.getInteger(requestBody, "defrayerId", -1);
            MemberEntity defrayer = null;
            MemberEntity accepter = null;
            if (defrayerId != -1) {
                Optional<MemberEntity> defrayer_opt = getBean(MemberEntityAction.class, request).findById(defrayerId);
                Preconditions.checkState(defrayer_opt.isPresent(), "投保人Id=%s信息不存在...", defrayerId);
                defrayer = defrayer_opt.get();
            } else {
                defrayer = initMember(requestBody, "defrayer", request);
            }
            boolean myself = MapUtils.getBoolean(requestBody, "myself", false);
            if (myself) {
                accepter = defrayer;
            } else {
                Integer accepterId = MapUtils.getInteger(requestBody, "accepterId", -1);
                if (accepterId != -1) {
                    Optional<MemberEntity> accepter_opt = getBean(MemberEntityAction.class, request).findById(accepterId);
                    Preconditions.checkState(accepter_opt.isPresent(), "被保人信息%s不存在...", accepterId);
                    accepter = accepter_opt.get();
                } else {
                    accepter = initMember(requestBody, "accepter", request);
                }
            }

            //保单基本信息 insuranceNo
            String insuranceNo = MapUtils.getString(requestBody, "insuranceNo");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(insuranceNo), "保单编号不可以为空值...");
            String _insuredDate = MapUtils.getString(requestBody, "insuredDate");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(_insuredDate), "投保日期不可以为空值...");
            Date insuredDate = DateUtils.parseDate(_insuredDate, "yyyy-MM-dd");
            String paymentType = MapUtils.getString(requestBody, "paymentType");
            double payAmount = MapUtils.getDouble(requestBody, "payAmount");
            Preconditions.checkArgument(payAmount > 0.0D, "缴费金额不可以为空值...");
            String bankType = MapUtils.getString(requestBody, "bankType");
            String bankCardNo = MapUtils.getString(requestBody, "bankCardNo");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(bankCardNo), "银行卡号不可以为空值...");
            String relationship = MapUtils.getString(requestBody, "relationshipType");
            String insuranceInfos = MapUtils.getString(requestBody, "insuranceInfos");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(insuranceInfos), "险种及金额不可以为空值...");
            List<String> insurances = Stream.of(StringUtils.split(insuranceInfos, '$')).collect(Collectors.toList());
            String beneficiary = MapUtils.getString(requestBody, "beneficiary");
            Preconditions.checkArgument(!Strings.isNullOrEmpty(beneficiary), "受益人信息不可以为空值...");
            String remarks = MapUtils.getString(requestBody, "remarks");
            if (myself) {
                getBean(InsurancePolicyService.class, request).insuredSelf(defrayer, insuranceNo, insuredDate,
                        paymentType, payAmount, bankType, bankCardNo, insurances, beneficiary, remarks);
            } else {
                getBean(InsurancePolicyService.class, request).insured(defrayer, accepter, insuranceNo, insuredDate,
                        paymentType, payAmount, bankType, bankCardNo, relationship,
                        insurances, beneficiary, remarks);
            }
            commitTx(request, tx);
        } catch (Exception e) {
            rollbackTx(request, tx);
            throw e;
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    private MemberEntity initMember(Map<String, Object> requestBody, String prefix, HttpServletRequest request) {
        String name = MapUtils.getString(requestBody, String.format("%s_name", prefix));
        String cardId = MapUtils.getString(requestBody, String.format("%s_cardID", prefix));
        // String phone = MapUtils.getString(requestBody, String.format("%s_phone", prefix));
        String mobile = MapUtils.getString(requestBody, String.format("%s_mobile", prefix));
        String birthday = MapUtils.getString(requestBody, String.format("%s_birthday", prefix));
        LocalDate _birthday = DateTimeUtils.parseYYYYMMDD(birthday);
        int sex = MapUtils.getInteger(requestBody, String.format("%s_sex", prefix));
        int education = MapUtils.getInteger(requestBody, String.format("%s_educationType", prefix));
        int height = MapUtils.getInteger(requestBody, String.format("%s_height", prefix));
        int weight = MapUtils.getInteger(requestBody, String.format("%s_weight", prefix));
        String familyAddr = MapUtils.getString(requestBody, String.format("%s_familyAddr", prefix));
        String workAddr = MapUtils.getString(requestBody, String.format("%s_workAddr", prefix));
        String email = MapUtils.getString(requestBody, String.format("%s_email", prefix));
        return getBean(MemberEntityAction.class, request).insert(name, cardId, null, mobile, _birthday, sex,
                education, height, weight, familyAddr, workAddr, email);
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("insuranceJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

    @Override
    protected PlatformTransactionManager getTransactionManager(HttpServletRequest request) {
        return getBean("transactionManager", DataSourceTransactionManager.class, request);
    }
}
