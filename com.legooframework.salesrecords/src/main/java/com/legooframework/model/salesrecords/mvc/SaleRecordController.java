package com.legooframework.model.salesrecords.mvc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.*;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/sale")
public class SaleRecordController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(SaleRecordController.class);

    @RequestMapping(value = "/90days/bymember.json")
    public JsonMessage loadSaleRecodesByMember(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadSaleRecodesByMember(requestBody=%s)", requestBody));
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        Optional<CrmOrganizationEntity> company = getBean(CrmOrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent());
        Preconditions.checkNotNull(memberId, "memberId 不可以为空...");
        Preconditions.checkNotNull(storeId, "storeId 不可以为空...");
        Optional<CrmStoreEntity> store = getBean(CrmStoreEntityAction.class, request).findById(company.get(), storeId);
        Preconditions.checkState(store.isPresent(), "id=%s对应的门店不存在...", storeId);
        Optional<CrmMemberEntity> member = getBean(CrmMemberEntityAction.class, request).loadMemberByCompany(company.get(), memberId);
        Preconditions.checkState(member.isPresent(), "id =%s 对应的会员不存在...");
        Optional<List<SaleRecordEntity>> saleRecords = getBean(SaleRecordEntityAction.class, request)
                .loadMemberBy90Days(member.get(), store.get());
        if (!saleRecords.isPresent()) return JsonMessageBuilder.OK().toMessage();
        List<Map<String, Object>> params = Lists.newArrayList();
        for (SaleRecordEntity sa : saleRecords.get()) {
            params.add(sa.toViewMap());
        }
        return JsonMessageBuilder.OK().withPayload(params).toMessage();
    }
}

