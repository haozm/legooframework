package com.csosm.module.member.entity;


import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.csosm.module.webchat.entity.Distance;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class Member4MatchWebChatDto implements Cloneable, Distance {

    private final Integer id;
    private final String name, phone;
    private final Integer sex;
    private String srfm;
    private String crfm;
    private Date lastVisitTime;
    private String memberCardNum;
    private double jaccardDistance = 99D;
    private double jaroWinklerDistance = 0D;
    private double levenshteinDistance = Integer.MAX_VALUE;

    public Member4MatchWebChatDto(Integer id, String name, String phone, Integer sex,
                           String srfm, String crfm, Date lastVisitTime,
                           String memberCardNum) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.sex = sex;
        this.srfm = srfm;
        this.crfm = crfm;
        this.lastVisitTime = lastVisitTime;
        this.memberCardNum = memberCardNum;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("memberId", getId());
        map.put("memberName", name);
        map.put("memberPhone", phone);
        map.put("distance", getJaroWinklerDistance());
        map.put("sex", sex);
        map.put("srfm", this.srfm);
        map.put("memberCardNum", this.memberCardNum);
        map.put("crfm", this.crfm);
        map.put("lastVisitTime", this.lastVisitTime == null ? "" : DateFormatUtils.format(this.lastVisitTime, "yyyy-MM-dd hh:mm:ss"));
        return map;
    }

    public boolean isNullOrEmpty() {
        return Strings.isNullOrEmpty(name) && Strings.isNullOrEmpty(phone);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSex() {
        return sex;
    }

    public Optional<String> getName() {
        return Optional.fromNullable(name);
    }

    public String getMemberCardNum() {
        return memberCardNum;
    }

    public Optional<String> getPhone() {
        return Optional.fromNullable(phone);
    }

    @Override
    public double getJaccardDistance() {
        return jaccardDistance;
    }

    public void setJaccardDistance(double jaccardDistance) {
        this.jaccardDistance = jaccardDistance;
    }

    @Override
    public double getJaroWinklerDistance() {
        return jaroWinklerDistance;
    }

    public void setJaroWinklerDistance(double jaroWinklerDistance) {
        this.jaroWinklerDistance = jaroWinklerDistance;
    }

    @Override
    public double getLevenshteinDistance() {
        return levenshteinDistance;
    }

    public void setLevenshteinDistance(double levenshteinDistance) {
        this.levenshteinDistance = levenshteinDistance;
    }


    public Date getLastVisitTime() {
        return lastVisitTime;
    }

    public String getSrfm() {
        return srfm;
    }

    public String getCrfm() {
        return crfm;
    }

    public Member4MatchWebChatDto cloneMe() {
        try {
            return (Member4MatchWebChatDto) this.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member4MatchWebChatDto that = (Member4MatchWebChatDto) o;
        return Double.compare(that.jaccardDistance, jaccardDistance) == 0 &&
                Double.compare(that.jaroWinklerDistance, jaroWinklerDistance) == 0 &&
                Double.compare(that.levenshteinDistance, levenshteinDistance) == 0 &&
                java.util.Objects.equals(id, that.id) &&
                java.util.Objects.equals(name, that.name) &&
                java.util.Objects.equals(phone, that.phone) &&
                java.util.Objects.equals(sex, that.sex) &&
                java.util.Objects.equals(srfm, that.srfm) &&
                java.util.Objects.equals(crfm, that.crfm) &&
                java.util.Objects.equals(lastVisitTime, that.lastVisitTime) &&
                java.util.Objects.equals(memberCardNum, that.memberCardNum);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, name, phone, sex, srfm, crfm, lastVisitTime,
                jaccardDistance, jaroWinklerDistance, levenshteinDistance, memberCardNum);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("phone", phone)
                .add("sex", sex)
                .add("srfm", srfm)
                .add("crfm", crfm)
                .add("lastVisitTime", lastVisitTime)
                .add("jaccardDistance", jaccardDistance)
                .add("jaroWinklerDistance", jaroWinklerDistance)
                .add("levenshteinDistance", levenshteinDistance)
                .add("memberCardNum", memberCardNum)
                .toString();
    }
}
