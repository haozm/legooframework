package com.legooframework.model.statistical.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.covariant.entity.UserAuthorEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StatisticalLayoutEntity extends BaseEntity<Integer> {

    private Integer companyId, roleId;
    private String type, ranges, statisticalId, title;
    private List<String> summaryIds;
    private List<String> echartIds;
    private String subsummaryId, tableId;
    private List<String> list;

    Integer getCompanyId() {
        return companyId;
    }

    StatisticalLayoutEntity(Integer id, ResultSet res) {
        super(id);
        try {
            this.companyId = ResultSetUtil.getObject(res, "company_id", Long.class).intValue();
            this.type = ResultSetUtil.getString(res, "layout_type");
            this.roleId = res.getInt("role_id");
            this.statisticalId = res.getString("statistical_id");
            this.title = res.getString("title");
            this.echartIds = Lists.newArrayList();
            List<String> list = Lists.newArrayList();
            for (int i = 1; i <= 12; i++) {
                String _cloumn = String.format("region_%s", StringUtils.leftPad(String.valueOf(i), 2, '0'));
                Optional<String> _value_opt = ResultSetUtil.getOptObject(res, _cloumn, String.class);
                if (_value_opt.isPresent() && StringUtils.isNotEmpty(_value_opt.get())) {
                    if (StringUtils.startsWith(_value_opt.get(), "DATERANGE$")) {
                        this.ranges = StringUtils.substringAfter(_value_opt.get(), "DATERANGE$");
                        list.add(_value_opt.get());
                    } else if (StringUtils.startsWith(_value_opt.get(), "SUMMARY$")) {
                        this.summaryIds = Splitter.on(',').splitToList(StringUtils.substringAfter(_value_opt.get(), "SUMMARY$"));
                        list.add(_value_opt.get());
                    } else if (StringUtils.startsWith(_value_opt.get(), "SUBSUMMARY$")) {
                        this.subsummaryId = StringUtils.substringAfter(_value_opt.get(), "SUBSUMMARY$");
                        list.add(_value_opt.get());
                    } else if (StringUtils.startsWith(_value_opt.get(), "TABLE$")) {
                        this.tableId = StringUtils.substringAfter(_value_opt.get(), "TABLE$");
                        list.add(_value_opt.get());
                    } else if (StringUtils.startsWith(_value_opt.get(), "ECHART$")) {
                        list.add(_value_opt.get());
                        this.echartIds.add(_value_opt.get());
                    }
                }
            }
            this.list = ImmutableList.copyOf(list);
        } catch (SQLException e) {
            throw new RuntimeException("Restore StatisticalLayoutEntity has SQLException", e);
        }
    }

    boolean isCompanyRange() {
        return this.roleId == -1;
    }

    boolean matchByUser(UserAuthorEntity user) {
        return user.getMaxRoleId() == this.roleId;
    }

    public Map<String, Object> buildLayout(UserAuthorEntity user, StatisticalDefinedFactory layoutFactory) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("type", type);
        params.put("title", this.title);
        List<Map<String, Object>> echarts_map = Lists.newArrayList();
        params.put("layout", Joiner.on(';').join(this.list));
        for (String $it : list) {
            if (StringUtils.startsWith($it, "DATERANGE$")) {
                getRangeIfExits().ifPresent(x -> params.put("ranges", x));
            } else if (StringUtils.startsWith($it, "SUMMARY$")) {
                List<Map<String, Object>> summaries = layoutFactory.getSummaryMetas(summaryIds);
                if (CollectionUtils.isNotEmpty(summaries)) {
                    summaries.forEach(map -> map.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                            user.getCompanyId(), type, MapUtils.getString(map, "rid"))));
                    params.put("summary", summaries);
                }
                String _temp = Joiner.on(',').join(summaryIds);
                params.put("linkUrl", String.format("/statistical/api/query/summary/%s/data.json?pt=%s&stm=%s&rid=%s",
                        user.getCompanyId(), type, _temp, type));
            } else if (StringUtils.startsWith($it, "SUBSUMMARY$")) {
                Optional<Map<String, Object>> exits = layoutFactory.getSubSummaryMeta(StringUtils.substringAfter($it, "SUBSUMMARY$"));
                exits.ifPresent(x -> {
                    x.put("linkUrl", String.format("/statistical/api/query/details/%s/data.json?pt=%s&stm=%s&rid=%s",
                            companyId, type, MapUtils.getString(x, "sql"), this.getId()));
                    x.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                            companyId, type, this.getId()));
                    params.put("subsummary", x);
                });
            } else if (StringUtils.startsWith($it, "TABLE$")) {
                Optional<Map<String, Object>> table = layoutFactory.getTableById(StringUtils.substringAfter($it, "TABLE$"));
                table.ifPresent(echarts_map::add);
            } else if (StringUtils.startsWith($it, "ECHART$")) {
                String echatId = StringUtils.substringAfter($it, "ECHART$");
                Optional<Map<String, Object>> echart = layoutFactory.getEchartById(echatId);
                echart.ifPresent(echarts_map::add);
            }
        }
        if (CollectionUtils.isNotEmpty(echarts_map)) {
            echarts_map.forEach(
                    map -> map.put("pageUrl", String.format("/statistical/api/layout/load/%s/subpage.json?pt=%s&rid=%s",
                            user.getCompanyId(), type, MapUtils.getString(map, "rid"))));
            echarts_map.forEach(
                    map -> map.put("linkUrl", String.format("/statistical/api/query/details/%s/data.json?pt=%s&stm=%s&rid=%s",
                            user.getCompanyId(), type, MapUtils.getString(map, "sql"), MapUtils.getString(map, "rid"))));
            params.put("echarts", echarts_map);
        }
        return params;
    }

    String getType() {
        return type;
    }

    Optional<String> getSubsummaryId() {
        return Optional.ofNullable(subsummaryId);
    }

    Optional<String> getTableId() {
        return Optional.ofNullable(tableId);
    }

    public Optional<List<String>> getSummaryIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(summaryIds) ? null : summaryIds);
    }

    public Optional<List<String>> getEchartIds() {
        return Optional.ofNullable(CollectionUtils.isEmpty(echartIds) ? null : echartIds);
    }

    private Optional<List<Map<String, Object>>> getRangeIfExits() {
        if (Strings.isNullOrEmpty(ranges)) return Optional.empty();
        List<Map<String, Object>> maplist = Lists.newArrayList();
        LocalDate today = LocalDate.now();
        for (String $it : StringUtils.split(ranges, ',')) {
            Map<String, Object> map = Maps.newHashMap();
            if (StringUtils.startsWith($it, "TODAY")) {
                map.put("id", "TODAY");
                map.put("title", "今天");
                map.put("minDay", today.toString("yyyy-MM-dd"));
                map.put("maxDay", today.toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "WEEK")) {
                map.put("id", "WEEK");
                map.put("title", "本周");
                map.put("minDay", today.dayOfWeek().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfWeek().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "BEFWEEK")) {
                map.put("id", "BEFWEEK");
                map.put("title", "上周");
                today = today.plusWeeks(-1);
                map.put("minDay", today.dayOfWeek().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfWeek().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "MONTH")) {
                map.put("id", "MONTH");
                map.put("title", "本月");
                map.put("minDay", today.dayOfMonth().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfMonth().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "BEFMONTH")) {
                map.put("id", "BEFMONTH");
                map.put("title", "上月");
                today = today.plusMonths(-1);
                map.put("minDay", today.dayOfMonth().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfMonth().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "LAST3MONTH")) {
                map.put("id", "LAST3MONTH");
                map.put("title", "近三个月");
                map.put("minDay", today.plusMonths(-2).dayOfMonth().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfMonth().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "JIDU")) {
                int month = today.getMonthOfYear();
                map.put("id", "JIDU");
                map.put("title", "本季度");
                LocalDate min = null, max = null;
                if (month <= 3) {
                    min = new LocalDate(today.getYear(), 1, 1);
                    max = new LocalDate(today.getYear(), 3, 30);
                } else if (month <= 6) {
                    min = new LocalDate(today.getYear(), 4, 1);
                    max = new LocalDate(today.getYear(), 6, 30);
                } else if (month <= 9) {
                    min = new LocalDate(today.getYear(), 7, 1);
                    max = new LocalDate(today.getYear(), 9, 30);
                } else {
                    min = new LocalDate(today.getYear(), 10, 1);
                    max = new LocalDate(today.getYear(), 12, 31);
                }
                map.put("minDay", min.toString("yyyy-MM-dd"));
                map.put("maxDay", max.toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "BEFJIDU")) {
                map.put("id", "BEFJIDU");
                map.put("title", "上季度");
                int month = today.getMonthOfYear();
                LocalDate min = null, max = null;
                if (month <= 3) {
                    min = new LocalDate(today.getYear() - 1, 10, 1);
                    max = new LocalDate(today.getYear() - 1, 12, 31);
                } else if (month <= 6) {
                    min = new LocalDate(today.getYear(), 1, 1);
                    max = new LocalDate(today.getYear(), 3, 30);
                } else if (month <= 9) {
                    min = new LocalDate(today.getYear(), 4, 1);
                    max = new LocalDate(today.getYear(), 6, 30);
                } else {
                    min = new LocalDate(today.getYear(), 7, 1);
                    max = new LocalDate(today.getYear(), 9, 30);
                }
                map.put("minDay", min.toString("yyyy-MM-dd"));
                map.put("maxDay", max.toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "HALFYEAR")) {
                map.put("id", "HALFYEAR");
                map.put("title", "近半年");
                int month = today.getMonthOfYear();
                LocalDate min = null, max = null;
                if (month <= 6) {
                    min = new LocalDate(today.getYear(), 1, 1);
                    max = new LocalDate(today.getYear(), 6, 30);
                } else {
                    min = new LocalDate(today.getYear(), 7, 1);
                    max = new LocalDate(today.getYear(), 12, 31);
                }
                map.put("minDay", min.toString("yyyy-MM-dd"));
                map.put("maxDay", max.toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "YEAR")) {
                map.put("id", "YEAR");
                map.put("title", "本年");
                map.put("minDay", today.dayOfYear().withMinimumValue().toString("yyyy-MM-dd"));
                map.put("maxDay", today.dayOfYear().withMaximumValue().toString("yyyy-MM-dd"));
            } else if (StringUtils.startsWith($it, "CUSTOMIZE")) {
                map.put("id", "CUSTOMIZE");
                map.put("title", "自定义");
                LocalDate now = LocalDate.now();
                LocalDate before = now.plusMonths(-3);
                map.put("minDay", before.toString("yyyy-MM-dd"));
                map.put("maxDay", now.toString("yyyy-MM-dd"));
            }
            if (StringUtils.endsWith($it, "default")) {
                map.put("default", true);
            }
            maplist.add(map);
        }
        return Optional.of(maplist);
    }

    //    TODAY:今天
//    WEEK：本周
//    BEFWEEK：上周
//    MONTH：本月
//    BEFMONTH：本月：上月
//    LAST3MONTH:近三个月
//    JIDU：本季度
//    BEFJIDU:上季度
//    HALFREAR：近半年
//    YEAR：本年
//    CUSTOMIZE：自定义
    @Override
    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", companyId);
        params.put("type", type);
        getRangeIfExits().ifPresent(x -> params.put("ranges", x));
        return params;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("companyId", companyId)
                .add("type", type)
                .add("ranges", ranges)
                .add("subsummaryId", subsummaryId)
                .add("tableId", tableId)
                .add("summaryIds", summaryIds)
                .add("echartIds", echartIds)
                .add("seq", list)
                .toString();
    }
}
