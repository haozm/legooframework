package com.legooframework.model.membercare.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class TaskCareDetailRule {

    private final String id;
    private Duration delay;
    private Duration expired;
    private boolean auto, enabled;
    private LocalTime startTime;
    private Range<Long> consumption;

    Map<String, Object> toViewMap() {
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", id);
        params.put("delay", delay.toString());
        params.put("expired", expired.toString());
        params.put("auto", auto);
        params.put("enabled", enabled);
        params.put("startTime", startTime == null ? null : startTime.toString("HH:mm"));
        if (consumption != null) {
            params.put("min", consumption.lowerEndpoint());
            params.put("max", consumption.upperEndpoint());
        }
        return params;
    }

    boolean isDisabled(TaskCareDetailRule that) {
        return this.enabled && !that.enabled;
    }

    boolean isEnabled(TaskCareDetailRule that) {
        return !this.enabled && that.enabled;
    }

    boolean isEnabledBoth(TaskCareDetailRule that) {
        return this.enabled && that.enabled;
    }

    boolean isCloseAuto(TaskCareDetailRule that) {
        return this.enabled && that.enabled && this.auto && !that.auto;
    }

    boolean isOpenAuto(TaskCareDetailRule that) {
        return this.enabled && that.enabled && !this.auto && that.auto;
    }

    boolean isChangeTime(TaskCareDetailRule that) {
        return this.enabled && that.enabled && this.auto && that.auto &&
                !this.startTime.toString("HHmm").equals(that.startTime.toString("HHmm"));
    }

    public Duration getDelay() {
        return delay;
    }

    Duration getExpired() {
        return expired;
    }

    private TaskCareDetailRule(String id, String delayVal, String expiredVal, Long start, Long end, boolean enabled,
                               boolean auto, String startTime) {
        this.id = id;
        this.delay = new Duration("delay", delayVal);
        this.expired = new Duration("expired", expiredVal);
        this.consumption = Range.openClosed(start, end);
        this.enabled = enabled;
        this.auto = auto;
        if (this.auto)
            Preconditions.checkArgument(!Strings.isNullOrEmpty(startTime), "任务开始执行时间不可未空值....");
        if (StringUtils.isNotEmpty(startTime)) {
            String[] times = StringUtils.split(startTime, ':');
            Preconditions.checkState(times.length > 1, "时间格式异常 %s", startTime);
            this.startTime = new LocalTime(Integer.valueOf(times[0]), Integer.valueOf(times[1]));
        }
    }

    private TaskCareDetailRule(String id, String delayVal, String expiredVal, boolean enabled, boolean auto, String startTime) {
        this.id = id;
        this.delay = new Duration("delay", delayVal);
        this.expired = new Duration("expired", expiredVal);
        this.auto = auto;
        this.enabled = enabled;
        if (this.auto)
            Preconditions.checkArgument(!Strings.isNullOrEmpty(startTime), "任务开始执行时间不可未空值....");
        if (StringUtils.isNotEmpty(startTime)) {
            String[] times = StringUtils.split(startTime, ':');
            this.startTime = new LocalTime(Integer.valueOf(times[0]), Integer.valueOf(times[1]));
        }
    }

    static TaskCareDetailRule createTouch90Rule(String ruleBuilderSpec) {
        Map<String, String> params = Splitter.on(",").trimResults().withKeyValueSeparator('=').split(ruleBuilderSpec);
        String delay = MapUtils.getString(params, "delay");
        String id = StringUtils.endsWith(delay, "h") ? "0" : delay.substring(0, delay.length() - 1);
        String expired = MapUtils.getString(params, "expired");
        boolean enabled = MapUtils.getBoolean(params, "enabled", true);
        boolean auto = MapUtils.getBoolean(params, "auto", false);
        String startTime = MapUtils.getString(params, "startTime", null);
        if (auto) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(startTime), "id=%s开启自动时，开始时间不可以为空值...", id);
        }
        long min = MapUtils.getLong(params, "min", -1L);
        long max = MapUtils.getLong(params, "max", -1L);
        if (min == -1L) {
            return new TaskCareDetailRule(id, delay, expired, enabled, auto, startTime);
        } else {
            return new TaskCareDetailRule(id, delay, expired, min, max, enabled, auto, startTime);
        }
    }

    String getId() {
        return id;
    }

    boolean isEnabled() {
        return enabled;
    }

    boolean isAuto() {
        return auto;
    }

    LocalTime getStartTime() {
        return startTime;
    }

//    Optional<UpcomingTaskDetailEntity> createDetail(TaskCareRuleEntity rule, UpcomingTaskEntity task, SaleRecordEntity saleRecord) {
//        if (!isEnabled()) return Optional.empty();
//        UpcomingTaskDetailEntity _task_detail = null;
//        LocalDateTime _start = null;
//        LocalDateTime sale_date = saleRecord.getSaleDate();
//        if (delay.getTimeUnit().equals(TimeUnit.HOURS)) {
//            // _start = saleRecord.getSaleDate().plusHours((int) delay.getDuration());
//            _start = saleRecord.getSaleDate();
//        } else if (delay.getTimeUnit().equals(TimeUnit.DAYS)) {
//            _start = sale_date.plusDays((int) delay.getDuration());
//            _start = new LocalDateTime(_start.getYear(), _start.getMonthOfYear(), _start.getDayOfMonth(), 0, 0, 1);
//        } else {
//            throw new IllegalArgumentException(format("尚未支持该参数解析 %s", delay.getTimeUnit()));
//        }
//        LocalDateTime _expired = null;
//        if (this.expired.getDuration() == 0L) {
//            _expired = new LocalDateTime(_start.getYear(), _start.getMonthOfYear(), _start.getDayOfMonth(), 23, 59, 59);
//        } else if (expired.getTimeUnit().equals(TimeUnit.DAYS)) {
//            _expired = _start.plusDays((int) this.expired.getDuration());
//            _expired = new LocalDateTime(_expired.getYear(), _expired.getMonthOfYear(), _expired.getDayOfMonth(),
//                    23, 59, 59);
//        } else {
//            throw new IllegalArgumentException(format("尚未支持该参数解析 %s", _expired));
//        }
//
//        if (consumption == null) {
//            _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired, rule, this);
//        } else {
//            long saleTotalAmount = saleRecord.getSaleTotalAmount().longValue();
//            if (saleTotalAmount < consumption.lowerEndpoint() || consumption.contains(saleTotalAmount)) {
//                _task_detail = new UpcomingTaskDetailEntity(task, _start, _expired, rule, this);
//            }
//        }
//        return Optional.ofNullable(_task_detail);
//    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(",").add("id=" + id)
                .add("delay=" + delay).add("expired=" + expired);
        if (auto)
            sj.add("auto=" + auto).add("startTime=" + startTime.toString("HH:mm"));
        if (consumption != null) {
            sj.add("min=" + consumption.lowerEndpoint()).add("max=" + consumption.upperEndpoint());
        }
        return sj.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskCareDetailRule)) return false;
        TaskCareDetailRule that = (TaskCareDetailRule) o;
        return auto == that.auto &&
                Objects.equals(delay, that.delay) &&
                Objects.equals(expired, that.expired) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(consumption, that.consumption);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delay, expired, consumption, startTime);
    }
}
