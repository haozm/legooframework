package com.csosm.module.labels;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.csosm.module.labels.entity.LabelNodeAction;
import com.csosm.module.labels.entity.LabelNodeEntity;
import com.csosm.module.labels.entity.TxtLabelDto;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 短信发送相关 管理WEB-API 服务输出
@Controller(value = "labekMvcController")
@RequestMapping(value = "/label")
public class LabekMvcController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LabekMvcController.class);

    @RequestMapping(value = "/types/list.json")
    @ResponseBody
    public Map<String, Object> loadTypesByCompany(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyId", loginUser.getCompany().isPresent() ? loginUser.getCompany().get().getId() : -1);
        Optional<List<Map<String, Object>>> list = queryEngine(request)
                .queryForList("LabelNode", "loadTypesByCompany", params);
        if (list.isPresent()) {
            List<Map<String, Object>> tree_nodes = Lists.newArrayList();
            for (Map<String, Object> $it : list.get()) {
                Map<String, Object> data = Maps.newHashMap();
                data.put("id", MapUtils.getObject($it, "id"));
                data.put("label", MapUtils.getObject($it, "labelName"));
                data.put("att", $it);
                tree_nodes.add(data);
            }
            return wrapperResponse(tree_nodes);
        }
        return wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/txt/add.json")
    @ResponseBody
    public Map<String, Object> addTxtLabelByCompany(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "登陆用户所属公司为空...");
        Long pid = MapUtils.getLong(requestBody, "pId");
        Preconditions.checkNotNull(pid, "入参 pid 不可以为空值...");
        String name = MapUtils.getString(requestBody, "labelName");
        String desc = MapUtils.getString(requestBody, "labelDesc");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name 不可以为空值...");
        Long id = labelNodeAction(request).insertTxtLabel(pid, loginUser.getCompany().get(), null, name, desc);
        return wrapperResponse(id);
    }

    @RequestMapping(value = "/store/add.json")
    @ResponseBody
    public Map<String, Object> addTxtLabelByStore(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getStore().isPresent(), "登陆用户所属门店为空...");
        String name = MapUtils.getString(requestBody, "labelName");
        String desc = MapUtils.getString(requestBody, "labelDesc");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参name 不可以为空值...");
        Long id = labelNodeAction(request).addStoreLabel(loginUser.getStore().get(), name, desc);
        return wrapperResponse(id);
    }

    @RequestMapping(value = "/store/list.json")
    @ResponseBody
    public Map<String, Object> loadTxtLabelByStore(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), null));
        LoginUserContext loginUser = loadLoginUser(request);
        if (!loginUser.getStore().isPresent()) {
            if (logger.isWarnEnabled())
                logger.warn(String.format("登陆用户%s所属门店为空...,无门店标签数据...", loginUser.getUsername()));
            return wrapperResponse(new String[0]);
        }
        Optional<List<LabelNodeEntity>> labels = labelNodeAction(request).loadEnabledByStore(loginUser.getStore().get());
        if (!labels.isPresent()) return wrapperResponse(new String[0]);
        List<Map<String, Object>> datas = Lists.newArrayList();

        for (LabelNodeEntity $it : labels.get()) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", $it.getId());
            map.put("labelName", $it.getName());
            datas.add(map);
        }
        return wrapperResponse(datas);
    }

    @RequestMapping(value = "/sub/list.json")
    @ResponseBody
    public Map<String, Object> loadSublist(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Long pid = MapUtils.getLong(requestBody, "pId");
        Preconditions.checkNotNull(pid, "入参 pid 不可以为空值...");
        Map<String, Object> params = Maps.newHashMap();
        params.put("pId", pid);
        params.put("companyId", loginUser.getCompany().isPresent() ? loginUser.getCompany().get().getId() : -1);
        Optional<List<Map<String, Object>>> list = queryEngine(request)
                .queryForList("LabelNode", "loadSubList", params);
        //TODO 将 labelEnbale 0:1 转换为true 或 false
        if(list.isPresent()) {
        	list.get().stream().forEach(x -> {
        		Integer enable = MapUtils.getInteger(x, "labelEnbale");
        		x.put("labelEnbale", 1 == enable ? true:false);
        	});
        }
        return wrapperResponse(list.isPresent() ? list.get() : new String[0]);
    }

    /**
     * 获取常用的标签信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/usage/list.json")
    @ResponseBody
    public Map<String, Object> loadUsagelist(HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUsagelist [%s]", request.getRequestURI()));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getStore().isPresent(), "登录用户无门店信息");
        Preconditions.checkState(loginUser.getCompany().isPresent(), "登录用户无公司信息");
        List<Map<String, Object>> datas = Lists.newArrayList();
        Optional<List<LabelNodeEntity>> storelabels = labelNodeAction(request).loadEnabledByStore(loginUser.getStore().get());
        if (storelabels.isPresent()) {
            for (LabelNodeEntity $it : storelabels.get()) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("labelId", $it.getId());
                map.put("labelName", $it.getName());
                datas.add(map);
            }
        }
        Optional<List<LabelNodeEntity>> companyLabels = labelNodeAction(request).loadEnabledByCompany(loginUser.getCompany().get());
        if (companyLabels.isPresent()) {
            for (LabelNodeEntity $it : companyLabels.get()) {
                if ($it.getpId() == 100L || $it.getpId() == 200L) continue;
                Map<String, Object> map = Maps.newHashMap();
                map.put("labelId", $it.getId());
                map.put("labelName", $it.getName());
                datas.add(map);
            }
        }
        return wrapperResponse(datas);
    }

    @RequestMapping(value = "/com/enabled/list.json")
    @ResponseBody
    public Map<String, Object> loadEnabledLables(HttpServletRequest request) {
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前用户无公司信息，无法执行该查询...");
        Optional<List<LabelNodeEntity>> labels = labelNodeAction(request).loadEnabledByCompany(loginUser.getCompany().get());
        if (labels.isPresent()) {
            List<TxtLabelDto> dtos = Lists.newArrayListWithCapacity(labels.get().size());
            for (LabelNodeEntity $it : labels.get()) {
                if ($it.getId() <= 999999 && $it.getId() >= 100000) dtos.add($it.getTxtLabelDto());
            }
            for (TxtLabelDto $p : dtos) {
                for (LabelNodeEntity $it : labels.get()) {
                    if ($it.getpId().equals($p.getId())) $p.addChild($it.getTxtLabelDto());
                }
            }
            return wrapperResponse(dtos);
        }
        return wrapperResponse(new String[0]);
    }

    @RequestMapping(value = "/edit.json")
    @ResponseBody
    public Map<String, Object> disabledLable(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Long id = MapUtils.getLong(requestBody, "id");
        Preconditions.checkNotNull(id, "入参 id 不可以为空值...");
        String name = MapUtils.getString(requestBody, "labelName");
        String desc = MapUtils.getString(requestBody, "labelDesc");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(name), "入参 labelName 不可以为空值...");
        labelNodeAction(request).change(id, name, desc, user);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/status/switch.json")
    @ResponseBody
    public Map<String, Object> switchStatus(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        Long id = MapUtils.getLong(requestBody, "id");
        Preconditions.checkNotNull(id, "入参 id 不可以为空值...");
        String status = MapUtils.getString(requestBody, "status");
        Preconditions.checkArgument(StringUtils.equals(status, "true") || StringUtils.equals(status, "false"),
                "状态 status =%s 取值错误，合法取值范围为(true,false)...");
        LoginUserContext user = loadLoginUser(request);
        if (StringUtils.equals("true", status)) {
            getBean(LabelNodeAction.class, request).enabled(id, user);
        } else if (StringUtils.equals("false", status)) {
            getBean(LabelNodeAction.class, request).disbaled(id, user);
        }
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/delete.json")
    @ResponseBody
    public Map<String, Object> deleteLable(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext user = loadLoginUser(request);
        Long id = MapUtils.getLong(requestBody, "id");
        Preconditions.checkNotNull(id, "入参 id 不可以为空值...");
        labelNodeAction(request).delete(id, user);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/batch/remark.json")
    @ResponseBody
    public Map<String, Object> remarkLabels(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        //  markLables(Long labelId, Set<Integer> memberIds, Set<String> weixinIds, LoginUserContext loginUser)
        Long labelId = MapUtils.getLong(requestBody, "labelId");
        Preconditions.checkNotNull(labelId, "标签ID不可以为空值...");
        String args_memberIds = MapUtils.getString(requestBody, "memberIds");
        String args_weixinIds = MapUtils.getString(requestBody, "weixinIds");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(args_memberIds) || !Strings.isNullOrEmpty(args_weixinIds),
                "需指定需要标签的会员列表或者微信用户列表...");
        Set<Integer> memberIds = Sets.newHashSet();
        Set<String> weixinIds = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(args_memberIds)) {
            for (String $it : StringUtils.split(args_memberIds, ',')) memberIds.add(Integer.valueOf($it));
        }
        if (!Strings.isNullOrEmpty(args_weixinIds)) {
            weixinIds.addAll(Arrays.asList(StringUtils.split(args_weixinIds, ',')));
        }
        getBean(LabelMarkedService.class, request).markLables(labelId, memberIds, weixinIds, loginUser);
        return wrapperResponse(null);
    }


    @RequestMapping(value = "/single/remove.json")
    @ResponseBody
    public Map<String, Object> removeLabels(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        //  removeLabel(Long labelId, Integer memberId, String weixinId, LoginUserContext loginUser)
        Long labelId = MapUtils.getLong(requestBody, "labelId");
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Preconditions.checkArgument(null != memberId || !Strings.isNullOrEmpty(weixinId),
                "需指定需要标签的会员或者微信用户...");
        getBean(LabelMarkedService.class, request).removeLabel(labelId, memberId, weixinId, loginUser);
        return wrapperResponse(null);
    }

    @RequestMapping(value = "/load/byuser.json")
    @ResponseBody
    public Map<String, Object> loadLabelsByUser(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]->reqMap=%s", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        StoreEntity store = loginUser.getExitsStore();
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId", -1);

        if (storeId != -1) {
            Optional<StoreEntity> str_opt = getBean(StoreEntityAction.class, request).findById(storeId);
            Preconditions.checkState(str_opt.isPresent(), "ID=%s 对应的门店不存在...", storeId);
            store = str_opt.get();
        }

        Preconditions.checkArgument(null != memberId || !Strings.isNullOrEmpty(weixinId),
                "需指定需要标签的会员或者微信用户...");
        Map<String, Object> params = loginUser.toMap();
        params.put("storeId", store.getId());
        params.put("memberId", memberId);
        params.put("weixinId", weixinId);
        Optional<List<Map<String, Object>>> list = queryEngine(request).queryForList("LabelNode", "findByUser", params);
        return wrapperResponse(list.isPresent() ? list.get() : new String[0]);
    }

    private QueryEngineService queryEngine(HttpServletRequest request) {
        return getBean("queryEngineService", QueryEngineService.class, request);
    }

    private LabelNodeAction labelNodeAction(HttpServletRequest request) {
        return getBean("userLabelNodeAction", LabelNodeAction.class, request);
    }

}
