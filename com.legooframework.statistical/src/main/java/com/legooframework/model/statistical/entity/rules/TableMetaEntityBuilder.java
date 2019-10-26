package com.legooframework.model.statistical.entity.rules;

import com.google.common.base.Preconditions;
import com.legooframework.model.statistical.entity.SummaryMetaEntity;
import com.legooframework.model.statistical.entity.TableMetaEntity;

class TableMetaEntityBuilder extends FieldMetaRefSupport {

    final static int SUMMARY_TYPE = 0;
    final static int TABLE_TYPE = 1;
    private String id, sql, subpage, linkUrl, title;
    private int type;

    TableMetaEntityBuilder(String id, String title, String sql, String linkUrl, int type, String subpage) {
        this.title = title;
        this.linkUrl = linkUrl;
        this.id = id;
        this.sql = sql;
        this.type = type;
        this.subpage = subpage;
    }

    TableMetaEntity buildTable() {
        Preconditions.checkState(this.type == TABLE_TYPE);
        return new TableMetaEntity(id, title, sql, linkUrl, fieldMetaRefs);
    }

    SummaryMetaEntity buildSummary() {
        Preconditions.checkState(this.type == SUMMARY_TYPE);
        return new SummaryMetaEntity(id, title, sql, subpage, fieldMetaRefs);
    }

}
