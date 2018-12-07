package com.legooframework.model.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.legooframework.model.base.runtime.LoginContext;
import org.apache.commons.collections4.MapUtils;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Optional;

public abstract class WkSessionUtil {

    private static final Logger logger = LoggerFactory.getLogger(WkSessionUtil.class);

    private static final String ATR_STATUS = "status";
    private static final String ATR_TOKEN = "cssessionid";
    private static final String ATR_USER = "user";
    private static final String VAL_STATUS_ONLINE = "online";
    private static final String VAL_STATUS_OFFLINE = "offline";
    private static Splitter.MapSplitter MAPSPLITTER = Splitter.on('&').withKeyValueSeparator('=');

    public static String getStatus(WebSocketSession session) {
        return MapUtils.getString(session.getAttributes(), ATR_STATUS, VAL_STATUS_OFFLINE);
    }

    public static void colse(WebSocketSession session) {
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.NORMAL);
            } catch (Exception e) {
                logger.error(String.format("colse(WebSocketSession = %s )异常.", session.getId()), e);
            }
        }
    }

    public static Optional<String> getToken(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        if (Strings.isNotBlank(query)) {
            Map<String, String> data = MAPSPLITTER.split(query);
            String token = MapUtils.getString(data, ATR_TOKEN, null);
            return Optional.ofNullable(token);
        }
        return Optional.empty();
    }

    public static String getToken(WebSocketSession session) {
        String _token = MapUtils.getString(session.getAttributes(), ATR_TOKEN);
        Preconditions.checkNotNull(_token, "无法从 WebSocketSession 获取当前授权Token....");
        return _token;
    }

    public static boolean isOnline(WebSocketSession session) {
        String _status = getStatus(session);
        return VAL_STATUS_ONLINE.equals(_status);
    }

    public static void setToken(Map<String, Object> attributes, String token) {
        attributes.put(ATR_TOKEN, token);
    }

    public static void setUser(Map<String, Object> attributes, LoginContext loginContext) {
        attributes.put(ATR_USER, loginContext);
    }

    public static LoginContext getUser(WebSocketSession session) {
        return (LoginContext) session.getAttributes().get(ATR_USER);
    }

    public static void setOnlineStatus(Map<String, Object> attributes) {
        attributes.put(ATR_STATUS, VAL_STATUS_ONLINE);
    }

    public static void setOfflineStatus(Map<String, Object> attributes) {
        attributes.put(ATR_STATUS, VAL_STATUS_OFFLINE);
    }

}
