package com.legooframework.model.smsprovider.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class SMSSubAccountEntity extends BaseEntity<String> {

    private final String apikey, username, password;
    private SMSChannel channel;
    private final String httpSendUrl, httpStatusUrl, httpReplayUrl;
    private boolean enabled;
    private final boolean encryptPwd;
    private boolean encoding = false;
    private String httpSendUrlEncode, httpStatusUrlEncode, httpReplayUrlEncode;
    private String smsSuffix;

    SMSSubAccountEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.apikey = ResultSetUtil.getString(res, "apikey");
            this.username = ResultSetUtil.getString(res, "username");
            String _pwd = ResultSetUtil.getString(res, "password");
            this.encryptPwd = StringUtils.startsWith(_pwd, "{MD5}");
            this.password = this.encryptPwd ? _pwd.substring(5) : _pwd;
            this.httpSendUrl = ResultSetUtil.getString(res, "httpSendUrl");
            this.httpStatusUrl = ResultSetUtil.getString(res, "httpStatusUrl");
            this.httpReplayUrl = ResultSetUtil.getString(res, "httpReplayUrl");
            this.channel = SMSChannel.paras(ResultSetUtil.getObject(res, "smsChannel", Integer.class));
            this.encoding = true;
            this.enabled = ResultSetUtil.getBooleanByInt(res, "enabled");
            this.smsSuffix = ResultSetUtil.getOptString(res, "smsSuffix", null);
            Joiner.MapJoiner joiner = Joiner.on('&').withKeyValueSeparator('=');
            this.httpSendUrlEncode = createHttpSendUrl(joiner);
            this.httpStatusUrlEncode = createHttpStatusByMobilesUrl(joiner);
            this.httpReplayUrlEncode = createHttpReplayUrl(joiner);
        } catch (SQLException e) {
            throw new RuntimeException("Restore SMSSubAccountEntity has SQLException", e);
        }
    }

    boolean isChannel(SMSChannel channel) {
        return this.channel == channel;
    }

    public String getHttpSendUrl() {
        return httpSendUrlEncode;
    }

    public String getHttpReplyUrl() {
        return httpReplayUrlEncode;
    }

    public String getUsername() {
        return username;
    }

    boolean isEnabled() {
        return enabled;
    }

    private String createHttpSendUrl(Joiner.MapJoiner joiner) {
        Map<String, String> params = Maps.newHashMap();
        params.put("apikey", apikey);
        params.put("username", username);
        params.put(encryptPwd ? "password_md5" : "password", password);
        params.put("encode", "UTF-8");
        return String.format("%s?%s&mobile={mobile}&content={content}", httpSendUrl, joiner.join(params));
    }

    private String createHttpReplayUrl(Joiner.MapJoiner joiner) {
        Map<String, String> params = Maps.newHashMap();
        params.put("apikey", apikey);
        params.put("username", username);
        params.put(encryptPwd ? "password_md5" : "password", password);
        params.put("encode", "UTF-8");
        return String.format("%s&%s", httpReplayUrl, joiner.join(params));
    }

    private String createHttpStatusByMobilesUrl(Joiner.MapJoiner joiner) {
        Map<String, String> params = Maps.newHashMap();
        params.put("apikey", apikey);
        params.put("username", username);
        params.put(encryptPwd ? "password_md5" : "password", password);
        return String.format("%s?%s", httpStatusUrl, joiner.join(params));
    }

    public String getHttpStatusUrl() {
        return String.format("%s&from={start}&to={end}&mobile={mobile}", httpStatusUrlEncode);
    }

    Optional<String> getSmsSuffix() {
        return Optional.ofNullable(Strings.isNullOrEmpty(smsSuffix) ? null : smsSuffix);
    }

    public String fmtContent(String content) {
        if (!getSmsSuffix().isPresent()) return content;
        if (StringUtils.endsWith(content, this.smsSuffix)) return content;
        return content + this.smsSuffix;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("apikey", apikey)
                .add("username", username)
                .add("password", "********")
                .add("channel", channel)
                .add("httpSendUrl", httpSendUrl)
                .add("httpStatusUrl", httpStatusUrl)
                .add("smsSuffix", smsSuffix)
                .toString();
    }
}
