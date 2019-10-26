package com.legooframework.model.devices.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

public class RemoteCmdAction extends BaseEntityAction<RemoteCmdEntity> {

    private static final Logger logger = LoggerFactory.getLogger(RemoteCmdAction.class);

    private Gson gson;

    public RemoteCmdAction() {
        super(null);
        this.gson = new Gson();
    }

    public void getContactCmd(LoginContext loginUser, String... deviceIds) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (ArrayUtils.isEmpty(deviceIds)) return;
        batchSave(buildCmd("get_contact", loginUser, deviceIds));
    }

    public void clearContactCmd(LoginContext loginUser, String... deviceIds) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (ArrayUtils.isEmpty(deviceIds)) return;
        batchSave(buildCmd("clear_contact", loginUser, deviceIds));
    }


    public void clearUnclaimCmd(LoginContext loginUser, String... deviceIds) {
        Preconditions.checkNotNull(loginUser, "入参 LoginUserContext loginUser 不可以为空值...");
        if (ArrayUtils.isEmpty(deviceIds)) return;
        batchSave(buildCmd("clear_unclaim", loginUser, deviceIds));
    }

    private void batchSave(List<RemoteCmdEntity> commands) {
        if (CollectionUtils.isEmpty(commands)) return;
        super.batchInsert(getStatementFactory(), getModelName(), "batchInsert", commands);
        if (logger.isDebugEnabled())
            logger.debug(String.format("本批次共计写入远程命令 %s 共计 %s 条", commands.get(0).getTag(), commands.size()));
    }

    private List<RemoteCmdEntity> buildCmd(String tag, LoginContext loginUser, String... deviceIds) {
        List<RemoteCmdEntity> cmds = Lists.newArrayListWithCapacity(deviceIds.length);
        String fromDeviceId = String.format("EMP_%s", loginUser.getLoginId());
        String toDeviceId = "00000001";
        String uuid = String.valueOf(System.currentTimeMillis());
        Map<String, Object> command = Maps.newHashMap();
        command.put("tag", "T_FFFFFFFFFFF");
        for (String $it : deviceIds) {
            command.put("ids", $it);
            cmds.add(new RemoteCmdEntity(tag, fromDeviceId, toDeviceId, gson.toJson(command), uuid, null));
        }
        return cmds;
    }

    @Override
    protected RowMapper<RemoteCmdEntity> getRowMapper() {
        return null;
    }
}
