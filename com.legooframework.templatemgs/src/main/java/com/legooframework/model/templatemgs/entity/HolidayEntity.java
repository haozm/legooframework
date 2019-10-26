package com.legooframework.model.templatemgs.entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntity;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.templatemgs.entity.Cron.CalendarType;
import com.legooframework.model.templatemgs.entity.HolidayEntity.CronType;

public class HolidayEntity extends BaseEntity<String> {

	// 节日名称
	private String name;
	// 节日备注
	private String remark;
	// 节日日期表达式
	private String cron;
	// 公司ID
	private Integer companyId;
	// 门店ID
	private Integer storeId;
	// 节日持续时间，以天为单位
	private Integer duration;
	// 节日类型
	private Type type;
	// 是否禁用,false为禁用，true为启用
	private boolean enable = true;
	// 日期表达式类型，1 公历日期表达式 2 农历日期表达式
	private CronType cronType;

	private List<Integer> companyBlackList = Lists.newArrayList();

	private List<Integer> storeBlackList = Lists.newArrayList();

	private Object createUserId;
	
	//日期中文表示格式
	private String cronContext;
	
	private LocalDate cronDate;
	
	private CalendarType calendarType;
	
	private HolidayEntity(String id, String name, String remark, String cron,String cronContext, CronType cronType,LocalDate cronDate, 
			CalendarType calendarType,Integer companyId,
			Integer storeId, Integer duration, Type type, boolean enable) {
		super(id);
		this.name = name;
		this.remark = remark;
		this.cron = cron;
		this.companyId = companyId;
		this.storeId = storeId;
		this.duration = duration;
		this.type = type;
		this.cronType = cronType;
		this.enable = enable;
		this.cronContext = cronContext;
		this.cronDate = cronDate;
		this.calendarType = calendarType;
	}

	private HolidayEntity(String id, String name, String remark, String cron,String cronContext, CronType cronType, LocalDate cronDate, CalendarType calendarType,Integer companyId,
			Integer storeId, Integer duration, Type type, boolean enable, List<Integer> companyBlackList,
			List<Integer> storeBlackList) {
		super(id);
		this.name = name;
		this.remark = remark;
		this.cron = cron;
		this.companyId = companyId;
		this.storeId = storeId;
		this.duration = duration;
		this.type = type;
		this.cronType = cronType;
		this.enable = enable;
		this.cronContext = cronContext;
		this.cronDate = cronDate;
		this.companyBlackList = companyBlackList;
		this.storeBlackList = storeBlackList;
		this.calendarType = calendarType;
	}

	/**
	 * 从数据库中还原
	 * 
	 * @param name
	 * @param remark
	 * @param cron
	 * @param cronType
	 * @param companyId
	 * @param storeId
	 * @param duration
	 * @param type
	 * @return
	 */
	public static HolidayEntity valueOf(String id, String name, String remark, String cron,String cronContext, String cronType,String cronDate,String calendarType,
			Integer companyId, Integer storeId, Integer duration, String type, Integer enable,
			String companyBlackListStr, String storeBlackListStr) {
		Splitter splitter = Splitter.on(",");
		List<Integer> companyBlackList = Strings.isNullOrEmpty(companyBlackListStr) ? Lists.newArrayList()
				: splitter.splitToList(companyBlackListStr).stream().map(Integer::valueOf).collect(Collectors.toList());

		List<Integer> storeBlackList = Strings.isNullOrEmpty(storeBlackListStr) ? Lists.newArrayList()
				: splitter.splitToList(storeBlackListStr).stream().map(Integer::valueOf).collect(Collectors.toList());
		
		return new HolidayEntity(id, name, remark, cron, cronContext,null == cronType?null:CronType.valueOf(cronType),
				null == cronDate?null:LocalDate.parse(cronDate, DateTimeFormatter.ISO_DATE),
				null == calendarType?null:CalendarType.valueOf(calendarType), companyId, storeId, duration,
				Type.valueOf(type), 0 == enable?false:true, companyBlackList, storeBlackList);
	}

	private static String uuId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	/**
	 * 创建系统级别的节日
	 * 
	 * @param name
	 * @param remark
	 * @param cron
	 * @param duration
	 * @return
	 */
	public static HolidayEntity createSystemHoliday(String name, String remark, String cron, String cronContext,CronType cronType,LocalDate cronDate,CalendarType calendarType,
			Integer duration) {
		return new HolidayEntity(uuId(), name, remark, cron, cronContext,cronType,cronDate, calendarType,-1, -1, duration, Type.SYSTEM, true);
	}

	/**
	 * 创建公司级别的节日
	 * 
	 * @param company
	 * @param name
	 * @param remark
	 * @param cron
	 * @param duration
	 * @return
	 */
	public static HolidayEntity createCompanyHoliday(CrmOrganizationEntity company, String name, String remark,
			String cron,String cronContext, CronType cronType, LocalDate cronDate,CalendarType calendarType,Integer duration) {
		return new HolidayEntity(uuId(), name, remark, cron,cronContext, cronType,cronDate,calendarType, company.getId(), null, duration, Type.COMPANY, true);
	}

	/**
	 * 创建门店级别的节日
	 * 
	 * @param store
	 * @param name
	 * @param remark
	 * @param cron
	 * @param duration
	 * @return
	 */
	public static HolidayEntity createStoreHoliday(CrmStoreEntity store, String name, String remark, String cron,String cronContext,
			CronType cronType, LocalDate cronDate,CalendarType calendarType,Integer duration) {
		return new HolidayEntity(uuId(), name, remark, cron,cronContext, cronType, cronDate,calendarType,store.getCompanyId(), store.getId(), duration,
				Type.STORE, true);
	}

	public Optional<HolidayEntity> modify(String name, String remark, String cron,String cronContext, CronType cronType, LocalDate cronDate,CalendarType calendarType,Integer duration) {
		HolidayEntity clone = null;
		try {
			clone = (HolidayEntity)this.clone();
			clone.cron = cron;
			clone.cronDate = cronDate;
			clone.calendarType = calendarType;
			clone.cronContext = cronContext;
			clone.cronType = cronType;
			clone.duration = duration;
			clone.name = name;
			clone.remark = remark;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(this.equals(clone)) return Optional.empty();
		return Optional.of(clone);
			
	}

	/**
	 * 节日级别
	 * 
	 * @author Administrator SYSTEM:系统级别、COMPANY:公司级别、STORE:
	 */
	private enum Type {
		SYSTEM, COMPANY,STORE;
	}

	public static enum CronType {
		SOLAR, LUNAR;
		
		public static CronType checkAndGet(String name) {
			Optional<CronType> cronTypeOpt = Arrays.stream(CronType.values()).filter(x -> x.name().equals(name)).findFirst();
			Preconditions.checkArgument(cronTypeOpt.isPresent(),String.format("CronType 值范围为【%s】","LUNAR,SOLAR"));
			return cronTypeOpt.get();
		}
	}
	
	public void enable() {
		this.enable = true;
	}

	public void disable() {
		this.enable = false;
	}
	
	public boolean isEnable() {
		return this.enable;
	}

	public void allowCompany(CrmOrganizationEntity company) {
		if (isAllowCompany(company))
			return;
		this.companyBlackList.remove(company.getId());
	}

	public void unallowCompany(CrmOrganizationEntity company) {
		if (!isAllowCompany(company))
			return;
		this.companyBlackList.add(company.getId());
	}

	public void allowStore(CrmStoreEntity store) {
		if (isAllowStore(store))
			return;
		this.storeBlackList.remove(store.getId());
	}

	public void unallowStore(CrmStoreEntity store) {
		if (!isAllowStore(store))
			return;
		this.storeBlackList.add(store.getId());
	}

	public boolean isSystemHoliday() {
		return this.type == type.SYSTEM;
	}

	public boolean isCompanyHoliday() {
		return this.type == type.COMPANY;
	}

	public boolean isStoreHoliday() {
		return this.type == type.STORE;
	}


	public boolean isAllowCompany(CrmOrganizationEntity company) {
		return !this.companyBlackList.contains(company.getId());
	}

	public boolean isAllowStore(CrmStoreEntity store) {
		return !this.storeBlackList.contains(store.getId());
	}

	public void setCreator(LoginContext loginUser) {
		this.createUserId = loginUser.getLoginId();
	}
	
	public boolean isBelongTo(CrmOrganizationEntity company) {
		return isCompanyHoliday() && this.companyId.intValue() == company.getCompanyId().intValue();
	}
	
	public boolean isBelongTo(CrmStoreEntity store) {
		return isStoreHoliday() && this.storeId.intValue() == store.getId().intValue();
	}
	
	public boolean isBelongToSystem() {
		return isSystemHoliday();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("id", this.getId());
		map.put("name", this.name);
		map.put("remark", this.remark);
		map.put("cron", this.cron);
		map.put("cronContext", this.cronContext);
		map.put("cronType", null== this.cronType?null:this.cronType.name());
		map.put("cronDate", null == cronDate?null:cronDate.format(DateTimeFormatter.ISO_DATE));
		map.put("calendarType", null == calendarType?null:calendarType.name());
		map.put("duration", this.duration);
		map.put("type", null == type?null:type.name());
		map.put("storeId", this.storeId);
		map.put("enable", this.enable?1:0);
		Joiner joiner = Joiner.on(",");
		map.put("companyBlackList",
				CollectionUtils.isEmpty(this.companyBlackList) ? "" : joiner.join(this.companyBlackList));
		map.put("storeBlackList", CollectionUtils.isEmpty(this.storeBlackList) ? "" : joiner.join(this.storeBlackList));
		map.put("companyId", this.companyId);
		map.put("createUserId", this.createUserId);
		map.put("dayOfWeek", null);
		if(CalendarType.EVERYWEEK == calendarType) {
			map.remove("cronType");
			Splitter sp = Splitter.on(" ");
			List<String> list = sp.splitToList(this.cron);
			if(list.size() == 6)
				map.put("dayOfWeek", list.get(5));
		}
		return map;
	}

	public String getName() {
		return name;
	}

	public String getRemark() {
		return remark;
	}

	public String getCron() {
		return cron;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getDuration() {
		return duration;
	}

	public Type getType() {
		return type;
	}

	public CronType getCronType() {
		return cronType;
	}

	public List<Integer> getCompanyBlackList() {
		return companyBlackList;
	}

	public List<Integer> getStoreBlackList() {
		return storeBlackList;
	}

	public Object getCreateUserId() {
		return createUserId;
	}
	
	public String getCronContext() {
		return cronContext;
	}

	public LocalDate getCronDate() {
		return cronDate;
	}

	public CalendarType getCalendarType() {
		return calendarType;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((calendarType == null) ? 0 : calendarType.hashCode());
		result = prime * result + ((companyBlackList == null) ? 0 : companyBlackList.hashCode());
		result = prime * result + ((companyId == null) ? 0 : companyId.hashCode());
		result = prime * result + ((createUserId == null) ? 0 : createUserId.hashCode());
		result = prime * result + ((cron == null) ? 0 : cron.hashCode());
		result = prime * result + ((cronContext == null) ? 0 : cronContext.hashCode());
		result = prime * result + ((cronDate == null) ? 0 : cronDate.hashCode());
		result = prime * result + ((cronType == null) ? 0 : cronType.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + (enable ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((remark == null) ? 0 : remark.hashCode());
		result = prime * result + ((storeBlackList == null) ? 0 : storeBlackList.hashCode());
		result = prime * result + ((storeId == null) ? 0 : storeId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		HolidayEntity other = (HolidayEntity) obj;
		if (calendarType != other.calendarType)
			return false;
		if (companyBlackList == null) {
			if (other.companyBlackList != null)
				return false;
		} else if (!companyBlackList.equals(other.companyBlackList))
			return false;
		if (companyId == null) {
			if (other.companyId != null)
				return false;
		} else if (!companyId.equals(other.companyId))
			return false;
		if (createUserId == null) {
			if (other.createUserId != null)
				return false;
		} else if (!createUserId.equals(other.createUserId))
			return false;
		if (cron == null) {
			if (other.cron != null)
				return false;
		} else if (!cron.equals(other.cron))
			return false;
		if (cronContext == null) {
			if (other.cronContext != null)
				return false;
		} else if (!cronContext.equals(other.cronContext))
			return false;
		if (cronDate == null) {
			if (other.cronDate != null)
				return false;
		} else if (!cronDate.equals(other.cronDate))
			return false;
		if (cronType != other.cronType)
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (enable != other.enable)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (remark == null) {
			if (other.remark != null)
				return false;
		} else if (!remark.equals(other.remark))
			return false;
		if (storeBlackList == null) {
			if (other.storeBlackList != null)
				return false;
		} else if (!storeBlackList.equals(other.storeBlackList))
			return false;
		if (storeId == null) {
			if (other.storeId != null)
				return false;
		} else if (!storeId.equals(other.storeId))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HolidayEntity [name=" + name + ", remark=" + remark + ", cron=" + cron + ", companyId=" + companyId
				+ ", storeId=" + storeId + ", duration=" + duration + ", type=" + type + ", enable=" + enable
				+ ", cronType=" + cronType + ", companyBlackList=" + companyBlackList + ", storeBlackList="
				+ storeBlackList + ", createUserId=" + createUserId + ", cronContext=" + cronContext + ", cronDate="
				+ cronDate + ", calendarType=" + calendarType + "]";
	}
	
}
