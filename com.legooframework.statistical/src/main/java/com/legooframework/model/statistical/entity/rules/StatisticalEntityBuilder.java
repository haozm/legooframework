package com.legooframework.model.statistical.entity.rules;

import com.google.common.collect.Lists;
import com.legooframework.model.statistical.entity.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

public class StatisticalEntityBuilder {

    private final String id, title;
    private List<FieldMetaEntity> fieldMetas = Lists.newArrayList();
    private List<TableMetaEntity> tableMetas = Lists.newArrayList();
    private List<SummaryMetaEntity> summaryMetas = Lists.newArrayList();
    private SummaryMetaEntity subSummaryMeta;
    private List<EchartMetaEntity> echartMetas = Lists.newArrayList();

    StatisticalEntityBuilder(String id, String title) {
        this.id = id;
        this.title = title;
    }

    void setSummaryMeta(SummaryMetaEntity summaryMeta) {
        summaryMetas.add(summaryMeta);
    }

    void addFieldMeta(FieldMetaEntity fieldMeta) {
        this.fieldMetas.add(fieldMeta);
    }

    void setTableMeta(TableMetaEntity tableMeta) {
        this.tableMetas.add(tableMeta);
    }

    void addEchartMeta(EchartMetaEntity echartMeta) {
        this.echartMetas.add(echartMeta);
    }

    void setSubSummaryMeta(SummaryMetaEntity subSummaryMeta) {
        this.subSummaryMeta = subSummaryMeta;
    }

    public StatisticalEntity building() {
        return new StatisticalEntity(id, title, fieldMetas,
                CollectionUtils.isEmpty(tableMetas) ? null : tableMetas,
                CollectionUtils.isEmpty(summaryMetas) ? null : summaryMetas,
                subSummaryMeta, echartMetas);
    }

}
