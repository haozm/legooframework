package com.legooframework.model.smsgateway.service;

import com.legooframework.model.core.cache.CaffeineCacheManager;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.Optional;

public class SmsTempCacheService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsTempCacheService.class);

    @SuppressWarnings("unchecked")
    public void put(String batchNo, List<String> payloads) {
        List<String> temp_list = getTempCache().get(batchNo, List.class);
        if (CollectionUtils.isNotEmpty(temp_list)) {
            temp_list.addAll(payloads);
        } else {
            getTempCache().put(batchNo, payloads);
        }
        if (logger.isDebugEnabled())
            logger.debug(String.format("putMessageCache(%s,payloads's size is %s)", batchNo, payloads.size()));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> get(String batchNo) {
        List<String> temp_list = getTempCache().get(batchNo, List.class);
        getTempCache().evict(batchNo);
        if (logger.isDebugEnabled())
            logger.debug(String.format("getMessageCache(%s,payloads's size is %s)", batchNo,
                    CollectionUtils.isEmpty(temp_list) ? null : temp_list.size()));
        return Optional.ofNullable(CollectionUtils.isEmpty(temp_list) ? null : temp_list);
    }


    private Cache getTempCache() {
        return getBean("smsClientCacheManager", CaffeineCacheManager.class).getCache("smsTempCache");
    }
}
