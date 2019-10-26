package com.legooframework.model.wechatcircle.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WechatCircleEventListeningService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(WechatCircleEventListeningService.class);

//    public void listeningEvent(@Header(name = "loginContext") LoginContext user, @Header(name = "eventName") String eventName,
//                               @Payload WechatCircleEvent wechatCircleEvent) {
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("listeningEvent(user,evnetName:%s,payload:%s)", eventName, wechatCircleEvent));
//        LoginContextHolder.setCtx(user);
//        try {
//            if (StringUtils.equals(WechatCircleEvent.EVENT_WECHATCIRCLE_UNREAD_CMTS, eventName)) {
//                DataSourcesFrom source = wechatCircleEvent.getDataSourcesPayload();
//                CircleUnReadDto circleUnRead = getCommonsService().unreadStatistics(source);
//                getBean(WechatCircleProxyAction.class).wechatcircleUnread(circleUnRead);
//            }
//        } finally {
//            LoginContextHolder.clear();
//        }
//
//    }

}
