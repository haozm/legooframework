package com.legooframework.model.membercare.jobs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.CareRuleEntityAction;
import com.legooframework.model.membercare.entity.Touch90CareRuleEntity;
import com.legooframework.model.membercare.entity.UpcomingTaskEntity;
import com.legooframework.model.membercare.entity.UpcomingTaskEntityAction;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Touch90ItemProcessor implements ItemProcessor<List<SaleRecordByStore>, List<UpcomingTaskEntity>> {

    @Override
    public List<UpcomingTaskEntity> process(List<SaleRecordByStore> merge_sale) throws Exception {
        final CrmOrganizationEntity company = merge_sale.iterator().next().getCompany();
        List<CrmStoreEntity> stores = merge_sale.stream().map(SaleRecordByStore::getStore).collect(Collectors.toList());
        List<UpcomingTaskEntity> upcomingTasks = Lists.newArrayList();
        Optional<List<UpcomingTaskEntity>> store_tasks = upcomingTaskAction.loadEnabledTouch90(company, stores);
        stores.forEach(st -> {
            Optional<SaleRecordByStore> cursor_opt = merge_sale.stream().filter(x -> x.getStore().equals(st)).findFirst();
            Preconditions.checkState(cursor_opt.isPresent());
            SaleRecordByStore cursor = cursor_opt.get();
            Touch90CareRuleEntity rule = loadRuleByStore(company, st);
            ArrayListMultimap<Integer, UpcomingTaskEntity> multimap = ArrayListMultimap.create();
            if (store_tasks.isPresent()) {
                List<UpcomingTaskEntity> sub_list = store_tasks.get().stream().filter(x -> x.getStoreId().equals(st.getId()))
                        .collect(Collectors.toList());
                sub_list.forEach(task -> multimap.put(task.getMemberId(), task));
            }
            cursor.getMember().forEach(mm -> {
                List<UpcomingTaskEntity> my_tasks = multimap.get(mm.getId());
                upcomingTasks.addAll(rule.createTasks(st, mm, my_tasks, cursor.getSaleRecords(mm)));
            });
        });
        return upcomingTasks;
    }

    /**
     * 获取门店指定的touch90 规则
     *
     * @param company
     * @param store
     * @return
     */
    private Touch90CareRuleEntity loadRuleByStore(CrmOrganizationEntity company, CrmStoreEntity store) {
        Optional<List<Touch90CareRuleEntity>> touch90CareRules = careRuleEntityAction.loadAllTouch90Rules(company);
        Preconditions.checkState(touch90CareRules.isPresent());
        int[] _args = new int[]{-1, store.getId()};
        List<Touch90CareRuleEntity> sub_list = touch90CareRules.get().stream()
                .filter(x -> ArrayUtils.contains(_args, x.getStoreId()))
                .collect(Collectors.toList());
        Optional<Touch90CareRuleEntity> touch90_store = sub_list.stream()
                .filter(x -> x.getStoreId().equals(store.getId())).findFirst();
        return touch90_store.orElseGet(() -> sub_list.get(0));
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
