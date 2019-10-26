package com.legooframework.model.templatemgs.entity;

import java.sql.ResultSet;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.entity.BaseEntityAction;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginUser;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.templatemgs.entity.Cron.CalendarType;
import com.legooframework.model.templatemgs.entity.HolidayEntity.CronType;

public class HolidayEntityAction extends BaseEntityAction<HolidayEntity> {

	protected HolidayEntityAction() {
		super("HolidayEntity");
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 查找id对应的节日
	 * @param holidayId
	 * @return
	 */
	public Optional<HolidayEntity> findById(String holidayId) {
		Objects.requireNonNull(holidayId);
		Map<String, Object> params = Maps.newHashMap();
		params.put("id", holidayId);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "findById", params);
		HolidayEntity entity = getNamedParameterJdbcTemplate().query(sql, params, getResultSetExtractor());
		return Optional.ofNullable(entity);
	}
	
	/**
	 * 加载id对应的节日
	 * @param holidayId
	 * @return
	 */
	public HolidayEntity loadById(String holidayId) {
		Optional<HolidayEntity> opt = findById(holidayId);
		Preconditions.checkState(opt.isPresent(), String.format("节日【%s】不存在", holidayId));
		return opt.get();
	}	
	
	public List<HolidayEntity> loadSystemHolidays(){
		Map<String,Object> map = Maps.newHashMap();
		map.put("system", 1);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "find_holidays", map);
		List<HolidayEntity> result = getNamedParameterJdbcTemplate().query(sql, map, new RowMapperImpl());
		if(CollectionUtils.isEmpty(result)) return Collections.EMPTY_LIST;
		return result;
	}
	
	public List<HolidayEntity> loadCompanyHolidays(CrmOrganizationEntity company){
		Map<String,Object> map = Maps.newHashMap();
		map.put("company", 1);
		map.put("companyId", company.getId());
		String sql = getStatementFactory().getExecSql("HolidayEntity", "find_holidays", map);
		List<HolidayEntity> result = getNamedParameterJdbcTemplate().query(sql, map, new RowMapperImpl());
		if(CollectionUtils.isEmpty(result)) return Collections.EMPTY_LIST;
		return result;
	}
	
	public List<HolidayEntity> loadStoreHolidays(CrmStoreEntity store){
		Map<String,Object> map = Maps.newHashMap();
		map.put("store", 1);
		map.put("companyId", store.getCompanyId());
		map.put("storeId", store.getId());
		String sql = getStatementFactory().getExecSql("HolidayEntity", "find_holidays", map);
		List<HolidayEntity> result = getNamedParameterJdbcTemplate().query(sql, map, new RowMapperImpl());
		if(CollectionUtils.isEmpty(result)) return Collections.EMPTY_LIST;
		return result;
	}
	/**
	 * 解析成cron表达式
	 * @param year
	 * @param month
	 * @param day
	 * @param dayOfWeek
	 * @return
	 */
	private Cron parseCron(Integer year, Integer month, Integer day, Cron.DayOfWeek dayOfWeek) {
		if (null == year) {
			if (null == month) {
				if (null != day)
					return Cron.ofEveryMonth(day);
				if (null != dayOfWeek) 
					return Cron.ofEveryWeek(dayOfWeek);
				return Cron.ofEveryMonth(day);
			}
			return Cron.ofEveryYear(month, day);
		}
		return Cron.ofDate(year, month, day);
	}
	
	/**
	 * 新增系统节日
	 * @param loginUser
	 * @param name
	 * @param remark
	 * @param year
	 * @param month
	 * @param day
	 * @param dayOfWeek
	 * @param cronType
	 * @param duration
	 */
	public void addSystemHoliday(LoginContext loginUser,String name, String remark, Integer year,Integer month,Integer day,Cron.DayOfWeek dayOfWeek,
			CronType cronType,LocalDate cronDate,CalendarType calendarType,Integer duration) {
		Cron cron = parseCron(year,month,day,dayOfWeek);
		HolidayEntity holiday = HolidayEntity.createSystemHoliday(name, remark, cron.getCron(),cron.getContext(),cronType,cronDate,calendarType, duration);
		holiday.setCreator(loginUser);
		addHoliday(holiday);
	}
	/**
	 * 新增公司节日
	 * @param loginUser
	 * @param comapny
	 * @param name
	 * @param remark
	 * @param year
	 * @param month
	 * @param day
	 * @param dayOfWeek
	 * @param cronType
	 * @param duration
	 */
	public void addCompanyHoliday(LoginContext loginUser,CrmOrganizationEntity comapny,String name, String remark, Integer year,Integer month,Integer day,Cron.DayOfWeek dayOfWeek,
			CronType cronType,LocalDate cronDate,CalendarType calendarType,Integer duration) {
		Cron cron = parseCron(year,month,day,dayOfWeek);
		HolidayEntity holiday = HolidayEntity.createCompanyHoliday(comapny, name, remark, cron.getCron(),cron.getContext(),cronType,cronDate,calendarType, duration);
		holiday.setCreator(loginUser);
		addHoliday(holiday);
	}
	/**
	 * 新增门店节日
	 * @param loginUser
	 * @param store
	 * @param name
	 * @param remark
	 * @param year
	 * @param month
	 * @param day
	 * @param dayOfWeek
	 * @param cronType
	 * @param duration
	 */
	public void addStoreHoliday(LoginContext loginUser,CrmStoreEntity store,String name, String remark, Integer year,Integer month,Integer day,Cron.DayOfWeek dayOfWeek,
			CronType cronType,LocalDate cronDate,CalendarType calendarType,Integer duration) {
		Cron cron = parseCron(year,month,day,dayOfWeek);
		HolidayEntity holiday = HolidayEntity.createStoreHoliday(store, name, remark, cron.getCron(),cron.getContext(),cronType,cronDate,calendarType,duration);
		holiday.setCreator(loginUser);
		addHoliday(holiday);
	}
	/**
	 * 新增节日
	 * @param holiday
	 */
	public void addHoliday(HolidayEntity holiday) {
		Objects.requireNonNull(holiday);
		Map<String,Object> params = holiday.toMap();
		String sql = getStatementFactory().getExecSql("HolidayEntity", "insert_holiday", params);
		int result = getNamedParameterJdbcTemplate().update(sql, params);
		Preconditions.checkArgument(1 == result, "新增节日失败");
	}
	
	/**
	 * 修改节日信息
	 * @param holidayId
	 * @param name
	 * @param remark
	 * @param year
	 * @param month
	 * @param day
	 * @param dayOfWeek
	 * @param cronType
	 * @param duration
	 */
	public void modifyHoliday(String holidayId,String name, String remark, Integer year,Integer month,Integer day,Cron.DayOfWeek dayOfWeek,
			CronType cronType,LocalDate cronDate,CalendarType calendarType,Integer duration) {
		HolidayEntity holiday = loadById(holidayId);
		Cron cron = parseCron(year,month,day,dayOfWeek);
		Optional<HolidayEntity> modifierOpt = holiday.modify(name, remark, cron.getCron(),cron.getContext(), cronType, cronDate,calendarType,duration);
		if(modifierOpt.isPresent())
			updateHoliday(modifierOpt.get());
	}
	
	/**
	 * 更新节日
	 * @param holiday
	 */
	private void updateHoliday(HolidayEntity holiday) {
		Map<String,Object> params = holiday.toMap();
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_holiday", params);
		int result = getNamedParameterJdbcTemplate().update(sql, params);
		Preconditions.checkArgument(1 == result, "更新节日失败");
	}
	/**
	 * 禁用系统节日
	 * @param holidayId
	 */
	public void disableSystemHoliday(String holidayId) {
		changeHolidayEnable(holidayId,false);
	}
	/**
	 * 启用系统节日
	 * @param holidayId
	 */
	public void enableSystemHoliday(String holidayId) {
		changeHolidayEnable(holidayId,true);
	}
	
	public void changeHolidayEnable(String holidayId,boolean enable) {
		HolidayEntity holiday = loadById(holidayId);
		changeHolidayEnable(holiday, enable);
	}
	
	public void changeHolidayEnable(HolidayEntity holiday,boolean enable) {
		if(enable) {
			if(holiday.isEnable()) return ;
			holiday.enable();
		}else {
			if(!holiday.isEnable()) return ;
			holiday.disable();
		}
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_enable", holiday.toMap());
		getNamedParameterJdbcTemplate().update(sql, holiday.toMap());
	}
	/**
	 * 禁用公司节日
	 * @param company
	 * @param holidayId
	 */
	public void disableCompanyHoliday(CrmOrganizationEntity company,String holidayId) {
		HolidayEntity holiday = loadById(holidayId);
		if(holiday.isBelongTo(company)) {
			changeHolidayEnable(holiday,false);
			return ;
		}
		holiday.unallowCompany(company);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_company_blackList", holiday.toMap());
		getNamedParameterJdbcTemplate().update(sql, holiday.toMap());
	}
	
	/**
	 * 启用公司节日
	 * @param company
	 * @param holidayId
	 */
	public void enableCompanyHoliday(CrmOrganizationEntity company,String holidayId) {
		HolidayEntity holiday = loadById(holidayId);
		if(holiday.isBelongTo(company)) {
			changeHolidayEnable(holiday,true);
			return ;
		}
		holiday.allowCompany(company);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_company_blackList", holiday.toMap());
		getNamedParameterJdbcTemplate().update(sql, holiday.toMap());
	}
	
	/**
	 * 禁用门店节日
	 * @param store
	 * @param holidayId
	 */
	public void disableStoreHoliday(CrmStoreEntity store,String holidayId) {
		HolidayEntity holiday = loadById(holidayId);
		if(holiday.isBelongTo(store)) {
			changeHolidayEnable(holiday,false);
			return ;
		}
		holiday.unallowStore(store);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_store_blackList", holiday.toMap());
		getNamedParameterJdbcTemplate().update(sql, holiday.toMap());
	}
	
	/**
	 * 启用门店节日
	 * @param store
	 * @param holidayId
	 */
	public void enableStoreHoliday(CrmStoreEntity store,String holidayId) {
		HolidayEntity holiday = loadById(holidayId);
		if(holiday.isBelongTo(store)) {
			changeHolidayEnable(holiday,true);
			return ;
		}
		holiday.allowStore(store);
		String sql = getStatementFactory().getExecSql("HolidayEntity", "update_store_blackList", holiday.toMap());
		getNamedParameterJdbcTemplate().update(sql, holiday.toMap());
		
	}
	
	

	protected ResultSetExtractor<HolidayEntity> getResultSetExtractor() {
		// TODO Auto-generated method stub
		return new ResultSetExtractorImpl();
	}

	class RowMapperImpl implements RowMapper<HolidayEntity> {
		@Override
		public HolidayEntity mapRow(ResultSet resultSet, int i) throws SQLException {
			return buildByResultSet(resultSet);
		}
	}

	class ResultSetExtractorImpl implements ResultSetExtractor<HolidayEntity> {

		@Override
		public HolidayEntity extractData(ResultSet resultSet) throws SQLException, DataAccessException {
			if (resultSet.next()) {
				return buildByResultSet(resultSet);
			}
			return null;
		}
	}
	
	private HolidayEntity buildByResultSet(ResultSet resultSet) throws SQLException {
		return HolidayEntity.valueOf(resultSet.getString("id"),resultSet.getString("name"), resultSet.getString("remark"), 
				resultSet.getString("cron"),resultSet.getString("cronContext"), resultSet.getString("cronType"),resultSet.getString("cronDate"), resultSet.getString("calendarType"), resultSet.getInt("companyId"),
				resultSet.getInt("storeId"), resultSet.getInt("duration"), resultSet.getString("type"),resultSet.getInt("enable"),
				resultSet.getString("companyBlackList"),resultSet.getString("storeBlackList"));
	}

	
	@Override
	protected RowMapper<HolidayEntity> getRowMapper() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
