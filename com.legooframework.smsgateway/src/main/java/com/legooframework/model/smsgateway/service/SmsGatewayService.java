package com.legooframework.model.smsgateway.service;

import com.google.common.base.Preconditions;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import com.legooframework.model.covariant.service.CovariantService;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.smsgateway.entity.AutoRunChannel;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public class SmsGatewayService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsGatewayService.class);


    public void batchSendMessage(AutoRunChannel autoRunChannel, Collection<Integer> memerIds, String content,
                                 String[] imgUrls, UserAuthorEntity user) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("batchSendMessage(memerIds=%d,content=%s,imgUrls=%s)",
                    CollectionUtils.isEmpty(memerIds) ? 0 : memerIds.size(), content, Arrays.toString(imgUrls)));
        final String batchNo = UUID.randomUUID().toString();
        for (Integer mId : memerIds) {
            MemberAgg memberAgg = null;
            try {
                memberAgg = getBean(CovariantService.class).loadMemberAgg(mId);
            } catch (Exception e) {
                logger.error(String.format("loadMemberAgg(%d) has error...", mId), e);
                continue;
            }
            Preconditions.checkNotNull(memberAgg);

        }
    }


    private void fmtSms(MemberAgg memberAgg, String content) {

    }

}
