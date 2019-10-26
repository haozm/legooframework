package com.csosm.module.webchat.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntityAction;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RemoteCmdAction extends BaseEntityAction<RemoteCmdEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCmdAction.class);

    private Gson gson;

    public RemoteCmdAction() {
        super("RemoteCmdEntity", null);
        this.gson = new Gson();
    }

    public void getContactCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("get_contact", loginUser, devices));
    }

    public void clearContactCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("clear_contact", loginUser, devices));
    }

    public void rebootCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("reboot", loginUser, devices));
    }

    public void uploadLogCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("get_logs", loginUser, devices));
    }

    public void bildStoreCmd(DevicesEntity device, LoginUserContext user) {
        Preconditions.checkNotNull(device, "入参 DevicesEntity device 不可以为空值...");
        String fromDeviceId = String.format("EMP_%s", user == null ? 0 : user.getUserId());
        String toDeviceId = "00000001";
        String uuid = String.valueOf(System.currentTimeMillis());
        Map<String, Object> command = Maps.newHashMap();
        command.put("tag", "bind_store");
        command.put("ids", String.format("%s%s", device.getCompanyId(), device.getStoreId()));
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(1);
        cmds.add(new RemoteCmdEntity("bind_store", fromDeviceId, toDeviceId, gson.toJson(command), uuid, null));
        batchSave(cmds);
    }

    public void autoAddMemberCmd(DevicesEntity device, StoreEntity store, String phoneNo, String addMsg, String orgId) {
        Objects.requireNonNull(device);
        String toDeviceId = device.getId();
        Map<String, Object> command = Maps.newHashMap();
        command.put("tag", addMsg);
        command.put("ids", phoneNo);
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(1);
        cmds.add(new RemoteCmdEntity("add_member", "FFFFFFFF", toDeviceId, gson.toJson(command), orgId, null));
        batchSave(cmds);
    }

    public void initStoreCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        Preconditions.checkNotNull(devices, "入参  List<DevicesEntity> devices 不可以为空值...");
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(devices.size());
        String fromDeviceId = String.format("EMP_%s", loginUser.getUserId());
        for (DevicesEntity $it : devices) {
            String toDeviceId = $it.getId();
            String uuid = String.valueOf(System.currentTimeMillis());
            Map<String, Object> command = Maps.newHashMap();
            command.put("tag", "T_FFFFFFFFFFF");
            command.put("ids", String.format("%s%s", $it.getCompanyId(), $it.getStoreId()));
            cmds.add(new RemoteCmdEntity("init_store", fromDeviceId, toDeviceId, gson.toJson(command), uuid, null));
        }
        batchSave(cmds);
    }

    public void clearUnclaimCmd(LoginUserContext loginUser, List<DevicesEntity> devices) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("clear_unclaim", loginUser, devices));
    }

    public void setMainParam(LoginUserContext loginUser, List<DevicesEntity> devices) {
        LoginUserContext user = loginUser == null ? LoginUserContext.anonymous() : loginUser;
        if (CollectionUtils.isEmpty(devices)) return;
        batchSave(buildCmd("set_main_params", user, devices));
    }

    public void batchSave(List<RemoteCmdEntity> commands) {
        if (CollectionUtils.isEmpty(commands)) return;
        getJdbcTemplate().batchUpdate(getExecSql("batchInsert", null), commands, 500,
                new ParameterizedPreparedStatementSetter<RemoteCmdEntity>() {
                    // tag, fromdeviceid, todeviceid, orderMSG, uuid,  remark
                    @Override
                    public void setValues(PreparedStatement ps, RemoteCmdEntity entity) throws SQLException {
                        //id,batch_id,serial_num,life_status,createUserId
                        ps.setString(1, entity.getTag());
                        ps.setString(2, entity.getFromDeviceId());
                        ps.setString(3, entity.getToDeviceId());
                        ps.setString(4, entity.getCommand());
                        ps.setString(5, entity.getBatchNo());
                        ps.setString(6, entity.getRemark());
                    }
                });
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次共计写入写入远程命令 %s 共计 %s 条", commands.get(0).getTag(), commands.size()));
    }

    public Optional<List<RemoteCmdEntity>> findCmdListByDevice(String deviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "入参 String deviceId 不可以为空...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("deviceId", deviceId);
        List<RemoteCmdEntity> remoteCmds = getNamedParameterJdbcTemplate()
                .query(getExecSql("findCmdListByDevice", null), params, new RowMapperImpl());
        if (logger.isDebugEnabled())
            logger.debug(String.format("findCmdListByDevice( %s ) size is %s", deviceId,
                    CollectionUtils.isEmpty(remoteCmds) ? 0 : remoteCmds.size()));
        return Optional.fromNullable(CollectionUtils.isEmpty(remoteCmds) ? null : remoteCmds);
    }

    private List<RemoteCmdEntity> buildCmd(String tag, LoginUserContext loginUser, List<DevicesEntity> devices) {
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(devices.size());
        String fromDeviceId = String.format("EMP_%s", loginUser.getUserId());
        String uuid = String.valueOf(System.currentTimeMillis());
        Map<String, Object> command = Maps.newHashMap();
        command.put("tag", "T_FFFFFFFFFFF");
        for (DevicesEntity $it : devices) {
            command.put("ids", $it.getId());
            cmds.add(new RemoteCmdEntity(tag, fromDeviceId, $it.getId(), gson.toJson(command), uuid, null));
        }
        return cmds;
    }

    @Override
    protected ResultSetExtractor<RemoteCmdEntity> getResultSetExtractor() {
        return null;
    }

    class RowMapperImpl implements RowMapper<RemoteCmdEntity> {
        @Override
        public RemoteCmdEntity mapRow(ResultSet resultSet, int i) throws SQLException {
            return buildByResultSet(resultSet);
        }
    }

    private RemoteCmdEntity buildByResultSet(ResultSet resultSet) throws SQLException {
        return new RemoteCmdEntity(resultSet.getLong("id"), resultSet);
    }
}
