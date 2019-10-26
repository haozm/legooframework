package com.csosm.module.storeview;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.storeview.entity.StoreViewEntity;
import com.csosm.module.storeview.entity.StoreViewEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller(value = "crmStoreViewController")
@RequestMapping(value = "/inner")
public class CrmAdapterController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CrmAdapterController.class);

    @RequestMapping(value = "/storeview/recharge/{companyId}/all.json")
    @ResponseBody
    public Map<String, Object> loadAllReChargeStoreView(@PathVariable(value = "companyId") Integer companyId,
                                                        HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadByStore(url=%s)", request.getRequestURL()));
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent(), "不存在 companyId = %s 对应的公司", companyId);
        List<StoreViewEntity> storeViews = getBean(StoreViewEntityAction.class, request)
                .loadSmsRechargeTree(company.get(), LoginUserContext.anonymous());
        List<Map<String, Object>> datas = Lists.newArrayList();
        for (StoreViewEntity $it : storeViews) {
            Map<String, Object> data = Maps.newHashMap();
            data.put("id", $it.getId());
            data.put("cId", companyId);
            data.put("pId", $it.getParentId());
            data.put("name", $it.getNodeName());
            data.put("sIds", $it.getStoreIds());
            datas.add(data);
        }
        return wrapperResponse(datas);
    }

}
