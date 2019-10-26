package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import org.apache.commons.collections4.MapUtils;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public class Touch90JobEntity extends BaseEntity<Long> {

    private final static String JOB_NAME = BusinessType.TOUCHED90.getDesc();
    private final static String START_TIME_NAME = "start.time";
    private final static String END_TIME_NAME = "end.time";
    private final static String JOB_PARAMS_NAME = "job.params";
    private final String categories;
    private final Integer companyId, storeId;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private static final Splitter.MapSplitter MAP_SPLITTER = Splitter.on(',').withKeyValueSeparator('=');

    private Touch90JobEntity(Integer companyId, Integer storeId, String categories, LocalDateTime start, LocalDateTime end) {
        super(0L, companyId.longValue(), -1L);
        this.startTime = start;
        this.endTime = end;
        this.companyId = companyId;
        this.storeId = storeId;
        this.categories = categories;
    }

    public static Touch90JobEntity getInstance(JobParameters parameters) {
        Date start = parameters.getDate(START_TIME_NAME);
        Date end = parameters.getDate(END_TIME_NAME);
        String jobParams = parameters.getString(JOB_PARAMS_NAME);
        Map<String, String> params = MAP_SPLITTER.split(jobParams);
        return new Touch90JobEntity(MapUtils.getInteger(params, "companyId"), MapUtils.getInteger(params, "storeId"),
                MapUtils.getString(params, "categories"), LocalDateTime.fromDateFields(start), LocalDateTime.fromDateFields(end));
    }

    public static Touch90JobEntity init(CrmStoreEntity store, String categories) {
        LocalDateTime now = LocalDateTime.now();
        return new Touch90JobEntity(store.getCompanyId(), store.getId(), categories, now.plusMinutes(-5), now);
    }

    public JobParameters currentJobParameters() {
        JobParametersBuilder pb = new JobParametersBuilder();
        String jobParams = String.format("companyId=%s,storeId=%s,categories=%s", companyId, storeId, categories);
        return pb.addDate(START_TIME_NAME, this.startTime.toDate()).addDate(END_TIME_NAME, this.endTime.toDate())
                .addString(JOB_PARAMS_NAME, jobParams).toJobParameters();
    }

    public Optional<Touch90JobEntity> nextJobParameters(int minute) {
        if (minute <= 5) minute = 5;
        LocalDateTime _next_time = this.endTime.plusMinutes(minute);
        LocalDateTime _now = LocalDateTime.now();
        if (_now.isBefore(_next_time)) return Optional.empty();
        return Optional.of(new Touch90JobEntity(this.companyId, this.storeId, this.categories, this.endTime.plusSeconds(1), _now));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("company", companyId)
                .add("storeId", storeId)
                .add("jobName", JOB_NAME)
                .add("categories", categories)
                .add(START_TIME_NAME, startTime)
                .add(END_TIME_NAME, endTime)
                .toString();
    }
}
