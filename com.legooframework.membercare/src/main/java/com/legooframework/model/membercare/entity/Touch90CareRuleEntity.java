package com.legooframework.model.membercare.entity;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.common.primitives.Longs;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Touch90CareRuleEntity extends AbstractCareRuleRule {

    private static final Splitter.MapSplitter DETAIL_SPLITTER = Splitter.on(',').trimResults().withKeyValueSeparator('=');
    private static final Splitter STAGE_SPLITTER = Splitter.on('$').trimResults();
    private static final Joiner STAGE_JOINER = Joiner.on('$');
    private static final Ordering<Touch90Detail> ordering = Ordering.natural();
    private Range<Integer> maxConsumptionDays;
    private Range<Integer> maxAmountOfconsumption;
    private List<Touch90Detail> ruleDetails;

    Touch90CareRuleEntity(CrmOrganizationEntity company, CrmStoreEntity store, boolean automatic, boolean enabled,
                          int maxConsumptionDays, int maxAmountOfconsumption, String details) {
        super(company.getId().longValue(), -1L, TaskType.Touche90, enabled, automatic, store.getId(), company.getId());
        this.maxConsumptionDays = Range.closed(0, maxConsumptionDays);
        this.maxAmountOfconsumption = Range.closed(0, maxAmountOfconsumption);
        setEditTime(DateTime.now());
        this.ruleDetails = parseDetail(details);
    }

    // for DB
    Touch90CareRuleEntity(ResultSet res, boolean enabled, boolean automatic, Integer storeId, Integer companyId) {
        super(res, TaskType.Touche90, enabled, automatic, storeId, companyId);
        try {
            String content_str = ResultSetUtil.getString(res, "content");
            Map<String, String> maps = DETAIL_SPLITTER.split(content_str);
            this.maxConsumptionDays = Range.closed(0, MapUtils.getInteger(maps, "maxConsumptionDays"));
            this.maxAmountOfconsumption = Range.closed(0, MapUtils.getInteger(maps, "maxAmountOfconsumption"));
            String details_str = ResultSetUtil.getString(res, "details");
            this.ruleDetails = parseDetail(details_str);
        } catch (SQLException e) {
            throw new RuntimeException("Restore Touch90CareRuleEntity has SQLException", e);
        }
    }

    public List<UpcomingTaskEntity> createTasks(final CrmStoreEntity store, final CrmMemberEntity member,
                                                List<UpcomingTaskEntity> touch90Tasks,
                                                List<SaleRecordEntity> saleRecords) {
        saleRecords.forEach(x -> Preconditions.checkState(x.getMemberId().equals(member.getId())));
        if (CollectionUtils.isNotEmpty(touch90Tasks))
            touch90Tasks.forEach(x -> Preconditions.checkState(x.isOwner(store, member)));
        List<UpcomingTaskEntity> task_90 = CollectionUtils.isNotEmpty(touch90Tasks) ?
                Lists.newArrayList(touch90Tasks) : Lists.newArrayList();
        UpcomingTaskEntity _current = null;
        List<SaleRecordEntity> _saleRecords = Lists.newArrayList(saleRecords);
        if (CollectionUtils.isNotEmpty(touch90Tasks)) {
            _current = touch90Tasks.get(touch90Tasks.size() - 1);
        } else {
            SaleRecordEntity _temp = _saleRecords.remove(0);
            _current = UpcomingTaskEntity.createTouche90Job(isAutomatic(), member, store, _temp);
            _current.setTaskDetails(new UpcomingTaskDetailList(createDetail(_current, _temp)));
            task_90.add(_current);
        }

        if (CollectionUtils.isEmpty(_saleRecords)) return task_90;

        UpcomingTaskEntity _next;
        for (SaleRecordEntity $it : _saleRecords) {
            _next = UpcomingTaskEntity.createTouche90Job(isAutomatic(), member, store, $it);
            _next.setTaskDetails(new UpcomingTaskDetailList(createDetail(_next, $it)));
            int _days = Days.daysBetween(_current.getSaleDate(), _next.getSaleDate()).getDays();
            if (!maxConsumptionDays.contains(_days)) {
                _current = _next;
                task_90.add(_current);
                continue;
            }
            int saleTotalAmount = $it.getSaleTotalAmount().intValue();
            if (!maxAmountOfconsumption.contains(saleTotalAmount)) {
                _current = _next;
                task_90.add(_current);
                continue;
            }
            _current.addMergeInfo($it.getId());
        }
        return task_90;
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
        Map<String, Object> res = super.toParamMap("maxConsumptionDays", "maxAmountOfconsumption", "ruleDetails");
        String content = String.format("maxConsumptionDays=%s,maxAmountOfconsumption=%s", maxConsumptionDays.upperEndpoint(),
                maxAmountOfconsumption.upperEndpoint());
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
                ruleDetails.add(new Touch90Detail(MapUtils.getString(params, "delay"), MapUtils.getString(params, "expired")));
            } else {
                ruleDetails.add(new Touch90Detail(MapUtils.getString(params, "delay"), MapUtils.getString(params, "expired"),
                        min, max));
            }
        });
        return ordering.sortedCopy(ruleDetails);
    }

    class Touch90Detail implements Comparable<Touch90Detail> {
        private Duration delay;
        private Duration expired;
        private Range<Long> consumption;

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
            if (delay.timeUnit.equals(TimeUnit.HOURS)) {
                _start = saleRecord.getSaleDate().plusHours((int) delay.duration);
            } else if (delay.timeUnit.equals(TimeUnit.DAYS)) {
                _start = saleRecord.getSaleDate().plusDays((int) delay.duration);
            } else {
                throw new IllegalArgumentException(String.format("尚未支持该参数解析 %s", delay.timeUnit));
            }
            LocalDateTime _expired = null;
            if (this.expired.duration == 0L) {
                _expired = new LocalDateTime(_start.getYear(), _start.getMonthOfYear(), _start.getDayOfMonth(), 23, 59, 59);
            } else {
                _expired = _start.plusDays((int) this.expired.duration);
            }

            if (consumption == null) {
                _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired);
            } else {
                long saleTotalAmount = saleRecord.getSaleTotalAmount().longValue();
                if (saleTotalAmount < consumption.lowerEndpoint() || consumption.contains(saleTotalAmount)) {
                    _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired);
                }
            }
            return _task_detail;
        }

        @Override
        public int compareTo(Touch90Detail o) {
            return Longs.compare(this.delay.toHours(), o.delay.toHours());
        }

        @Override
        public String toString() {
            return String.format("delay=%s,expired=%s", delay, expired);
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
                                String.format("key %s invalid format.  was %s, must end with one of [dDhHmMsS]", key, value));
                }
                this.timeUnit = timeUnit;
                this.duration = Long.parseLong(value.substring(0, value.length() - 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                        String.format("key %s value set to %s, must be integer", key, value));
            }
        }

        private long toHours() {
            return timeUnit.toHours(duration);
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
                    throw new IllegalArgumentException(String.format("非法的参数....%s ", timeUnit));
            }
            return String.format("%s%s", duration, res);
        }
    }

}
