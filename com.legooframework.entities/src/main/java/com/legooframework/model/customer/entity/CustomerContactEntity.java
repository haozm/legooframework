package com.legooframework.model.customer.entity;

import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class CustomerContactEntity extends BaseEntity<CustomerId> {

    private String callphoneNumber, telephoneNumber, qqNumber, email;

    private String workAddress, homeAddress;

    public CustomerContactEntity(CustomerEntity customer, String callphoneNumber, String telephoneNumber,
                                 String qqNumber, String email, String workAddress, String homeAddress, LoginContext lc) {
        super(customer.getId(), lc.getTenantId(), lc.getLoginId());
        this.callphoneNumber = callphoneNumber;
        this.telephoneNumber = telephoneNumber;
        this.qqNumber = qqNumber;
        this.email = email;
        this.workAddress = workAddress;
        this.homeAddress = homeAddress;
    }

    public CustomerContactEntity(CustomerId customerId, ResultSet res) throws SQLException {
        super(customerId, res);
        this.callphoneNumber = res.getString("callphoneNumber");
        this.telephoneNumber = res.getString("telephoneNumber");
        this.qqNumber = res.getString("qqNumber");
        this.email = res.getString("email");
        this.workAddress = res.getString("workAddress");
        this.homeAddress = res.getString("homeAddress");
    }

    public Optional<CustomerContactEntity> modifyVaried(String callphoneNumber, String telephoneNumber,
                                                        String qqNumber, String email, String workAddress, String homeAddress) {
        CustomerContactEntity clone = (CustomerContactEntity) this.cloneMe();
        clone.callphoneNumber = callphoneNumber;
        clone.telephoneNumber = telephoneNumber;
        clone.qqNumber = qqNumber;
        clone.email = email;
        clone.workAddress = workAddress;
        clone.homeAddress = homeAddress;
        if (clone.equals(this))
            return Optional.empty();
        return Optional.of(clone);
    }

    public Optional<CustomerContactEntity> modifyTelephone(String telephoneNumber) {
        CustomerContactEntity clone = (CustomerContactEntity) this.cloneMe();
        clone.telephoneNumber = telephoneNumber;
        if (clone.equals(this))
            return Optional.empty();
        return Optional.of(clone);
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> paramMap = super.toParamMap(excludes);
        paramMap.put("id", this.getId().getId());
        paramMap.put("channel", this.getId().getChannel().getVal());
        paramMap.put("storeId", this.getId().getStoreId());
        paramMap.put("callphoneNumber", this.callphoneNumber);
        paramMap.put("telephoneNumber", this.telephoneNumber);
        paramMap.put("qqNumber", this.qqNumber);
        paramMap.put("email", this.email);
        paramMap.put("workAddress", this.workAddress);
        paramMap.put("homeAddress", this.homeAddress);
        return paramMap;
    }

    public String getCallphoneNumber() {
        return callphoneNumber;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getQqNumber() {
        return qqNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getWorkAddress() {
        return workAddress;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((callphoneNumber == null) ? 0 : callphoneNumber.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((homeAddress == null) ? 0 : homeAddress.hashCode());
        result = prime * result + ((qqNumber == null) ? 0 : qqNumber.hashCode());
        result = prime * result + ((telephoneNumber == null) ? 0 : telephoneNumber.hashCode());
        result = prime * result + ((workAddress == null) ? 0 : workAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomerContactEntity other = (CustomerContactEntity) obj;
        if (callphoneNumber == null) {
            if (other.callphoneNumber != null)
                return false;
        } else if (!callphoneNumber.equals(other.callphoneNumber))
            return false;
        if (email == null) {
            if (other.email != null)
                return false;
        } else if (!email.equals(other.email))
            return false;
        if (homeAddress == null) {
            if (other.homeAddress != null)
                return false;
        } else if (!homeAddress.equals(other.homeAddress))
            return false;
        if (qqNumber == null) {
            if (other.qqNumber != null)
                return false;
        } else if (!qqNumber.equals(other.qqNumber))
            return false;
        if (telephoneNumber == null) {
            if (other.telephoneNumber != null)
                return false;
        } else if (!telephoneNumber.equals(other.telephoneNumber))
            return false;
        if (workAddress == null) {
            if (other.workAddress != null)
                return false;
        } else if (!workAddress.equals(other.workAddress))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CustomerContactEntity [callphoneNumber=" + callphoneNumber + ", telephoneNumber=" + telephoneNumber
                + ", qqNumber=" + qqNumber + ", email=" + email + ", workAddress=" + workAddress + ", homeAddress="
                + homeAddress + "]";
    }

}
