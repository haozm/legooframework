package com.legooframework.model.picturemgn.entity;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

public class PictureSpaceEntity extends BaseEntity<Long> {
	// 永久有效标志
	private final static String FOREVER_RANGE = "~";
	// 所有标志
	private final static String ALL_RANGE = "*";
	// 日期格式化
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static DecimalFormat PRICE_FORMATTER = new DecimalFormat("0.00");
	// 门店优先级
	private final static int STORE_PRIORITY = 1;
	// 部门优先级
	private final static int DEPARTMENT_PRIORITY = 5;
	// 公司优先级
	private final static int COMPANY_PRIORITY = 9;
	// 使用空间
	private Long space;
	// 空间价格
	private BigDecimal price;
	// 组织ID
	private final Integer orgId;
	// 公司ID
	private final Integer companyId;
	// 允许使用的门店
	private final Set<String> allowStores = Sets.newHashSet();
	// 允许使用的公司
	private final Set<String> limitStores = Sets.newHashSet();
	// 优先级
	private int priority;
	// 时间限制文本描述
	private String rangeCtx = FOREVER_RANGE;
	// 是否共享 0代表不共享 1代表共享
	private int shared;
	// 最小有效时间
	private Date beginTime;
	// 最大有效时间
	private Date endTime;

	private PictureSpaceEntity(Long space, String price, Integer orgId, Integer companyId, int priority,
			int shared,Date beginTime,Date endTime) {
		super(null);
		this.space = space;
		if (null != price)
			this.price = new BigDecimal(price);
		this.orgId = orgId;
		this.companyId = companyId;
		this.priority = priority;
		if(null != beginTime && null != endTime) {
			this.beginTime = beginTime;
			this.endTime = endTime;
			this.rangeCtx = String.format("begin:%s,end:%s", formatDate(beginTime), formatDate(endTime));
		}
		this.shared = shared;
	}

	// 格式化时间
	private static String formatDate(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return DATE_FORMATTER.format(localDateTime);
	}
	
	/**
	 * 创建永久有效的门店图片存储空间
	 * @param space 空间大小
	 * @param price 空间价格
	 * @param store 门店
	 * @return
	 */
	public static PictureSpaceEntity newStoreFreeSpace(Long space, String price, CrmStoreEntity store) {
		Objects.requireNonNull(space, "入参space不能为空");
		return new PictureSpaceEntity(space,price,store.getId(),store.getCompanyId(),STORE_PRIORITY,1,null,null);
	}
	
	/**
	 * 创建有有效期的门店图片存储空间
	 * @param space 空间大小
	 * @param price 空间价格
	 * @param store 门店
	 * @param beginTime 有效期开始时间
	 * @param endTime 有效期结束时间
	 * @return
	 */
	public static PictureSpaceEntity newStoreSpace(Long space, String price, CrmStoreEntity store, Date beginTime,
			Date endTime) {
		Objects.requireNonNull(space, "入参space不能为空");
		PictureSpaceEntity picSpace = new PictureSpaceEntity(space,price,store.getId(),store.getCompanyId(),STORE_PRIORITY,1,beginTime,endTime);
		picSpace.allowStores.add(String.valueOf(store.getId()));
		return picSpace;
	}

	/**
	 * 创建有有效期的公司图片存储空间
	 * @param space
	 * @param price
	 * @param company
	 * @param beginTime
	 * @param endTime
	 * @return
	 */
	public static PictureSpaceEntity newCompanySpace(Long space, String price, CrmOrganizationEntity company, Date beginTime,
			Date endTime) {
		Objects.requireNonNull(space, "入参space不能为空");
		PictureSpaceEntity picSpace = new PictureSpaceEntity(space,price,company.getId(),company.getId(),COMPANY_PRIORITY,1,beginTime,endTime);
		picSpace.allowStores.add(ALL_RANGE);
		return picSpace;
	}
	
	public static PictureSpaceEntity newDepartmentSpace(Long space,String price,CrmOrganizationEntity department,Date beginTime,Date endTime) {
		Objects.requireNonNull(space, "入参space不能为空");
		PictureSpaceEntity picSpace = new PictureSpaceEntity(space,price,department.getId(),department.getId(),DEPARTMENT_PRIORITY,1,beginTime,endTime);
		picSpace.allowStores.add(ALL_RANGE);
		return picSpace;
	}
	/**
	 * 判断空间是否有效
	 * @return
	 */
	public boolean isValidated() {
		Date now = new Date();
		return this.beginTime.getTime() <= now.getTime() && now.getTime() <= this.endTime.getTime();
	}
	
	/**
	 * 允许门店，即添加该门店到白名单中
	 * @param store
	 */
	public void allow(CrmStoreEntity store) {
		if(this.allowStores.contains(ALL_RANGE)) this.allowStores.remove(ALL_RANGE);
		this.allowStores.add(String.valueOf(store.getId()));
	}
	
	/**
	 * 允许多个门店
	 * @param stores
	 */
	public void allow(Collection<CrmStoreEntity> stores) {
		if(CollectionUtils.isEmpty(stores)) return ;
		if(this.allowStores.contains(ALL_RANGE)) this.allowStores.remove(ALL_RANGE);
		this.limitStores.addAll(stores.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toSet()));
	}
	/**
	 * 禁止门店，即添加该门店到黑名单中
	 * @param store
	 */
	public void limit(CrmStoreEntity store) {
		this.limitStores.add(String.valueOf(store.getId()));
	}
	
	/**
	 * 禁用多个门店
	 * @param stores
	 */
	public void limit(Collection<CrmStoreEntity> stores) {
		if(CollectionUtils.isEmpty(stores)) return ;
		this.limitStores.addAll(stores.stream().map(x -> String.valueOf(x.getId())).collect(Collectors.toSet()));
	}
	
	//增添空间
	public void increaseSpace(Long space) {
		this.space = this.space + space;
	}
	
	
	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Long getSpace() {
		return space;
	}

	public Integer getOrgId() {
		return orgId;
	}

	public Integer getCompanyId() {
		return companyId;
	}
	
}
