package com.legooframework.model.picturemgn.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.jdbc.ResultSetUtil;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;

public class PictureLabelEntity extends BaseEntity<Long> {
	// 永久有效标志
	private final static String FOREVER_RANGE = "~";
	// 日期格式化
	private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

	private final static Long COMPANY_ROOT_LABEL_ID = 100L;

	private final static Long STORE_ROOT_LABEL_ID = 200L;
	// 标签名称
	private final String name;
	// 标签描述
	private String desc;
	// 父标签ID
	private Long pId;
	// 门店ID 默认为-1
	private Integer storeId = -1;
	// 公司ID 默认为-1
	private Integer companyId = -1;
	// 是否开启
	private boolean enable = true;
	// 子标签ID集合
	private Set<Long> childIds = Sets.newHashSet();
	// 时间限制
	private Range<Date> dateRange;
	// 时间限制文本描述
	private String rangeCtx = FOREVER_RANGE;

	private PictureLabelEntity(Long id, String name, String desc, Long pId, Integer storeId, Integer companyId,
			Set<Long> childIds, Range<Date> dateRange, String rangeCtx) {
		super(id);
		this.name = name;
		if (!Strings.isNullOrEmpty(desc))
			this.desc = desc;
		if (null != pId)
			this.pId = pId;
		if (null != storeId)
			this.storeId = storeId;
		if (null != companyId)
			this.companyId = companyId;
		if (!CollectionUtils.isEmpty(childIds))
			this.childIds = childIds;
		if (null != rangeCtx)
			this.dateRange = dateRange;
		if (!Strings.isNullOrEmpty(rangeCtx))
			this.rangeCtx = rangeCtx;
	}

	/**
	 * 创建公司根图片标签
	 * 
	 * @param company
	 * @return
	 */
	public static PictureLabelEntity newRootPictureLabel(CrmOrganizationEntity company) {
		return new PictureLabelEntity(COMPANY_ROOT_LABEL_ID, "系统标签", "公司系统标签", COMPANY_ROOT_LABEL_ID, null,
				company.getId(), null, null, null);
	}

	/**
	 * 创建门店根图片标签
	 * 
	 * @param store
	 * @return
	 */
	public static PictureLabelEntity newRootPictureLabel(CrmStoreEntity store) {
		return new PictureLabelEntity(STORE_ROOT_LABEL_ID, "门店自定义标签", "门店自定义标签", STORE_ROOT_LABEL_ID, store.getId(),
				null, null, null, null);
	}

	// 创建带描述的门店图片标签
	public PictureLabelEntity nextStorePictureLabel(String name, String desc) {
		requireArgumentCheck(name);
		return new PictureLabelEntity(nextChildLabelId(), name, desc, this.getId(), this.storeId, null, null, null,
				null);
	}

	// 创建带时间限制的门店图片标签
	public PictureLabelEntity nextStorePictureLabel(String name, String desc, Date minDate, Date maxDate) {
		requireArgumentCheck(name);
		dateCheck(minDate, maxDate);
		String context = String.format("min:%s,max:%s", formatDate(minDate), formatDate(maxDate));
		return new PictureLabelEntity(nextChildLabelId(), name, desc, this.getId(), this.storeId, null, null,
				Range.closed(minDate, maxDate), context);
	}

	// 创建带描述的公司图片标签
	public PictureLabelEntity nextCompanyPictureLabel(String name, String desc) {
		requireArgumentCheck(name);
		return new PictureLabelEntity(nextChildLabelId(), name, desc, this.getId(), null, this.companyId, null, null,
				null);
	}

	// 创建带时间限制的公司图片标签
	public PictureLabelEntity nextCompanyPictureLabel(String name, String desc, Date minDate, Date maxDate) {
		requireArgumentCheck(name);
		dateCheck(minDate, maxDate);
		String context = String.format("min:%s,max:%s", formatDate(minDate), formatDate(maxDate));
		return new PictureLabelEntity(nextChildLabelId(), name, desc, this.getId(), null, this.companyId, null,
				Range.closed(minDate, maxDate), context);
	}

	// 获取下一个子标签ID
	public Long nextChildLabelId() {
		Optional<Long> maxOpt = this.childIds.stream().reduce(Long::max);
		if (!maxOpt.isPresent())
			return this.getId() * 1000L + 100L;
		if (maxOpt.get() % (this.getId() * 000L + 100L) == 999)
			throw new IllegalArgumentException(String.format("标签[%s]定义的子标签已达到最大值", this.getId()));
		Long next = maxOpt.get() + 1;
		if(next >= 299999999999999L)
			throw new IllegalArgumentException("标签深度已达到4级，无法继续添加");
		return next;
	}

	// 必须值校验
	private static void requireArgumentCheck(String name) {
		if (Strings.isNullOrEmpty(name))
			throw new IllegalArgumentException("图片标签名称不能为空");
	}



	// 有效期校验
	private static void dateCheck(Date minDate, Date maxDate) {
		Objects.requireNonNull(minDate, "图片标签最小有效时间不能为空");
		Objects.requireNonNull(maxDate, "图片标签最大有效时间不能为空");
		if (maxDate.compareTo(minDate) < 0)
			throw new IllegalArgumentException(String.format("有效期[minDate=%s,maxDate=%s]设置错误", minDate, maxDate));
	}

	// 格式化时间
	private static String formatDate(Date date) {
		LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
		return DATE_FORMATTER.format(localDateTime);
	}

	// 日期格式化为字符串
	private static Date strToDate(String date) {
		ZonedDateTime atStartOfDay = LocalDate.parse(date).atStartOfDay(ZoneId.systemDefault());
		return Date.from(atStartOfDay.toInstant());
	}

	// 启用
	private void enable() {
		this.enable = true;
	}

	// 禁用
	private void disable() {
		this.enable = false;
	}

	// 判断ID是否门店根标签
	public static boolean isStoreRootLabel(Long id) {
		return STORE_ROOT_LABEL_ID.longValue() == id.longValue();
	}

	// 判断ID是否公司根标签
	public static boolean isCompanyRootLabel(Long id) {
		return COMPANY_ROOT_LABEL_ID.longValue() == id.longValue();
	}

	private final static Splitter splitter = Splitter.on(",");

	/**
	 * 从数据库还原对象
	 * 
	 * @param rs
	 *            数据集合
	 * @return
	 */
	public PictureLabelEntity(Long id, ResultSet rs) {
		super(id, rs);
		try {
			this.pId = ResultSetUtil.getObject(rs, "pid", Long.class);
			this.name = ResultSetUtil.getObject(rs, "name", String.class);
			this.desc = ResultSetUtil.getObject(rs, "desc", String.class);
			String rangeCtx = ResultSetUtil.getObject(rs, "rangeCtx", String.class);
			if (null == rangeCtx || rangeCtx.indexOf("~") != -1) {
				this.rangeCtx = "~";
			} else {
				this.rangeCtx = rangeCtx;
				Map<String, String> rangeCtxMap = Splitter.on(",").withKeyValueSeparator(":").split(this.rangeCtx);
				this.dateRange = Range.closed(strToDate(MapUtils.getString(rangeCtxMap, "min")),
						strToDate(MapUtils.getString(rangeCtxMap, "max")));
			}
			String childIds = ResultSetUtil.getObject(rs, "childIds", String.class);
			if (!Strings.isNullOrEmpty(childIds))
				Splitter.on(",").splitToList(childIds).stream().forEach(x -> this.childIds.add(Long.parseLong(x)));
			Integer enable = ResultSetUtil.getObject(rs, "enable", Integer.class);
			this.enable = enable == 1 ? true : false;
			this.storeId = ResultSetUtil.getObject(rs, "storeId", Integer.class);
			this.companyId = ResultSetUtil.getObject(rs, "companyId", Integer.class);
		} catch (SQLException e) {
			throw new RuntimeException("Restore PictureLabelEntity has SQLException", e);
		}
	}

	public String getDesc() {
		return desc;
	}

	public Long getpId() {
		return pId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public void setStoreId(Integer storeId) {
		this.storeId = storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public Set<Long> getChildIds() {
		return childIds;
	}

	public String getName() {
		return name;
	}

	public String getRangeCtx() {
		return rangeCtx;
	}

	@Override
	public Map<String, Object> toParamMap(String... excludes) {
		Map<String, Object> paramMap = super.toParamMap(excludes);
		paramMap.put("id", this.getId());
		paramMap.put("pId", this.pId);
		paramMap.put("name", this.name);
		paramMap.put("desc", this.desc);
		paramMap.put("rangeCtx", this.rangeCtx);
		paramMap.put("storeId", this.storeId);
		paramMap.put("companyId", this.companyId);
		return paramMap;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	@Override
	public String toString() {
		return "PictureLabelEntity [name=" + name + ", desc=" + desc + ", pId=" + pId + ", storeId=" + storeId
				+ ", companyId=" + companyId + ", enable=" + enable + ", childIds=" + childIds + ", dateRange="
				+ dateRange + ", rangeCtx=" + rangeCtx + "]";
	}

}
