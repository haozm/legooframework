package com.legooframework.model.membercare.entity;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import java.util.*;
import java.util.stream.Stream;

public class UpcomingTaskDetailList {

    private static Ordering<UpcomingTaskDetailEntity> ordering
            = Ordering.natural().onResultOf((Function<UpcomingTaskDetailEntity, Date>) foo -> foo.getStartDateTime().toDate());
    private static Splitter.MapSplitter SPLITTER = Splitter.on(',').withKeyValueSeparator('=');
    private final List<UpcomingTaskDetailEntity> delegate;

    UpcomingTaskDetailList(List<UpcomingTaskDetailEntity> details) {
        this.delegate = Lists.newArrayListWithCapacity(details.size());
        details.sort(ordering);
        this.delegate.addAll(details);
    }

    UpcomingTaskDetailList(final UpcomingTaskEntity task90, String details) {
        this.delegate = Lists.newArrayList();
        Stream.of(StringUtils.split(details, '$')).forEach(stage -> {
            Map<String, String> map = SPLITTER.split(stage);
            String id = MapUtils.getString(map, "id");
            String taskId = MapUtils.getString(map, "ti");
            TaskStatus taskStatus = TaskStatus.paras(MapUtils.getIntValue(map, "su"));
            LocalDateTime startDateTime = DateTimeUtils.parseDef(MapUtils.getString(map, "sd"));
            LocalDateTime expiredDateTime = DateTimeUtils.parseDef(MapUtils.getString(map, "ed"));
            LocalDateTime finshDateTime = Strings.isNullOrEmpty(MapUtils.getString(map, "fd")) ? null :
                    DateTimeUtils.parseDef(MapUtils.getString(map, "fd"));
            String stepIndex = MapUtils.getString(map, "ix");
            this.delegate.add(new UpcomingTaskDetailEntity(id, taskId, taskStatus,
                    startDateTime, expiredDateTime, stepIndex, finshDateTime));
        });
        this.delegate.sort(ordering);
    }

    boolean allExpired() {
        UpcomingTaskDetailEntity last = this.delegate.get(this.delegate.size() - 1);
        return last.getExpiredDateTime().isBefore(LocalDateTime.now());
    }

    Optional<List<UpcomingTaskDetailEntity>> canceled() {
        final List<UpcomingTaskDetailEntity> list = Lists.newArrayList();
        delegate.forEach(item -> item.canceled().ifPresent(list::add));
        return CollectionUtils.isNotEmpty(list) ? Optional.empty() : Optional.of(list);
    }

    Optional<UpcomingTaskDetailEntity> findById(String detailId) {
        return this.delegate.stream().filter(x -> x.getId().equals(detailId)).findFirst();
    }

    boolean isLastStep(UpcomingTaskDetailEntity detail) {
        return this.delegate.get(this.delegate.size() - 1).getId().equals(detail.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UpcomingTaskDetailList)) return false;
        UpcomingTaskDetailList that = (UpcomingTaskDetailList) o;
        return ListUtils.isEqualList(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("details", delegate)
                .toString();
    }

    List<UpcomingTaskDetailEntity> getDelegate() {
        return Lists.newArrayList(delegate);
    }

}
