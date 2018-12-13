package com.legooframework.model.dict.service;

import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.event.MessageHelper;
import com.legooframework.model.dict.dto.KvTypeDictDto;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import com.legooframework.model.dict.event.DictEventFactory;
import com.legooframework.model.dict.event.DictModuleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;
import java.util.Optional;

/**
 * 模块事件监听
 */
public class EventListenerService extends DictService {

    private static final Logger logger = LoggerFactory.getLogger(EventListenerService.class);

    public Message<?> handleMessage(@Header(name = "loginContext") LoginContext loginContext,
                                    @Payload DictModuleEvent event) {
        LoginContextHolder.setCtx(loginContext);
        logger.info(event.toString());
        try {
            if (DictEventFactory.isLoadDictByTypeEvent(event)) {
                Optional<List<KvDictEntity>> optional =
                        getBean(KvDictEntityAction.class).findByType(event.getDictType());
                if (optional.isPresent()) {
                    KvTypeDictDto typeDictDto = new KvTypeDictDto(optional.get(), event.getDictType());
                    return MessageHelper.buildResponse(event, Optional.of(typeDictDto));
                } else {
                    return MessageHelper.buildResponse(event, Optional.empty());
                }
            }
        } catch (Exception e) {
            return MessageHelper.buildException(event, e);
        }
        return MessageHelper.buildResponse(event, Optional.empty());
    }

}
