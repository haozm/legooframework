package com.legooframework.model.membercare.entity;

import com.google.common.base.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmMemberEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.salesrecords.entity.SaleRecordEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDateTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Touch90CareRuleEntity extends AbstractCareRuleRule {

    private static final Splitter.MapSplitter DETAIL_SPLITTER = Splitter.on(',').trimResults().withKeyValueSeparator('=');
    private static final Splitter STAGE_SPLITTER = Splitter.on('$').trimResults();
    private static final Joiner STAGE_JOINER = Joiner.on('$');
    private static final Ordering<Touch90Detail> ordering = Ordering.natural()
            .onResultOf((Function<Touch90Detail, Long>) x -> x == null ? 0 : x.delay.toHours());
    private static Comparator<UpcomingTaskEntity> TOUCH90_ORDERING =
            Comparator.comparingLong(o -> o.getSaleDate().toDateTime().getMillis());
    private boolean enabled;
    private boolean concalBefore;
    private Range<Integer> maxConsumptionDays;
    private Range<Integer> maxAmountOfconsumption;
    private List<Touch90Detail> ruleDetails;

    public Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("maxConsumptionDays", maxConsumptionDays.upperEndpoint());
        params.put("maxAmountOfconsumption", maxAmountOfconsumption.upperEndpoint());
        params.put("concalBefore", concalBefore);
        params.put("enabled", enabled);
        params.put("automatic", isAutomatic());
        List<Map<String, Object>> list = Lists.newArrayList();
        ruleDetails.forEach(rule -> list.add(rule.toViewMap()));
        params.put("ruleDetail", list);
        return params;
    }

    Touch90CareRuleEntity buildStore(CrmStoreEntity store) {
        Touch90CareRuleEntity clone = (Touch90CareRuleEntity) super.cloneMe();
        clone.setStoreId(store.getId());
        return clone;
    }

    // 是否允许合并
    private boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
        super.setValues(ps);
        String content = format("maxConsumptionDays=%s,maxAmountOfconsumption=%s,concalBefore=%s",
                maxConsumptionDays.upperEndpoint(),
                maxAmountOfconsumption.upperEndpoint(), concalBefore);
        ps.setObject(7, enabled ? 1 : 0);
        ps.setObject(8, content);
        ps.setObject(9, STAGE_JOINER.join(this.ruleDetails));
    }

    Touch90CareRuleEntity(CrmOrganizationEntity company, boolean automatic, boolean enabled,
                          int maxConsumptionDays, int maxAmountOfconsumption, boolean concalBefore, String details) {
        super(company.getId().longValue(), -1L, TaskType.Touche90, automatic, -1, company.getId());
        this.enabled = enabled;
        this.concalBefore = concalBefore;
        this.maxConsumptionDays = Range.closed(0, maxConsumptionDays);
        this.maxAmountOfconsumption = Range.closed(0, maxAmountOfconsumption);
        setEditTime(DateTime.now());
        List<Touch90Detail> ruleDetails = parseDetail(details);
        this.ruleDetails = ordering.sortedCopy(ruleDetails);
        LocalDateTime current = LocalDateTime.now();
        for (Touch90Detail item : this.ruleDetails) {
            LocalDateTime next = current.plusHours((int) (item.delay.toHours() + item.expired.toHours()));
            Preconditions.checkState(current.isBefore(next), "Touch90 设置节点 %s 存在日期重叠...", item);
            current = next;
        }
    }

    // for DB
    Touch90CareRuleEntity(ResultSet res, boolean enabled, boolean automatic, Integer storeId, Integer companyId) {
        super(res, TaskType.Touche90, automatic, storeId, companyId);
        try {
            this.enabled = enabled;
            String content_str = ResultSetUtil.getString(res, "content");
            Map<String, String> maps = DETAIL_SPLITTER.split(content_str);
            this.maxConsumptionDays = Range.closed(0, MapUtils.getInteger(maps, "maxConsumptionDays"));
            this.maxAmountOfconsumption = Range.closed(0, MapUtils.getInteger(maps, "maxAmountOfconsumption"));
            this.concalBefore = MapUtils.getBoolean(maps, "concalBefore", false);
            String details_str = ResultSetUtil.getString(res, "details");
            List<Touch90Detail> ruleDetails = parseDetail(details_str);
            this.ruleDetails = ordering.sortedCopy(ruleDetails);
        } catch (SQLException e) {
            throw new RuntimeException("Restore Touch90CareRuleEntity has SQLException", e);
        }
    }

    public Optional<Touch90TaskDto> createTasks(final CrmStoreEntity store, final CrmMemberEntity member,
                                                List<UpcomingTaskEntity> touch90Tasks,
                                                List<SaleRecordEntity> sale_records) {
        sale_records.forEach(x -> Preconditions.checkState(x.getMemberId().equals(member.getId())));
        if (CollectionUtils.isNotEmpty(touch90Tasks)) {
            touch90Tasks.forEach(x -> Preconditions.checkState(x.isOwner(store, member)));
            touch90Tasks.sort(TOUCH90_ORDERING);
        }

        List<SaleRecordEntity> saleRecords = Lists.newArrayList(sale_records);
        Touch90TaskDto touch90TaskDto = new Touch90TaskDto(store.getCompanyId());

        // 优先处理 冲单的记录 自动合并 并取消掉
        if (saleRecords.size() > 1) {
            List<SaleRecordEntity> negative_sale = saleRecords.stream().filter(SaleRecordEntity::isNegativeOrZore)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(negative_sale)) {
                saleRecords.removeAll(negative_sale);
                List<Long> saleTotalAmounts = negative_sale.stream().map(x -> Math.abs(x.getSaleTotalAmount().longValue()))
                        .collect(Collectors.toList());
                negative_sale.clear();
                saleRecords.forEach(x -> {
                    if (saleTotalAmounts.contains(x.getSaleTotalAmount().longValue())) {
                        negative_sale.add(x);
                    }
                });
                if (CollectionUtils.isNotEmpty(negative_sale))
                    saleRecords.removeAll(negative_sale);
            }
        }

        if (CollectionUtils.isEmpty(saleRecords)) return Optional.empty();

        // 是否开启合并项
        if (!isEnabled()) {
            saleRecords.forEach(x -> {
                UpcomingTaskEntity _current = UpcomingTaskEntity.createTouche90Job(isAutomatic(), member, store, x);
                _current.setTaskDetails(new UpcomingTaskDetailList(createDetail(_current, x)));
                touch90TaskDto.setInserts(_current);
            });
            return Optional.of(touch90TaskDto);
        }

        UpcomingTaskEntity _current = null;
        List<SaleRecordEntity> _saleRecords = Lists.newArrayList(saleRecords);
        if (CollectionUtils.isNotEmpty(touch90Tasks)) {
            _current = touch90Tasks.get(touch90Tasks.size() - 1);
        } else {
            SaleRecordEntity _temp = _saleRecords.remove(0);
            _current = UpcomingTaskEntity.createTouche90Job(isAutomatic(), member, store, _temp);
            _current.setTaskDetails(new UpcomingTaskDetailList(createDetail(_current, _temp)));
            touch90TaskDto.setInserts(_current);
        }

        if (CollectionUtils.isEmpty(_saleRecords)) return Optional.of(touch90TaskDto);

        UpcomingTaskEntity _next;
        for (SaleRecordEntity $it : _saleRecords) {
            _next = UpcomingTaskEntity.createTouche90Job(isAutomatic(), member, store, $it);
            _next.setTaskDetails(new UpcomingTaskDetailList(createDetail(_next, $it)));
            int _days = Days.daysBetween(_current.getSaleDate(), _next.getSaleDate()).getDays();
            if (!maxConsumptionDays.contains(_days)) {
                _current = _next;
                touch90TaskDto.setInserts(_current);
                continue;
            }
            int saleTotalAmount = $it.getSaleTotalAmount().intValue();
            if (!maxAmountOfconsumption.contains(saleTotalAmount)) {
                if (concalBefore) {
                    _current.canceled().ifPresent(x -> touch90TaskDto.setUpdateDetail(x.getTaskDetails()));
                }
                _current = _next;
                touch90TaskDto.setInserts(_current);
                continue;
            }
            _current.addMergeInfo($it.getId()).ifPresent(touch90TaskDto::setUpdates);
        }
        return Optional.of(touch90TaskDto);
    }

    private List<UpcomingTaskDetailEntity> createDetail(final UpcomingTaskEntity task, final SaleRecordEntity saleRecord) {
        final List<UpcomingTaskDetailEntity> list = Lists.newArrayList();
        this.ruleDetails.forEach(rule -> {
            UpcomingTaskDetailEntity _temp = rule.createDetail(task, saleRecord);
            if (_temp != null) list.add(_temp);
        });
        return list;
    }

    @Override
    public Map<String, Object> toParamMap(String... excludes) {
        Map<String, Object> res = super.toParamMap("maxConsumptionDays", "maxAmountOfconsumption", "concalBefore", "ruleDetails");
        String content = format("maxConsumptionDays=%s,maxAmountOfconsumption=%s,concalBefore=%s", maxConsumptionDays.upperEndpoint(),
                maxAmountOfconsumption.upperEndpoint(), concalBefore);
        res.put("content", content);
        res.put("details", STAGE_JOINER.join(this.ruleDetails));
        return res;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Touch90CareRuleEntity)) return false;
        if (!super.equals(o)) return false;
        Touch90CareRuleEntity that = (Touch90CareRuleEntity) o;
        return Objects.equals(maxConsumptionDays, that.maxConsumptionDays) &&
                Objects.equals(maxAmountOfconsumption, that.maxAmountOfconsumption) &&
                ListUtils.isEqualList(ruleDetails, that.ruleDetails);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), maxConsumptionDays, maxAmountOfconsumption, ruleDetails);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("super", super.toString())
                .add("maxConsumptionDays", maxConsumptionDays)
                .add("maxAmountOfconsumption", maxAmountOfconsumption)
                .add("concalBefore", concalBefore)
                .add("details", ruleDetails)
                .toString();
    }

    private List<Touch90Detail> parseDetail(String details) {
        final List<Touch90Detail> ruleDetails = Lists.newArrayList();
        Iterable<String> stage = STAGE_SPLITTER.split(details);
        stage.forEach(stg -> {
            Map<String, String> params = DETAIL_SPLITTER.split(stg);
            Long min = MapUtils.getLong(params, "min");
            Long max = MapUtils.getLong(params, "max", Long.MAX_VALUE);
            if (min == null) {
                ruleDetails.add(new Touch90Detail(MapUtils.getString(params, "delay"),
                        MapUtils.getString(params, "expired")));
            } else {
                ruleDetails.add(new Touch90Detail(MapUtils.getString(params, "delay"),
                        MapUtils.getString(params, "expired"),
                        min, max));
            }
        });
//        Collections.sort();
        return ordering.sortedCopy(ruleDetails);
    }

    class Touch90Detail {
        private Duration delay;
        private Duration expired;
        private Range<Long> consumption;

        Map<String, Object> toViewMap() {
            Map<String, Object> params = Maps.newHashMap();
            params.put("delay", delay.toString());
            params.put("expired", expired.toString());
            if (consumption != null) {
                params.put("min", consumption.lowerEndpoint());
                params.put("max", consumption.upperEndpoint());
            }
            return params;
        }

        Touch90Detail(String delayVal, String expiredVal, Long start, Long end) {
            this.delay = new Duration("delay", delayVal);
            this.expired = new Duration("expired", expiredVal);
            this.consumption = Range.openClosed(start, end);
        }

        Touch90Detail(String delayVal, String expiredVal) {
            this.delay = new Duration("delay", delayVal);
            this.expired = new Duration("expired", expiredVal);
        }

        UpcomingTaskDetailEntity createDetail(UpcomingTaskEntity task, SaleRecordEntity saleRecord) {
            UpcomingTaskDetailEntity _task_detail = null;

            LocalDateTime _start = null;
            LocalDateTime sale_date = saleRecord.getSaleDate();
            if (delay.timeUnit.equals(TimeUnit.HOURS)) {
                _start = saleRecord.getSaleDate().plusHours((int) delay.duration);
            } else if (delay.timeUnit.equals(TimeUnit.DAYS)) {
                _start = sale_date.plusDays((int) delay.duration);
                _start = new LocalDateTime(_start.getYear(), _start.getMonthOfYear(), _start.getDayOfMonth(), 0, 0, 0);
            } else {
                throw new IllegalArgumentException(format("尚未支持该参数解析 %s", delay.timeUnit));
            }

            LocalDateTime _expired = null;
            if (this.expired.duration == 0L) {
                _expired = new LocalDateTime(_start.getYear(), _start.getMonthOfYear(), _start.getDayOfMonth(), 23, 59, 59);
            } else if (expired.timeUnit.equals(TimeUnit.DAYS)) {
                _expired = _start.plusDays((int) this.expired.duration);
                _expired = new LocalDateTime(_expired.getYear(), _expired.getMonthOfYear(), _expired.getDayOfMonth(),
                        23, 59, 59);
            } else {
                throw new IllegalArgumentException(format("尚未支持该参数解析 %s", _expired));
            }

            if (consumption == null) {
                _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired, delay.toString());
            } else {
                long saleTotalAmount = saleRecord.getSaleTotalAmount().longValue();
                if (saleTotalAmount < consumption.lowerEndpoint() || consumption.contains(saleTotalAmount)) {
                    _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired, delay.toString());
                }
            }
            return _task_detail;
        }

        @Override
        public String toString() {
            return format("delay=%s,expired=%s", delay, expired);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Touch90Detail)) return false;
            Touch90Detail that = (Touch90Detail) o;
            return Objects.equals(delay, that.delay) &&
                    Objects.equals(expired, that.expired) &&
                    Objects.equals(consumption, that.consumption);
        }

        @Override
        public int hashCode() {
            return Objects.hash(delay, expired, consumption);
        }
    }

    class Duration {

        private final long duration;
        private final TimeUnit timeUnit;

        Duration(String key, String value) {
            try {
                char lastChar = value.charAt(value.length() - 1);
                TimeUnit timeUnit;
                switch (lastChar) {
                    case 'd':
                        timeUnit = TimeUnit.DAYS;
                        break;
                    case 'h':
                        timeUnit = TimeUnit.HOURS;
                        break;
                    case 'm':
                        timeUnit = TimeUnit.MINUTES;
                        break;
                    case 's':
                        timeUnit = TimeUnit.SECONDS;
                        break;
                    default:
                        throw new IllegalArgumentException(
                                format("key %s invalid format.  was %s, must end with one of [dDhHmMsS]", key, value));
                }
                this.timeUnit = timeUnit;
                this.duration = Long.parseLong(value.substring(0, value.length() - 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        format("key %s value set to %s, must be integer", key, value));
            }
        }

        long getDuration() {
            return duration;
        }

        private long toHours() {
            return TimeUnit.HOURS.convert(duration, timeUnit);
        }

        @Override
        public String toString() {
            char res;
            switch (timeUnit) {
                case DAYS:
                    res = 'd';
                    break;
                case HOURS:
                    res = 'h';
                    break;
                case MINUTES:
                    res = 'm';
                    break;
                case SECONDS:
                    res = 's';
                    break;
                default:
                    throw new IllegalArgumentException(format("非法的参数....%s ", timeUnit));
            }
            return format("%s%s", duration, res);
        }
    }

}
