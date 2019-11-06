package com.legooframework.model.templatemgs.mvc;

import com.google.common.collect.Lists;
import com.legooframework.model.core.utils.WebUtils;
import com.legooframework.model.smsgateway.entity.SendMessageTemplate;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JobDetailTemplate4Replace {

    private final List<SendMessageTemplate> jobDetails;
    private final String template;
    private final Integer employeeId, companyId;

    JobDetailTemplate4Replace(Integer companyId, Integer employeeId, String fragment, boolean encoding) {
        String[] args = StringUtils.split(fragment, "||");
        this.template = encoding ? WebUtils.decodeUrl(args[1]) : args[1];
        args = StringUtils.split(args[0], '|');
        List<SendMessageTemplate> _list = Lists.newArrayListWithCapacity(args.length);
        Stream.of(args).forEach(x -> _list.add(SendMessageTemplate.createByMemberId(x)));
        this.jobDetails = _list;
        this.employeeId = employeeId;
        this.companyId = companyId;
    }

    List<SendMessageTemplate> getJobDetails() {
        return jobDetails;
    }

    List<Integer> getMemberIds() {
        return jobDetails.stream().map(SendMessageTemplate::getMemberId).collect(Collectors.toList());
    }

    Integer getCompanyId() {
        return companyId;
    }

    String getDecoceTemplate() {
        return WebUtils.decodeUrl(template);
    }

    String getTemplate() {
        return template;
    }

    @Override
    public String toString() {
        return StringUtils.join(this.jobDetails, '|');
    }
}
