package com.legooframework.model.smsgateway.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.commons.collections4.CollectionUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ReimburseResDto {

    private final Integer companyId, storeId;
    private final String batchNo;
    private final int totalSmsCount;
    private final List<String> smsIds;

    ReimburseResDto(ResultSet res) {
        try {
            this.companyId = res.getInt("company_id");
            this.storeId = res.getInt("store_id");
            this.totalSmsCount = res.getInt("totalSmsCount");
            this.batchNo = UUID.randomUUID().toString();
            String smsIds_raw = res.getString("smsIds");
            if (!Strings.isNullOrEmpty(smsIds_raw)) {
                this.smsIds = Splitter.on(',').splitToList(smsIds_raw);
            } else {
                this.smsIds = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore SendMsg4ReimburseDto has SQLException", e);
        }
    }

    public Integer getStoreId() {
        return storeId;
    }

    boolean isEmpty() {
        return CollectionUtils.isEmpty(smsIds);
    }

    Integer getCompanyId() {
        return companyId;
    }

    String getBatchNo() {
        return batchNo;
    }

    int getTotalSmsCount() {
        return totalSmsCount;
    }

    List<String> getSmsIds() {
        return smsIds;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("batchNo", batchNo)
                .add("totalSmsCount", totalSmsCount)
                .add("smsIds' size ", smsIds == null ? 0 : smsIds.size())
                .toString();
    }
}
