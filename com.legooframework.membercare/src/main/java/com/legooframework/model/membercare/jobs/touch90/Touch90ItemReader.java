package com.legooframework.model.membercare.jobs.touch90;

import com.google.common.base.Splitter;
import com.legooframework.model.salesrecords.service.SaleRecordByStore;
import com.legooframework.model.salesrecords.service.SaleRecordService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Touch90ItemReader implements ItemReader<SaleRecordByStore>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(Touch90ItemReader.class);

    private static final Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').withKeyValueSeparator('=');

    @Override
    public SaleRecordByStore read() throws Exception {
        String params_str = (String) params.get(0);
        Date start_date = (Date) params.get(1);
        Date end_date = (Date) params.get(2);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Touch90ItemReader.read(%s,%s,%s) begin start", params_str, start_date, end_date));
        step += 1;
        if (step == 2) return null;
        Map<String, String> params = MAP_SPLITTER.split(params_str);
        Optional<SaleRecordByStore> saleRecordByStore = this.appCtx.getBean(SaleRecordService.class)
                .loadSaleRecordByStore(MapUtils.getInteger(params, "companyId"), MapUtils.getInteger(params, "storeId"),
                        MapUtils.getString(params, "categories"), start_date, end_date, true);
        if (logger.isDebugEnabled())
            logger.debug(String.format("Touch90ItemReader.read(%s,%s,%s,%s) size is %s", MapUtils.getInteger(params, "companyId"),
                    MapUtils.getInteger(params, "storeId"), start_date, end_date,
                    saleRecordByStore.map(SaleRecordByStore::size).orElse(0)));
        return saleRecordByStore.orElse(null);
    }

//   <value>#{jobParameters['job.params']}</value>
//   <value>#{jobParameters['start.time']}</value>
//   <value>#{jobParameters['end.time']}</value>

    private List<?> params;
    private int step = 0;

    public void setParams(List<?> params) {
        this.params = params;
    }

    private ApplicationContext appCtx;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        this.appCtx = appCtx;
    }

}
