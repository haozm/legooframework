package com.legooframework.model.templatemgs.entity;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleMsgTemplateList {

    private final List<SimpleMsgTemplate> templates;

    private SimpleMsgTemplateList(List<SimpleMsgTemplate> templates) {
        this.templates = templates;
    }

    public boolean hasTempaltes() {
        return CollectionUtils.isNotEmpty(templates);
    }

    public Optional<String> getDefTempateByRuleId(String classifies, Integer storeId) {
        if (CollectionUtils.isEmpty(templates)) return Optional.empty();
        List<SimpleMsgTemplate> list = this.templates.stream().filter(SimpleMsgTemplate::isDefault)
                .filter(x -> x.isClassifies(classifies)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) return Optional.empty();
        Optional<SimpleMsgTemplate> opt = list.stream().filter(x -> x.hasStore(storeId)).findFirst();
        if (opt.isPresent()) return Optional.of(opt.get().getTemplate());
        opt = list.stream().filter(SimpleMsgTemplate::isCompany).findFirst();
        return opt.map(SimpleMsgTemplate::getTemplate);
    }

    public static SimpleMsgTemplateList create(String payloads) {
        String[] args = StringUtils.splitByWholeSeparator(payloads, "||");
        List<SimpleMsgTemplate> templates = Lists.newArrayList();
        Stream.of(args).forEach(arg -> templates.add(SimpleMsgTemplate.create(arg)));
        return new SimpleMsgTemplateList(templates);
    }

    public static SimpleMsgTemplateList createEmpty() {
        return new SimpleMsgTemplateList(null);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("templates", templates == null ? 0 : templates.size())
                .toString();
    }
}
