package com.legooframework.model.statistical.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatisticalEntity extends BaseEntity<String> {

    private static final Logger logger = LoggerFactory.getLogger(StatisticalEntity.class);
    private final List<FieldMetaEntity> fieldMetas;
    private final List<TableMetaEntity> tableMetas;
    private final List<SummaryMetaEntity> summaryMetas;
    private final SummaryMetaEntity subSummaryMeta;
    private final List<EchartMetaEntity> echartMetas;
    private final String title;

    public StatisticalEntity(String id, String title, List<FieldMetaEntity> fieldMetas, List<TableMetaEntity> tableMetas,
                             List<SummaryMetaEntity> summaryMetas, SummaryMetaEntity subSummaryMeta,
                             List<EchartMetaEntity> echartMetas) {
        super(id);
        this.title = title;
        this.fieldMetas = fieldMetas;
        this.tableMetas = tableMetas;
        this.subSummaryMeta = subSummaryMeta;
        this.summaryMetas = summaryMetas;
        this.echartMetas = CollectionUtils.isEmpty(echartMetas) ? null : ImmutableList.copyOf(echartMetas);
    }

    String getSubSummarySql() {
        Preconditions.checkNotNull(subSummaryMeta);
        return subSummaryMeta.getSql();
    }

//    String getTableSql(String tableId) {
//        Optional<TableMetaEntity> table = getTableIfExits(tableId);
//        Preconditions.checkState(table.isPresent(), "Id=%d 对应的表格定义不存在...", tableId);
//        return table.get().getSql();
//    }

    public Map<String, Object> toSubPage(StatisticalLayoutEntity layout) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("title", title);
        params.put("rid", this.getId());
        params.put("id", String.format("%s.subpage", this.getId()));
        if (subSummaryMeta != null && layout.getSubsummaryId().isPresent()) {
            Map<String, Object> _temp = subSummaryMeta.toViewMap(this);
            _temp.put("linkUrl", String.format("/statistical/api/query/details/%s/data.json?pt=%s&stm=%s&rid=%s",
                    layout.getCompanyId(), layout.getType(), subSummaryMeta.getSql(), this.getId()));
            _temp.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                    layout.getCompanyId(), layout.getType(), this.getId()));
            params.put("summary", _temp);
        }
        List<Map<String, Object>> list = Lists.newArrayListWithCapacity(2);
        if (layout.getTableId().isPresent()) {
            Optional<TableMetaEntity> table = getTableIfExits(layout.getTableId().get());
            Preconditions.checkState(table.isPresent());
            list.add(table.get().toViewMap(this));
            Optional<String> _opt = table.get().getLinkUrl();
            if (_opt.isPresent()) {
                String url = _opt.get();
                list.forEach(map -> map.put("linkUrl", String.format("/statistical/api/query/pages/%s/data.json?pt=%s&stm=%s&rid=%s",
                        layout.getCompanyId(), layout.getType(), table.get().getSql(), this.getId())));
                list.forEach(map -> map.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                        layout.getCompanyId(), layout.getType(), this.getId())));
            } else {
                list.forEach(map -> map.put("linkUrl", String.format("/statistical/api/query/details/%s/data.json?pt=%s&stm=%s&rid=%s",
                        layout.getCompanyId(), layout.getType(), table.get().getSql(), this.getId())));
                list.forEach(map -> map.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                        layout.getCompanyId(), layout.getType(), this.getId())));
            }
        }
        if (CollectionUtils.isNotEmpty(echartMetas)) {
            list.add(echartMetas.get(0).toViewMap(this));
        }
        params.put("echarts", list);
        return params;
    }

    private EchartMetaEntity loadEchartMetaById(String echartId) {
        Preconditions.checkState(CollectionUtils.isNotEmpty(echartMetas), "图片未定义仪表盘信息....");
        Optional<EchartMetaEntity> exits = this.echartMetas.stream().filter(x -> StringUtils.equals(x.getId(), echartId)).findFirst();
        Preconditions.checkState(exits.isPresent(), "Id=%s 对应的仪表盘信息未定义...", echartId);
        return exits.get();
    }

    FieldMetaEntity loadFieldById(String id) {
        Optional<FieldMetaEntity> field = this.fieldMetas.stream().filter(x -> StringUtils.equals(x.getId(), id)).findFirst();
        Preconditions.checkState(field.isPresent());
        return field.get();
    }

    Optional<TableMetaEntity> getTableIfExits(String tableId) {
        if (CollectionUtils.isEmpty(tableMetas)) return Optional.empty();
        return tableMetas.stream().filter(x -> StringUtils.equals(x.getId(), tableId)).findFirst();
    }


    Optional<Map<String, Object>> getTableMapIfExits(String tableId) {
        if (CollectionUtils.isEmpty(tableMetas)) return Optional.empty();
        Optional<TableMetaEntity> exits = tableMetas.stream().filter(x -> StringUtils.equals(x.getId(), tableId)).findFirst();
        return exits.map(x -> x.toViewMap(this));
    }

    Optional<SummaryMetaEntity> getSummaryMetaIfExits(String summeryId) {
        if (CollectionUtils.isEmpty(summaryMetas)) return Optional.empty();
        return summaryMetas.stream().filter(x -> StringUtils.equals(x.getId(), summeryId)).findFirst();
    }

    Optional<SummaryMetaEntity> getSubSummaryMeta() {
        return Optional.ofNullable(subSummaryMeta);
    }

    Optional<EchartMetaEntity> findEchartMetaById(String echartId) {
        if (CollectionUtils.isEmpty(this.echartMetas)) return Optional.empty();
        Optional<EchartMetaEntity> entity = this.echartMetas.stream().filter(x -> StringUtils.equals(x.getId(), echartId)).findFirst();
        if (logger.isDebugEnabled())
            logger.debug(String.format("getEchartMeta(%s) return %s", echartId, entity.orElse(null)));
        return entity;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("fieldMetas's size", fieldMetas.size())
                .add("has TableMetas ", CollectionUtils.isNotEmpty(tableMetas))
                .add("has summaryMeta", CollectionUtils.isNotEmpty(summaryMetas))
                .add("has subSummaryMeta", subSummaryMeta != null)
                .add("echartMetas's size ", echartMetas.size())
                .toString();
    }
}
