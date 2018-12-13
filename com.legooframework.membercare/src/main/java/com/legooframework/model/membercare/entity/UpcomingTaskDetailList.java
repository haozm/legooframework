package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.legooframework.model.core.utils.DateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UpcomingTaskDetailList {

    private static final Splitter STAGE_SPLITTER = Splitter.on('$').trimResults();
    private static Ordering<UpcomingTaskDetailEntity> ordering = Ordering.from(new UpcomingTaskDetailComparator());
    private final List<UpcomingTaskDetailEntity> delegate;

    UpcomingTaskDetailList(List<UpcomingTaskDetailEntity> details) {
        this.delegate = Lists.newArrayListWithCapacity(details.size());
        details.sort(ordering);
        this.delegate.addAll(details);
    }

    UpcomingTaskDetailList(final UpcomingTaskEntity task90, String details) {
        this.delegate = Lists.newArrayList();
        STAGE_SPLITTER.split(details).forEach(stage -> {
            String[] args = StringUtils.split(stage, ',');
            this.delegate.add(new UpcomingTaskDetailEntity(args[0], task90, TaskStatus.paras(Integer.valueOf(args[1])),
                    DateTimeUtils.parseDef(args[2]), DateTimeUtils.parseDef(args[3]),
                    StringUtils.equals("NO", args[4]) ? null : DateTimeUtils.parseDef(args[5])));
        });
        this.delegate.sort(ordering);
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
        return this.delegate.get(this.delegate.size()-1).getId().equals(detail.getId());
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

    static class UpcomingTaskDetailComparator implements Comparator<UpcomingTaskDetailEntity> {
        @Override
        public int compare(UpcomingTaskDetailEntity a, UpcomingTaskDetailEntity b) {
            return a.getStartDateTime().equals(b.getStartDateTime()) ? 0 :
                    a.getStartDateTime().isBefore(b.getStartDateTime()) ? -1 : 1;
        }
    }
}
