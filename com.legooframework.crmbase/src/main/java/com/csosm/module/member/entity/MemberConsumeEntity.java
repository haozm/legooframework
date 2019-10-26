package com.csosm.module.member.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import com.csosm.commons.entity.BaseEntity;
import com.google.common.collect.Maps;

public class MemberConsumeEntity extends BaseEntity<Integer> {

	protected MemberConsumeEntity(MemberEntity member) {
		super(member.getId());
	}

	// 充值金额
	private BigDecimal rechargeAmount = new BigDecimal(0);

	// RFM值
	private String rfm = "0-0-0";

	// 首次消费总额
	private BigDecimal firstSaleRecordAmount = new BigDecimal(0);

	// 首次消费单号
	private String firstSaleRecordNo;

	// 消费总次数
	private Integer consumeTotalCount = 0;

	// 消费总次数（当年）
	private Integer consumeTotalCountCurYear = 0;

	// 最大客单价
	private BigDecimal maxConsumePrice = new BigDecimal(0);

	// 最大客单价(当年)
	private BigDecimal maxConsumePriceCurYear = new BigDecimal(0);

	// 消费总金额
	private BigDecimal totalConsumeAmount = new BigDecimal(0);

	// 消费总金额（当年）
	private BigDecimal totalConsumeAmountCurYear = new BigDecimal(0);

	// 最后一次到店时间
	private Date lastVisitTime;

	private MemberConsumeEntity(Integer id, BigDecimal rechargeAmount, String rfm, BigDecimal firstSaleRecordAmount,
			String firstSaleRecordNo, Integer consumeTotalCount, Integer consumeTotalCountCurYear,
			BigDecimal maxConsumePrice, BigDecimal maxConsumePriceCurYear, BigDecimal totalConsumeAmount,
			BigDecimal totalConsumeAmountCurYear, Date lastVisitTime) {
		super(id);
		this.rechargeAmount = rechargeAmount;
		this.rfm = rfm;
		this.firstSaleRecordAmount = firstSaleRecordAmount;
		this.firstSaleRecordNo = firstSaleRecordNo;
		this.consumeTotalCount = consumeTotalCount;
		this.consumeTotalCountCurYear = consumeTotalCountCurYear;
		this.maxConsumePrice = maxConsumePrice;
		this.maxConsumePriceCurYear = maxConsumePriceCurYear;
		this.totalConsumeAmount = totalConsumeAmount;
		this.totalConsumeAmountCurYear = totalConsumeAmountCurYear;
		this.lastVisitTime = lastVisitTime;
	}

	protected static MemberConsumeEntity valueOf(Integer id, BigDecimal rechargeAmount, String rfm,
			BigDecimal firstSaleRecordAmount, String firstSaleRecordNo, Integer consumeTotalCount,
			Integer consumeTotalCountCurYear, BigDecimal maxConsumePrice, BigDecimal maxConsumePriceCurYear,
			BigDecimal totalConsumeAmount, BigDecimal totalConsumeAmountCurYear, Date lastVisitTime) {
		return new MemberConsumeEntity(id, rechargeAmount, rfm, firstSaleRecordAmount, firstSaleRecordNo,
				consumeTotalCount, consumeTotalCountCurYear, maxConsumePrice, maxConsumePriceCurYear,
				totalConsumeAmount, totalConsumeAmountCurYear, lastVisitTime);
	}

	public Map<String, Object> toStorageMap() {
		Map<String,Object> map = Maps.newHashMap();
		map.put("memberId", this.getId());
		return map;
	}

	public BigDecimal getRechargeAmount() {
		return rechargeAmount;
	}

	public String getRfm() {
		return rfm;
	}

	public BigDecimal getFirstSaleRecordAmount() {
		return firstSaleRecordAmount;
	}

	public String getFirstSaleRecordNo() {
		return firstSaleRecordNo;
	}

	public Integer getConsumeTotalCount() {
		return consumeTotalCount;
	}

	public Integer getConsumeTotalCountCurYear() {
		return consumeTotalCountCurYear;
	}

	public BigDecimal getMaxConsumePrice() {
		return maxConsumePrice;
	}

	public BigDecimal getMaxConsumePriceCurYear() {
		return maxConsumePriceCurYear;
	}

	public BigDecimal getTotalConsumeAmount() {
		return totalConsumeAmount;
	}

	public BigDecimal getTotalConsumeAmountCurYear() {
		return totalConsumeAmountCurYear;
	}

	public Date getLastVisitTime() {
		return lastVisitTime;
	}
	
	

}
