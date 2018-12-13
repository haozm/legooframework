package com.legooframework.model.wechat.entity;

import java.util.Arrays;
import java.util.Optional;

public enum ModifyOperation {
	ADD("添加", "add"), REMOVE("移除", "remove"), INIT("初始化", "init");
	private final String name;

	private final String value;

	private ModifyOperation(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public static Optional<ModifyOperation> valOf(String value) {
		return Arrays.stream(values()).filter(x -> x.value.equals(value)).findFirst();
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

}
