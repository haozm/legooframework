package com.csosm.module.labels;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.labels.entity.UserRemarksAction;
import com.csosm.module.labels.entity.UserRemarksEntity;
import com.csosm.module.query.QueryEngineService;
import com.csosm.module.webchat.WechatFriendService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

// 短信发送相关 管理WEB-API 服务输出
@Controller(value = "remarksMvcController")
@RequestMapping(value = "/remaker")
public class RemarksMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(RemarksMvcController.class);

    @RequestMapping(value = "/byuser/list.json")
    @ResponseBody
    public Map<String, Object> loadRemarksByUser(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息...");
        String weixinId = Strings.emptyToNull(MapUtils.getString(requestBody, "weixinId"));
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        Preconditions.checkState(weixinId != null || memberId != null, "weixinId 或者 memberId 必须指定其中一个参数");
        Map<String, Object> params = Maps.newHashMap();
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);
        StoreEntity store = loginUser.getStore().orNull();

        if (storeId != -1) {
            Optional<StoreEntity> str_opt = getBean(StoreEntityAction.class, request).findById(storeId);
            Preconditions.checkState(str_opt.isPresent(), "ID=%s 对应的门店不存在...", storeId);
            store = str_opt.get();
        }

        params.put("storeId", store == null ? -1 : store.getId());
        params.put("memberIds", null);
        params.put("weixinIds", null);
        if (memberId != null) {
            params.put("memberIds", new Integer[]{memberId});
        } else {
            params.put("weixinIds", new String[]{weixinId});
        }
        Optional<List<Map<String, Object>>> list = queryEngine(request).queryForList("UserRemarks", "findByWeixinOrMember", params);
        return wrapperResponse(list.isPresent() ? list.get() : new String[0]);
    }

    @RequestMapping(value = "/byuser/mark.json")
    @ResponseBody
    public Map<String, Object> markAnyUser(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息...");
        String weixinId = MapUtils.getString(requestBody, "weixinId", null);
        Integer memberId = MapUtils.getInteger(requestBody, "memberId", null);
        String remarks = MapUtils.getString(requestBody, "remarks");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarks), "用户备注不可以为空值...");
        UserRemarksEntity marks = getBean(LabelMarkedService.class, request).addRemarks(loginUser, memberId, weixinId, remarks);
        return wrapperResponse(marks.toMap());
    }

    @RequestMapping(value = "/byuser/remove.json")
    @ResponseBody
    public Map<String, Object> removeMarker(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        loadLoginUser(request);
        String remarksId = MapUtils.getString(requestBody, "ids");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(remarksId), "待删除的备注ids不可以为空...");
        List<Long> ids = Lists.newArrayList();
        for (String $it : StringUtils.split(remarksId, ',')) ids.add(Long.valueOf($it));
        userRemarksAction(request).removeRemarks(ids);
        return wrapperResponse(null);
    }
    
    /**
     * 认领并打备注
     * @param http_request_map
     * @param request
     * @return
     */
    @RequestMapping(value = "/remarkAndSign.json")
    @ResponseBody
    public Map<String, Object> remarkAndSign(@RequestBody Map<String, String> http_request_map,
                                             HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登陆用户无门店信息...");
        String weixinId = MapUtils.getString(http_request_map, "weixinId", null);
        String remarks = MapUtils.getString(http_request_map, "remarks", null);
        Integer employeeId = MapUtils.getInteger(http_request_map, "employeeId", null);
        getBean(LabelMarkedService.class, request).remarkAndSign(loginUser, employeeId, weixinId, remarks);
        return wrapperEmptyResponse();
    }

    private QueryEngineService queryEngine(HttpServletRequest request) {
        return getBean("queryEngineService", QueryEngineService.class, request);
    }

    private UserRemarksAction userRemarksAction(HttpServletRequest request) {
        return getBean("userRemarksAction", UserRemarksAction.class, request);
    }

}
