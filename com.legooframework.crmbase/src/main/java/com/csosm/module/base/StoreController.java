package com.csosm.module.base;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.base.entity.StoreEntity;
import com.csosm.module.base.entity.StoreEntityAction;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller(value = "storeController")
@RequestMapping("/store")
public class StoreController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(StoreController.class);

	@RequestMapping(value = "/change/qrcode.json")
	@ResponseBody
	public Map<String, Object> modifyQrCode(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("modifyQrCode(%s,url=%s)", requestBody, request.getRequestURL()));
		String deviceId = MapUtils.getString(requestBody, "deviceId");
		String qrCode = MapUtils.getString(requestBody, "qrcode");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(deviceId), "设备ID值不可以为空...");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(qrCode), "设备qrCode值不可以为空...");
		boolean res = getBean(StoreEntityAction.class, request).saveOrupdateQRcode(deviceId, qrCode);
		if (res)
			getBean(BaseModelServer.class, request).cleanCache("adapterCache");
		return wrapperResponse(null);
	}

	@RequestMapping(value = "/change/birthdaybefore.json")
	@ResponseBody
	public Map<String, Object> modifyBirthdayBefore(@RequestBody Map<String, String> requestBody,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("modifyBirthdayBefore(%s,url=%s)", requestBody, request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		Preconditions.checkState(user.getStore().isPresent(), "当前用户所属门店为空，无法设置相关参数...");
		int birthdayBefore = MapUtils.getIntValue(requestBody, "birthdayBefore", 0);
		boolean res = getBean(StoreEntityAction.class, request)
				.saveOrupdateBirthdayBefore(user.getStore().get().getId(), birthdayBefore);
		if (res)
			getBean(BaseModelServer.class, request).cleanCache("adapterCache");
		return wrapperResponse(null);
	}

	@RequestMapping(value = "/change/beforeDays.json")
	@ResponseBody
	public Map<String, Object> modifyBeforeDays(@RequestBody Map<String, String> requestBody,
			HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("modifyBeforeDays(%s,url=%s)", requestBody, request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		Preconditions.checkState(user.getStore().isPresent(), "当前用户所属门店为空，无法设置相关参数...");
		int beforeDays = MapUtils.getIntValue(requestBody, "beforeDays", 0);
		boolean res = getBean(StoreEntityAction.class, request).saveOrupdateBeforeDays(user.getStore().get().getId(),
				beforeDays);
		if (res)
			getBean(BaseModelServer.class, request).cleanCache("adapterCache");
		return wrapperResponse(null);
	}

	@RequestMapping(value = "/load/mystore.json")
	@ResponseBody
	public Map<String, Object> loadMyStoreInfo(HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("loadMyStoreInfo(%s,url=%s)", "null", request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		Preconditions.checkState(user.getStore().isPresent(), "当前用户所属门店为空，无法设置相关参数...");
		Integer storeId = user.getStore().get().getId();
		StoreEntity store = getBean(StoreEntityAction.class, request).loadById(storeId);
		return wrapperResponse(store.toMap());
	}

	private class ParamsHolder {

		private final Map<String, Object> params;

		public ParamsHolder(Map<String, Object> params) {
			this.params = params;
		}

		public String checkAndGetName() {
			Preconditions.checkArgument(params.containsKey("name"), "请求参数缺失门店名称[name]");
			return MapUtils.getString(params, "name");
		}

		public Integer checkAndGetParentId() {
			Preconditions.checkArgument(params.containsKey("parentId"), "请求参数缺失门店名称[parentId]");
			return MapUtils.getInteger(params, "parentId");
		}

		public String getPhone() {
			return MapUtils.getString(params, "phone");
		}

		public Integer checkAndGetState() {
			Preconditions.checkArgument(params.containsKey("state"), "请求参数缺失门店开启或禁用状态[status]");
			return MapUtils.getInteger(params, "state");
		}

		public Integer checkAndGetType() {
			Preconditions.checkArgument(params.containsKey("type"), "请求参数缺失门店类型[type]");
			return MapUtils.getInteger(params, "type");
		}
		
		public String getAddress() {
			return MapUtils.getString(params, "address");
		}

		public Integer checkAndGetStoreId() {
			Preconditions.checkArgument(params.containsKey("storeId"), "请求参数缺失门店编号[storeId]");
			return MapUtils.getInteger(params, "storeId");
		}
		
		public Integer checkAndGetHiddenMemberPhoneFlag() {
			Preconditions.checkArgument(params.containsKey("hiddenMemberPhoneFlag"), "请求参数缺少隐藏会员号码[hiddenMemberPhoneFlag]");
			return MapUtils.getInteger(params, "hiddenMemberPhoneFlag");
		}
	}
	
	/**
	 * 加载门店信息
	 * @param requestBody
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "web/load.json")
	@ResponseBody
	public Map<String, Object> loadStore(@RequestBody Map<String, Object> requestBody,HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("loadStore(%s,url=%s)", "null", request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		ParamsHolder holder = new ParamsHolder(requestBody);
		Integer storeId = holder.checkAndGetStoreId();
		Optional<StoreEntity> storeOpt = getBean(StoreEntityAction.class, request).findById(storeId);
		Preconditions.checkState(storeOpt.isPresent(), String.format("不存在门店[%s]", storeId));
		return wrapperResponse(storeOpt.get().toViewMap());
	}
	/**
	 * 新增门店
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "web/save.json")
	@ResponseBody
	public Map<String, Object> saveStore(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("saveStore(%s,url=%s)", "null", request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		ParamsHolder holder = new ParamsHolder(requestBody);
		Integer parentId = holder.checkAndGetParentId();
		String name = holder.checkAndGetName();
		String phone = holder.getPhone();
		Integer type = holder.checkAndGetType();
		Integer state = holder.checkAndGetState();
		String address = holder.getAddress();
		getBean(StoreServer.class, request).saveStore(user, parentId, name, phone, type, state, address);
		return wrapperEmptyResponse();
	}
	/**
	 * 编辑门店信息
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "web/edit.json")
	@ResponseBody
	public Map<String, Object> editStore(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("editStore(%s,url=%s)", "null", request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		ParamsHolder holder = new ParamsHolder(requestBody);
		Integer storeId = holder.checkAndGetStoreId();
		String name = holder.checkAndGetName();
		String phone = holder.getPhone();
		Integer type = holder.checkAndGetType();
		Integer state = holder.checkAndGetState();
		String address = holder.getAddress();
		Integer hiddenPhone = holder.checkAndGetHiddenMemberPhoneFlag();
		getBean(StoreServer.class, request).editStore(user, storeId, name, phone, type, state, address, hiddenPhone);
		return wrapperEmptyResponse();
	}
	/**
	 * 迁移门店
	 * @param requestBody
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "web/switch.json")
	@ResponseBody
	public Map<String, Object> switchStore(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("switchStore(%s,url=%s)", "null", request.getRequestURL()));
		LoginUserContext user = loadLoginUser(request);
		ParamsHolder holder = new ParamsHolder(requestBody);
		Integer storeId = holder.checkAndGetStoreId();
		Integer parentId = holder.checkAndGetParentId();
		getBean(StoreServer.class, request).switchStore(user, parentId, storeId);
		return wrapperEmptyResponse();
	}
}
