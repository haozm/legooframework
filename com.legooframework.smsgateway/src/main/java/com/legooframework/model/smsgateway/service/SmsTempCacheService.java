package com.legooframework.model.smsgateway.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.cache.CaffeineCacheManager;
import com.legooframework.model.covariant.entity.OrgEntity;
import com.legooframework.model.smsgateway.entity.MsgReplaceHoldEntityAction;
import com.legooframework.model.smsgateway.entity.MsgReplaceHoldList;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import com.legooframework.model.smsgateway.mvc.JobDetailTemplate4Replace;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SmsTempCacheService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(SmsTempCacheService.class);

    public void replaceTemplateAction(OrgEntity company, Integer employeeId, List<JobDetailTemplate4Replace> template4Replaces) {
        MsgReplaceHoldList replaceHoldList = getBean(MsgReplaceHoldEntityAction.class).loadByCompany(company);
        List<Integer> memberIds = Lists.newArrayList();
        template4Replaces.forEach(x -> memberIds.addAll(x.getMemberIds()));
        Map<String, Object> params = Maps.newHashMap();
        params.put("employeeId", employeeId);
        params.put("companyId", company.getId());
        params.put("memberIds", memberIds);
        Optional<List<Map<String, Object>>> _member_infos = getJdbcQuerySupport()
                .queryForList("MsgReplaceHoldEntity", "loadReplaceSource", params);
        Map<Integer, Map<String, Object>> replaceSources = Maps.newHashMap();
        _member_infos.ifPresent(x -> x.forEach(m -> replaceSources.put(MapUtils.getInteger(m, "id"), m)));
        template4Replaces.forEach(x -> replaceMembers(replaceHoldList, x, replaceSources));
    }

    /**
     * 替换动作本身  阿弥陀佛
     *
     * @param replaceHoldList  格式化列表
     * @param replaceSourceMap 格式化结果
     */
    private void replaceMembers(MsgReplaceHoldList replaceHoldList, JobDetailTemplate4Replace replaces,
                                Map<Integer, Map<String, Object>> replaceSourceMap) {
        List<SendMessageTemplate> templates = replaces.getJobDetails();
        templates.forEach(template -> {
            Map<String, Object> replace = replaceSourceMap.get(template.getMemberId());
            if (MapUtils.isNotEmpty(replace)) {
                String[] _tems = replaceHoldList.replaceMembers(replace, replaces.getTemplate());
//                final String mobile, final String name, final String context,
//                final String resulat, final String weixinId, final String deviceId, final AutoRunChannel autoRunChannel

                template.setContext(_tems[2]);
                //template.setResulat(_tems[3]);
                template.setWeixinInfo(_tems[4], _tems[5]);
            } else {

                template.setContext("EMPTY");
                //template.setResulat("NOTEXITS");
                template.setWeixinInfo(null, null);
            }
        });
    }

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
