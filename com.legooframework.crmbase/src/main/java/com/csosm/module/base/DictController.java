package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.KvDictEntity;
import com.csosm.module.base.entity.KvDictEntityAction;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller(value = "dictController")
@RequestMapping("/dict")
public class DictController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DictController.class);

    // 获取指定的字典类型参数
    @RequestMapping(value = "/load/bytypes.json")
    @ResponseBody
    public Map<String, Object> loadDictByTypes(@RequestBody(required = false) Map<String, String> http_request_map,
                                               HttpServletRequest request) {
        LoginUserContext user = loadLoginUser(request);
        Preconditions.checkState(user.getCompany().isPresent(), "当前登陆用户无租户信息...");
        String types = MapUtils.getString(http_request_map, "types");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(types), "缺少参数 types ....");
        Map<String, List<KvDictEntity>> res = getBean(KvDictEntityAction.class, request).loadByTypes(user.getCompany().get(),
                StringUtils.split(types, ','));
        return wrapperResponse(res);
    }

}
