package com.legooframework.model.membercare.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.BatchSetter;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.joda.time.LocalDate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Touch90CareLogEntity extends BaseEntity<String> implements BatchSetter {

    private static final Joiner STAGE_JOINER = Joiner.on(',');
    private final Integer storeId, companyId;
    private final String categories, logDatePk;
    private final LocalDate logDate;
    private int count4Add;
    private Set<Integer> addList;
    private int count4Update;
    private Set<Integer> updateList;

    Touch90CareLogEntity(CrmStoreEntity store, String categories, LocalDate logDate, Collection<Integer> addList, Collection<Integer> updateList) {
        super("", store.getCompanyId().longValue(), -1L);
        this.storeId = store.getId();
        this.companyId = store.getCompanyId();
        this.logDate = logDate;
        this.logDatePk = logDate.toString("yyyy-MM-dd");
        this.addList = Sets.newHashSet(addList);
        this.updateList = Sets.newHashSet(updateList);
        this.count4Add = addList.size();
        this.count4Update = updateList.size();
        this.categories = categories;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
//        company_id, store_id, log_date, add_list, add_size, update_list, update_size
        ps.setObject(1, this.companyId);
        ps.setObject(2, this.storeId);
        ps.setObject(3, this.logDate.toDate());
        ps.setObject(4, CollectionUtils.isNotEmpty(addList) ? STAGE_JOINER.join(addList) : null);
        ps.setObject(5, count4Add);
        ps.setObject(6, CollectionUtils.isNotEmpty(updateList) ? STAGE_JOINER.join(updateList) : null);
        ps.setObject(7, count4Update);
        ps.setObject(8, this.companyId.longValue());
        ps.setObject(9, this.categories);
        ps.setObject(10, this.logDatePk);
    }

    Touch90CareLogEntity(String id, ResultSet res) {
        super(id, res);
        try {
            this.storeId = ResultSetUtil.getObject(res, "storeId", Integer.class);
            this.categories = ResultSetUtil.getOptString(res, "categories", null);
            this.companyId = ResultSetUtil.getObject(res, "companyId", Integer.class);
            this.logDatePk = ResultSetUtil.getString(res, "logDatePk");
            this.logDate = LocalDate.fromDateFields(ResultSetUtil.getObject(res, "logDate", Date.class));
            String _add_list = ResultSetUtil.getOptString(res, "addList", null);
            if (Strings.isEmpty(_add_list)) {
                this.addList = null;
                this.count4Add = 0;
            } else {
                this.addList = Stream.of(StringUtils.split(_add_list, ',')).map(Integer::valueOf).collect(Collectors.toSet());
                this.count4Add = this.addList.size();
            }
            String _update_list = ResultSetUtil.getOptString(res, "updateList", null);
            if (Strings.isEmpty(_update_list)) {
                this.updateList = null;
                this.count4Update = 0;
            } else {
                this.updateList = Stream.of(StringUtils.split(_update_list, ',')).map(Integer::valueOf).collect(Collectors.toSet());
                this.count4Update = this.updateList.size();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore Touch90CareLogEntity has SQLException", e);
        }
    }

    void merge(Touch90CareLogEntity other) {
        if (CollectionUtils.isNotEmpty(other.addList)) {
            if (CollectionUtils.isEmpty(this.addList)) {
                this.addList = Sets.newHashSet(other.addList);
            } else {
                this.addList.addAll(other.addList);
            }
            this.count4Add = this.addList.size();
        }
        if (CollectionUtils.isNotEmpty(other.updateList)) {
            if (CollectionUtils.isEmpty(this.updateList)) {
                this.updateList = Sets.newHashSet(other.updateList);
            } else {
                this.updateList.addAll(other.updateList);
            }
            this.count4Update = this.updateList.size();
        }
    }

    boolean equalsInstance(Touch90CareLogEntity other) {
        return this.companyId.equals(other.companyId) && this.storeId.equals(other.storeId) &&
                this.logDate.equals(other.logDate);
    }

    public Integer getStoreId() {
        return storeId;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    LocalDate getLogDate() {
        return logDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Touch90CareLogEntity)) return false;
        Touch90CareLogEntity that = (Touch90CareLogEntity) o;
        return Objects.equals(storeId, that.storeId) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(companyId, that.companyId) &&
                Objects.equals(logDatePk, that.logDatePk) &&
                SetUtils.isEqualSet(addList, that.addList) &&
                SetUtils.isEqualSet(updateList, that.updateList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, categories, companyId, logDatePk, addList, updateList);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("companyId", companyId)
                .add("storeId", storeId)
                .add("categories", categories)
                .add("logDatePk", logDatePk)
                .add("count4Add", count4Add)
                .add("addList", addList)
                .add("count4Update", count4Update)
                .add("updateList", updateList)
                .toString();
    }
}
