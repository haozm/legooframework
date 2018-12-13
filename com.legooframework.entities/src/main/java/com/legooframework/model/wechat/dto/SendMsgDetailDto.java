package com.legooframework.model.wechat.dto;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.jdbc.BatchSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SendMsgDetailDto implements BatchSetter {

    private Long batchNo;
    private Long formuser;
    private String toDeviceId, fromDeviceId, content, touser, msgId;
    private int contentType;
    private int isgroup = 0;
    private Long storeId, companyId;
    private int order;

    public SendMsgDetailDto(Long batchNo, String content, String touser, int contentType,
                            Long storeId, Long companyId, int order, LoginContext loginUser) {
        this.content = content;
        this.touser = touser;
        this.formuser = loginUser.getLoginId();
        this.fromDeviceId = loginUser.getLoginId().toString();
        this.batchNo = batchNo;
        this.contentType = contentType;
        this.storeId = storeId;
        this.companyId = companyId;
        this.order = order;
        this.msgId = UUID.randomUUID().toString();
    }

    public String getTouser() {
        return touser;
    }

    public void setFormuser(Long formuser) {
        this.formuser = formuser;
    }

    public void setToDeviceId(String toDeviceId) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(toDeviceId), "发送设备不可以为空....");
        this.toDeviceId = toDeviceId;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        INSERT INTO yycomm.Send_MSG_List
//                ( 1content, 2 touser, 3`type`,4 isgroup,5 todeviceid,6 store_id,7 company_id,8 UUID, fromuser,9
//                        10 IDGroup,11 IDSort, 12 fromdeviceid)
//        VALUES ( ?,        ?,       ?,     ?,       ?,          ?,        ?,          ?,    ?,
//          ?,   ? , ?)
        ps.setString(1, this.content);
        ps.setString(2, this.touser);
        ps.setInt(3, this.contentType);
        ps.setInt(4, this.isgroup);
        ps.setString(5, this.toDeviceId);
        ps.setLong(6, this.storeId);
        ps.setLong(7, this.companyId);
        ps.setString(8, this.msgId);
        ps.setLong(9, this.formuser);
        ps.setObject(10, this.batchNo);
        ps.setInt(11, this.order);
        ps.setString(12, this.fromDeviceId);
    }
}
