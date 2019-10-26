package com.legooframework.model.families.mvc;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.legooframework.model.families.entity.FamilyEntityAction;
import com.legooframework.model.families.entity.MemberFamilyBO;
import com.legooframework.model.families.service.FamilyService;

@Controller
@RequestMapping("/family")
public class FamilyController extends BaseController{
	
private static final Logger logger = LoggerFactory.getLogger(FamilyController.class);
	
	/**
	 * 添加或修改家庭成员
	 * @param request
	 * @param requestBody
	 * @return
	 */
	@RequestMapping(value = "/replace.json")
	@ResponseBody
	public Map<String, Object> replaceFamily(HttpServletRequest request, @RequestBody MemberFamilyBO memberFamilyBO) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("replaceFamily[%s]->reqMap=%s", request.getRequestURI(), memberFamilyBO));
		LoginUserContext loginUser = loadLoginUser(request);
		getBean(FamilyService.class, request).saveOrUpdateMemberFamily(loginUser, memberFamilyBO);
		return wrapperEmptyResponse();
	}
	
	/**
	 * 加载家庭成员
	 * @param request
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "/load.json")
	@ResponseBody
	public Map<String,Object> loadFamily(HttpServletRequest request,@RequestBody Map<String,Object> params){
		if (logger.isDebugEnabled())
			logger.debug(String.format("loadFamily[%s]->reqMap=%s", request.getRequestURI(), params));
		LoginUserContext loginUser = loadLoginUser(request);
		Preconditions.checkArgument(params.containsKey("familyId"), "请求参数缺少familyId");
		String familyId = MapUtils.getString(params, "familyId");
		MemberFamilyBO memberFamilyBO = getBean(FamilyEntityAction.class, request).loadMemberFamilyBO(familyId);
		return wrapperResponse(memberFamilyBO);
	}
	/**
	 * 家庭成员绑定会员
	 * @param request
	 * @param params
	 * @return
	 */
	@RequestMapping(value = "member/bind.json")
	@ResponseBody
	public Map<String,Object> bindMember(HttpServletRequest request,@RequestBody Map<String,Object> params){
		if (logger.isDebugEnabled())
			logger.debug(String.format("loadFamily[%s]->reqMap=%s", request.getRequestURI(), params));
		LoginUserContext loginUser = loadLoginUser(request);
		Preconditions.checkArgument(params.containsKey("familyId"), "请求参数缺少familyId");
		Preconditions.checkArgument(params.containsKey("memberId"), "请求参数缺少memberId");
		String familyId = MapUtils.getString(params, "familyId");
		Integer memberId = MapUtils.getInteger(params, "memberId");
		Integer membership = MapUtils.getInteger(params, "membership");
		String appellation = MapUtils.getString(params, "appellation");
		getBean(FamilyService.class, request).bindMember(loginUser, familyId, memberId, membership, appellation);
		return wrapperEmptyResponse();
	}
	
}
