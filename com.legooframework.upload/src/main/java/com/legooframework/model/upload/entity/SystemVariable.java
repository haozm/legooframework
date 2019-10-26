package com.legooframework.model.upload.entity;

import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.common.collect.Maps;

/**
 * 系统变量类，主要用于定义系统变量
 *
 * @author Administrator
 */
public enum SystemVariable {

	DATE("date", DateTime.now().toString("yyyyMMdd"));

	private final String name;

	private final Object value;

	private SystemVariable(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public static Map<String, Object> toMap() {

		Map<String, Object> retMap = Maps.newHashMap();

		for (SystemVariable var : values()) {
			retMap.put(var.name, DateTime.now().toString("yyyyMMdd"));
		}
		return retMap;
	}

	public static Set<String> names() {
		return toMap().keySet();
	}

}
