package com.legooframework.model.smsgateway.filter;

import com.google.common.collect.ArrayListMultimap;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.smsgateway.entity.SMSBlackListEntity;
import com.legooframework.model.smsgateway.entity.SMSBlackListEntityAction;
import com.legooframework.model.smsgateway.entity.SMSTransportLogEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class MobileNumberBlackListInterceptor extends SmsSendInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(MobileNumberBlackListInterceptor.class);

    public MobileNumberBlackListInterceptor(SMSBlackListEntityAction blackListEntityAction) {
        this.blackListEntityAction = blackListEntityAction;
    }

    @Override
    public boolean filter(Collection<SMSTransportLogEntity> smsTransportLogs) {
        ArrayListMultimap<Integer, SMSTransportLogEntity> multimap = ArrayListMultimap.create();
        smsTransportLogs.stream().filter(x -> !x.isError()).forEach(x -> multimap.put(x.getCompanyId(), x));
        if (multimap.isEmpty()) return true;
        Set<Integer> companyIds = multimap.keySet();
        for (Integer companyId : companyIds) {
            Optional<List<SMSBlackListEntity>> blackList = blackListEntityAction.loadByCompanyId(companyId);
            if (!blackList.isPresent()) continue;
            List<String> black_phones = blackList.get().stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<SMSTransportLogEntity> logs = multimap.get(companyId);
            logs.forEach(x -> {
                try {
                    if (black_phones.contains(x.getSms().getPhoneNo())) {
                        x.errorByBlackList();
                    }
                } catch (Exception e) {
                    logger.error(String.format("MobileNumberBlackListInterceptor hander %s has error",
                            x.getSms().getPhoneNo()), e);
                    x.errorUnknow(e);
                }
            });
        }
        return false;
    }

    private SMSBlackListEntityAction blackListEntityAction;

}
