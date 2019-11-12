package com.legooframework.model.smsgateway.filter;

import com.legooframework.model.smsgateway.entity.SMSBlackListEntity;
import com.legooframework.model.smsgateway.entity.SMSBlackListEntityAction;
import com.legooframework.model.smsgateway.entity.SendMsg4SendEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MobileNumberBlackListInterceptor extends SmsSendInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MobileNumberBlackListInterceptor.class);

    public MobileNumberBlackListInterceptor(SMSBlackListEntityAction blackListEntityAction) {
        this.blackListEntityAction = blackListEntityAction;
    }

    @Override
    public boolean filter(Collection<SendMsg4SendEntity> smsTransportLogs) {
//        Collection<SendMsg4SendEntity> logs = smsTransportLogs.stream().filter(x -> !x.isError()).collect(Collectors.toList());
//        if (CollectionUtils.isEmpty(logs)) return true;
//        Optional<List<SMSBlackListEntity>> blackList = blackListEntityAction.loadAll();
//        if (!blackList.isPresent()) return true;
//        logs.forEach(x -> {
//            try {
//                Optional<SMSBlackListEntity> ins = blackList.get().stream().filter(y -> y.isBlackPhone(x)).findFirst();
//                if (ins.isPresent()) x.errorByBlackList();
//            } catch (Exception e) {
////                logger.error(String.format("MobileNumberBlackListInterceptor hander %s has error",
////                        x.getSms().getPhoneNo()), e);
//                x.errorByException(e);
//            }
//        });
        return false;
    }

    private SMSBlackListEntityAction blackListEntityAction;

}
