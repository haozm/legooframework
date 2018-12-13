package com.legooframework.model.organization.mvc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.organization.entity.EquipmentEntity;
import com.legooframework.model.organization.entity.EquipmentEntityAction;
import com.legooframework.model.organization.service.EquipmentService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController(value = "equipmentController")
@RequestMapping(value = "/equ")
public class EquipmentController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentController.class);

    @PostMapping(value = "/enabled/switch/control.json")
    public JsonMessage enbaledOrDisbaledDevice(@RequestBody Map<String, String> datas,
                                               HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        getLoginContext();
        String deviceId = MapUtils.getString(datas, "deviceId", null);
        Preconditions.checkArgument(StringUtils.isNotBlank(deviceId), "设备ID(deviceId)不可以为空值.");
        boolean action_val = MapUtils.getBoolean(datas, "action");
        if (action_val) {
            getBean(EquipmentService.class, request).enbaledDevice(deviceId);
        } else {
            getBean(EquipmentService.class, request).disabledDevice(deviceId);
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    // 统计在线设备数量
    @RequestMapping(value = "/online/amount.json")
    public JsonMessage onlineDeviceCount(HttpServletRequest request) throws Exception {
        LoginContext loginContext = getLoginContext();
        Optional<List<EquipmentEntity>> devices = getBean(EquipmentEntityAction.class, request)
                .loadAllByLoginUser(loginContext);
        Map<String, Object> map = Maps.newHashMap();
        map.put("online", 0);
        map.put("offline", 0);
        if (devices.isPresent()) {
            List<String> ids = devices.get().stream().map(EquipmentEntity::getId).collect(Collectors.toList());
            Map<String, Object> params = Maps.newHashMap();
            params.put("ids", ids);
            Optional<List<Map<String, Object>>> datas = getJdbcQuerySupport(request)
                    .queryForList("equipment", "onlineview", params);
            datas.ifPresent(x -> x.forEach(r -> map.put(MapUtils.getString(r, "status"),
                    MapUtils.getIntValue(r, "amount"))));
        }
        return JsonMessageBuilder.OK().withPayload(map).toMessage();
    }


    private JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("orgJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

}
