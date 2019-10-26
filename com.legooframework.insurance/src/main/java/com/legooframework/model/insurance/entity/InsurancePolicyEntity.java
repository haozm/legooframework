package com.legooframework.model.insurance.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.dict.entity.KvDictEntity;
import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class InsurancePolicyEntity extends BaseEntity<Integer> {

    private Integer defrayer, accepter;
    private String insuranceNo;
    private LocalDate insuredDate;

    private String beneficiary;

    private final String relationshipType, relationshipName;
    private final boolean myself;

    private String paymentType, paymentTypeName;
    private BigDecimal payAmount;

    // private List<InsuranceInfoEntity> insuranceInfos;
    private Integer bankCardId;

    private String remarks;

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap("insuranceInfos");
        params.put("defrayer", defrayer);
        params.put("accepter", accepter);
        params.put("insuranceNo", insuranceNo);
        params.put("insuredDate", insuredDate.toDate());
        params.put("beneficiary", beneficiary);
        params.put("relationshipType", relationshipType);
        params.put("relationshipName", relationshipName);
        params.put("myself", myself ? 1 : 0);
        params.put("paymentType", paymentType);
        params.put("payAmount", payAmount);
        params.put("bankCardId", bankCardId);
        params.put("remarks", remarks);
        return params;
    }

    public String getInsuranceNo() {
        return insuranceNo;
    }

    InsurancePolicyEntity(Long id, ResultSet res) {
        super(id.intValue());
        try {
            this.defrayer = ResultSetUtil.getObject(res, "defrayer", Long.class).intValue();
            this.accepter = ResultSetUtil.getObject(res, "accepter", Long.class).intValue();
            this.insuranceNo = ResultSetUtil.getString(res, "insuranceNo");
            this.insuredDate = LocalDate.fromDateFields(res.getDate("insuredDate"));
            this.beneficiary = ResultSetUtil.getString(res, "beneficiary");
            this.relationshipType = ResultSetUtil.getString(res, "relationshipType");
            this.relationshipName = ResultSetUtil.getString(res, "relationshipName");
            this.myself = ResultSetUtil.getBooleanByInt(res, "myself");
            this.paymentType = ResultSetUtil.getString(res, "paymentType");
            this.paymentTypeName = ResultSetUtil.getString(res, "paymentTypeName");
            this.payAmount = res.getBigDecimal("payAmount");
            this.bankCardId = ResultSetUtil.getObject(res, "bankCardId", Long.class).intValue();
            this.remarks = ResultSetUtil.getOptString(res, "remarks", null);
        } catch (SQLException e) {
            throw new RuntimeException("Restore InsurancePolicyEntity has SQLException", e);
        }
    }

    public InsurancePolicyEntity(MemberEntity defrayer, MemberEntity accepter, String insuranceNo, Date insuredDate,
                                 KvDictEntity relationship, KvDictEntity paymentType, Double payAmount, BankCardEntity backCard,
                                 List<InsuranceInfoEntity> insuranceInfos, String beneficiary, String remarks) {
        super(UUID.randomUUID().toString().hashCode());
        this.defrayer = defrayer.getId();
        this.accepter = accepter.getId();

        this.insuranceNo = insuranceNo;
        this.beneficiary = beneficiary;
        this.relationshipType = relationship.getValue();
        this.relationshipName = null;
        this.insuredDate = LocalDate.fromDateFields(insuredDate);
        this.myself = false;
        this.paymentType = paymentType.getValue();
        this.payAmount = new BigDecimal(payAmount);
        List<InsuranceInfoEntity> _list = insuranceInfos.stream().filter(InsuranceInfoEntity::isPrimary).collect(Collectors.toList());
        Preconditions.checkState(_list.size() >= 1, "同一保单需有一份主险...");
//        insuranceInfos.forEach(x -> x.setInsuranceId(this));
//        this.insuranceInfos = insuranceInfos;
        this.bankCardId = backCard.getId();
        this.remarks = remarks;
    }

    public InsurancePolicyEntity(MemberEntity defrayer, String insuranceNo, Date insuredDate,
                                 KvDictEntity paymentType, Double payAmount, BankCardEntity backCard,
                                 List<InsuranceInfoEntity> insuranceInfos, String beneficiary, String remarks) {
        super(UUID.randomUUID().toString().hashCode());
        this.defrayer = defrayer.getId();
        this.accepter = defrayer.getId();
        this.insuranceNo = insuranceNo;
        this.beneficiary = beneficiary;
        this.relationshipType = "YY";
        this.relationshipName = "本人";
        this.myself = true;
        this.paymentType = paymentType.getValue();
        this.insuredDate = LocalDate.fromDateFields(insuredDate);
        this.payAmount = new BigDecimal(payAmount);
        List<InsuranceInfoEntity> _list = insuranceInfos.stream().filter(InsuranceInfoEntity::isPrimary).collect(Collectors.toList());
        Preconditions.checkState(_list.size() >= 1, "同一保单需有一份主险...");
//        insuranceInfos.forEach(x -> x.setInsuranceId(this));
//        this.insuranceInfos = insuranceInfos;
        this.bankCardId = backCard.getId();
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InsurancePolicyEntity)) return false;
        if (!super.equals(o)) return false;
        InsurancePolicyEntity that = (InsurancePolicyEntity) o;
        return myself == that.myself &&
                Objects.equal(defrayer, that.defrayer) &&
                Objects.equal(accepter, that.accepter) &&
                Objects.equal(insuranceNo, that.insuranceNo) &&
                Objects.equal(insuredDate.toString("yyyy-MM-dd"), that.insuredDate.toString("yyyy-MM-dd")) &&
                Objects.equal(relationshipType, that.relationshipType) &&
                Objects.equal(relationshipName, that.relationshipName) &&
                Objects.equal(paymentType, that.paymentType) &&
                Objects.equal(payAmount, that.payAmount) &&
                Objects.equal(bankCardId, that.bankCardId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(defrayer, accepter, insuranceNo, insuredDate, beneficiary, relationshipType,
                relationshipName, myself, paymentType, payAmount, bankCardId);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("insuranceNo", insuranceNo)
                .add("insuredDate", insuredDate.toString("yyyy-MM-dd"))
                .add("defrayer", defrayer)
                .add("accepter", accepter)
                .add("beneficiary", beneficiary)
                .add("relationshipType", relationshipType)
                .add("relationshipName", relationshipName)
                .add("myself", myself)
                .add("paymentType", paymentType)
                .add("payAmount", payAmount)
                .add("bankCardId", bankCardId)
                .toString();
    }
}
