package com.legooframework.model.wechatcircle.entity;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class DataSourcesFrom {

    private final String weixinId;
    private final Integer companyId, storeId;

    public DataSourcesFrom(String weixinId, Integer companyId, Integer storeId) {
        this.weixinId = weixinId;
        this.companyId = companyId;
        this.storeId = storeId;
    }

    // create 4 DB
    DataSourcesFrom(String sourcesFrom) {
        String[] args = StringUtils.split(sourcesFrom, ',');
        Preconditions.checkArgument(args.length == 3, "非法的入参 %s，无法转化为 DataSourcesFrom", sourcesFrom);
        this.weixinId = args[0];
        this.companyId = Integer.valueOf(args[1]);
        this.storeId = Integer.valueOf(args[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourcesFrom that = (DataSourcesFrom) o;
        return Objects.equal(weixinId, that.weixinId) &&
                Objects.equal(companyId, that.companyId) &&
                Objects.equal(storeId, that.storeId);
    }

    public String getWeixinId() {
        return weixinId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public Integer getStoreId() {
        return storeId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(weixinId, companyId, storeId);
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s", weixinId, companyId, storeId);
    }
}
