package com.legooframework.model.smsgateway.mvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DictController extends SmsBaseController {

    private static final Logger logger = LoggerFactory.getLogger(DictController.class);

//    @PostMapping(value = "/single/detail.json")
//    public JsonMessage getDictByType(@RequestBody(required = false) Map<String, String> params,
//                                     HttpServletRequest request) {
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("[URI = %s] And RequestBody = %s", request.getRequestURI(), params));
//        LoginContextHolder.setAnonymousCtx();
//        try {
//            String group = MapUtils.getString(params, "group");
//            if (StringUtils.isEmpty(group)) return JsonMessageBuilder.OK().toMessage();
//            List<KvDictDto> list = findByGroup(group, request);
//            return JsonMessageBuilder.OK().withPayload(list).toMessage();
//        } finally {
//            LoginContextHolder.clear();
//        }
//    }
//
//    @PostMapping(value = "/groups/detail.json")
//    public JsonMessage getDictByGroups(@RequestBody(required = false) Map<String, String> params,
//                                       HttpServletRequest request) {
//        if (logger.isDebugEnabled())
//            logger.debug(String.format("[URI = %s] And RequestBody = %s", request.getRequestURI(), params));
//        if (MapUtils.isEmpty(params)) return JsonMessageBuilder.OK().toMessage();
//        LoginContextHolder.setAnonymousCtx();
//        try {
//            String groups = MapUtils.getString(params, "groups");
//            if (StringUtils.isEmpty(groups)) return JsonMessageBuilder.OK().toMessage();
//            String[] items = StringUtils.split(groups, ',');
//            Map<String, Object> data = Maps.newHashMap();
//            Stream.of(items).forEach(x -> data.put(x, findByGroup(x, request)));
//            return JsonMessageBuilder.OK().withPayload(data).toMessage();
//        } finally {
//            LoginContextHolder.clear();
//        }
//    }

//    private List<KvDictDto> findByGroup(String groupName, HttpServletRequest request) {
//        Optional<List<KvDictEntity>> dict_opt = getBean(KvDictEntityAction.class, request).findByType(groupName);
//        List<KvDictDto> dictDtos = Lists.newArrayList();
//        if (dict_opt.isPresent())
//            dictDtos = dict_opt.get().stream().map(KvDictEntity::createDto).collect(Collectors.toList());
//        return dictDtos;
//    }

}
