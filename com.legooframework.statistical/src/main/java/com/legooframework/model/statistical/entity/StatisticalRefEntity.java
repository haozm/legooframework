package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class StatisticalRefEntity extends BaseEntity<Integer> {

    private Integer companyId, roleId;
    private String statisticalId;
    private List<String> tableFieldIds;
    private String echartId;

    public StatisticalRefEntity(Integer id) {
        super(id);
    }

    StatisticalRefEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.companyId = ResultSetUtil.getObject(res, "company_id", Long.class).intValue();
            this.roleId = ResultSetUtil.getObject(res, "role_id", Long.class).intValue();
            this.statisticalId = ResultSetUtil.getString(res, "statistical_id");
            this.echartId = ResultSetUtil.getString(res, "echart_id");
            String _tableFieldIds = ResultSetUtil.getOptString(res, "table_field_ids", null);
            if (!Strings.isNullOrEmpty(_tableFieldIds)) {
                this.tableFieldIds = Lists.newArrayList();
                this.tableFieldIds.addAll(Arrays.asList(StringUtils.split(_tableFieldIds, ',')));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Restore StatisticalRefEntity has SQLException", e);
        }
    }

    boolean isCompanyRange() {
        return roleId == 0;
    }

    public boolean isRoleRange() {
        return roleId != 0;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("roleId", roleId)
                .add("statisticalId", statisticalId)
                .add("tableFieldIds", tableFieldIds)
                .add("echartId", echartId)
                .toString();
    }
}
