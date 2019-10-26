package com.legooframework.model.insurance.mvc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.insurance.entity.MemberEntity;
import com.legooframework.model.insurance.entity.MemberEntityAction;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/member")
public class MemberServiceController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MemberServiceController.class);

    @PostMapping(value = "/find/bycardid.json")
    public JsonMessage findByCardID(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("findByCardID(%s)", requestBody));
        LoginContextHolder.setAnonymousCtx();
        String cardID = MapUtils.getString(requestBody, "cardID");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(cardID), "参数 身份证ID 不可以为空值....");
        Optional<MemberEntity> member = getBean(MemberEntityAction.class, request).findByIDCard(cardID);
        return JsonMessageBuilder.OK().withPayload(member.map(MemberEntity::toViewMap).orElse(null)).toMessage();
    }

}
