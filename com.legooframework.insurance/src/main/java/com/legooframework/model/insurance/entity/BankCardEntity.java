package com.legooframework.model.insurance.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public class BankCardEntity extends BaseEntity<Integer> {

    private Integer memberId;
    private String bankType;
    private final String bankName;
    private String account;

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> params = super.toParamMap(excludes);
        params.put("memberId", memberId);
        params.put("bankType", bankType);
        params.put("bankName", bankName);
        params.put("account", account);
        return params;
    }

    BankCardEntity(MemberEntity member, String bankType, String account) {
        super(UUID.randomUUID().toString().hashCode());
        this.memberId = member.getId();
        this.bankType = bankType;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(account), "银行卡卡号为必填项");
        this.account = account;
        this.bankName = null;
    }

    BankCardEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.memberId = ResultSetUtil.getObject(res, "memberId", Long.class).intValue();
            this.bankType = ResultSetUtil.getString(res, "bankType");
            this.bankName = ResultSetUtil.getString(res, "bankName");
            this.account = ResultSetUtil.getString(res, "account");
        } catch (SQLException e) {
            throw new RuntimeException("Restore BankCardEntity has SQLException", e);
        }
    }

    Integer getMemberId() {
        return memberId;
    }

    String getBankName() {
        return bankName;
    }

    String getBankType() {
        return bankType;
    }

    String getAccount() {
        return account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BankCardEntity)) return false;
        BankCardEntity that = (BankCardEntity) o;
        return Objects.equal(this.getId(), that.getId()) &&
                Objects.equal(memberId, that.memberId) &&
                Objects.equal(bankType, that.bankType) &&
                Objects.equal(account, that.account);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.getId(), memberId, bankType, account);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("memberId", memberId)
                .add("bankType", bankType)
                .add("bankName", bankName)
                .add("account", account)
                .toString();
    }
}
