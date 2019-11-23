package com.legooframework.model.covariant.entity;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SendSmsEntity extends BaseEntity<Integer> implements BatchSetter {

    private String content, extFlag, mobile, batchNo, receiverName, returnStatus;
    private int smsChanel, smsLength, smsCount, sendStatus;
    private BusinessType businessType;
    private Integer companyId, storeId, organizationId, employeeId;

    public String getMobile() {
        return mobile;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public SendSmsEntity(String content, String extFlag, String mobile, String receiverName, String batchNo, BusinessType businessType,
                         Integer companyId, Integer storeId, Integer organizationId, Integer employeeId, String receiveStatus) {
        super(0);
        this.content = content;
        this.extFlag = extFlag;
        this.mobile = mobile;
        this.receiverName = receiverName;
        this.batchNo = batchNo;
        this.businessType = businessType;
        this.smsChanel = businessType.getSmsChannel();
        smsCount(content);
        this.companyId = companyId;
        this.storeId = storeId;
        this.organizationId = organizationId;
        this.employeeId = employeeId == 0 ? null : employeeId;
        if (!isOkPhone()) {
            this.sendStatus = 0;
            this.returnStatus = "error:电话号码错误";
        } else if (StringUtils.isNoneEmpty(receiveStatus)) {
            this.sendStatus = 0;
            this.returnStatus = String.format("error:%s", receiveStatus);
        } else {
            this.sendStatus = 2;
            this.returnStatus = null;
        }
    }

    private void smsCount(String content) {
        this.smsLength = content.length();
        if (smsLength < 71) {
            this.smsCount = 1;
        } else {
            if (smsLength % 67 > 0) {
                this.smsCount = smsLength / 67 + 1;
            } else {
                this.smsCount = smsLength / 67;
            }
        }
    }

    private boolean isOkPhone() {
        return StringUtils.isNoneEmpty(mobile) && mobile.length() == 11 && NumberUtils.isDigits(mobile);
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getSmsCount() {
        return smsCount;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        //content, extFlag, mobile, `type`, 5loginUser_id,   6smsCount,7 smsLength,
        // 8company_id, 9employee_id, 10 store_id,
        // receiverName, organization_id, smsChanel, batchAddNo
        ps.setObject(1, this.content);
        ps.setObject(2, this.extFlag);
        ps.setObject(3, this.mobile);
        ps.setObject(4, this.businessType.getValue());
        ps.setObject(5, this.employeeId);
        ps.setObject(6, this.smsCount);
        ps.setObject(7, this.smsLength);
        ps.setObject(8, this.companyId);
        ps.setObject(9, this.employeeId);
        ps.setObject(10, this.storeId);
        ps.setObject(11, this.receiverName);
        ps.setObject(12, this.organizationId == 0 ? null : this.organizationId);
        ps.setObject(13, this.smsChanel);
        ps.setObject(14, this.batchNo);
        ps.setObject(15, this.returnStatus);
        ps.setObject(16, this.sendStatus);
    }

    public static SendSmsEntity createSmsByStore(String content, String phone, String name, StoEntity store, BusinessType businessType,
                                                 String batchNo, String errMsg) {
        return new SendSmsEntity(content, "jobAutoSend", phone, name, batchNo, businessType,
                store.getCompanyId(), store.getId(), store.getOrgId(), 0, errMsg);
    }

    public static SendSmsEntity createSmsByCompany(String content, String phone, String name, OrgEntity company, BusinessType businessType,
                                                   String batchNo, String errMsg) {
        return new SendSmsEntity(content, "jobAutoSend", phone, name, batchNo, businessType, company.getId(), null, null, 0, errMsg);
    }


    static SendSmsEntity createSmsByMember(String content, MemberEntity member, String batchNo, BusinessType businessType, String errMsg) {
        return new SendSmsEntity(content, "jobAutoSend", member.getPhone(), member.getName(), batchNo, businessType,
                member.getCompanyId(), member.getStoreId(), member.getOrgId(), 0, errMsg);
    }

    public void setError(String errMsg) {
        this.sendStatus = 0;
        this.returnStatus = String.format("error:%s", errMsg);
    }

    public String getContent() {
        return content;
    }
}
