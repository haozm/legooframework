package com.legooframework.model.membercare.jobs.touch90;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.*;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Optional;

public class Touch90ItemProcessor implements ItemProcessor<SaleRecordByStore, List<Touch90TaskDto>> {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemProcessor.class);

    @Override
    public List<Touch90TaskDto> process(SaleRecordByStore saleRecordByStore) throws Exception {
        final CrmStoreEntity store = saleRecordByStore.getStore();

        Optional<List<UpcomingTaskEntity>> store_tasks = upcomingTaskAction.loadEnabledTouch90(store,
                saleRecordByStore.getCategories());
        ArrayListMultimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
        store_tasks.ifPresent(upcomingTasks -> upcomingTasks.forEach(x -> multimap.put(x.getMemberId(), x)));

        final TaskCareRule4Touch90Entity rule = careRuleEntityAction.loadRuleByStoreWithCategories(store,
                saleRecordByStore.getCategories());

        List<Touch90TaskDto> upcomingTasks = Lists.newArrayList();
        saleRecordByStore.getMember().forEach(mm -> rule.createTasks(store, mm, multimap.get(mm.getId()),
                saleRecordByStore.getSaleRecords(mm)).ifPresent(upcomingTasks::add));

        return upcomingTasks;
    }

    private TaskCareRule4Touch90EntityAction careRuleEntityAction;
    private UpcomingTaskEntityAction upcomingTaskAction;

    public void setCareRuleEntityAction(TaskCareRule4Touch90EntityAction careRuleEntityAction) {
        this.careRuleEntityAction = careRuleEntityAction;
    }

    public void setUpcomingTaskAction(UpcomingTaskEntityAction upcomingTaskAction) {
        this.upcomingTaskAction = upcomingTaskAction;
    }
}
