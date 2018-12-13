package com.legooframework.model.dict.mvc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController(value = "dictController")
@RequestMapping(value = "/dict")
public class DictController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DictController.class);

    @GetMapping(value = "/single/detail.json")
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

    @GetMapping(value = "/groups/detail.json")
    public JsonMessage getDictByGroups(@RequestBody(required = false) Map<String, String> params,
                                       HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] And RequestBody = %s", request.getRequestURI(), params));
        if (MapUtils.isEmpty(params)) return JsonMessageBuilder.OK().toMessage();
        getLoginContext();
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

}
