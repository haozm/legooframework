package com.csosm.module.webchat;

import com.csosm.commons.event.EventBusSubscribe;
import com.csosm.commons.mvc.HttpMessage;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.commons.util.MyWebUtil;
import com.csosm.module.base.CacheMvcController;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.webchat.entity.DevicesEntity;
import com.csosm.module.webchat.entity.DevicesEntityAction;
import com.csosm.module.webchat.entity.RemoteCmdAction;
import com.csosm.module.webchat.event.StoreBildDeviceEvent;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class DeviceService extends AbstractBaseServer implements EventBusSubscribe {

    private static final Logger logger = LoggerFactory.getLogger(CacheMvcController.class);

    RestTemplate getRestClient() {
        return getBean("wechatRestTemplate", RestTemplate.class);
    }
    
    void changeDviceByStore(String oldDeviceid, String newDeivceId, Integer storeId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newDeivceId), "待绑定的设备ID不可以为空....");

        Optional<DevicesEntity> device = getBean(DevicesEntityAction.class).findByDeviceId(oldDeviceid);
        Preconditions.checkState(device.isPresent(), "解绑的设备DeviceId= %s 不存在", oldDeviceid);

        Integer companyId = device.get().getCompanyId();
        Optional<OrganizationEntity> organization = getBean(OrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(organization.isPresent(), "id=%d 对应的公司不存在...", companyId);
        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findStoreFromCompany(storeId, organization.get());
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);
        Preconditions.checkState(device.get().getStoreId().equals(store.get().getId()), "解绑的设备Id=%s 与当前门店信息不一致...");

        //Optional<DevicesEntity> newDevice = getBean(DevicesEntityAction.class).findByDeviceIdWithAll(newDeivceId);
        //Preconditions.checkState(!newDevice.isPresent(), "newDeivceId=%s已经被使用，无法再次激活...", newDeivceId);

        getBean(DevicesEntityAction.class).changeDeivce(device.get().getId(), newDeivceId);
        changeDevice(device.get(), newDeivceId, store.get());
        Optional<DevicesEntity> _dv = getBean(DevicesEntityAction.class).findByDeviceId(newDeivceId);
        Preconditions.checkState(_dv.isPresent(), "持久化数据异常，deviceId=%s对应的设备信息获取失败...", newDeivceId);
        getBean(RemoteCmdAction.class).bildStoreCmd(_dv.get(), null);
    }

    void bildDviceToStore(String pinCode, String deviceId, Integer companyId, Integer storeId, String imei) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "待绑定的设备ID不可以为空....");
        Optional<OrganizationEntity> organization = getBean(OrganizationEntityAction.class).findCompanyById(companyId);
        Preconditions.checkState(organization.isPresent(), "id=%d 对应的公司不存在...", companyId);
        checkPinCode(pinCode, organization.get());

        Optional<StoreEntity> store = getBean(StoreEntityAction.class).findStoreFromCompany(storeId, organization.get());
        Preconditions.checkState(store.isPresent(), "Id=%s 对应的门店不存在....", storeId);
        bildDviceToStore(pinCode, deviceId, store.get(), imei);
    }

    private void bildDviceToStore(String pinCode, String deviceId, StoreEntity store, String imei) {
        int res = getBean(DevicesEntityAction.class).bildDeviceToStore(store, deviceId);
        if (res == 1) {
            // getBean(DeviceActiveDetailAction.class).recodeActiveDetail(store, pinCode, device.getId());
            Optional<DevicesEntity> device = getBean(DevicesEntityAction.class).findByDeviceId(deviceId);
            Preconditions.checkState(device.isPresent(), "持久化数据异常，deviceId=%s对应的设备信息获取失败...", deviceId);
            // 写入指令库，指定绑定门店指令
            getBean(RemoteCmdAction.class).bildStoreCmd(device.get(), null);
            activePinCode(pinCode, device.get(), imei);
        } else {
            logger.warn(String.format("重复绑定设备到门店动作 device= %s: storeId= %s", deviceId, store.getId()));
            Optional<DevicesEntity> device = getBean(DevicesEntityAction.class).findByDeviceId(deviceId);
            Preconditions.checkState(device.isPresent(), "id=%s 对应的门店不存在....", deviceId);
            getAsyncEventBus().post(new StoreBildDeviceEvent(device.get(), store, null));
        }
    }

    @Subscribe
    public void handleStoreBildDeviceEvent(StoreBildDeviceEvent event) {
        if (logger.isDebugEnabled()) logger.debug(String.format("Subscribe Event %s", event));
        getBean(RemoteCmdAction.class).bildStoreCmd(event.getDevice(), event.getUserContext().orNull());
    }

    private void checkPinCode(String code, OrganizationEntity company) {
        String url = String.format("http://%s/regiscenter/api/regcnt/{pinCode}/{companyId}/check4use.json", centerUrl);
        ResponseEntity<String> result = getRestClient().postForEntity(url,
                null, String.class, code, company.getId());
        Preconditions.checkState(result.getStatusCode() == HttpStatus.OK, "远程校验pinCode 发生网络异常 HttpStatus=%s...",
                result.getStatusCode());
        HttpMessage httpMessage = MyWebUtil.decode4HttpMessage(result.getBody());
        Preconditions.checkState(httpMessage.isOK(), httpMessage.getErrDetail());
    }

    private void activePinCode(String code, DevicesEntity devices, String imei) {
        String url = String.format("http://%s/regiscenter/api/regcnt/active.json", centerUrl);
        Map<String, Object> params = Maps.newHashMap();
        if (imei != null) params.put("imei", imei);
        params.put("pincode", code);
        params.put("deviceId", devices.getId());
        params.put("storeId", devices.getStoreId());
        getRestClient().postForEntity(url, params, String.class);
    }

    private void changeDevice(DevicesEntity devices, String newDeviceId, StoreEntity store) {
        String url = String.format("http://%s/regiscenter/api/regcnt/%s/%s/%s/changedevice.json", centerUrl,
                devices.getId(), newDeviceId, store.getId());
        getRestClient().postForEntity(url, Maps.newHashMap(), String.class);
    }

    Map<String, Object> loadActiveDetail(String devideId) {
        String url = String.format("http://%s/regiscenter/api/regcnt/load/%s/detail.json", centerUrl, devideId);
        if (logger.isDebugEnabled())
            logger.debug(String.format("http://%s/regiscenter/api/regcnt/load/%s/detail.json", centerUrl, devideId));
        ResponseEntity<String> res = getRestClient().postForEntity(url, Maps.newHashMap(), String.class);
        String body = res.getBody();
        if (logger.isDebugEnabled())
            logger.debug(String.format("[loadActiveDetail(%s)][response]:%s", devideId, body));
        JsonObject message = (JsonObject) new JsonParser().parse(body);
        String code = message.get("code").getAsString();
        if (StringUtils.equals("0000", code)) {
            JsonObject payload = message.getAsJsonObject("data");
            Map<String, Object> map = Maps.newHashMap();
            map.put("deviceId", payload.get("deviceId").isJsonNull() ? null : payload.get("deviceId").getAsString());
            map.put("companyId", payload.get("companyId").getAsInt());
//            map.put("storeId", payload.get("storeId").isJsonNull() ? null : payload.get("storeId").getAsInt());
            map.put("activeDate", payload.get("activeDate").isJsonNull() ? null : payload.get("activeDate").getAsString());
            return map;
        }
        throw new RuntimeException(message.get("msg").getAsString());
    }

    private String centerUrl;

    public void setCenterUrl(String centerUrl) {
        this.centerUrl = centerUrl;
    }

}
