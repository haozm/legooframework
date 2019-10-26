package com.legooframework.model.rfm.mvc;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.legooframework.model.rfm.entity.RFM4OrgEntity;
import com.legooframework.model.rfm.entity.RFM4OrgEntityAction;
import com.legooframework.model.rfm.service.RFM4OrgService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController(value = "rfmController")
@RequestMapping("/rfm")
public class RFMController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RFMController.class);

    @RequestMapping(value = "/{channel}/addorupdate.json")
    public Map<String, Object> savaOrUpdateComRFM(@PathVariable(name = "channel") String channel,
                                                  @RequestBody Map<String, String> requestBody,
                                                  HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("savaOrUpdateComRFM(requestBody=%s)", requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        OrganizationEntity company = loginUser.getCompany().get();
        int type = MapUtils.getInteger(requestBody, "type", 1);
        Preconditions.checkArgument(type == 1 || type == 2, "入参 type 取值非法，取值范围是[%s]...", "1,2");
        int rewrite = MapUtils.getInteger(requestBody, "rewrite", 0);

        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        if (null != storeId) {
            Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, company);
            Preconditions.checkState(store.isPresent(), "ID=%s 对应的门店不存在...", storeId);
            getBean(RFM4OrgEntityAction.class, request).savaOrUpdateStoreRFM(loginUser, store.get(), type,
                    MapUtils.getIntValue(requestBody, "rV2"), MapUtils.getIntValue(requestBody, "rV3"),
                    MapUtils.getIntValue(requestBody, "rV4"), MapUtils.getIntValue(requestBody, "rV5"),
                    MapUtils.getIntValue(requestBody, "fV1"), MapUtils.getIntValue(requestBody, "fV2"),
                    MapUtils.getIntValue(requestBody, "fV3"), MapUtils.getIntValue(requestBody, "fV4"),
                    MapUtils.getIntValue(requestBody, "mV1"), MapUtils.getIntValue(requestBody, "mV2"),
                    MapUtils.getIntValue(requestBody, "mV3"), MapUtils.getIntValue(requestBody, "mV4"));
            return wrapperEmptyResponse();
        }
        final TransactionStatus ts = startTx(UUID.randomUUID().toString());
        OrganizationEntity organization = null;
        try {
            Integer orgId = MapUtils.getInteger(requestBody, "orgId");

            if (null != orgId) {
                Optional<List<OrganizationEntity>> orgList = getBean(OrganizationEntityAction.class, request)
                        .findOrgByIds(company.getId(), Lists.newArrayList(orgId));
                Preconditions.checkState(orgList.isPresent(), "ID=%s 对应的组织不存在...", orgId);
                organization = orgList.get().get(0);
            }

            getBean(RFM4OrgService.class, request).savaOrUpdateCompanyRFM(loginUser,
                    organization == null ? company : organization,
                    type,
                    MapUtils.getIntValue(requestBody, "rV2"), MapUtils.getIntValue(requestBody, "rV3"),
                    MapUtils.getIntValue(requestBody, "rV4"), MapUtils.getIntValue(requestBody, "rV5"),
                    MapUtils.getIntValue(requestBody, "fV1"), MapUtils.getIntValue(requestBody, "fV2"),
                    MapUtils.getIntValue(requestBody, "fV3"), MapUtils.getIntValue(requestBody, "fV4"),
                    MapUtils.getIntValue(requestBody, "mV1"), MapUtils.getIntValue(requestBody, "mV2"),
                    MapUtils.getIntValue(requestBody, "mV3"), MapUtils.getIntValue(requestBody, "mV4"),
                    rewrite);
            commitTx(ts);
        } catch (Exception e) {
            logger.error(String.format("保存 %s RFM 值失败...", organization == null ? company : organization), e);
            rollbackTx(ts);
            throw e;
        }
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/{channel}/load/byId.json")
    public Map<String, Object> loadById(@PathVariable(name = "channel") String channel,
                                        @RequestBody Map<String, String> requestBody,
                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadById(requestBody=%s)", requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        OrganizationEntity com = loginUser.getCompany().get();
        Integer orgId = MapUtils.getInteger(requestBody, "orgId", -1);
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        java.util.Optional<RFM4OrgEntity> rfm;
        if (com.getId().equals(orgId)) {
            rfm = getBean(RFM4OrgEntityAction.class, request).loadComOrOrgRFM(com);
        } else if (orgId != -1) {
            Optional<List<OrganizationEntity>> orgOpt = getBean(OrganizationEntityAction.class, request)
                    .findOrgByIds(com.getId(), Lists.newArrayList(orgId));
            Preconditions.checkState(orgOpt.isPresent());
            rfm = getBean(RFM4OrgEntityAction.class, request).loadComOrOrgRFM(orgOpt.get().get(0));
        } else {
            Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, com);
            Preconditions.checkState(store.isPresent(), "ID=%s 对应的公司不存在...", storeId);
            rfm = getBean(RFM4OrgEntityAction.class, request).loadStoreRFM(store.get());
        }
        return rfm.isPresent() ? wrapperResponse(rfm.get().toView()) : wrapperEmptyResponse();
    }

}
