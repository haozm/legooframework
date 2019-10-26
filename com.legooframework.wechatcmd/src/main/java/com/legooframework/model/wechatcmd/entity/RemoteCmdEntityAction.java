package com.legooframework.model.wechatcmd.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.wechatcircle.entity.CircleUnReadDto;
import com.legooframework.model.wechatcircle.entity.WechatCircleSyncTime;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class RemoteCmdEntityAction extends BaseEntityAction<RemoteCmdEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCmdEntityAction.class);

    private Gson gson = new Gson();

    public RemoteCmdEntityAction() {
        super(null);
    }

//    public void unReadCircleCmd(CircleUnReadDto circleUnReadDto) {
//        Optional<String> deviceId = loadDeviceId(circleUnReadDto.getSource().getCompanyId(), circleUnReadDto.getSource().getStoreId(),
//                circleUnReadDto.getSource().getWeixinId(), "unReadCircleCmd");
//        if (!deviceId.isPresent()) {
//            logger.warn(String.format("unReadCircleCmd(%s) not exits divceId", circleUnReadDto));
//            return;
//        }
//        RemoteCmdEntity remoteCmd = new RemoteCmdEntity("public_msg", "EMP_0000", deviceId.get(), gson.toJson(circleUnReadDto.toMap()),
//                UUID.randomUUID().toString(), null);
//        batchSave(Lists.newArrayList(remoteCmd));
//    }

    private Optional<String> loadDeviceId(Integer companyId, Integer storeId, String weixinId, String action) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("storeId", storeId);
        params.put("weixinId", weixinId);
        params.put("sql", action);
        return super.queryForSimpleObj("loadDeviceId", params, String.class);
    }

    public RemoteCmdEntity syncWechatCircle(String deviceId, WechatCircleSyncTime syncTime, boolean batch, String batchNo) {
        Map<String, Object> command = Maps.newHashMap();
        command.put("oprType", batch ? 1 : 0);
        command.put("userName", batch ? "" : deviceId);
        command.put("priority", 0);
        command.put("time", syncTime.hasLastTime() ? syncTime.getLastTime() : 0L);
        command.put("oprRule", syncTime.hasLastTime() ? 0 : 1);
        command.put("oprPages", 10);
        RemoteCmdEntity remoteCmd = new RemoteCmdEntity("snsMsg", "EMP_0000", deviceId, gson.toJson(command),
                batchNo, null);
        if (logger.isDebugEnabled())
            logger.debug(String.format("syncWechatCircle(...) cmd :%s", remoteCmd));
        return remoteCmd;
    }

    public void batchSave(List<RemoteCmdEntity> commands) {
        if (CollectionUtils.isEmpty(commands)) return;
        super.batchInsert("batchInsert", commands);
        if (logger.isDebugEnabled())
            logger.debug(String.format("写入远程命令 %s 共计 %s 条", commands.get(0).getTag(), commands.size()));
    }

    public void clearUnclaimCmd(List<String> deviceIds) {
        if (CollectionUtils.isEmpty(deviceIds)) return;
        batchSave(buildCmd("clear_unclaim", deviceIds));
    }

    private List<RemoteCmdEntity> buildCmd(String tag, List<String> deviceIds) {
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(deviceIds.size());
        String uuid = String.valueOf(System.currentTimeMillis());
        Map<String, Object> command = Maps.newHashMap();
        command.put("tag", "T_FFFFFFFFFFF");
        deviceIds.forEach(x -> {
            command.put("ids", x);
            cmds.add(new RemoteCmdEntity(tag, "EMP_0000", x, gson.toJson(command), uuid, null));
        });
        return cmds;
    }

    @Override
    protected RowMapper<RemoteCmdEntity> getRowMapper() {
        return new RowMapperImpl();
    }

    class RowMapperImpl implements RowMapper<RemoteCmdEntity> {
        @Override
        public RemoteCmdEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return new RemoteCmdEntity(resultSet.getLong("id"), resultSet);
        }
    }
}
