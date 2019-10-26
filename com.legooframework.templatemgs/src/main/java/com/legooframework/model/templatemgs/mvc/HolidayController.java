package com.legooframework.model.templatemgs.mvc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.legooframework.model.core.base.runtime.LoginContext;
import com.legooframework.model.core.base.runtime.LoginContextHolder;
import com.legooframework.model.core.jdbc.JdbcQuerySupport;
import com.legooframework.model.core.web.BaseController;
import com.legooframework.model.core.web.JsonMessage;
import com.legooframework.model.core.web.JsonMessageBuilder;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntity;
import com.legooframework.model.crmadapter.entity.CrmOrganizationEntityAction;
import com.legooframework.model.crmadapter.entity.CrmStoreEntity;
import com.legooframework.model.crmadapter.entity.CrmStoreEntityAction;
import com.legooframework.model.templatemgs.entity.Cron;
import com.legooframework.model.templatemgs.entity.HolidayEntity.CronType;
import com.legooframework.model.templatemgs.entity.HolidayEntity;
import com.legooframework.model.templatemgs.entity.HolidayEntityAction;

@RestController
@RequestMapping(value = "/holiday")
public class HolidayController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(HolidayController.class);
    /**
     * 查找单个节日信息
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/find.json")
    public JsonMessage findHoliday(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkArgument(requestBody.containsKey("holidayId"), "请求参数缺少【holidayId】");
        String holidayId = MapUtils.getString(requestBody, "holidayId");
        HolidayEntity holiday = getBean(HolidayEntityAction.class, request).loadById(holidayId);
        return JsonMessageBuilder.OK().withPayload(holiday.toMap()).toMessage();
    }

	/**
	 * 查询节日列表
	 * 
	 * @param requestBody
	 * @param request
	 * @return
	 */
	@PostMapping(value = "/list.json")
	public JsonMessage listHoliday(@RequestBody(required = false) Map<String, Object> requestBody, HttpServletRequest request) {
		LoginContext user = LoginContextHolder.get();
		List<Map<String, Object>> result = Lists.newArrayList();
		Integer storeId = MapUtils.getInteger(requestBody, "storeId");
		Integer enable = MapUtils.getInteger(requestBody, "enable");
		Map<String, Object> params = Maps.newHashMap();
		params.put("enable", enable);
		if (null != storeId || user.isStoreManager()) {
			params.put("companyId", user.getTenantId().intValue());
			params.put("storeId", null == storeId?user.getStoreId():storeId);
			Optional<List<Map<String, Object>>> listOpt = getJdbcQuerySupport(request).queryForList("holiday",
					"load_store_holiday", params);
			if (listOpt.isPresent())
				result = listOpt.get();
		} else if (user.isManager()) {
			Preconditions.checkState(user.isManager(), "登录用户无权限查看系统节日");
			Optional<List<Map<String, Object>>> listOpt = getJdbcQuerySupport(request).queryForList("holiday",
					"load_system_holiday", null);
			if (listOpt.isPresent())
				result = listOpt.get();
		} else if (user.isAreaManagerRole() || user.isBoss() || user.isRegediter()) {
			params.put("companyId", user.getTenantId().intValue());
			Optional<List<Map<String, Object>>> listOpt = getJdbcQuerySupport(request).queryForList("holiday",
					"load_company_holiday", params);
			if (listOpt.isPresent())
				result = listOpt.get();
		}
		if (result.isEmpty())
			return JsonMessageBuilder.OK().withPayload(result).toMessage();
		return JsonMessageBuilder.OK().withPayload(result.stream().map(x -> {
			Long status = (Long) x.get("enable");
			x.put("enable", status == 1L ? true : false);
			Long editable = (Long) x.get("editable");
			x.put("editable", editable == 1L ? true : false);
			return x;
		}).collect(Collectors.toList())).toMessage();
	}
    /**
     * 添加节日
     *
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/add.json")
    public JsonMessage addHoliday(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("addHoliday(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkArgument(requestBody.containsKey("name"), "请求参数缺少节日名称【name】");
        Preconditions.checkArgument(requestBody.containsKey("calendarType"), "请求参数缺少节日周期类型【calendarType】");
        String name = MapUtils.getString(requestBody, "name");
        String remark = MapUtils.getString(requestBody, "remark");
        Integer year = null;
        Integer month = null;
        Integer day = null;
        Cron.DayOfWeek dayOfWeek = null;
        LocalDate cronDate = null;
        Integer duration = 1;
        CronType cronType = CronType.SOLAR;
        String calendarTypeStr = MapUtils.getString(requestBody, "calendarType");
        Cron.CalendarType calendarType = Cron.CalendarType.checkAndGet(calendarTypeStr);
        if (Cron.CalendarType.EVERYWEEK == calendarType) {
            Preconditions.checkArgument(requestBody.containsKey("dayOfWeek"), "请求参数缺少【dayOfWeek】");
            String dayOfWeekStr = MapUtils.getString(requestBody, "dayOfWeek");
            dayOfWeek = Cron.DayOfWeek.checkAndGet(dayOfWeekStr);
        } else {
            Preconditions.checkArgument(requestBody.containsKey("cronDate"), "请求参数缺少节日设置时间【cronDate】");
            Preconditions.checkArgument(requestBody.containsKey("cronType"), "请求参数缺少节日日历类型【cronType】");
            Preconditions.checkArgument(requestBody.containsKey("duration"), "请求参数缺少节日期限【duration】");
            String cronDateStr = MapUtils.getString(requestBody, "cronDate");
            cronDate = LocalDate.parse(cronDateStr, DateTimeFormatter.ISO_DATE);
            duration = MapUtils.getInteger(requestBody, "duration");
            String cronTypeStr = MapUtils.getString(requestBody, "cronType");
            cronType = CronType.checkAndGet(cronTypeStr);
            if (Cron.CalendarType.FULLDATE == calendarType) {
                year = cronDate.getYear();
                month = cronDate.getMonthValue();
                day = cronDate.getDayOfMonth();
            } else if (Cron.CalendarType.EVERYYEAR == calendarType) {
                month = cronDate.getMonthValue();
                day = cronDate.getDayOfMonth();
            } else if (Cron.CalendarType.EVERYMONTH == calendarType) {
                day = cronDate.getDayOfMonth();
            }
        }
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        if (null != storeId || user.isStoreManager()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            Preconditions.checkState(companyOpt.isPresent(), "登录用户无公司信息");
            Optional<CrmStoreEntity> storeOpt = getBean(CrmStoreEntityAction.class, request).findById(companyOpt.get(),
                    null != storeId ? storeId : user.getStoreId());
            getBean(HolidayEntityAction.class, request).addStoreHoliday(user, storeOpt.get(), name, remark, year, month,
                    day, dayOfWeek, cronType, cronDate, calendarType, duration);
        } else if (user.isManager()) {
            getBean(HolidayEntityAction.class, request).addSystemHoliday(user, name, remark, year, month, day,
                    dayOfWeek, cronType, cronDate, calendarType, duration);
            return JsonMessageBuilder.OK().toMessage();
        } else if (user.isAreaManagerRole() || user.isBoss() || user.isRegediter()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            getBean(HolidayEntityAction.class, request).addCompanyHoliday(user, companyOpt.get(), name, remark, year,
                    month, day, dayOfWeek, cronType, cronDate, calendarType, duration);
            return JsonMessageBuilder.OK().toMessage();
        } else {
            throw new IllegalArgumentException("当前登录用户无权限操作节日");
        }
        return JsonMessageBuilder.OK().toMessage();
    }
    /**
     * 修改节日
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/modify.json")
    public JsonMessage modifyHoliday(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("modifyHoliday(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkArgument(requestBody.containsKey("name"), "请求参数缺少节日名称【name】");
        Preconditions.checkArgument(requestBody.containsKey("calendarType"), "请求参数缺少节日周期类型【calendarType】");
        String name = MapUtils.getString(requestBody, "name");
        String remark = MapUtils.getString(requestBody, "remark");
        Integer year = null;
        Integer month = null;
        Integer day = null;
        Cron.DayOfWeek dayOfWeek = null;
        LocalDate cronDate = null;
        Integer duration = 1;
        CronType cronType = CronType.SOLAR;
        String calendarTypeStr = MapUtils.getString(requestBody, "calendarType");
        Cron.CalendarType calendarType = Cron.CalendarType.checkAndGet(calendarTypeStr);
        if (Cron.CalendarType.EVERYWEEK == calendarType) {
            Preconditions.checkArgument(requestBody.containsKey("dayOfWeek"), "请求参数缺少【dayOfWeek】");
            String dayOfWeekStr = MapUtils.getString(requestBody, "dayOfWeek");
            dayOfWeek = Cron.DayOfWeek.checkAndGet(dayOfWeekStr);
        } else {
            Preconditions.checkArgument(requestBody.containsKey("cronDate"), "请求参数缺少节日设置时间【cronDate】");
            Preconditions.checkArgument(requestBody.containsKey("cronType"), "请求参数缺少节日日历类型【cronType】");
            Preconditions.checkArgument(requestBody.containsKey("duration"), "请求参数缺少节日期限【duration】");
            String cronDateStr = MapUtils.getString(requestBody, "cronDate");
            cronDate = LocalDate.parse(cronDateStr, DateTimeFormatter.ISO_DATE);
            duration = MapUtils.getInteger(requestBody, "duration");
            String cronTypeStr = MapUtils.getString(requestBody, "cronType");
            cronType = CronType.checkAndGet(cronTypeStr);
            if (Cron.CalendarType.FULLDATE == calendarType) {
                year = cronDate.getYear();
                month = cronDate.getMonthValue();
                day = cronDate.getDayOfMonth();
            } else if (Cron.CalendarType.EVERYYEAR == calendarType) {
                month = cronDate.getMonthValue();
                day = cronDate.getDayOfMonth();
            } else if (Cron.CalendarType.EVERYMONTH == calendarType) {
                day = cronDate.getDayOfMonth();
            }
        }
        String holidayId = MapUtils.getString(requestBody, "holidayId");
        getBean(HolidayEntityAction.class, request).modifyHoliday(holidayId, name, remark, year, month, day,
                dayOfWeek,
                cronType, cronDate, calendarType, duration);
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 启用节日
     *
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/enable.json")
    public JsonMessage enableHoliday(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("enableHoliday(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkArgument(requestBody.containsKey("holidayId"), "请求参数缺少节日ID");
        String holidayId = MapUtils.getString(requestBody, "holidayId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        if (null != storeId || user.isStoreManager()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            Preconditions.checkState(companyOpt.isPresent(), "登录用户无公司信息");
            Optional<CrmStoreEntity> storeOpt = getBean(CrmStoreEntityAction.class, request).findById(companyOpt.get(),
            		null != storeId?storeId:user.getStoreId());
            Preconditions.checkState(storeOpt.isPresent(), "登录用户无门店信息");
            getBean(HolidayEntityAction.class, request).enableStoreHoliday(storeOpt.get(), holidayId);
        } else if (user.isManager()) {
            getBean(HolidayEntityAction.class, request).enableSystemHoliday(holidayId);
            return JsonMessageBuilder.OK().toMessage();
        } else if (user.isAreaManagerRole() || user.isBoss() || user.isRegediter()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            Preconditions.checkArgument(companyOpt.isPresent(), "当前登录用户无公司信息");
            getBean(HolidayEntityAction.class, request).enableCompanyHoliday(companyOpt.get(), holidayId);
            return JsonMessageBuilder.OK().toMessage();
        } else {
            throw new IllegalArgumentException("当前登录用户无权限操作节日");
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    /**
     * 禁用节日
     *
     * @param requestBody
     * @param request
     * @return
     */
    @PostMapping(value = "/disable.json")
    public JsonMessage disableHoliday(@RequestBody Map<String, Object> requestBody, HttpServletRequest request) {
        if (logger.isDebugEnabled())
            logger.debug(String.format("disableHoliday(%s)", requestBody));
        LoginContext user = LoginContextHolder.get();
        Preconditions.checkArgument(requestBody.containsKey("holidayId"), "请求参数缺少节日ID");
        String holidayId = MapUtils.getString(requestBody, "holidayId");
        Integer storeId = MapUtils.getInteger(requestBody, "storeId");
        if (null != storeId || user.isStoreManager()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            Preconditions.checkState(companyOpt.isPresent(), "登录用户无公司信息");
            Optional<CrmStoreEntity> storeOpt = getBean(CrmStoreEntityAction.class, request).findById(companyOpt.get(),
            		null != storeId?storeId:user.getStoreId());
            Preconditions.checkState(storeOpt.isPresent(), "登录用户无门店信息");
            getBean(HolidayEntityAction.class, request).disableStoreHoliday(storeOpt.get(), holidayId);
        } else if (user.isManager()) {
            getBean(HolidayEntityAction.class, request).disableSystemHoliday(holidayId);
            return JsonMessageBuilder.OK().toMessage();
        } else if (user.isAreaManagerRole() || user.isBoss() || user.isRegediter()) {
            Optional<CrmOrganizationEntity> companyOpt = getBean(CrmOrganizationEntityAction.class, request)
                    .findCompanyById(user.getTenantId().intValue());
            Preconditions.checkArgument(companyOpt.isPresent(), "当前登录用户无公司信息");
            getBean(HolidayEntityAction.class, request).disableCompanyHoliday(companyOpt.get(), holidayId);
            return JsonMessageBuilder.OK().toMessage();
        } else {
            throw new IllegalArgumentException("当前登录用户无权限操作节日");
        }
        return JsonMessageBuilder.OK().toMessage();
    }

    JdbcQuerySupport getJdbcQuerySupport(HttpServletRequest request) {
        return getBean("templateJdbcQuerySupport", JdbcQuerySupport.class, request);
    }

}
