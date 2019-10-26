package com.csosm.module.webchat;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.cache.GuavaCacheManager;
import com.csosm.module.base.entity.DeviceNetCfgEntity;
import com.csosm.module.base.entity.DeviceNetCfgEntityAction;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.OrganizationEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.webchat.entity.DevicesEntity;
import com.csosm.module.webchat.entity.DevicesEntityAction;
import com.csosm.module.webchat.entity.RemoteCmdAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.crmadapter.entity.UploadInformationAction;

@Controller(value = "deviceController")
@RequestMapping("/device")
public class DeviceMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DeviceMvcController.class);

    @RequestMapping(value = "/unread/msg/total.json")
    @ResponseBody
    public Map<String, Object> unReadMsgTotal(@RequestBody Map<String, String> http_request_map, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("unReadMsgTotal( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        if (userContext.getStore().isPresent()) {
            List<DevicesEntity> devices = getBean(DevicesEntityAction.class, request).loadGodDeviceByStore(userContext.getStore().get());
            if (CollectionUtils.isEmpty(devices)) return wrapperResponse(new String[0]);
            DevicesEntity device = devices.get(0);
            if (!device.getWeixin().isPresent()) return wrapperResponse(new String[0]);
            Map<String, Object> params = Maps.newHashMap();
            params.put("companyId", userContext.getCompany().get().getId());
            params.put("storeId", userContext.getStore().get().getId());
            params.put("weixinId", device.getWeixin().get());
            params.put("tableName", String.format("CONTACT_%s_%s", userContext.getCompany().get().getId(), userContext.getStore().get().getId()));
            Optional<List<Map<String, Object>>> unread_msg = queryEngineService.queryForList("webchat", "unReadMsgTotal", params);
            return wrapperResponse(unread_msg.isPresent() ? unread_msg.get() : new String[0]);
        }
        return wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/bystore/status.json")
    @ResponseBody
    public Map<String, Object> loadStatusByStore(@RequestBody Map<String, String> http_request_map,
                                                 HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadStatusByStore( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Map<String, Object> params = userContext.toMap();
        Integer storeId = MapUtils.getInteger(http_request_map, "int_storeId");
        Preconditions.checkNotNull(storeId, "入参 int_storeId 不可以为空值...");
        params.put("storeId", storeId);
        Map<String, Object> data = Maps.newHashMap();
        data.put("deviceOnline", false);
        data.put("weixinOnline", false);
        Optional<Map<String, Object>> result_set = queryEngineService.queryForMap("devices", "onlines", params);
        if (result_set.isPresent()) data.putAll(result_set.get());
        return wrapperResponse(data);
    }
    
    @RequestMapping(value = "/change/device.json")
    @ResponseBody
    public Map<String, Object> changeDevice(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String oldDeivceId = MapUtils.getString(requestBody, "oldDeivceId");
        String newDeivceId = MapUtils.getString(requestBody, "newDeivceId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(newDeivceId), "入参 newDeivceId 不可以为空值...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(oldDeivceId), "入参 oldDeivceId 不可以为空值...");
        Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空值...");
        getBean(DeviceService.class, request).changeDviceByStore(oldDeivceId, newDeivceId, storeId);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/bindstore/byone.json")
    @ResponseBody
    public Map<String, Object> deviceBindStore(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]deviceBildStore( requestBody = %s)", request.getRequestURI(), requestBody));
        int companyId = MapUtils.getIntValue(requestBody, "companyId", -1);
        Preconditions.checkArgument(-1 != companyId, "入参 companyId 不可以为空值...");
        int storeId = MapUtils.getIntValue(requestBody, "storeId", -1);
        Preconditions.checkArgument(-1 != storeId, "入参 storeId 不可以为空值...");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(storeId);
        Preconditions.checkState(store.isPresent());
        Optional<DeviceNetCfgEntity> config = getBean(DeviceNetCfgEntityAction.class, request).loadDeviceNetCfg(store.get());
        Preconditions.checkState(config.isPresent(), "当前公司尚未配置通信参数，无法执行后续操作...");
        String pinCode = MapUtils.getString(requestBody, "pinCode");
        String deviceId = MapUtils.getString(requestBody, "deviceId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "入参 deviceId 不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(pinCode), "入参 pinCode 不可以为空...");
        String imei = MapUtils.getString(requestBody, "imei");
        getBean(DeviceService.class, request).bildDviceToStore(pinCode, deviceId, companyId, storeId, imei);
        getBean(GuavaCacheManager.class, request).clearByName("adapterCache");
        Map<String, Object> res = config.get().toMap();

        Map<String, Object> activeInfo = getBean(DeviceService.class, request).loadActiveDetail(deviceId);
        res.put("bindDate", MapUtils.getString(activeInfo, "activeDate"));

        res.put("qiniu", getBean(UploadInformationAction.class, request).getUploadInfo(store.get().getExistCompanyId()));
        return wrapperResponse(res);
    }

    @RequestMapping(value = "/check/bydevice.json")
    @ResponseBody
    public Map<String, Object> checkByDeviceId(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        String deviceId = MapUtils.getString(requestBody, "deviceId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "请提供deviceId值...");
        Optional<DevicesEntity> device = getBean(DevicesEntityAction.class, request).findByDeviceId(deviceId);
        Preconditions.checkState(device.isPresent(), "该设备不存在或者尚未绑定门店...");

        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findById(device.get().getStoreId());
        Preconditions.checkState(store.isPresent(), "数据异常,id=%s 对应的门店信息不存在...", device.get().getStoreId());

        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request)
                .findCompanyById(device.get().getCompanyId());
        Preconditions.checkState(company.isPresent(), "数据异常,id=%s 对应的公司信息不存在...", device.get().getStoreId());

        Optional<DeviceNetCfgEntity> config = getBean(DeviceNetCfgEntityAction.class, request).loadDeviceNetCfg(store.get());
        Preconditions.checkState(config.isPresent(), "当前公司尚未配置通信参数，无法执行后续操作...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", company.get().getId());
        params.put("companyName", company.get().getName());
        params.put("storeId", store.get().getId());
        params.put("storeName", store.get().getName());

        Map<String, Object> activeInfo = getBean(DeviceService.class, request).loadActiveDetail(device.get().getId());
        params.put("bindDate", MapUtils.getString(activeInfo, "activeDate"));

        params.put("config", config.get().toMap());
        params.put("qiniu", getBean(UploadInformationAction.class, request).getUploadInfo(device.get().getCompanyId()));
        return wrapperResponse(params);
    }

    @RequestMapping(value = "/config/bydeviceId.json")
    @ResponseBody
    public Map<String, Object> loadNwtConfigByDevice(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadNwtConfigByDevice( requestBody = %s)", request.getRequestURI(), requestBody));
        String deviceId = MapUtils.getString(requestBody, "deviceId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "请提供deviceId值...");
        Optional<DevicesEntity> devices = getBean(DevicesEntityAction.class, request).findByDeviceId(deviceId);
        Preconditions.checkState(devices.isPresent(), "deviceId=%s 对应的设备不存在 ...", deviceId);
        Optional<OrganizationEntity> company = getBean(OrganizationEntityAction.class, request)
                .findCompanyById(devices.get().getCompanyId());
        Preconditions.checkState(company.isPresent(), "id=%s对应的公司不存在....");
        Optional<StoreEntity> store = getBean(StoreEntityAction.class, request)
                .findStoreFromCompany(devices.get().getStoreId(), company.get());
        Preconditions.checkState(store.isPresent());
        Optional<DeviceNetCfgEntity> config = getBean(DeviceNetCfgEntityAction.class, request).loadDeviceNetCfg(store.get());
        Preconditions.checkState(config.isPresent(), "当前公司尚未配置通信参数，无法执行后续操作...");
        Map<String, Object> map = config.get().toMap();
        Map<String, Object> activeInfo = getBean(DeviceService.class, request).loadActiveDetail(devices.get().getId());
        map.put("bindDate", MapUtils.getString(activeInfo, "activeDate"));
        map.put("qiniu", getBean(UploadInformationAction.class, request).getUploadInfo(company.get().getId()));
        return wrapperResponse(map);
    }

    @RequestMapping(value = "/manager/list.json")
    @ResponseBody
    public Map<String, Object> deviceMsgList(@RequestBody Map<String, String> http_request_map, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgList( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Map<String, Object> params = userContext.toMap();
        String storeIds = MapUtils.getString(http_request_map, "storeIds");
        params.put("storeIds", Strings.isNullOrEmpty(storeIds) ? null : StringUtils.split(storeIds, ','));
        Integer deviceOnline = MapUtils.getInteger(http_request_map, "deviceOnline");
        if (null != deviceOnline) params.put("deviceOnline", deviceOnline);
        Integer weixinOnline = MapUtils.getInteger(http_request_map, "weixinOnline");
        if (null != weixinOnline) params.put("weixinOnline", weixinOnline);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("devices", "mnglist", params);
        return query_res.isPresent() ? wrapperResponse(query_res.get()) : wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/manager/sublist.json")
    @ResponseBody
    public Map<String, Object> deviceMsgSubList(@RequestBody Map<String, String> http_request_map, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgSubList( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Map<String, Object> params = userContext.toMap();
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Preconditions.checkNotNull(storeId, "门店storeId不可以空值...");
        params.put("storeId", storeId);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("devices", "sub_mnglist", params);
        return query_res.isPresent() ? wrapperResponse(query_res.get()) : wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/remote/command.json")
    @ResponseBody
    public Map<String, Object> remoteCommand(@RequestBody Map<String, String> http_request_map, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("remoteCommand( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        String command = MapUtils.getString(http_request_map, "command");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(command), "入参 command 不可以为空...");
        String[] cmd_a = new String[]{"clearunclaim", "getcontact", "clearcontact", "uploadlog", "reboot",
                "setMainParamByLoginUser"};
        String[] cmd_b = new String[]{"initstore", "setMainParam"};
        String deviceIds = MapUtils.getString(http_request_map, "deviceIds");
        Integer storeId = MapUtils.getInteger(http_request_map, "storeId");
        Integer companyId = MapUtils.getInteger(http_request_map, "companyId");

        if (ArrayUtils.contains(cmd_a, command)) {
            Optional<List<DevicesEntity>> devices = getBean(DevicesEntityAction.class, request).
                    findGodDeviceByIds(userContext.getCompany().get(), StringUtils.split(deviceIds, ','));
            Preconditions.checkState(devices.isPresent());
            if (cmd_a[0].equals(command)) {
                getBean(RemoteCmdAction.class, request).clearUnclaimCmd(userContext, devices.get());
            } else if (cmd_a[1].equals(command)) {
                getBean(RemoteCmdAction.class, request).getContactCmd(userContext, devices.get());
            } else if (cmd_a[2].equals(command)) {
                getBean(RemoteCmdAction.class, request).clearContactCmd(userContext, devices.get());
            } else if (cmd_a[3].equals(command)) {
                getBean(RemoteCmdAction.class, request).uploadLogCmd(userContext, devices.get());
            } else if (cmd_a[4].equals(command)) {
                getBean(RemoteCmdAction.class, request).rebootCmd(userContext, devices.get());
            } else if (cmd_a[5].equals(command)) {
                getBean(RemoteCmdAction.class, request).setMainParam(userContext, devices.get());
            }
        } else if (ArrayUtils.contains(cmd_b, command)) {
            if (cmd_b[0].equals(command)) {
                Preconditions.checkState(userContext.getCompany().isPresent(), "当前登陆用户无公司信息....");
                String storeIds = MapUtils.getString(http_request_map, "storeIds");
                Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "入参 storeIds 不可以为空...");
                List<Integer> store_ids = Lists.newArrayList();
                for (String $it : StringUtils.split(storeIds, ',')) store_ids.add(Integer.valueOf($it));
                Optional<List<StoreEntity>> stores = getBean(StoreEntityAction.class, request).findByIds(store_ids);
                Preconditions.checkState(stores.isPresent(), "%s 对应的门店不存在...", storeIds);
                Optional<List<DevicesEntity>> devices = getBean(DevicesEntityAction.class, request).
                        findGodDeviceByIds(userContext.getCompany().get(), StringUtils.split(deviceIds, ','));
                Preconditions.checkState(devices.isPresent());
                getBean(RemoteCmdAction.class, request).initStoreCmd(userContext, devices.get());
                // 主动clear-Cache
                getBean(GuavaCacheManager.class, request).clearByName("adapterCache");
            } else if (cmd_b[1].equals(command)) {
                Preconditions.checkNotNull(storeId, "入参 storeId 不可以为空...");
                Preconditions.checkNotNull(companyId, "入参 companyId 不可以为空...");
                Optional<OrganizationEntity> com = getBean(OrganizationEntityAction.class, request).findCompanyById(companyId);
                Preconditions.checkState(com.isPresent(), "ID=%s对应的公司不存在...", companyId);
                Optional<StoreEntity> store = getBean(StoreEntityAction.class, request).findStoreFromCompany(storeId, com.get());
                Preconditions.checkState(store.isPresent(), "ID=%s对应的公司不存在...", storeId);
                Optional<List<DevicesEntity>> devs = getBean(DevicesEntityAction.class, request).findGodDeviceByStore(store.get());
                Preconditions.checkState(devs.isPresent(), "当前门店无绑定微信，下发失败...");
                getBean(RemoteCmdAction.class, request).setMainParam(userContext, devs.get());
            }
        } else {
            return wrapperErrorResponse(null, String.format("非法的指令:%s", command));
        }
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/load/cmds/bydevice.json")
    @ResponseBody
    public Map<String, Object> loadCmdsByDevice(@RequestBody Map<String, String> http_request_map, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadCmdsByDevice( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        String deviceId = MapUtils.getString(http_request_map, "deviceId");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        Optional<List<Map<String, Object>>> cmds = getBean("queryEngineService", QueryEngineService.class, request)
                .queryForList("devices", "findCmdListByDevice", params);
        if (!cmds.isPresent()) return wrapperEmptyResponse();
        return wrapperResponse(cmds.get());
    }

    @RequestMapping(value = "/manager/amount.json")
    @ResponseBody
    public Map<String, Object> deviceMsgAmount(@RequestBody Map<String, String> http_request_map,
                                               HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("deviceMsgAmount( http_request_map = %s)", http_request_map));
        LoginUserContext userContext = loadLoginUser(request);
        Map<String, Object> params = userContext.toMap();
        String storeIds = MapUtils.getString(http_request_map, "storeIds");
        params.put("storeIds", !Strings.isNullOrEmpty(storeIds) ? StringUtils.split(storeIds, ',') : null);
        Optional<List<Map<String, Object>>> query_res = queryEngineService.queryForList("devices", "amount_manage", params);

        Map<String, Object> amount = Maps.newHashMap();
        amount.put("allDevice", 0);
        amount.put("onlineDevice", 0);

        amount.put("allWeixin", 0);
        amount.put("onlineWeixin", 0);

        amount.put("allSubDevice", 0);
        amount.put("onlineSubDevice", 0);
        if (query_res.isPresent())
            for (Map<String, Object> $it : query_res.get()) {
                if (StringUtils.equals("device", MapUtils.getString($it, "subType"))) {
                    amount.put("allDevice", MapUtils.getIntValue(amount, "allDevice", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineDevice", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("weixin", MapUtils.getString($it, "subType"))) {
                    amount.put("allWeixin", MapUtils.getIntValue(amount, "allWeixin", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineWeixin", MapUtils.getIntValue($it, "amount", 0));
                } else if (StringUtils.equals("subDevice", MapUtils.getString($it, "subType"))) {
                    amount.put("allSubDevice", MapUtils.getIntValue(amount, "allSubDevice", 0) +
                            MapUtils.getIntValue($it, "amount", 0));
                    if (MapUtils.getIntValue($it, "status", -1) == 1)
                        amount.put("onlineSubDevice", MapUtils.getIntValue($it, "amount", 0));
                }
            }
        return wrapperResponse(amount);
    }

    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;

}
