package com.legooframework.model.templatemgs.mvc;

import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.core.web.TreeNode;
import com.legooframework.model.templatemgs.service.TemplateMgnService;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping(value = "/template")
public class TemplateMgnController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateMgnController.class);

    @PostMapping(value = "/load/classify-tree.json")
    public JsonMessage loadClassifyTree(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContextHolder.setAnonymousCtx();
        Integer companyId = MapUtils.getInteger(requestBody, "companyId");
        TreeNode treeNode = getBean(TemplateMgnService.class, request).loadTreeNodeByCompany(companyId);
        return JsonMessageBuilder.OK().withPayload(treeNode).toMessage();
    }

}
