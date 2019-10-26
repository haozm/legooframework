package com.legooframework.model.insurance.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.jdbc.PagingResult;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.dict.dto.KvDictDto;
import com.legooframework.model.dict.entity.KvDictEntity;
import com.legooframework.model.dict.entity.KvDictEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController(value = "InsuranceDictController")
@RequestMapping(value = "/dict")
public class DictController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DictController.class);

    @PostMapping(value = "/type/list.json")
    public JsonMessage loadTypeList(HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        Optional<List<Map<String, Object>>> types = getJdbcQuerySupport(request)
                .queryForList("InsuranceDict", "typeList", null);
        return JsonMessageBuilder.OK().withPayload(types.orElse(null)).toMessage();
    }

    @PostMapping(value = "/detail/list.json")
    public JsonMessage loadDetailList(@RequestBody(required = false) Map<String, String> requestBody, HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        Map<String, Object> params = Maps.newHashMap();
        String dictType = MapUtils.getString(requestBody, "dictType");
        if (!Strings.isNullOrEmpty(dictType)) params.put("dictType", dictType);
        int pageNum = MapUtils.getInteger(requestBody, "page", 1);
        int pageSize = MapUtils.getInteger(requestBody, "limit", 20);
        PagingResult pages = getJdbcQuerySupport(request).queryForPage("InsuranceDict", "detailList", pageNum, pageSize,
                params);
        return JsonMessageBuilder.OK().withPayload(pages.toData()).toMessage();
    }

    @PostMapping(value = "/{action}/detail.json")
    public JsonMessage addDictDetail(@PathVariable(value = "action") String action,
                                     @RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addDictDetail(%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        String dictType = MapUtils.getString(requestBody, "dictType");
        String fieldValue = MapUtils.getString(requestBody, "fieldValue");
        String fieldName = MapUtils.getString(requestBody, "fieldName");
        String fieldDesc = MapUtils.getString(requestBody, "fieldDesc");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(dictType), "字典分类不可以为空值...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldValue), "字典内置值不可以为空值...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fieldName), "字典展示值不可以为空值...");
        if (StringUtils.equals("add", action)) {
            getBean("insuranceDictEntityAction", KvDictEntityAction.class, request)
                    .insert(dictType, fieldValue, fieldName, fieldDesc, 99);
        } else if (StringUtils.equals("update", action)) {
            getBean("insuranceDictEntityAction", KvDictEntityAction.class, request)
                    .editAction(dictType, fieldValue, fieldName, fieldDesc, 99);
        } else {
            throw new IllegalArgumentException("非法的入参...");
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/single/detail.json")
    public JsonMessage getDictByType(@RequestBody(required = false) Map<String, String> params,
                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] And RequestBody = %s", request.getRequestURI(), params));
        getLoginContext();
        String group = MapUtils.getString(params, "group");
        if (StringUtils.isEmpty(group)) return JsonMessageBuilder.OK().toMessage();
        List<KvDictDto> list = findByGroup(group, request);
        return JsonMessageBuilder.OK().withPayload(list).toMessage();
    }

    @PostMapping(value = "/groups/detail.json")
    public JsonMessage getDictByGroups(@RequestBody(required = false) Map<String, String> params,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] And RequestBody = %s", request.getRequestURI(), params));
        if (MapUtils.isEmpty(params)) return JsonMessageBuilder.OK().toMessage();
        LoginContextHolder.setAnonymousCtx();
        String groups = MapUtils.getString(params, "groups");
        if (StringUtils.isEmpty(groups)) return JsonMessageBuilder.OK().toMessage();
        String[] items = StringUtils.split(groups, ',');
        Map<String, Object> data = Maps.newHashMap();
        Stream.of(items).forEach(x -> data.put(x, findByGroup(x, request)));
        return JsonMessageBuilder.OK().withPayload(data).toMessage();
    }

    private List<KvDictDto> findByGroup(String groupName, HttpServletRequest request) {
        Optional<List<KvDictEntity>> dict_opt = getBean(KvDictEntityAction.class, request).findByType(groupName);
        List<KvDictDto> dictDtos = Lists.newArrayList();
        if (dict_opt.isPresent())
            dictDtos = dict_opt.get().stream().map(KvDictEntity::createDto).collect(Collectors.toList());
        return dictDtos;
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("insuranceJdbcQuerySupport", JdbcQuerySupport.class, request);
    }
}
