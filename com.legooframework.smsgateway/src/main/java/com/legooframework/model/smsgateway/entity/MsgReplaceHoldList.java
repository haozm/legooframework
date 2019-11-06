package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgReplaceHoldList {

    private static final Logger logger = LoggerFactory.getLogger(MsgReplaceHoldList.class);

    private final static String REGEX = "(?<=\\{)[^}]*(?=\\})";
    private final List<MsgReplaceHoldEntity> replaceHolds;
    private final List<String> tokenList;
    private Integer companyId;

    MsgReplaceHoldList(UserAuthorEntity user, List<MsgReplaceHoldEntity> replaceHolds) {
        this.companyId = user.getTenantId().intValue();
        if (CollectionUtils.isEmpty(replaceHolds)) {
            this.tokenList = null;
            this.replaceHolds = null;
        } else {
            this.replaceHolds = Lists.newArrayList(replaceHolds);
            this.tokenList = this.replaceHolds.stream().map(MsgReplaceHoldEntity::getReplaceToken).collect(Collectors.toList());
        }
    }

    MsgReplaceHoldList(OrgEntity company, List<MsgReplaceHoldEntity> replaceHolds) {
        this.companyId = company.getId();
        if (CollectionUtils.isEmpty(replaceHolds)) {
            this.tokenList = null;
            this.replaceHolds = null;
        } else {
            this.replaceHolds = Lists.newArrayList(replaceHolds);
            this.tokenList = this.replaceHolds.stream().map(MsgReplaceHoldEntity::getReplaceToken).collect(Collectors.toList());
        }
    }

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(replaceHolds);
    }

    public String[] replaceMembers(Map<String, Object> params, String template) {
        String _name = MapUtils.getString(params, "会员姓名").trim();
        String _mobile = MapUtils.getString(params, "mobile").trim();
        String _weixinId = MapUtils.getString(params, "weixinId", null);
        String _deviceId = MapUtils.getString(params, "deviceId", null);

        _name = Strings.isNullOrEmpty(_name) ? "会员" : _name;
        _mobile = Strings.isNullOrEmpty(_mobile) ? "0000" : _mobile;
        if (!StringUtils.containsAny(template, '{', '}'))
            return new String[]{_mobile, _name, template, "OK", _weixinId, _deviceId};
        String replaced = "";
        try {
            Preconditions.checkState(!isEmpty(), "该模版含有含有变量，需要替换的模版不存在...");
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            replaced = substitutor.replace(template);
            Preconditions.checkState(!StringUtils.containsAny(replaced, '{', '}'), "模版尚存在{}非法字符串：%s", replaced);
            return new String[]{_mobile, _name, replaced, "OK", _weixinId, _deviceId};
        } catch (Exception e) {
            logger.error("replacereplace(%s ) has error", e);
            return new String[]{_mobile, _name, replaced, "ERROR", _weixinId, _deviceId};
        }
    }

    /**
     * 是否存在需要替换掉模板内容
     *
     * @param template
     * @return
     */
    public boolean hasReplaceParamter(String template) {
        if (!StringUtils.containsAny(template, '{', '}')) return false;
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(template);
        List<String> list = Lists.newArrayList();
        while (matcher.find()) list.add(matcher.group());
        return !CollectionUtils.isEmpty(list);
    }

    public boolean checkTemplate(String template) {
        if (!StringUtils.containsAny(template, '{', '}')) return true;
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(template);
        List<String> list = Lists.newArrayList();
        while (matcher.find()) list.add(matcher.group());
        if (CollectionUtils.isEmpty(list)) return true;
        Preconditions.checkState(!isEmpty(), "尚未定义对应的模板，无法替换模板内容...");
        list.forEach(x -> Preconditions.checkState(tokenList.contains(x), "缺少%s 对应的模板替换值", x));
        return true;
    }

    public List<MsgReplaceHoldEntity> getReplaceHolds() {
        return replaceHolds;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("replaceHolds", replaceHolds)
                .toString();
    }
}
