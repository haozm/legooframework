package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.*;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller(value = "memberController")
@RequestMapping("/member")
public class MemberController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(MemberController.class);

    @RequestMapping(value = "/purchasing/{memberId}/{range}/amount.json")
    @ResponseBody
    public Map<String, Object> loadPurchasingBehavior(@PathVariable int memberId, @PathVariable String range,
                                                      HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadPurchasingBehavior(url=%s)", request.getRequestURL()));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆用户无公司属性....");
        Integer companyId = loginUser.getCompany().get().getId();
        return loadPurchasingBehavior(companyId, memberId, range);
    }

    @RequestMapping(value = "/purchasing/amount.json")
    @ResponseBody
    public Map<String, Object> loadPurchasingBehavior(@RequestBody Map<String, String> requestBody,
                                                      HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadPurchasingBehavior( requestBody = %s)", request.getRequestURI(),
                    requestBody));
        Preconditions.checkArgument(requestBody.containsKey("memberId") && requestBody.containsKey("range"),
                "请求参数不能缺少memberId及range");
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆用户无公司属性....");
        Integer companyId = loginUser.getCompany().get().getId();
        return loadPurchasingBehavior(companyId, MapUtils.getInteger(requestBody, "memberId"),
                MapUtils.getString(requestBody, "range"));
    }

    private Map<String, Object> loadPurchasingBehavior(Integer companyId, Integer memberId, String range) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("memberId", memberId);
        params.put("companyId", companyId);
        Optional<Map<String, Object>> data = queryEngineService.queryForMap("member", "loadPurchasingBehavior", params);
        if (!data.isPresent())
            return wrapperResponse("");
        Map<String, Object> store_data = Maps.newHashMap();
        Map<String, Object> company_data = Maps.newHashMap();
        for (String key : data.get().keySet()) {
            if (StringUtils.startsWith(key, "c_")) {
                company_data.put(key.substring(2), data.get().get(key));
            } else if (StringUtils.startsWith(key, "s_")) {
                store_data.put(key.substring(2), data.get().get(key));
            } else {
                store_data.put(key, data.get().get(key));
                company_data.put(key, data.get().get(key));
            }
        }
        if (StringUtils.equals("store", range)) {
            return wrapperResponse(MapUtils.isEmpty(store_data) ? "" : store_data);
        } else if (StringUtils.equals("company", range)) {
            return wrapperResponse(MapUtils.isEmpty(company_data) ? "" : company_data);
        } else if (StringUtils.equals("all", range)) {
            Map<String, Object> all_data = Maps.newHashMap();
            all_data.put("store", MapUtils.isNotEmpty(store_data) ? store_data : "");
            all_data.put("company", MapUtils.isNotEmpty(company_data) ? company_data : "");
            return wrapperResponse(all_data);
        } else {
            throw new IllegalArgumentException(String.format("非法的入参请求 range= %s 错误，取值范围是 [store,company,all]", range));
        }
    }

    @RequestMapping(value = "/information.json")
    @ResponseBody
    public Map<String, Object> loadMemberInformation(@RequestBody Map<String, String> requestBody,
                                                     HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadMemberInformation( requestBody = %s)", request.getRequestURI(),
                    requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("memberId"), "请求参数不能缺少memberId");
        MemberEntity member = memberAction.loadById(loginUser.getExitsStore(), MapUtils.getInteger(requestBody, "memberId"));
        MemberExtraEntity memberExtra = new MemberExtraEntity(member.getId());
        Optional<MemberExtraEntity> memberExtraOpt = memberExtraAction.findByMember(member);
        if (memberExtraOpt.isPresent())
            memberExtra = memberExtraOpt.get();
        Map<String, Object> result = member.toViewMap();
        result.putAll(memberExtra.toMap());
        return wrapperResponse(result);
    }

    private Date defaultTime(String time) {
        Date date = null;
        try {
            date = DateUtils.parseDate(time, "yyyy-MM-dd");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Preconditions.checkState(date != null, String.format("采用格式[%s]解析时间[%s]错误", "yyyy-MM-dd", time));
        return date;
    }

    @RequestMapping(value = "/update.json")
    @ResponseBody
    public Map<String, Object> updateMember(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]updateMember( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("memberId"), "请求参数不能缺少memberId");
        saveOrUpdateMember(false, requestBody, loginUser);
        saveOrUpdateMemberExtra(loginUser.getExitsStore(), requestBody);
        return wrapperEmptyResponse();
    }

    @RequestMapping(value = "/save.json")
    @ResponseBody
    public Map<String, Object> saveMember(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]saveMember( requestBody = %s)", request.getRequestURI(), requestBody));
        Preconditions.checkArgument(requestBody.containsKey("memberId"), "请求参数不能缺少memberId");
        LoginUserContext loginUser = loadLoginUser(request);
        MemberEntity member = saveOrUpdateMember(true, requestBody, loginUser);
        saveOrUpdateMemberExtra(loginUser.getExitsStore(), requestBody, member);
        Map<String, Object> vo = Maps.newHashMap();
        vo.put("memberId", member.getId());
        return wrapperResponse(vo);
    }

    /**
     * 分配职员给导购
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/assign/shoppingguide.json")
    @ResponseBody
    public Map<String, Object> bildMemnerToShapping(@RequestBody Map<String, String> requestBody,
                                                    HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]bildMemnerToShapping( requestBody = %s)", request.getRequestURI(), requestBody));
        Preconditions.checkArgument(requestBody.containsKey("memberIds"), "请求参数不能缺少 memberIds");
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent());
        String memberIds = MapUtils.getString(requestBody, "memberIds");
        boolean isbuilding = MapUtils.getBooleanValue(requestBody, "isbuilding", true);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(memberIds), "入参 memberIds 不可以为空值...");
        List<Integer> memberIDs = Lists.newArrayList();
        for (String cur : StringUtils.split(memberIds, ',')) memberIDs.add(Integer.valueOf(cur));
        Integer employeeId = MapUtils.getInteger(requestBody, "employeeId");
        if (isbuilding)
            Preconditions.checkNotNull(employeeId, "导购ID不可以为空值...");
        getBean(MemberServer.class, request).buildMembersToShopping(employeeId, memberIDs, isbuilding, loginUser);
        return wrapperEmptyResponse();
    }

    /**
     * 批量设置一批会员 无效 或者有效
     *
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/maked/{effective}/result.json")
    @ResponseBody
    public Map<String, Object> makeMembersUneffective(@RequestBody Map<String, String> requestBody,
                                                      @PathVariable String effective,
                                                      HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]makeMembersUneffective( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent());
        String memberIds = MapUtils.getString(requestBody, "memberId");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(memberIds), "入参 memberId 不可以为空值...");
        List<Integer> memberIDs = Lists.newArrayList();
        for (String cur : StringUtils.split(memberIds, ',')) memberIDs.add(Integer.valueOf(cur));
        if (StringUtils.equalsIgnoreCase("enbaled", effective)) {
            getBean(MemberEntityAction.class, request).makeMembersUneffective(memberIDs, loginUser);
        }
        return wrapperEmptyResponse();
    }

    private MemberEntity saveOrUpdateMember(boolean isSave, Map<String, String> requestBody, LoginUserContext loginUser) {
        Integer memberId = MapUtils.getInteger(requestBody, "memberId");
        String name = MapUtils.getString(requestBody, "name");
        Integer memberType = MapUtils.getInteger(requestBody, "memberType");
        Integer sex = MapUtils.getInteger(requestBody, "sex");
        Integer serviceLevel = MapUtils.getInteger(requestBody, "serviceLevel");
        String phone = MapUtils.getString(requestBody, "phoneNo");
        Integer memberCardType = MapUtils.getInteger(requestBody, "memberCardType");
        String memberCardNum = MapUtils.getString(requestBody, "memberCardNum");
        String createCardTimeStr = MapUtils.getString(requestBody, "createCardTime");
        Date createCardTime = null;
        if (!Strings.isNullOrEmpty(createCardTimeStr)) {
            createCardTime = defaultTime(createCardTimeStr);
        }
        String qqNum = MapUtils.getString(requestBody, "qqNum");
        String weixinId = MapUtils.getString(requestBody, "weixinId");
        String weiboNum = MapUtils.getString(requestBody, "weiboNum");
        String email = MapUtils.getString(requestBody, "email");
        Integer marryStatus = MapUtils.getInteger(requestBody, "marryStatus");
        String detailAddress = MapUtils.getString(requestBody, "detailAddress");
        String idols = MapUtils.getString(requestBody, "idols");
        String carePeople = MapUtils.getString(requestBody, "carePeople");
        Integer zodiac = MapUtils.getInteger(requestBody, "zodiac");
        Integer characterType = MapUtils.getInteger(requestBody, "characterType");
        String jobType = MapUtils.getString(requestBody, "jobType");
        Integer faithType = MapUtils.getInteger(requestBody, "faithType");
        Integer likeContact = MapUtils.getInteger(requestBody, "likeContact");
        Integer calendarType = MapUtils.getInteger(requestBody, "calendarType");
        Date gregorianBirthday = null;
        Date lunarBirthday = null;
        String birthdayStr = MapUtils.getString(requestBody, "birthday");
        if (calendarType == 1) {
            if (!Strings.isNullOrEmpty(birthdayStr)) {
                gregorianBirthday = defaultTime(birthdayStr);
            }
        } else if (calendarType == 2) {
            if (!Strings.isNullOrEmpty(birthdayStr)) {
                lunarBirthday = defaultTime(birthdayStr);
            }
        }
        String iconUrl = MapUtils.getString(requestBody, "iconUrl");
        if (!isSave) {
            return memberServer.modifyMember(loginUser.getExitsStore(), memberId, name, memberType, sex, serviceLevel, phone, memberCardType,
                    memberCardNum, createCardTime, qqNum, weixinId, weiboNum, email, marryStatus, detailAddress, idols,
                    carePeople, zodiac, characterType, jobType, faithType, likeContact, gregorianBirthday,
                    lunarBirthday, iconUrl, calendarType);
        } else {
            Integer createCardStoreId = MapUtils.getInteger(requestBody, "createCardStoreId");
            return memberServer.saveMember(name, memberType, sex, serviceLevel, phone, memberCardType, memberCardNum,
                    createCardTime, createCardStoreId, null, weixinId, qqNum, weiboNum, email, gregorianBirthday,
                    lunarBirthday, calendarType, iconUrl, marryStatus, detailAddress, idols, carePeople, zodiac,
                    characterType, jobType, faithType, likeContact, loginUser);
        }
    }

    private void saveOrUpdateMemberExtra(StoreEntity store, Map<String, String> requestBody) {
        saveOrUpdateMemberExtra(store, requestBody, null);
    }

    private void saveOrUpdateMemberExtra(StoreEntity store, Map<String, String> requestBody, MemberEntity member) {
        if (member == null) member = memberAction.loadById(store, MapUtils.getInteger(requestBody, "memberId"));
        String jacketSize = MapUtils.getString(requestBody, "jacketSize");
        String bottomsSize = MapUtils.getString(requestBody, "bottomsSize");
        String braSize = MapUtils.getString(requestBody, "braSize");
        String briefsSize = MapUtils.getString(requestBody, "briefsSize");
        String shoeSize = MapUtils.getString(requestBody, "shoeSize");
        BigDecimal chest = MapUtils.getString(requestBody, "chest") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "chest"));
        BigDecimal clothingLong = MapUtils.getString(requestBody, "clothingLong") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "clothingLong"));
        BigDecimal sleeveLength = MapUtils.getString(requestBody, "sleeveLength") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "sleeveLength"));
        BigDecimal shoulder = MapUtils.getString(requestBody, "shoulder") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "shoulder"));
        BigDecimal waistline = MapUtils.getString(requestBody, "waistline") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "waistline"));
        BigDecimal hipline = MapUtils.getString(requestBody, "hipline") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "hipline"));
        BigDecimal thighCircumference = MapUtils.getString(requestBody, "thighCircumference") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "thighCircumference"));
        BigDecimal kneeCircumference = MapUtils.getString(requestBody, "kneeCircumference") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "kneeCircumference"));
        BigDecimal trouserLeg = MapUtils.getString(requestBody, "trouserLeg") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "trouserLeg"));
        BigDecimal beforeFork = MapUtils.getString(requestBody, "beforeFork") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "beforeFork"));
        BigDecimal afterFork = MapUtils.getString(requestBody, "afterFork") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "afterFork"));
        BigDecimal outseam = MapUtils.getString(requestBody, "outseam") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "outseam"));
        BigDecimal onChest = MapUtils.getString(requestBody, "onChest") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "onChest"));
        BigDecimal underChest = MapUtils.getString(requestBody, "underChest") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "underChest"));
        BigDecimal footLength = MapUtils.getString(requestBody, "footLength") == null ? null
                : new BigDecimal(MapUtils.getString(requestBody, "footLength"));

        memberExtraAction.saveOrUpdateMemberExtra(member, jacketSize, bottomsSize, braSize, briefsSize, shoeSize, chest,
                clothingLong, sleeveLength, shoulder, waistline, hipline, thighCircumference, kneeCircumference,
                trouserLeg, beforeFork, afterFork, outseam, onChest, underChest, footLength);
    }
    
    @Resource
    private MemberExtraEntityAction memberExtraAction;
    @Resource
    private MemberEntityAction memberAction;
    @Resource(name = "queryEngineService")
    private QueryEngineService queryEngineService;
    @Resource
    private MemberServer memberServer;
}
