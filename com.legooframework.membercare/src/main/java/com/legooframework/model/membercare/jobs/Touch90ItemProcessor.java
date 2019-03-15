package com.legooframework.model.membercare.jobs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.*;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Touch90ItemProcessor implements ItemProcessor<List<SaleRecordByStore>, List<Touch90TaskDto>> {

    @Override
    public List<Touch90TaskDto> process(List<SaleRecordByStore> merge_sale) throws Exception {
        final CrmOrganizationEntity company = merge_sale.iterator().next().getCompany();
        List<CrmStoreEntity> stores = merge_sale.stream().map(SaleRecordByStore::getStore).collect(Collectors.toList());
        List<Touch90TaskDto> upcomingTasks = Lists.newArrayList();
        Optional<List<UpcomingTaskEntity>> store_tasks = upcomingTaskAction.loadEnabledTouch90(company, stores);
        stores.forEach(st -> {
            Optional<SaleRecordByStore> cursor_opt = merge_sale.stream().filter(x -> x.getStore().equals(st)).findFirst();
            Preconditions.checkState(cursor_opt.isPresent());
            SaleRecordByStore cursor = cursor_opt.get();
            Touch90CareRuleEntity rule = careRuleEntityAction.loadRuleByStore(company, st);
            ArrayListMultimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
            if (store_tasks.isPresent()) {
                List<UpcomingTaskEntity> sub_list = store_tasks.get().stream().filter(x -> x.getStoreId().equals(st.getId()))
                        .collect(Collectors.toList());
                sub_list.forEach(task -> multimap.put(task.getMemberId(), task));
            }
            cursor.getMember().forEach(mm -> {
                rule.createTasks(st, mm, multimap.get(mm.getId()), cursor.getSaleRecords(mm)).ifPresent(upcomingTasks::add);
            });
        });
        return upcomingTasks;
    }

    private CareRuleEntityAction careRuleEntityAction;
    private UpcomingTaskEntityAction upcomingTaskAction;

    public void setCareRuleEntityAction(CareRuleEntityAction careRuleEntityAction) {
        this.careRuleEntityAction = careRuleEntityAction;
    }

    public void setUpcomingTaskAction(UpcomingTaskEntityAction upcomingTaskAction) {
        this.upcomingTaskAction = upcomingTaskAction;
    }
}
