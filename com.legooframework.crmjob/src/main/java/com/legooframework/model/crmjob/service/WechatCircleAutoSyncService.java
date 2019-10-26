package com.legooframework.model.crmjob.service;

import com.google.common.collect.Lists;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.crmadapter.entity.WechatCircleProxyAction;
import com.legooframework.model.crmjob.entity.DeviceWithWeixinEntity;
import com.legooframework.model.crmjob.entity.DeviceWithWeixinEntityAction;
import com.legooframework.model.scheduler.entity.JobRunParams;
import com.legooframework.model.wechatcircle.entity.WechatCircleSyncTime;
import com.legooframework.model.wechatcmd.entity.RemoteCmdEntity;
import com.legooframework.model.wechatcmd.entity.RemoteCmdEntityAction;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class WechatCircleAutoSyncService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(WechatCircleAutoSyncService.class);

    /**
     * 同步任务机制
     *
     * @param jobRunParams 我的一些小秘密
     */
    public void autoSyncWechatCircle(JobRunParams jobRunParams) {
        LoginContextHolder.setIfNotExitsAnonymousCtx();
        try {
            Optional<List<DeviceWithWeixinEntity>> deviceAndWxs = getBean(DeviceWithWeixinEntityAction.class).loadAll();
            if (!deviceAndWxs.isPresent()) return;
            Optional<List<DeviceWithWeixinEntity>> todo_list = jobRunParams.filter(deviceAndWxs.get());
            if (todo_list.isPresent()) {
                List<String> weixinIds = todo_list.get().stream().map(DeviceWithWeixinEntity::getWeixinId).collect(Collectors.toList());
                List<WechatCircleSyncTime> wxCircleSyncTimes = getBean(WechatCircleProxyAction.class).batchSyncLastTime(weixinIds);
                List<RemoteCmdEntity> cmds = Lists.newArrayList();
                final String uuid = UUID.randomUUID().toString();
                wxCircleSyncTimes.forEach(sync -> {
                    Optional<DeviceWithWeixinEntity> dw = deviceAndWxs.get().stream()
                            .filter(x -> StringUtils.equals(sync.getWeixinId(), x.getWeixinId())).findFirst();
                    dw.ifPresent(deviceId -> {
                        RemoteCmdEntity cmd = getBean(RemoteCmdEntityAction.class).syncWechatCircle(deviceId.getDeviceId(), sync, true, uuid);
                        cmds.add(cmd);
                    });
                });
                if (logger.isDebugEnabled())
                    logger.debug(String.format("RemoteCmdEntityAction.batchSave(...) %s", cmds));
                if (CollectionUtils.isNotEmpty(cmds)) getBean(RemoteCmdEntityAction.class).batchSave(cmds);
            }
        } catch (Exception e) {
            logger.error(String.format("autoSyncWechatCircle(%s) has error", jobRunParams), e);
        } finally {
            LoginContextHolder.clear();
        }
    }

}
