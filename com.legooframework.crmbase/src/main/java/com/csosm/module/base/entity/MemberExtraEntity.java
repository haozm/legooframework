package com.csosm.module.base.entity;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.util.Map;

public class MemberExtraEntity extends BaseEntity<Integer> {
    // 上装尺码
    private String jacketSize;
    // 下装尺码
    private String bottomsSize;
    // 内衣尺码
    private String braSize;
    // 内裤尺码
    private String briefsSize;
    // 鞋子尺码
    private String shoeSize;
    // 胸围(cm)
    private BigDecimal chest;
    // 衣长(cm)
    private BigDecimal clothingLong;
    // 袖长(cm)
    private BigDecimal sleeveLength;
    // 肩宽(cm)
    private BigDecimal shoulder;
    // 腰围(cm)
    private BigDecimal waistline;
    // 臀围(cm)
    private BigDecimal hipline;
    // 大腿围(cm)
    private BigDecimal thighCircumference;
    // 膝围(cm)
    private BigDecimal kneeCircumference;
    // 裤脚(cm)
    private BigDecimal trouserLeg;
    // 前裆(cm)
    private BigDecimal beforeFork;
    // 后裆(cm)
    private BigDecimal afterFork;
    // 裤长(cm)
    private BigDecimal outseam;
    // 上胸围(cm)
    private BigDecimal onChest;
    // 下胸围(cm)
    private BigDecimal underChest;
    // 脚长(cm)
    private BigDecimal footLength;

    private Integer status;

    public MemberExtraEntity(Integer id) {
        super(id);
    }

    MemberExtraEntity(MemberEntity member, String jacketSize, String bottomsSize, String braSize, String briefsSize,
                      String shoeSize, BigDecimal chest, BigDecimal clothingLong, BigDecimal sleeveLength, BigDecimal shoulder,
                      BigDecimal waistline, BigDecimal hipline, BigDecimal thighCircumference, BigDecimal kneeCircumference,
                      BigDecimal trouserLeg, BigDecimal beforeFork, BigDecimal afterFork, BigDecimal outseam, BigDecimal onChest,
                      BigDecimal underChest, BigDecimal footLength, Integer status) {
        super(member.getId());
        this.jacketSize = jacketSize;
        this.bottomsSize = bottomsSize;
        this.braSize = braSize;
        this.briefsSize = briefsSize;
        this.shoeSize = shoeSize;
        this.chest = chest;
        this.clothingLong = clothingLong;
        this.sleeveLength = sleeveLength;
        this.shoulder = shoulder;
        this.waistline = waistline;
        this.hipline = hipline;
        this.thighCircumference = thighCircumference;
        this.kneeCircumference = kneeCircumference;
        this.trouserLeg = trouserLeg;
        this.beforeFork = beforeFork;
        this.afterFork = afterFork;
        this.outseam = outseam;
        this.onChest = onChest;
        this.underChest = underChest;
        this.footLength = footLength;
        this.status = status;
    }

    MemberExtraEntity(Integer memberId, String jacketSize, String bottomsSize, String braSize, String briefsSize,
                      String shoeSize, BigDecimal chest, BigDecimal clothingLong, BigDecimal sleeveLength, BigDecimal shoulder,
                      BigDecimal waistline, BigDecimal hipline, BigDecimal thighCircumference, BigDecimal kneeCircumference,
                      BigDecimal trouserLeg, BigDecimal beforeFork, BigDecimal afterFork, BigDecimal outseam, BigDecimal onChest,
                      BigDecimal underChest, BigDecimal footLength, Integer status) {
        super(memberId);
        this.jacketSize = jacketSize;
        this.bottomsSize = bottomsSize;
        this.braSize = braSize;
        this.briefsSize = briefsSize;
        this.shoeSize = shoeSize;
        this.chest = chest;
        this.clothingLong = clothingLong;
        this.sleeveLength = sleeveLength;
        this.shoulder = shoulder;
        this.waistline = waistline;
        this.hipline = hipline;
        this.thighCircumference = thighCircumference;
        this.kneeCircumference = kneeCircumference;
        this.trouserLeg = trouserLeg;
        this.beforeFork = beforeFork;
        this.afterFork = afterFork;
        this.outseam = outseam;
        this.onChest = onChest;
        this.underChest = underChest;
        this.footLength = footLength;
        this.status = status;
    }

    public MemberExtraEntity modify(String jacketSize, String bottomsSize, String braSize, String briefsSize,
                                    String shoeSize, BigDecimal chest, BigDecimal clothingLong, BigDecimal sleeveLength, BigDecimal shoulder,
                                    BigDecimal waistline, BigDecimal hipline, BigDecimal thighCircumference, BigDecimal kneeCircumference,
                                    BigDecimal trouserLeg, BigDecimal beforeFork, BigDecimal afterFork, BigDecimal outseam, BigDecimal onChest,
                                    BigDecimal underChest, BigDecimal footLength) {
        MemberExtraEntity clone = null;
        try {
            clone = (MemberExtraEntity) this.clone();
            clone.jacketSize = jacketSize;
            clone.bottomsSize = bottomsSize;
            clone.braSize = braSize;
            clone.briefsSize = briefsSize;
            clone.shoeSize = shoeSize;
            clone.chest = chest;
            clone.clothingLong = clothingLong;
            clone.sleeveLength = sleeveLength;
            clone.shoulder = shoulder;
            clone.waistline = waistline;
            clone.hipline = hipline;
            clone.thighCircumference = thighCircumference;
            clone.kneeCircumference = kneeCircumference;
            clone.trouserLeg = trouserLeg;
            clone.beforeFork = beforeFork;
            clone.afterFork = afterFork;
            clone.outseam = outseam;
            clone.onChest = onChest;
            clone.underChest = underChest;
            clone.footLength = footLength;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("修改数据发生异常");
        }
        return clone;
    }

    public String getJacketSize() {
        return jacketSize;
    }

    public String getBottomsSize() {
        return bottomsSize;
    }

    public String getBraSize() {
        return braSize;
    }

    public String getBriefsSize() {
        return briefsSize;
    }

    public String getShoeSize() {
        return shoeSize;
    }

    public BigDecimal getChest() {
        return chest;
    }

    public BigDecimal getClothingLong() {
        return clothingLong;
    }

    public BigDecimal getSleeveLength() {
        return sleeveLength;
    }

    public BigDecimal getShoulder() {
        return shoulder;
    }

    public BigDecimal getWaistline() {
        return waistline;
    }

    public BigDecimal getHipline() {
        return hipline;
    }

    public BigDecimal getThighCircumference() {
        return thighCircumference;
    }

    public BigDecimal getKneeCircumference() {
        return kneeCircumference;
    }

    public BigDecimal getTrouserLeg() {
        return trouserLeg;
    }

    public BigDecimal getBeforeFork() {
        return beforeFork;
    }

    public BigDecimal getAfterFork() {
        return afterFork;
    }

    public BigDecimal getOutseam() {
        return outseam;
    }

    public BigDecimal getOnChest() {
        return onChest;
    }

    public BigDecimal getUnderChest() {
        return underChest;
    }

    public BigDecimal getFootLength() {
        return footLength;
    }

    public Integer getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MemberExtraEntity that = (MemberExtraEntity) o;
        return Objects.equal(jacketSize, that.jacketSize) &&
                Objects.equal(bottomsSize, that.bottomsSize) &&
                Objects.equal(braSize, that.braSize) &&
                Objects.equal(briefsSize, that.briefsSize) &&
                Objects.equal(shoeSize, that.shoeSize) &&
                Objects.equal(chest, that.chest) &&
                Objects.equal(clothingLong, that.clothingLong) &&
                Objects.equal(sleeveLength, that.sleeveLength) &&
                Objects.equal(shoulder, that.shoulder) &&
                Objects.equal(waistline, that.waistline) &&
                Objects.equal(hipline, that.hipline) &&
                Objects.equal(thighCircumference, that.thighCircumference) &&
                Objects.equal(kneeCircumference, that.kneeCircumference) &&
                Objects.equal(trouserLeg, that.trouserLeg) &&
                Objects.equal(beforeFork, that.beforeFork) &&
                Objects.equal(afterFork, that.afterFork) &&
                Objects.equal(outseam, that.outseam) &&
                Objects.equal(onChest, that.onChest) &&
                Objects.equal(underChest, that.underChest) &&
                Objects.equal(footLength, that.footLength) &&
                Objects.equal(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), jacketSize, bottomsSize, braSize, briefsSize, shoeSize, chest, clothingLong,
                sleeveLength, shoulder, waistline, hipline, thighCircumference, kneeCircumference, trouserLeg, beforeFork,
                afterFork, outseam, onChest, underChest, footLength, status);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("memberId", getId());
        map.put("jacketSize", Strings.isNullOrEmpty(jacketSize) ? null : Integer.parseInt(jacketSize));
        map.put("bottomsSize", Strings.isNullOrEmpty(bottomsSize) ? null : Integer.parseInt(bottomsSize));
        map.put("braSize", Strings.isNullOrEmpty(braSize) ? null : Integer.parseInt(braSize));
        map.put("briefsSize", Strings.isNullOrEmpty(briefsSize) ? null : Integer.parseInt(briefsSize));
        map.put("shoeSize", Strings.isNullOrEmpty(shoeSize) ? null : Integer.parseInt(shoeSize));
        map.put("chest", chest == null ? null : chest.toString());
        map.put("clothingLong", clothingLong == null ? null : clothingLong.toString());
        map.put("sleeveLength", sleeveLength == null ? null : sleeveLength.toString());
        map.put("shoulder", shoulder == null ? null : shoulder.toString());
        map.put("waistline", waistline == null ? null : waistline.toString());
        map.put("hipline", hipline == null ? null : hipline.toString());
        map.put("thighCircumference", thighCircumference == null ? null : thighCircumference.toString());
        map.put("kneeCircumference", kneeCircumference == null ? null : kneeCircumference.toString());
        map.put("trouserLeg", trouserLeg == null ? null : trouserLeg.toString());
        map.put("beforeFork", beforeFork == null ? null : beforeFork.toString());
        map.put("afterFork", afterFork == null ? null : afterFork.toString());
        map.put("outseam", outseam == null ? null : outseam.toString());
        map.put("onChest", onChest == null ? null : onChest.toString());
        map.put("underChest", underChest == null ? null : underChest.toString());
        map.put("footLength", footLength == null ? null : footLength.toString());
        map.put("status", status);
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jacketSize", jacketSize)
                .add("bottomsSize", bottomsSize)
                .add("braSize", braSize)
                .add("briefsSize", briefsSize)
                .add("shoeSize", shoeSize)
                .add("chest", chest)
                .add("clothingLong", clothingLong)
                .add("sleeveLength", sleeveLength)
                .add("shoulder", shoulder)
                .add("waistline", waistline)
                .add("hipline", hipline)
                .add("thighCircumference", thighCircumference)
                .add("kneeCircumference", kneeCircumference)
                .add("trouserLeg", trouserLeg)
                .add("beforeFork", beforeFork)
                .add("afterFork", afterFork)
                .add("outseam", outseam)
                .add("onChest", onChest)
                .add("underChest", underChest)
                .add("footLength", footLength)
                .add("status", status)
                .toString();
    }
}
