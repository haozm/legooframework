package com.legooframework.model.templatemgs.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.service.BaseService;
import com.legooframework.model.core.osgi.Bundle;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.membercare.entity.BusinessType;
import com.legooframework.model.templatemgs.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateMgnService extends BundleService {

    private static final Logger logger = LoggerFactory.getLogger(TemplateMgnService.class);

    public Optional<TemplateClassifyEntity> findByHoliday(String holidayId) {
        Optional<HolidayEntity> holidayOpt = getBean(HolidayEntityAction.class).findById(holidayId);
        if (!holidayOpt.isPresent()) return Optional.empty();
        Optional<TemplateClassifyEntity> parentOpt = getClassifyAction().loadById("2000");
        Preconditions.checkArgument(parentOpt.isPresent(), "父级模板【%s】类不存在");
        TemplateClassifyEntity classifyTmp = TemplateClassifyEntity.createClassify4Holiday(parentOpt.get(), holidayOpt.get());
        return Optional.of(classifyTmp);
    }

    public Optional<TemplateClassifyEntity> loadTemplateById(String classify) {
        Optional<TemplateClassifyEntity> templateClassify = getClassifyAction().loadById(classify);
        if (!templateClassify.isPresent()) {
            templateClassify = findByHoliday(classify);
            if (!templateClassify.isPresent()) return Optional.empty();
            getBean(TemplateClassifyEntityAction.class).addTemplateClassfy(templateClassify.get());
        }
        return templateClassify;
    }

    public void insertTemplate(String title, String template, String classifyId, String useScopes, boolean isDefault,
                               LoginContext user) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "模板标题不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(template), "模板内容不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(classifyId), "模板分类不可以为空...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(useScopes), "模板使用范围不可以为空...");
        MsgReplaceHoldList replaceHold = getBean(MsgReplaceHoldEntityAction.class).loadByUser(user);
        Preconditions.checkState(replaceHold.checkTemplate(template));
        List<UseScope> _usp = Stream.of(StringUtils.split(useScopes, ',')).map(Integer::new).map(UseScope::paras)
                .collect(Collectors.toList());
        MsgTemplateEntity ins = new MsgTemplateEntity(user, title, template, classifyId, _usp, isDefault);
        getTemplateEntityAction().insert(ins, user);
        if (logger.isDebugEnabled())
            logger.debug(String.format("insertTemplate( %s ) ok", ins));
    }

    public void updateTemplate(String templateId, String title, String template, String classifyId, String useScopeVals,
                               LoginContext user) {

        if (!Strings.isNullOrEmpty(template)) {
            MsgReplaceHoldList replaceHold = getBean(MsgReplaceHoldEntityAction.class).loadByUser(user);
            Preconditions.checkState(replaceHold.checkTemplate(template));
        }

        Collection<UseScope> useScopes = null;
        if (!Strings.isNullOrEmpty(useScopeVals)) {
            useScopes = Stream.of(StringUtils.split(useScopeVals, ',')).map(Integer::valueOf)
                    .map(UseScope::paras).collect(Collectors.toList());
        }

        getBean(MsgTemplateEntityAction.class).changeTemplate(templateId, title, template, classifyId, useScopes);
        if (logger.isDebugEnabled())
            logger.debug(String.format("updateTemplate( %s,%s,%s ) ok", template, classifyId, useScopeVals));
    }

    /**
     * 加载 公司或者门店 可用的默认模板 仅仅用于 90 服务
     * 其他服务的待定
     * 代码的复杂度  我都怀疑人生了
     *
     * @param store 虎踞龙盘
     * @return 仰天长啸
     */
    public Touch90DefauteTemplate loadDefaultTouch90Template(CrmOrganizationEntity company, CrmStoreEntity store) {
        List<Map<String, Object>> touch90DefauteTemplates = Lists.newArrayList();
        String prefix = String.format("%s_%s_", BusinessType.TOUCHED90.toString(), company.getId());
        if (store == null) {
            Optional<List<MsgTemplateEntity>> sublist = getTemplateEntityAction().loadEnabledListByCom(company);
            if (!sublist.isPresent()) return null;
            List<MsgTemplateEntity> def_list = sublist.get().stream().filter(MsgTemplateEntity::isDefaulted)
                    .filter(x -> StringUtils.startsWith(x.getSingleClassifies(), prefix))
                    .filter(MsgTemplateEntity::isCompany)
                    .collect(Collectors.toList());
            def_list.forEach(opt -> {
                Map<String, Object> param = Maps.newHashMap();
                String[] args = StringUtils.split(opt.getSingleClassifies(), '_');
                param.put("subRuleId", opt.getSingleClassifies());
                param.put("categories", args[args.length - 2]);
                param.put("id", args[args.length - 1]);
                param.put("template", opt.getTemplate());
                touch90DefauteTemplates.add(param);
            });
            return new Touch90DefauteTemplate(company, null, touch90DefauteTemplates);
        }
        Optional<List<MsgTemplateEntity>> sublist = getTemplateEntityAction().loadEnabledTouch90ByStore(store);
        if (!sublist.isPresent()) return null;
        List<MsgTemplateEntity> def_list = sublist.get().stream().filter(MsgTemplateEntity::isDefaulted)
                .filter(x -> StringUtils.startsWith(x.getSingleClassifies(), prefix))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(def_list)) return null;
        ArrayListMultimap<String, MsgTemplateEntity> multimap = ArrayListMultimap.create();
        def_list.forEach(x -> multimap.put(x.getSingleClassifies(), x));
        multimap.keySet().forEach(key -> {
            List<MsgTemplateEntity> list = multimap.get(key);
            list.sort(MSGTEMPLATE_ORDERING);
            Map<String, Object> param = Maps.newHashMap();
            String[] args = StringUtils.split(list.get(0).getSingleClassifies(), '_');
            param.put("subRuleId", list.get(0).getSingleClassifies());
            param.put("categories", args[args.length - 2]);
            param.put("id", args[args.length - 1]);
            param.put("template", list.get(0).getTemplate());
            touch90DefauteTemplates.add(param);
        });
        return new Touch90DefauteTemplate(company, store, touch90DefauteTemplates);
    }

    private static Comparator<MsgTemplateEntity> MSGTEMPLATE_ORDERING = Comparator
            .comparingInt(x -> x.isStore() ? 10 : x.isCompany() ? 20 : 30);

    private MsgTemplateEntityAction getTemplateEntityAction() {
        return getBean(MsgTemplateEntityAction.class);
    }

    private TemplateClassifyEntityAction getClassifyAction() {
        return getBean(TemplateClassifyEntityAction.class);
    }
}
