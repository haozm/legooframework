package com.legooframework.model.devices.service;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.LegooEvent;
import com.legooframework.model.core.event.MessageHelper;
import com.legooframework.model.devices.entity.DeviceEntity;
import com.legooframework.model.devices.entity.DeviceEntityAction;
import com.legooframework.model.devices.event.DeviceEventFactory;
import com.legooframework.model.devices.event.DeviceModuleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Optional;

public class EventListenerService extends DeviceService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    public Message<?> handleMessage(@Header(name = "loginContext") LoginContext loginContext,
                                    @Payload LegooEvent legooEvent) {
        LoginContextHolder.setCtx(loginContext);
        if (logger.isDebugEnabled())
            logger.debug(legooEvent.toString());
        try {
            if (DeviceEventFactory.isLoadDeviceByImeiEvent(legooEvent)) {
                DeviceModuleEvent deviceModuleEvent = (DeviceModuleEvent) legooEvent;
                Optional<DeviceEntity> optional = getBean(DeviceEntityAction.class).findByImei(deviceModuleEvent.getImei());
                if (logger.isDebugEnabled())
                    logger.debug("load device[{}] by imei success", optional);
                return MessageHelper.buildResponse(legooEvent, optional);
            } else if (DeviceEventFactory.isLoadDeviceDtoByIdEvent(legooEvent)) {
                DeviceModuleEvent deviceModuleEvent = (DeviceModuleEvent) legooEvent;
                Optional<DeviceEntity> optional = getBean(DeviceEntityAction.class).findById(deviceModuleEvent.getId());
                if (!optional.isPresent())
                    return MessageHelper.buildResponse(legooEvent, Optional.empty());
                if (logger.isDebugEnabled())
                    logger.debug("load device[{}] by id success", optional);
                return MessageHelper.buildResponse(legooEvent, Optional.of(optional.get().createDto()));
            } else if (DeviceEventFactory.isSaveOrUpdateDeviceEvent(legooEvent)) {
                DeviceModuleEvent event = (DeviceModuleEvent) legooEvent;
                DeviceEntity entity = getBean(DeviceEntityAction.class).saveOrUpdate(event.getImei(),
                        event.getName(), event.getBrand(), event.getModel(), event.getColor(),
                        event.getCpu(), event.getMemorySize(), event.getOs(), event.getXportOs(),
                        event.getScreenSize(), event.getOsType(),
                        event.getPrice(), event.getProductionDate(), event.getRepairReason(), event.getScrapReason(), null, null);
                if (logger.isInfoEnabled())
                    logger.info("save or update device[{}] sucess", entity);
                return MessageHelper.buildResponse(legooEvent, Optional.of(entity));
            } else if (DeviceEventFactory.isChangeDeviceToNormalEvent(legooEvent)) {
                DeviceModuleEvent event = (DeviceModuleEvent) legooEvent;
                DeviceEntity entity = getBean(DeviceEntityAction.class).normalAction(event.getImei());
                if (logger.isDebugEnabled())
                    logger.debug("change device[{}] state to normal state sucess", entity);
                return MessageHelper.buildResponse(event, Optional.of(entity));
            } else if (DeviceEventFactory.isChangeDeviceToRepairEvent(legooEvent)) {
                DeviceModuleEvent event = (DeviceModuleEvent) legooEvent;
                DeviceEntity entity = getBean(DeviceEntityAction.class).repairAction(event.getImei());
                if (logger.isDebugEnabled())
                    logger.debug("change device[{}] state to repair state sucess", entity);
                return MessageHelper.buildResponse(event, Optional.of(entity));
            } else if (DeviceEventFactory.isChangeDeviceToScrapEvent(legooEvent)) {
                DeviceModuleEvent event = (DeviceModuleEvent) legooEvent;
                DeviceEntity entity = getBean(DeviceEntityAction.class).scrapAction(event.getImei());
                if (logger.isDebugEnabled())
                    logger.debug("change device[{}] state to scrap state sucess", entity);
                return MessageHelper.buildResponse(event, Optional.of(entity));
            }
        } catch (Exception e) {
            return MessageHelper.buildException(legooEvent, e);
        }
        return MessageHelper.buildResponse(legooEvent, Optional.empty());
    }
}
