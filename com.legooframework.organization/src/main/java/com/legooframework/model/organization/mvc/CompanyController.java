package com.legooframework.model.organization.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.commons.dto.TreeStructure;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.organization.entity.StoreTreeAction;
import com.legooframework.model.organization.entity.StoreTreeEntity;
import com.legooframework.model.organization.service.CompanyService;
import com.legooframework.model.organization.service.StoreService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController(value = "orgmsgController")
@RequestMapping(value = "/org")
public class CompanyController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);

    @PostMapping(value = "/add/store/action.json")
    public JsonMessage addStore(@RequestBody Map<String, String> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        LoginContext loginUser = getLoginContext();
//        public Long addStore(Long companyId, String storeCode, String fullName, String shortName,
//                String businessLicense, String detailAddress, String legalPerson,
//                String contactNumber, String remark, String storeType)
        String storeCode = MapUtils.getString(datas, "orgCode", null);
        Preconditions.checkArgument(StringUtils.isNotBlank(storeCode), "门店编码(orgCode)不可以为空值.");
        String fullName = MapUtils.getString(datas, "storeFullName", null);
        Preconditions.checkArgument(StringUtils.isNotBlank(fullName), "门店全程(storeFullName)不可以为空值.");
        Long storeId = getBean(CompanyService.class, request).addStore(loginUser.getTenantId(),
                storeCode, fullName, MapUtils.getString(datas, "storeShortName", fullName),
                MapUtils.getString(datas, "storeBusinessLicense", null),
                MapUtils.getString(datas, "storeDetailAddress", null),
                MapUtils.getString(datas, "storeLegalPerson", null),
                MapUtils.getString(datas, "storeContactNumber", null),
                MapUtils.getString(datas, "storeRemark", null),
                MapUtils.getString(datas, "storeType", null));
        return JsonMessageBuilder.OK().withPayload(storeId).toMessage();
    }

    @RequestMapping(value = "/all/store/tree.json")
    public JsonMessage loadAllStoreTree(HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] ", request.getRequestURI()));
        LoginContext loginUser = getLoginContext();
        TreeStructure treeRoot = getBean(StoreService.class, request).loadAllStoreTree(loginUser.getTenantId());
        return JsonMessageBuilder.OK().withPayload(new Object[]{treeRoot.toMap()}).toMessage();
    }

    @PostMapping(value = "/add/node/tree.json")
    public JsonMessage addTreeNode(@RequestBody Map<String, String> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        LoginContext loginUser = getLoginContext();
        Long pid = MapUtils.getLong(datas, "pid");
        Preconditions.checkNotNull(pid, "待新增节点的上级节点pid不可以为空...");
        String nodeName = MapUtils.getString(datas, "label");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(nodeName), "节点名称 label 不可以为空...");
        int nodeSeq = MapUtils.getIntValue(datas, "nodeSeq", 0);
        StoreTreeEntity entity = getBean(StoreService.class, request).addTreeNode(pid, nodeName, nodeSeq, loginUser);
        return JsonMessageBuilder.OK().withPayload(entity.getTreeNode().toMap()).toMessage();
    }

    @PostMapping(value = "/remove/node/tree.json")
    public JsonMessage removeTreeNode(@RequestBody Map<String, String> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        getLoginContext();
        Long id = MapUtils.getLong(datas, "id");
        Preconditions.checkNotNull(id, "待删除的节点id不可以为空...");
        getBean(StoreTreeAction.class, request).removeNode(id);
        return JsonMessageBuilder.OK().toMessage();
    }

    @PostMapping(value = "/add/store/tree.json")
    public JsonMessage addStoreToTreeNode(@RequestBody Map<String, String> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        getLoginContext();
        Long pid = MapUtils.getLong(datas, "pid");
        Preconditions.checkNotNull(pid, "待新增节点的上级节点pid不可以为空...");
        String storeIds = MapUtils.getString(datas, "storeIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "入参 storeIds 不允许为空...");
        Set<Long> storeid_set = Stream.of(StringUtils.split(storeIds, ','))
                .map(Long::valueOf).collect(Collectors.toSet());
        boolean res = getBean(StoreService.class, request).addStoreToTree(pid, storeid_set.toArray(new Long[]{}));
        return res ? JsonMessageBuilder.OK().toMessage() : JsonMessageBuilder.ERROR("已经存在对应的门店").toMessage();
    }

    @PostMapping(value = "/remove/store/tree.json")
    public JsonMessage remopveStoreFromTreeNode(@RequestBody Map<String, String> datas, HttpServletRequest request) throws Exception {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[URI = %s] RequestBody->%s", request.getRequestURI(), datas));
        getLoginContext();
        Long pid = MapUtils.getLong(datas, "pid");
        Preconditions.checkNotNull(pid, "待删除门店的上级节点pid不可以为空...");
        String storeIds = MapUtils.getString(datas, "storeIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(storeIds), "入参 storeIds 不允许为空...");
        Set<Long> storeid_set = Stream.of(StringUtils.split(storeIds, ','))
                .map(Long::valueOf).collect(Collectors.toSet());
        getBean(StoreService.class, request).removeStoreFromTree(pid, storeid_set.toArray(new Long[]{}));
        return JsonMessageBuilder.OK().toMessage();
    }

}
