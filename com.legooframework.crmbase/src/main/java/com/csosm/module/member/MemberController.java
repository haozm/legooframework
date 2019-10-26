package com.csosm.module.member;

import java.util.List;

import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.member.entity.MemberEntity;
import com.csosm.module.member.entity.MemberEntityAction;
import com.csosm.module.member.entity.MemberExtraEntity;
import com.csosm.module.member.entity.MemberExtraEntityAction;
import com.csosm.module.query.QueryEngineService;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.crmadapter.entity.SmsGatewayProxyAction;
import com.legooframework.model.membercare.entity.BusinessType;

@Controller(value = "memberController")
@RequestMapping("/member")
public class MemberController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
	/**
	 * 新增简单的会员信息
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/simple/save.json")
	@ResponseBody
	public Map<String, Object> saveSimpleMember(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(
					String.format("[%s]saveSimpleMember( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		Map<String, Object> view = Maps.newHashMap();
		if ("web".equals(channel) || "app".equals(channel)) {
			Integer result = memberServer.saveSimpleMember(loginUser, handler.checkAndGetName(),
					handler.checkAndGetSex(), handler.getMobilePhone(), handler.getServiceLevel(),
					handler.getEmployeeId());
			view.put("memberId", result);
		}
		return wrapperResponse(view);
	}

	
	/**
	 * 批量禁止会员
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/disable.json")
	@ResponseBody
	public Map<String, Object> disableMembers(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]disableMembers( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberAction.disableMembers(loginUser.getExitsStore(), handler.checkAndGetMemberIds());
		return wrapperEmptyResponse();
	}
	/**
	 * 批量启用会员
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/enable.json")
	@ResponseBody
	public Map<String, Object> enableMembers(@RequestBody Map<String, Object> requestBody, @PathVariable String channel,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]enableMembers( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberAction.enableMembers(loginUser.getExitsStore(), handler.checkAndGetMemberIds());
		return wrapperEmptyResponse();
	}

	/**
	 * 批量将会员公历生日转换为农历
	 * 
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/toLunar.json")
	@ResponseBody
	public Map<String, Object> toLunarMembers(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]toLunarMembers( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberAction.toLunarBirthDay(loginUser.getExitsStore(), handler.checkAndGetMemberIds());
		return wrapperEmptyResponse();
	}

	/**
	 * 批量标记手机有效
	 * 
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/mobilephone/active.json")
	@ResponseBody
	public Map<String, Object> activeMembersMobilePhone(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]activeMembersMobilePhone( requestBody = %s)", request.getRequestURI(),
					requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberAction.activateMobilePhone(loginUser.getExitsStore(), handler.checkAndGetMemberIds());
		return wrapperEmptyResponse();
	}

	/**
	 * 将会员分配给导购
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/employee/allot.json")
	@ResponseBody
	public Map<String, Object> allotMembersToEmployee(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]allotMembersToEmployee( requestBody = %s)", request.getRequestURI(),
					requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberServer.allotMembersToEmployee(loginUser, handler.checkAndGetMemberIds(),
					handler.checkAndGetEmployeeId());
		return wrapperEmptyResponse();
	}

	/**
	 * 解除导购会员分配
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/employee/deallocate.json")
	@ResponseBody
	public Map<String, Object> deallocateMembers(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]allotMembersToEmployee( requestBody = %s)", request.getRequestURI(),
					requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		if ("web".equals(channel) || "app".equals(channel))
			memberAction.deallocate(loginUser.getExitsStore(), handler.checkAndGetMemberIds());
		return wrapperEmptyResponse();
	}
	/**
	 * 编辑会员信息
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/{type}/replace.json")
	@ResponseBody
	public Map<String, Object> replaceMemberItem(@RequestBody Map<String, Object> requestBody,
			@PathVariable String channel, @PathVariable String type, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(
					String.format("[%s]replaceMemberItem( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		TransactionStatus startTx = startTx("replaceMemberItem");
		try {
			if ("web".equals(channel) || "app".equals(channel)) {
				if ("base".equals(type)) {
					if (handler.getOptMemberId().isPresent()) {
						memberAction.editMemberBase(loginUser, handler.checkAndGetMemberId(), handler.checkAndGetName(),
								handler.checkAndGetSex(), handler.getOptIconUrl(), handler.getOptNamePinyin(),
								handler.getOptBirthday(), handler.getOptLunarBirthday(), handler.getOptCalendarType(),
								handler.getOptMemberType(), handler.getOptServiceLevel(), handler.getOptEmail(),
								handler.getOptMobilephone(), handler.getOptTelephone(), handler.getOptQQNum(),
								handler.getOptWeiboNum(), handler.getOptWeixinId());
					} else {
						Integer memberId = memberServer.saveSimpleMember(loginUser, handler.checkAndGetName(),
								handler.checkAndGetSex(), handler.getMobilePhone(), handler.getServiceLevel(),
								handler.getEmployeeId());
						memberAction.editMemberBase(loginUser, memberId, handler.checkAndGetName(),
								handler.checkAndGetSex(), handler.getOptIconUrl(), handler.getOptNamePinyin(),
								handler.getOptBirthday(), handler.getOptLunarBirthday(), handler.getOptCalendarType(),
								handler.getOptMemberType(), handler.getOptServiceLevel(), handler.getOptEmail(),
								handler.getOptMobilephone(), handler.getOptTelephone(), handler.getOptQQNum(),
								handler.getOptWeiboNum(), handler.getOptWeixinId());
						Map<String,Object> result = Maps.newHashMap();
						result.put("memberId", memberId);
						return wrapperResponse(result);
					}
				}
				if ("card".equals(type)) {
					memberAction.editMemberCard(loginUser.getExitsStore(), handler.checkAndGetMemberId(),
							handler.getOptMemberCardType(), handler.getOptMemberCardNum(),handler.getOptCreateCardTime());
				}
				if ("addition".equals(type)) {
					memberAction.editMemberAddition(loginUser.getExitsStore(), handler.checkAndGetMemberId(),
							handler.getOptMarryStatus(), handler.getOptIdols(), handler.getOptCarePeople(),
							handler.getOptZodiac(), handler.getOptCharacterType(), handler.getOptJobType(),
							handler.getOptFaithType(), handler.getOptHobby(), handler.getOptLikeBrand(),
							handler.getOptLikeContact(), handler.getOptSpecialDay(), handler.getOptEducation(),
							handler.getOptLikeContactTime(), handler.getOptCertificate(),
							handler.getOptCertificateType(), handler.getOptDetailAddress());
				}
				if ("body".equals(type) || "clothes".equals(type)) {
					memberAction.editMemberExtra(loginUser.getExitsStore(), handler.checkAndGetMemberId(),
							handler.getOptJacketSize(), handler.getOptBottomsSize(), handler.getOptBraSize(),
							handler.getOptBriefsSize(), handler.getOptShoeSize(), handler.getOptChest(),
							handler.getOptClothingLong(), handler.getOptSleeveLength(), handler.getOptShoulder(),
							handler.getOptWaistline(), handler.getOptHipline(), handler.getOptThighCircumference(),
							handler.getOptKneeCircumference(), handler.getOptTrouserLeg(), handler.getOptBeforeFork(),
							handler.getOptAfterFork(), handler.getOptOutseam(), handler.getOptOnChest(),
							handler.getOptUnderChest(), handler.getOptFootLength());
				}
			}
		} catch (Exception e) {
			rollbackTx(startTx);
			throw new RuntimeException(e);
		}
		commitTx(startTx);
		return wrapperEmptyResponse();
	}
	/**
	 * 新增或修改会员信息
	 * @param requestBody
	 * @param channel
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{channel}/replace.json")
	@ResponseBody
	public Map<String, Object> replaceMember(@RequestBody Map<String, Object> requestBody, @PathVariable String channel,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]replaceMember( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		TransactionStatus startTx = startTx("replaceMember");
		Integer memberId = null;
		try {
			if ("web".equals(channel) || "app".equals(channel)) {
				if (!handler.getOptMemberId().isPresent()) {
					memberId = memberServer.saveSimpleMember(loginUser, handler.checkAndGetName(),
							handler.checkAndGetSex(), handler.getMobilePhone(), handler.getServiceLevel(),
							handler.getEmployeeId());
				} else {
					memberId = handler.checkAndGetMemberId();
				}
				Preconditions.checkArgument(null != memberId, "新建会员失败");
				memberAction.editMemberBase(loginUser, memberId, handler.checkAndGetName(), handler.checkAndGetSex(),
						handler.getOptIconUrl(), handler.getOptNamePinyin(), handler.getOptBirthday(),
						handler.getOptLunarBirthday(), handler.getOptCalendarType(), handler.getOptMemberType(),
						handler.getOptServiceLevel(), handler.getOptEmail(), handler.getOptMobilephone(),
						handler.getOptTelephone(), handler.getOptQQNum(), handler.getOptWeiboNum(),
						handler.getOptWeixinId());
				memberAction.editMemberCard(loginUser.getExitsStore(), memberId, handler.getOptMemberCardType(),
						handler.getOptMemberCardNum(),handler.getOptCreateCardTime());
				memberAction.editMemberAddition(loginUser.getExitsStore(), memberId, handler.getOptMarryStatus(),
						handler.getOptIdols(), handler.getOptCarePeople(), handler.getOptZodiac(),
						handler.getOptCharacterType(), handler.getOptJobType(), handler.getOptFaithType(),
						handler.getOptHobby(), handler.getOptLikeBrand(), handler.getOptLikeContact(),
						handler.getOptSpecialDay(), handler.getOptEducation(), handler.getOptLikeContactTime(),
						handler.getOptCertificate(), handler.getOptCertificateType(), handler.getOptDetailAddress());
				memberAction.editMemberExtra(loginUser.getExitsStore(), memberId, handler.getOptJacketSize(),
						handler.getOptBottomsSize(), handler.getOptBraSize(), handler.getOptBriefsSize(),
						handler.getOptShoeSize(), handler.getOptChest(), handler.getOptClothingLong(),
						handler.getOptSleeveLength(), handler.getOptShoulder(), handler.getOptWaistline(),
						handler.getOptHipline(), handler.getOptThighCircumference(), handler.getOptKneeCircumference(),
						handler.getOptTrouserLeg(), handler.getOptBeforeFork(), handler.getOptAfterFork(),
						handler.getOptOutseam(), handler.getOptOnChest(), handler.getOptUnderChest(),
						handler.getOptFootLength());
				memberAction.editMemberExtra(loginUser.getExitsStore(), memberId, handler.getOptJacketSize(),
						handler.getOptBottomsSize(), handler.getOptBraSize(), handler.getOptBriefsSize(),
						handler.getOptShoeSize(), handler.getOptChest(), handler.getOptClothingLong(),
						handler.getOptSleeveLength(), handler.getOptShoulder(), handler.getOptWaistline(),
						handler.getOptHipline(), handler.getOptThighCircumference(), handler.getOptKneeCircumference(),
						handler.getOptTrouserLeg(), handler.getOptBeforeFork(), handler.getOptAfterFork(),
						handler.getOptOutseam(), handler.getOptOnChest(), handler.getOptUnderChest(),
						handler.getOptFootLength());
			}
		} catch (Exception e) {
			rollbackTx(startTx);
			throw new RuntimeException(e);
		}
		commitTx(startTx);
		Map<String,Object> result = Maps.newHashMap();
		result.put("memberId", memberId);
		return wrapperResponse(result);
	}

	/*
	 * 获取会员扩展信息
	 */
	@RequestMapping(value = "/extra/map.json")
	@ResponseBody
	public Map<String, Object> loadMemberExtra(@RequestBody Map<String, Object> requestBody,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]loadMemberExtra( requestBody = %s)", request.getRequestURI(), requestBody));
		LoginUserContext loginUser = loadLoginUser(request);
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		MemberEntity member = memberAction.loadMemberById(loginUser.getExitsStore(), handler.checkAndGetMemberId());
		MemberExtraEntity memberExtra = memberExtraAction.loadByMember(member);
		return wrapperResponse(memberExtra.toMap());
	}

	@RequestMapping(value = "/list.json")
	@ResponseBody
	public Map<String, Object> loadMembers(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]loadMembers( requestBody = %s)", request.getRequestURI(), requestBody));
		return null;
	}

	@RequestMapping(value = "/information.json")
	@ResponseBody
	public Map<String, Object> loadMemberInfo(@RequestBody Map<String, Object> requestBody,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("[%s]loadMemberInfo( requestBody = %s)", request.getRequestURI(), requestBody));
		MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
		Map<String, Object> params = Maps.newHashMap();
		params.put("memberId", handler.checkAndGetMemberId());
		if (handler.getOptType().isPresent()) {
			String smtId = null;
			switch (handler.getOptType().get()) {
			case "base":
				smtId = "load_member_base";
				break;
			case "card":
				smtId = "load_member_card";
				break;
			case "addition":
				smtId = "load_member_addition";
				break;
			case "clothes":
				smtId = "load_member_clothes";
				break;
			case "body":
				smtId = "load_member_extra";
				break;
			default:
				break;
			}
			com.google.common.base.Optional<Map<String, Object>> optional = queryService.queryForMap("member", smtId,
					params);
			return wrapperResponse(optional.isPresent() ? optional.get() : null);
		}
		com.google.common.base.Optional<Map<String, Object>> optional = queryService.queryForMap("member",
				"load_member", params);
		return wrapperResponse(optional.isPresent() ? optional.get() : null);
	}
	 /**
     * 一键分配
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/oneKey/allot.json")
    @ResponseBody
    public Map<String, Object> oneKeyAllotMembers(@RequestBody(required=false) Map<String, String> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]oneKeyAllotMembers( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Integer storeId = null;
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        Preconditions.checkArgument(null != storeId,"获取门店失败");
        TransactionStatus tx = startTx("oneKeyAllotMembers");
        try {
        	memberServer.oneKeyAllotMembers(storeId);
        }catch (Exception e) {
			rollbackTx(tx);
			throw new RuntimeException(e);
		}
        commitTx(tx);
        return wrapperEmptyResponse();
    }
    /**
     * 解除最近一次一键分配的导购
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/oneKey/deallocate.json")
    @ResponseBody
    public Map<String, Object> deallocateOneKeyMembers(@RequestBody(required=false) Map<String, String> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]deallocateOneKeyMembers( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Integer storeId = null;
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        Preconditions.checkArgument(null != storeId,"获取门店失败");
        TransactionStatus tx = startTx("deallocateLatestMembers");
        try {
        	memberServer.deallocateOneKeyMembers(storeId);
        }catch (Exception e) {
			rollbackTx(tx);
			throw new RuntimeException(e);
		}
        commitTx(tx);
        return wrapperEmptyResponse();
    }
    /**
     * 解除单个导购分配
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/employee/deallocate.json")
    @ResponseBody
    public Map<String, Object> deallocateMembers(@RequestBody(required=false) Map<String, String> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]deallocateMembers( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("employeeId"),"请求参数缺少employeeId");
        Integer storeId = null;
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        Preconditions.checkArgument(null != storeId,"获取门店失败");
        TransactionStatus tx = startTx("deallocateMembers");
        try {
        	memberServer.deallocateMembers(storeId,MapUtils.getInteger(requestBody, "employeeId"));
        }catch (Exception e) {
			rollbackTx(tx);
			throw new RuntimeException(e);
		}
        commitTx(tx);
        return wrapperEmptyResponse();
    }
    /**
     * 迁移会员
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/tranfer.json")
    @ResponseBody
    public Map<String, Object> tranferMembers(@RequestBody(required=false) Map<String, String> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]tranferMembers( requestBody = %s)", request.getRequestURI(), requestBody));
    	
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("sourceEmpId")
        		&&requestBody.containsKey("destEmpId"), "请求参数缺少sourceEmpId或destEmpId");
        Integer storeId = null;
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        TransactionStatus tx = startTx("tranferMembers");
        try {
        memberServer.transferMembers(storeId,MapUtils.getInteger(requestBody, "sourceEmpId"),
        		MapUtils.getInteger(requestBody, "destEmpId"));
        }catch (Exception e) {
			rollbackTx(tx);
			throw new RuntimeException(e);
		}
        commitTx(tx);
        return wrapperEmptyResponse();
    }
    /**
     * 会员再分配
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/reassign.json")
    @ResponseBody
    public Map<String, Object> reassignMembers(@RequestBody(required=false) Map<String, String> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]tranferMembers( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkArgument(requestBody.containsKey("sourceEmpId")
        		&&requestBody.containsKey("destEmpId")
        		&&requestBody.containsKey("memberIds"), "请求参数缺少sourceEmpId、destEmpId或memberIds");
        Integer storeId = null;
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        String memberIdsStr = MapUtils.getString(requestBody, "memberIds");
        if(Strings.isNullOrEmpty(memberIdsStr)) return wrapperEmptyResponse();
        List<String> memberIdsStrs = Splitter.on(",").splitToList(memberIdsStr);
        if(CollectionUtils.isEmpty(memberIdsStrs)) return wrapperEmptyResponse();
        List<Integer> memberIds = Lists.newArrayList();
        for(String memberIdStr : memberIdsStrs) memberIds.add(Integer.parseInt(memberIdStr)) ;     
        memberServer.reassignMembers(storeId, MapUtils.getInteger(requestBody, "sourceEmpId"), 
        		MapUtils.getInteger(requestBody, "destEmpId"), memberIds);
        return wrapperEmptyResponse();
    }
    /**
     * 指定分配会员
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/random/assgin.json")
    @ResponseBody
    public Map<String, Object> assginRandomMembers(@RequestBody(required=false) Map<String, Object> requestBody,
                                           HttpServletRequest request){
    	if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]assginRandomMembers( requestBody = %s)", request.getRequestURI(), requestBody));
        LoginUserContext loginUser = loadLoginUser(request);
        Integer storeId = null;
        MemberParamsHandler handler = MemberParamsHandler.newInstance(requestBody);
        if(null !=requestBody && requestBody.containsKey("storeId")) {
        	storeId = MapUtils.getInteger(requestBody, "storeId");
        }else {
        	Preconditions.checkArgument(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        	storeId = loginUser.getStore().get().getId();
        }
        memberServer.assginRandomMembers(storeId, handler.checkAndGetAssigns());
        return wrapperEmptyResponse();
    }
    
    /**
     * 获取会员消费统计信息
     * @param requestBody
     * @param request
     * @return
     */
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
        Optional<Map<String, Object>> data = queryService.queryForMap("member", "loadPurchasingBehavior", params);
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
    
    /**
     * 获取会员消费统计信息
     * @param requestBody
     * @param request
     * @return
     */
    @RequestMapping(value = "/msg/send.json")
    @ResponseBody
    public Map<String, Object> sendMsg(@RequestBody Map<String, String> requestBody,
                                                      HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("[%s]loadPurchasingBehavior( requestBody = %s)", request.getRequestURI(),
                    requestBody));
        Preconditions.checkArgument(requestBody.containsKey("memberIds"),"缺少memberIds参数");
        Preconditions.checkArgument(requestBody.containsKey("sendChannel"),"缺少sendChannel参数");
        Preconditions.checkArgument(requestBody.containsKey("content"),"缺少content参数");
        LoginUserContext loginUser = loadLoginUser(request);
        Preconditions.checkState(loginUser.getCompany().isPresent(), "当前登陆用户无公司属性....");
        Preconditions.checkState(loginUser.getStore().isPresent(), "当前登录用户无门店信息");
        
        Integer companyId = loginUser.getCompany().get().getId();
        Integer storeId = loginUser.getStore().get().getId();
        Integer employeeId = null;
        if(requestBody.containsKey("employeeId")) {
        	employeeId = MapUtils.getInteger(requestBody, "employeeId");
        }else {
        	employeeId = loginUser.getEmployee().getId();
        }
        String memberIdsStr = MapUtils.getString(requestBody, "memberIds");
        List<Integer> memberIds = Lists.newArrayList();
        if(!Strings.isNullOrEmpty(memberIdsStr)) 
        	for(String memberId : memberIdsStr.split(","))
        		memberIds.add(Integer.valueOf(memberId));
        Integer channel = MapUtils.getInteger(requestBody, "sendChannel");
        String content = MapUtils.getString(requestBody, "content");
        
    	Preconditions.checkNotNull(employeeId, "发送人ID不能为空");
    	Preconditions.checkNotNull(channel, "发送渠道不能为空");
    	Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "发送内容不能为空");
    	if(CollectionUtils.isEmpty(memberIds)) return wrapperEmptyResponse();
    	
    	List<String> payloads = Lists.newArrayListWithCapacity(memberIds.size());
    	memberIds.stream().forEach(x -> {
    		payloads.add(String.format("%s,%s,%s", "-1",x,channel));
    	});
    	
    	smsAction.sendMessageProxy(companyId, storeId, employeeId, payloads, content, BusinessType.BATCHCARE, false, request);
        return wrapperEmptyResponse();
    }
    
   
    @Resource
    private SmsGatewayProxyAction smsAction;
	@Resource
	private MemberEntityAction memberAction;
	@Resource
	private MemberServer memberServer;
	@Resource
	private MemberExtraEntityAction memberExtraAction;
	@Resource
	private QueryEngineService queryService;
}
