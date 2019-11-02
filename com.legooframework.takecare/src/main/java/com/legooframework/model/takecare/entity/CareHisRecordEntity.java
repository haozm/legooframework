package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.entity.StringSerializer;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.*;
import com.legooframework.model.covariant.service.MemberAgg;
import com.legooframework.model.takecare.service.CareNinetyTaskAgg;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.StringJoiner;

public class CareHisRecordEntity extends BaseEntity<Integer> implements BatchSetter, StringSerializer<CareRecordEntity> {

    private String followUpContent, memberFeedback, planName, shoppingGuideSummary;
    private LocalDateTime followUpTime, updateTime;
    private int followUpWay, planType, planId, status, operateType;
    private Integer memberId, storeId, employeeId;

    @Override
    public String serializer() {
        StringJoiner sj = new StringJoiner("|");
        sj.setEmptyValue(DEF_EMPTY).add(getId().toString()).add(serializer(storeId))
                .add(serializer(employeeId)).add(serializer(memberId)).add(serializer(followUpWay)).add(serializer(planType))
                .add(serializer(planId)).add(serializer(status)).add(serializer(operateType))
                .add(serializer(followUpTime)).add(serializer(updateTime))
                .add(encodeHex(followUpContent)).add(encodeHex(memberFeedback)).add(encodeHex(planName))
                .add(encodeHex(shoppingGuideSummary));
        return sj.toString();
    }

    @Override
    public CareRecordEntity deserializer(String serializer) {
        return null;
    }

    private CareHisRecordEntity(int planId, String followUpContent, String memberFeedback, BusinessType businessType,
                                String shoppingGuideSummary, LocalDateTime followUpTime, SendChannel followUpWay,
                                Integer memberId, Integer storeId, Integer employeeId) {
        super(0);
        this.followUpContent = followUpContent;
        this.memberFeedback = memberFeedback;
        this.planName = businessType.getDesc();
        this.shoppingGuideSummary = shoppingGuideSummary;
        this.followUpTime = followUpTime;
        this.updateTime = followUpTime;
        this.followUpWay = followUpWay.getValue() + 1;
        this.planType = businessType.getValue();
        this.planId = planId;
        this.status = 1;
        this.operateType = 2;
        this.memberId = memberId;
        this.storeId = storeId;
        this.employeeId = employeeId;
    }

    void setPlanId(CareBirthdayEntity care) {
        this.planId = care.getCareId();
    }

    public static CareHisRecordEntity errorCareNinety4Member(CareNinetyTaskAgg agg, UserAuthorEntity user) {
        return new CareHisRecordEntity(agg.getTask().getPlanId(), agg.getErrorMsg(), null, BusinessType.NINETYPLAN, null,
                LocalDateTime.now(),
                SendChannel.CANCEL, agg.getMemberId().orElse(null), agg.getTask().getStoreId(), user == null ? null : user.getId());
    }

    public static CareHisRecordEntity offlineCareNinety4Member(CareNinetyTaskAgg agg, UserAuthorEntity user) {
        return new CareHisRecordEntity(agg.getTask().getPlanId(), "线下跟进", null, BusinessType.NINETYPLAN, null,
                LocalDateTime.now(),
                SendChannel.OFFLINE, agg.getMemberId().orElse(null), agg.getTask().getStoreId(), user == null ? null : user.getId());
    }

    public static CareHisRecordEntity cancelCareNinety4Member(CareNinetyTaskAgg agg, UserAuthorEntity user) {
        return new CareHisRecordEntity(agg.getTask().getPlanId(), "取消跟进", null, BusinessType.NINETYPLAN, null,
                LocalDateTime.now(),
                SendChannel.CANCEL, agg.getMemberId().orElse(null), agg.getTask().getStoreId(), user == null ? null : user.getId());
    }


    public static CareHisRecordEntity wxBirthdayCare4Member(CareNinetyTaskAgg agg, UserAuthorEntity user) {
        return new CareHisRecordEntity(agg.getTask().getPlanId(), agg.getTargetContent(), null, BusinessType.NINETYPLAN, null,
                LocalDateTime.now(),
                SendChannel.WECHAT, agg.getMemberId().orElse(null), agg.getTask().getStoreId(), user == null ? null : user.getId());
    }

    public static CareHisRecordEntity smsCareNinety4Member(CareNinetyTaskAgg agg, UserAuthorEntity user) {
        return new CareHisRecordEntity(agg.getTask().getPlanId(), agg.getTargetContent(), null, BusinessType.NINETYPLAN, null,
                LocalDateTime.now(),
                SendChannel.SMS, agg.getMemberId().orElse(null), agg.getTask().getStoreId(), user == null ? null : user.getId());
    }


    static CareHisRecordEntity cancelBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberEntity member) {
        return new CareHisRecordEntity(care.getCareId(), "取消跟进", null, BusinessType.BIRTHDAYCARE, null, LocalDateTime.now(),
                SendChannel.CANCEL, member.getId(), member.getStoreId(), employee == null ? null : employee.getId());
    }


    static CareHisRecordEntity offlineBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberEntity member) {
        return new CareHisRecordEntity(care.getCareId(), "线下跟进", null, BusinessType.BIRTHDAYCARE, null, LocalDateTime.now(),
                SendChannel.OFFLINE, member.getId(), member.getStoreId(), employee == null ? null : employee.getId());
    }

    static CareHisRecordEntity smsBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberAgg memberAgg,
                                                      String context) {
        MemberEntity member = memberAgg.getMember();
        LocalDateTime now = LocalDateTime.now();
        if (Strings.isNullOrEmpty(context)) context = "无模板信息...";
        return new CareHisRecordEntity(care.getCareId(), context, null, BusinessType.BIRTHDAYCARE, null, now,
                SendChannel.SMS, member.getId(), member.getStoreId(), employee == null ? null : employee.getId());
    }

    static CareHisRecordEntity wxBirthdayCare4Member(CareBirthdayEntity care, EmpEntity employee, MemberAgg agg, String context) {
        MemberEntity member = agg.getMember();
        LocalDateTime now = LocalDateTime.now();
        if (Strings.isNullOrEmpty(context)) context = "无模板信息...";
        return new CareHisRecordEntity(care.getCareId(), context, null, BusinessType.BIRTHDAYCARE, null, now,
                SendChannel.WECHAT, member.getId(), member.getStoreId(), employee == null ? null : employee.getId());
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        createTime, followUpContent, followUpTime, followUpWay, memberFeedback, planId, planName, planType,
        ps.setObject(1, followUpContent);
        ps.setObject(2, followUpWay);
        ps.setObject(3, memberFeedback);
        ps.setObject(4, planId);
        ps.setObject(5, planName);
        ps.setObject(6, planType);
        //  shoppingGuideSummary, status, updateTime, member_id, store_id, employee_id, operateType)
        ps.setObject(7, shoppingGuideSummary);
        ps.setObject(8, memberId);
        ps.setObject(9, storeId);
        ps.setObject(10, employeeId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("followUpContent", followUpContent)
                .add("memberFeedback", memberFeedback)
                .add("planName", planName)
                .add("shoppingGuideSummary", shoppingGuideSummary)
                .add("followUpTime", followUpTime)
                .add("updateTime", updateTime)
                .add("followUpWay", followUpWay)
                .add("planType", planType)
                .add("planId", planId)
                .add("status", status)
                .add("operateType", operateType)
                .add("memberId", memberId)
                .add("storeId", storeId)
                .add("employeeId", employeeId)
                .toString();
    }
}
