package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.TemplateReplaceException;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);

    public void batchSendMessage(StoEntity store, Collection<SendMessageTemplate> sendMsgTemplates, String msgTemplate,
                                 UserAuthorEntity user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSendMessage(store=%d,size=%d,user=%s)", store.getId(), sendMsgTemplates.size(), user));
        final String batchNo = UUID.randomUUID().toString();
        for (SendMessageTemplate $temp : sendMsgTemplates) {
            MemberAgg memberAgg = null;
            try {
                memberAgg = getBean(CovariantService.class).loadMemberAgg($temp.getMemberId());
            } catch (Exception e) {
                logger.error(String.format("loadMemberAgg(%d) has error...", $temp.getMemberId()), e);
                $temp.setError(String.format("获取ID=%d的用户失败", $temp.getMemberId()));
                continue;
            }
            Preconditions.checkNotNull(memberAgg);
            $temp.setMemberInfo(memberAgg.getMember().getPhone(), memberAgg.getMember().getName());
            memberAgg.getWxUser().ifPresent(wx -> $temp.setWeixinInfo(wx.getId(), wx.getDevicesId()));
            String _template = $temp.getCtxTemplate().orElse(msgTemplate);
            try {
                $temp.setContext(fmtMsgTemplate(memberAgg, _template));
            } catch (Exception e) {
                logger.error(String.format("fmtMsgTemplate(%s) has error...", _template), e);
                $temp.setError(String.format("格式化模板异常%s", _template));
                continue;
            }
        }
    }

    private String fmtMsgTemplate(MemberAgg memberAgg, String content) {
        Map<String, Object> params = memberAgg.toReplaceMap();
        String target = replaceTemplate(content, params);
        return target;
    }

    private String replaceTemplate(String content, Map<String, Object> params) throws TemplateReplaceException {
        if (MapUtils.isEmpty(params) || Strings.isNullOrEmpty(content)) return content;
        try {
            StringSubstitutor substitutor = new StringSubstitutor(params, "{", "}");
            return substitutor.replace(content);
        } catch (Exception e) {
            throw new TemplateReplaceException(String.format("模板 %s 替换发送异常...%s", content, params), e);
        }
    }

}
