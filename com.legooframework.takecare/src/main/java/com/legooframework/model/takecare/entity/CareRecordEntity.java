package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.takecare.service.CareNinetyTaskAgg;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CareRecordEntity extends BaseEntity<Long> implements BatchSetter {

    private final Integer employeeId, storeId, companyId, memberId;
    private Integer careId, subCareId;
    // memberId phone name
    // memberId weixin deviceId
    // memberId openId null
    private final String sendInfo01, sendInfo02;
    private final BusinessType businessType;
    private final SendChannel sendChannel;
    private final String context;
    private final boolean error;
    private final String message;
    private final String[] imgUrls;


    private CareRecordEntity(Integer careId, int subCareId, Integer companyId, Integer storeId, Integer employeeId,
                             BusinessType businessType, SendChannel sendChannel, Integer memberId,
                             String sendInfo01, String sendInfo02, String context, String[] imgUrls,
                             boolean error, String message) {
        super(0L);
        this.careId = careId;
        this.subCareId = subCareId;
        this.employeeId = employeeId;
        this.memberId = memberId;
        this.storeId = storeId;
        this.companyId = companyId;
        this.businessType = businessType;
        this.sendChannel = sendChannel;
        this.context = context;
        this.sendInfo01 = sendInfo01;
        this.sendInfo02 = sendInfo02;
        this.error = error;
        this.message = message;
        this.imgUrls = imgUrls;
    }


    private CareRecordEntity(Integer careId, Integer companyId, Integer storeId, Integer employeeId,
                             BusinessType businessType, SendChannel sendChannel, Integer memberId,
                             String sendInfo01, String sendInfo02, String context, String[] imgUrls,
                             boolean error, String message) {
        super(0L);
        this.careId = careId;
        this.subCareId = 0;
        this.employeeId = employeeId;
        this.memberId = memberId;
        this.storeId = storeId;
        this.companyId = companyId;
        this.businessType = businessType;
        this.sendChannel = sendChannel;
        this.context = context;
        this.sendInfo01 = sendInfo01;
        this.sendInfo02 = sendInfo02;
        this.error = error;
        this.message = message;
        this.imgUrls = imgUrls;
    }

    public SendSmsEntity createSendSms(String batchNo) {
        return new SendSmsEntity(this.context, "BirthdayCare", this.sendInfo01, this.sendInfo02, batchNo,
                BusinessType.BIRTHDAYCARE, companyId, storeId, 0, employeeId, null);
    }

    public SendWechatEntity createSendWxMsg(String batchNo) {
        return new SendWechatEntity(this.context, this.imgUrls, this.sendInfo01, storeId, companyId, sendInfo02,
                businessType);
    }


    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
// care_id, company_id, org_id, store_id, employee_id, member_id,
        ps.setObject(1, careId);
        ps.setObject(2, companyId);
        ps.setObject(3, 0);
        ps.setObject(4, storeId);
        ps.setObject(5, employeeId);
        ps.setObject(6, memberId);
// sendInfo01, business_type, send_channel, context,  tenant_id,error_tag, message send_info02
        ps.setObject(7, sendInfo01);
        ps.setObject(8, businessType.getValue());
        ps.setObject(9, sendChannel.getValue());
        ps.setObject(10, context);
        ps.setObject(11, companyId);
        ps.setObject(12, error ? 1 : 0);
        ps.setObject(13, message);
        ps.setObject(14, sendInfo02);
        ps.setObject(15, ArrayUtils.isEmpty(imgUrls) ? null : StringUtils.join(imgUrls, "####"));
        ps.setObject(16, subCareId == null ? 0 : subCareId);
    }

    CareRecordEntity(Long id, ResultSet res) {
        super(id);
        try {
            this.businessType = BusinessType.paras(res.getInt("business_type"));
            this.sendChannel = SendChannel.paras(res.getInt("send_channel"));
            this.companyId = ResultSetUtil.getObject(res, "company_id", Integer.class);
            this.storeId = ResultSetUtil.getObject(res, "store_id", Integer.class);
            this.memberId = ResultSetUtil.getObject(res, "member_id", Integer.class);
            this.employeeId = ResultSetUtil.getOptObject(res, "employee_id", Integer.class).orElse(0);
            this.sendInfo01 = ResultSetUtil.getOptString(res, "send_info01", null);
            this.sendInfo02 = ResultSetUtil.getOptString(res, "send_info02", null);
            this.message = ResultSetUtil.getOptString(res, "message", null);
            this.context = ResultSetUtil.getOptString(res, "context", null);
            this.careId = ResultSetUtil.getObject(res, "care_id", Integer.class);
            this.subCareId = ResultSetUtil.getObject(res, "sub_care_id", Integer.class);
            this.error = ResultSetUtil.getBooleanByInt(res, "error_tag");
            this.imgUrls = res.getString("img_urls") == null ? null : StringUtils.split(res.getString("img_urls"), "####");
        } catch (SQLException e) {
            throw new RuntimeException("Restore TakeCareRecordEntity has SQLException", e);
        }
    }

    public static CareRecordEntity wxNinetyCare4Member(CareNinetyTaskAgg task, UserAuthorEntity user, String[] imgUrls) {
        if (!task.getWxUser().isPresent()) {
            return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                    user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.WECHAT,
                    task.getMemberId().orElse(null), null, null, task.getTargetContent(), imgUrls, true, "无微信信息..");
        }
        return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.WECHAT,
                task.getMemberId().orElse(null), task.getWxUser().isPresent() ? task.getWxUser().get().getUserName() : null,
                task.getWxUser().isPresent() ? task.getWxUser().get().getDevicesId() : null, task.getTargetContent(), imgUrls, false, null);
    }
    
    public static CareRecordEntity sendSmsNinetyCare4Member(CareNinetyTaskAgg task, UserAuthorEntity user) {
        return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.SMS,
                task.getMemberId().orElse(null), task.getPhone().orElse(null), task.getMemberName().orElse(null),
                task.getTargetContent(), null, false, null);
    }

    public static CareRecordEntity errorNinetyCare4Member(CareNinetyTaskAgg task, UserAuthorEntity user) {
        return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.CANCEL,
                task.getMemberId().orElse(null), task.getPhone().orElse(null), task.getMemberName().orElse(null),
                "跟进异常", null, true, task.getErrorMsg());
    }

    public static CareRecordEntity cancelNinetyCare4Member(CareNinetyTaskAgg task, UserAuthorEntity user) {
        return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.CANCEL,
                task.getMemberId().orElse(null), task.getPhone().orElse(null), task.getMemberName().orElse(null),
                "取消跟进", null, false, null);
    }

    public static CareRecordEntity manualNinetyCare4Member(CareNinetyTaskAgg task, UserAuthorEntity user) {
        return new CareRecordEntity(task.getTask().getPlanId(), task.getTask().getId(), task.getTask().getCompanyId(), task.getTask().getStoreId(),
                user == null ? 0 : user.getId(), BusinessType.NINETYPLAN, SendChannel.OFFLINE,
                task.getMemberId().orElse(null), task.getPhone().orElse(null), task.getMemberName().orElse(null),
                "人工线下完成", null, false, null);
    }


    static CareRecordEntity cancelBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberEntity member) {
        return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.CANCEL,
                member.getId(), member.getPhone(), member.getName(), "取消跟进", null, false, null);

    }

    static CareRecordEntity manualBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberEntity member) {
        return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.OFFLINE,
                member.getId(), member.getPhone(), member.getName(), "人工线下完成", null, false, null);

    }

    void setCareId(CareBirthdayEntity birthdayCare) {
        this.careId = birthdayCare.getCareId();
    }

    static CareRecordEntity smsBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberAgg memberAgg,
                                                   String context) {
        MemberEntity member = memberAgg.getMember();
        if (!member.hasPhone()) {
            return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                    employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.SMS,
                    member.getId(), member.getOptPhone().orElse(null), member.getName(), context, null, true, "电话号码错误...");
        } else if (Strings.isNullOrEmpty(context)) {
            return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                    employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.SMS,
                    member.getId(), member.getOptPhone().orElse(null), member.getName(), context, null, true, "无模板信息");
        } else {
            return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                    employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.SMS,
                    member.getId(), member.getPhone(), member.getName(), context, null, false, null);
        }
    }

    public boolean isSave4NotSend() {
        return isError() || !(isSmsChannel() || isWxChannel());
    }

    public boolean isSave4Send() {
        return !isError() && (isSmsChannel() || isWxChannel());
    }

    public boolean isSmsChannel() {
        return SendChannel.SMS == this.sendChannel;
    }

    public boolean isWxChannel() {
        return SendChannel.WECHAT == this.sendChannel;
    }

    public boolean isError() {
        return error;
    }

    static CareRecordEntity wxBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberAgg agg,
                                                  String context, String[] imgUrls) {
        if (!agg.getWxUser().isPresent()) {
            return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                    employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.WECHAT,
                    agg.getMember().getId(), null, null, context, imgUrls, true, "无微信信息..");
        }
        if (Strings.isNullOrEmpty(context)) {
            return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                    employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.WECHAT,
                    agg.getMember().getId(), agg.getWxUser().isPresent() ? agg.getWxUser().get().getUserName() : null,
                    agg.getWxUser().isPresent() ? agg.getWxUser().get().getDevicesId() : null, context, imgUrls, true, "无模板信息");
        }
        return new CareRecordEntity(care.getCareId(), care.getCompanyId(), care.getStoreId(),
                employee == null ? 0 : employee.getId(), BusinessType.BIRTHDAYCARE, SendChannel.WECHAT,
                agg.getMember().getId(), agg.getWxUser().isPresent() ? agg.getWxUser().get().getUserName() : null,
                agg.getWxUser().isPresent() ? agg.getWxUser().get().getDevicesId() : null, context, imgUrls, false, null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("careId", careId)
                .add("employeeId", employeeId)
                .add("storeId", storeId)
                .add("companyId", companyId)
                .add("memberId", memberId)
                .add("sendInfo01", sendInfo01)
                .add("businessType", businessType)
                .add("sendChannel", sendChannel)
                .add("context", context)
                .toString();
    }
}
