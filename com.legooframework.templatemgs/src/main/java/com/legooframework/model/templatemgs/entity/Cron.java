package com.legooframework.model.templatemgs.entity;

import java.util.Arrays;
import java.util.Optional;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class Cron {

	public static enum DayOfWeek {
		
		SUN("周日"), MON("周一"), TUE("周二"), WED("周三"), THU("周四"), FRI("周五"), SAT("周六");
		
		private final String name;
		
		private DayOfWeek(String name) {
			this.name = name;
		}
		
		public static DayOfWeek checkAndGet(String name) {
			Optional<DayOfWeek> dayOfWeekOpt = Arrays.stream(DayOfWeek.values()).filter(x -> x.name().equals(name)).findFirst();
			Preconditions.checkArgument(dayOfWeekOpt.isPresent(),String.format("DayOfWeek 值范围为【%s】","SUN, MON, TUE, WED, THU, FRI, SAT"));
			return dayOfWeekOpt.get();
		}
		
		
		public String getName() {
			return this.name;
		}
	}
	
	public static enum CalendarType{
		FULLDATE,EVERYYEAR,EVERYMONTH,EVERYWEEK;
		
		public static CalendarType checkAndGet(String name) {
			Optional<CalendarType> calendarTypeOpt = Arrays.stream(CalendarType.values()).filter(x -> x.name().equals(name)).findFirst();
			Preconditions.checkArgument(calendarTypeOpt.isPresent(),String.format("CalendarType 值范围为【%s】","FULLDATE,EVERYYEAR,EVERYMONTH,EVERYWEEK"));
			return calendarTypeOpt.get();
		}
	}
	
	private Cron(String cron,String context) {
		this.context = context;
		this.cron = cron;
	}
	
	
	private String cron;
	
	private String context;
	
	public static Cron ofEveryWeek(DayOfWeek dayOfWeek) {
		String cron = String.format("0 0 0 ? * %s", dayOfWeek.name());
		String context = String.format("每%s", dayOfWeek.getName());
		return new Cron(cron, context);
	}

	public static Cron ofDate(Integer year, Integer month, Integer day) {
		String cron = String.format("0 0 0 %s %s ? %s", day, month, year);
		String context = String.format("%s年%s月%日", year,month,day);
		return new Cron(cron, context);
	}

	public static Cron ofEveryYear(Integer month, Integer day) {
		String cron = String.format("0 0 0 %s %s ? *", day, month);
		String context = String.format("每年%s月%s日", month,day);
		return new Cron(cron,context);
	}

	public static Cron ofEveryMonth(Integer day) {
		String cron = String.format("0 0 0 %s * ? *", day);
		String context = String.format("每年每月%s日", day);
		return new Cron(cron, context);
	}

	public String getCron() {
		return cron;
	}

	public String getContext() {
		return context;
	}

}
