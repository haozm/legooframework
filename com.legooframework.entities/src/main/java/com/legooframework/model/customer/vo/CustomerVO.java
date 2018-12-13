package com.legooframework.model.customer.vo;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import com.legooframework.model.customer.entity.CustomerConsumptionEntity;
import com.legooframework.model.customer.entity.CustomerContactEntity;
import com.legooframework.model.customer.entity.CustomerEntity;
import com.legooframework.model.wechat.entity.WechatAccountEntity;

public class CustomerVO {
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private CustomerVO() {
		throw new AssertionError();
	}
	
	public static Map<String,Object> createBaseInfoVO(CustomerEntity customer) {
		Map<String,Object> baseMap = Maps.newHashMap();
		baseMap.put("name",customer.getName());
		baseMap.put("sex", customer.getSex().isPresent()?customer.getSex().get().getName():"");
		baseMap.put("sexVal", customer.getSex().isPresent()?customer.getSex().get().getValue():"");
		baseMap.put("iconUrl", customer.getIconUrl());
		baseMap.put("birthday",customer.getBirthday()==null?"":dateFormat.format(customer.getBirthday()));
		baseMap.put("birthdayLunar", customer.getBirthdayLunar());
		baseMap.put("openMemberCerdStore", "");
		baseMap.put("openMemberCerdTime", "");
		baseMap.put("memberCerdNo", "");
		baseMap.put("memberCerdType", "");
		baseMap.put("certType", customer.getCertType().isPresent()?customer.getCertType().get().getName():"");
		baseMap.put("idNumber", customer.getIdNumber());
		return baseMap;
	}
	
	public static Map<String,Object> createContactVO(CustomerContactEntity contact){
		Map<String,Object> contactMap = Maps.newHashMap();
		contactMap.put("telephone", contact.getTelephoneNumber());
		return contactMap;
	}
	
	public static Map<String,Object> createConsumeVO(CustomerConsumptionEntity consume,CustomerEntity customer){
		Map<String,Object> consumeMap = Maps.newHashMap();
		Optional<Map<String,Object>> consumeOpt = consume.getCustomerItem(customer);
		consumeMap.put("recentBuyTime", consumeOpt.isPresent()?consumeOpt.get().get("c_recent_buy_time"):"");
		consumeMap.put("recentBuyAmount", consumeOpt.isPresent()?consumeOpt.get().get("c_recent_buy_amount02"):"");
		consumeMap.put("recentBuyGoodCount", consumeOpt.isPresent()?consumeOpt.get().get("c_recent_buy_goods_count"):"");
		consumeMap.put("recentBuyStore", consumeOpt.isPresent()?consumeOpt.get().get("c_recent_buy_store_name"):"");
		consumeMap.put("avgOneBillPrice", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_avg_p_o_p02"):"");
		consumeMap.put("avgJointAndServeral", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_avg_j_r"):"");
		consumeMap.put("avgOneGoodsPrice", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_avg_p_g_p02"):"");
		consumeMap.put("avgDiscount", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_avg_p_g_p02"):"");
		consumeMap.put("avgInterval", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_avg_interval"):"");
		consumeMap.put("thisYearBuyGoodsCount", consumeOpt.isPresent()?consumeOpt.get().get("c_ty_buy_goods_count"):"");
		consumeMap.put("thisYearBuyFreq", consumeOpt.isPresent()?consumeOpt.get().get("c_ty_buy_freq"):"");
		consumeMap.put("thisYearBuyAmount", consumeOpt.isPresent()?consumeOpt.get().get("c_ty_buy_amount02"):"");
		consumeMap.put("totalBuyGoodsCount", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_goods_count"):"");
		consumeMap.put("totalBuyFreq", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_freq"):"");
		consumeMap.put("totalBuyAmount", consumeOpt.isPresent()?consumeOpt.get().get("c_total_buy_amount02"):"");
		return consumeMap;
	}
	
	public static Map<String,Object> createWechatVO(WechatAccountEntity wechat){
		Map<String,Object> wechatMap = Maps.newHashMap();
		wechatMap.put("weixinId", wechat.getUserName());
		wechatMap.put("wxNickname", wechat.getNickName());
		wechatMap.put("wxRemark", wechat.getConRemark());
		return wechatMap;
	}

}
