package com.legooframework.model.membercare.entity;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class UpcomingTaskDetails extends ForwardingList<UpcomingTaskDetailEntity> {

    private List<UpcomingTaskDetailEntity> details;
    private static final Comparator<UpcomingTaskDetailEntity> UPCOMINGTASKDETAIL_ORDERING = Comparator
            .comparingInt(x -> Integer.valueOf(x.getStartDateTime().toString("yyyyMMddHH")));

    UpcomingTaskDetails(List<UpcomingTaskDetailEntity> details) {
        this.details = Lists.newArrayList(details);
        this.details.sort(UPCOMINGTASKDETAIL_ORDERING);
    }

    @Override
    protected List<UpcomingTaskDetailEntity> delegate() {
        if (details == null) details = Lists.newArrayListWithCapacity(10);
        return details;
    }

    public Optional<List<UpcomingTaskDetailEntity>> cancalAll() {
        List<UpcomingTaskDetailEntity> cancel_list = Lists.newArrayList();
        this.details.forEach(x -> x.makeCanceled().ifPresent(cancel_list::add));
        return Optional.ofNullable(CollectionUtils.isEmpty(cancel_list) ? null : cancel_list);
    }

}
