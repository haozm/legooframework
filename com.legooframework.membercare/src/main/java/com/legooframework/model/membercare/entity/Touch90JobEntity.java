package com.legooframework.model.membercare.entity;

import com.google.common.base.MoreObjects;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.utils.DateTimeUtils;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.util.Date;
import java.util.Optional;

public class Touch90JobEntity extends BaseEntity<Long> {

    private final static String JOB_NAME = TaskType.Touche90.getDesc();
    private final static String START_TIME_NAME = "start.time";
    private final static String END_TIME_NAME = "end.time";

    private final CrmOrganizationEntity company;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;

    private Touch90JobEntity(Long id, CrmOrganizationEntity company, LocalDateTime start, LocalDateTime end) {
        super(id);
        this.startTime = start;
        this.endTime = end;
        this.company = company;
    }

    public static Touch90JobEntity getInstance(Long id, CrmOrganizationEntity company, JobParameters parameters) {
        Date start = parameters.getDate(START_TIME_NAME);
        Date end = parameters.getDate(END_TIME_NAME);
        return new Touch90JobEntity(id, company, LocalDateTime.fromDateFields(start), LocalDateTime.fromDateFields(end));
    }

    public static Touch90JobEntity init(CrmOrganizationEntity company) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDate end = LocalDate.now().plusDays(-89);
        LocalDateTime startTime = DateTimeUtils.parseDef(String.format("%s 00:00:00", end.toString("yyyy-MM-dd")));
        return new Touch90JobEntity(0L, company, startTime, endTime);
    }

    public JobParameters currentJobParameters() {
        JobParametersBuilder pb = new JobParametersBuilder();
        return pb.addDate(START_TIME_NAME, this.startTime.toDate()).addDate(END_TIME_NAME, this.endTime.toDate())
                .toJobParameters();
    }

    public Optional<Touch90JobEntity> nextJob() {
        LocalDateTime _next_time = LocalDateTime.now().plusMinutes(30);
        if (_next_time.isBefore(this.endTime)) return Optional.empty();
        Date _start = this.endTime.plusSeconds(1).toDate();
        Date _end = LocalDateTime.now().toDate();
        return Optional.of(new Touch90JobEntity(null, company, LocalDateTime.fromDateFields(_start),
                LocalDateTime.fromDateFields(_end)));
    }

    public String getJobName() {
        return JOB_NAME;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("jobName", JOB_NAME)
                .add(START_TIME_NAME, startTime)
                .add(END_TIME_NAME, endTime)
                .add("company", company.getId())
                .toString();
    }
}
