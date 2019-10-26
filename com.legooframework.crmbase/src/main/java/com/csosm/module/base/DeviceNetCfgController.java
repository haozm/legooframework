package com.csosm.module.base;

import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.*;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller(value = "DeviceNetCfgController")
@RequestMapping("/netconfig")
public class DeviceNetCfgController extends BaseController {

    @RequestMapping(value = "/load/bystore.json")
    @ResponseBody
    public Map<String, Object> loadAllByCompany(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Integer companyId = MapUtils.getIntValue(requestBody, "companyId");
        Preconditions.checkNotNull(companyId, "入参 companyId 不可为空值...");
        Optional<OrganizationEntity> com = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(com.isPresent(), "不存在ID=%s 对应的公司", companyId);
        Integer storeId = MapUtils.getIntValue(requestBody, "storeId");
        Preconditions.checkNotNull(storeId, "入参 storeId 不可为空值...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, com.get());
        Preconditions.checkState(store.isPresent(), "不存在ID=%s 对应的门店", storeId);
        Optional<DeviceNetCfgEntity> optional = getBean(DeviceNetCfgEntityAction.class, request).loadDeviceNetCfg(store.get());
        if (!optional.isPresent()) return wrapperEmptyResponse();
        return wrapperResponse(optional.get().toViewMap());
    }


    @RequestMapping(value = "/saveOrUpdate.json")
    @ResponseBody
    public Map<String, Object> addNetConfig(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Integer storeId = MapUtils.getIntValue(requestBody, "storeId");
        Integer companyId = MapUtils.getIntValue(requestBody, "companyId");
        Integer udpPageSize = MapUtils.getIntValue(requestBody, "udpPageSize");
        Integer msgDelayTime = MapUtils.getIntValue(requestBody, "msgDelayTime");
        Integer keepliveDelayTime = MapUtils.getIntValue(requestBody, "keepliveDelayTime");
        Preconditions.checkNotNull(companyId);
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
        Preconditions.checkState(company.isPresent());
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, company.get());
        Preconditions.checkState(store.isPresent());

        Optional<DeviceNetCfgEntity> store_netcfg = getBean(DeviceNetCfgEntityAction.class, request).loadDeviceNetCfg(store.get());
        Preconditions.checkState(store_netcfg.isPresent());

        if (store_netcfg.get().hasStore()) {
            getBean(DeviceNetCfgEntityAction.class, request).change(store_netcfg.get().getId(), udpPageSize,
                    msgDelayTime, keepliveDelayTime);
        } else {
            Optional<DeviceNetCfgEntity> com_netcfg = getBean(DeviceNetCfgEntityAction.class, request).loadByCompany(company.get());
            Preconditions.checkState(com_netcfg.isPresent());
            getBean(DeviceNetCfgEntityAction.class, request).addByStore(store.get(), com_netcfg.get(), udpPageSize,
                    msgDelayTime, keepliveDelayTime);
        }
        return wrapperEmptyResponse();
    }

}
