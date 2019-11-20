package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.covariant.entity.StoEntity;
import com.legooframework.model.covariant.entity.TemplateReplaceException;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.smsgateway.entity.SMSEntity;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import com.legooframework.model.smsgateway.entity.SendMsg4InitEntity;
import com.legooframework.model.smsprovider.entity.SMSChannel;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);

    public void batchSendMessage(StoEntity store, Collection<SendMessageTemplate> sendMsgTemplates, String msgTemplate,
                                 UserAuthorEntity user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSendMessage(store=%d,size=%d,user=%s)", store.getId(), sendMsgTemplates.size(), user));
        final String batchNo = UUID.randomUUID().toString();
        List<SendMsg4InitEntity> sendMsg4Inits = Lists.newArrayList();
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
                $temp.setContext(replaceTemplate(_template, memberAgg.toReplaceMap()));
            } catch (Exception e) {
                logger.error(String.format("fmtMsgTemplate(%s) has error...", _template), e);
                $temp.setError(String.format("格式化模板异常%s", _template));
                continue;
            }
            SMSEntity.createSMSMsg($temp).forEach(sms ->
                    sendMsg4Inits.add(SendMsg4InitEntity.createInstance(store, sms, batchNo, $temp.getBusinessType())));
        }
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
