package com.legooframework.model.takecare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.covariant.entity.MemberEntity;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class CareBirthdayEntity extends BaseEntity<Integer> implements BatchSetter {

    private final Integer companyId, storeId, memberId;
    // 计划状态：1 - 未开始，2 - 已完成
    private Integer planState, careId;
    private Date followUpTime, updateTime;
    private int operateType = 0;
    // 内容
    private String followUpContent, memberFeedback, shoppingGuideSummary;
    private final int calendarType;
    private final LocalDate birthday, thisYearBirthday;
    private final LocalDate careDate;


    CareBirthdayEntity(MemberEntity member, String followUpContent) {
        super(0);
        this.companyId = member.getCompanyId();
        this.careId = 0;
        this.storeId = member.getStoreId();
        this.memberId = member.getId();
        this.calendarType = member.getCalendarType();
        this.birthday = member.getBirthday().orElse(null);
        this.thisYearBirthday = member.getThisYearBirthday().orElse(null);
        this.planState = member.getBirthday().isPresent() ? 2 : 3;
        if (!member.getBirthday().isPresent()) {
            this.followUpContent = "错误：无生日信息";
        } else if (Strings.isNullOrEmpty(followUpContent)) {
            this.followUpContent = "错误：无模板信息...";
        } else {
            this.followUpContent = followUpContent;
        }
        this.followUpTime = LocalDateTime.now().toDate();
        this.updateTime = this.followUpTime;
        this.careDate = LocalDateTime.now().toLocalDate();
    }

    public boolean hasError() {
        return this.planState != 2;
    }

    Integer getStoreId() {
        return storeId;
    }

    Integer getCompanyId() {
        return companyId;
    }

    CareBirthdayEntity(Integer id, ResultSet resultSet) throws RuntimeException {
        super(id);
        try {
            this.companyId = resultSet.getInt("company_id");
            this.storeId = resultSet.getInt("store_id");
            this.careId = id;
            this.memberId = resultSet.getInt("member_id");
            this.planState = resultSet.getInt("planState");
            this.updateTime = resultSet.getDate("updateTime");
            this.followUpTime = resultSet.getDate("followUpTime");
            this.followUpContent = resultSet.getString("followUpContent");
            this.memberFeedback = resultSet.getString("memberFeedback");
            this.calendarType = resultSet.getInt("calendarType");
            this.birthday = resultSet.getDate("birthday") == null ? null :
                    LocalDate.fromDateFields(resultSet.getDate("birthday"));
            this.thisYearBirthday = resultSet.getDate("this_year_birthday") == null ? null :
                    LocalDate.fromDateFields(resultSet.getDate("this_year_birthday"));
            this.careDate = resultSet.getDate("care_date") == null ? null :
                    LocalDate.fromDateFields(resultSet.getDate("care_date"));
        } catch (SQLException e) {
            throw new RuntimeException("还原对象 BirthdayCarePlanEntity 发生异常", e);
        }
    }

    Integer getCareId() {
        return careId;
    }

    boolean hasSaved() {
        return getId() != 0;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        followUpContent, followUpTime,  memberFeedback, planState, shoppingGuideSummary, updateTime,
        ps.setObject(1, this.followUpContent);
        ps.setObject(2, this.followUpTime);
        ps.setObject(3, this.memberFeedback);
        ps.setObject(4, this.planState);
        ps.setObject(5, this.shoppingGuideSummary);
        ps.setObject(6, this.updateTime);
        //member_id, store_id, operateType, company_id, calendarType, birthday, this_year_birthday
        ps.setObject(7, this.memberId);
        ps.setObject(8, this.storeId);
        ps.setObject(9, this.operateType);
        ps.setObject(10, this.companyId);
        ps.setObject(11, this.calendarType);
        ps.setObject(12, this.birthday == null ? new LocalDate(1970, 1, 1).toDate() : birthday.toDate());
        if (this.thisYearBirthday == null) {
            LocalDate now = LocalDate.now();
            ps.setObject(13, new LocalDate(now.getYear(), 1, 1).toDate());
        } else {
            ps.setObject(13, thisYearBirthday.toDate());
        }
        ps.setObject(14, this.careDate.toDate());
    }

    void setCareId(Integer careId) {
        this.careId = careId;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("planState", planState);
        params.put("updateTime", updateTime);
        params.put("followUpContent", followUpContent);
        params.put("memberFeedback", memberFeedback);
        return params;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("memberId", memberId)
                .add("planState", planState)
                .add("followUpTime", followUpTime)
                .add("updateTime", updateTime)
                .add("followUpContent", followUpContent)
                .add("memberFeedback", memberFeedback)
                .add("careDate", careDate)
                .add("calendarType", calendarType)
                .add("birthday", birthday == null ? null : birthday.toString("yyyy-MM-dd"))
                .add("thisYearBirthday", thisYearBirthday == null ? null : thisYearBirthday.toString("yyyy-MM-dd"))
                .toString();
    }
}
