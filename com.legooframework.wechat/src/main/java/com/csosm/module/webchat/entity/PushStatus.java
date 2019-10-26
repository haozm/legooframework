package com.csosm.module.webchat.entity;

public enum PushStatus {
	UNPUSH(0),
	PUSHED(1),
	PUSH_TO_DEVICE(2),
	SEND_ADD_MEMBER(3),
	ADD_MEMBER_FAIL(4),
	ADD_MEMBER_SUCCESS(5),
	ADD_MEMBER_REAL_SUCCESS(6);
	
	private final Integer value;
	
	private PushStatus(int value) {
		this.value = value;
	}
	
	public static PushStatus valueOf(Integer val) {
		for(PushStatus status : values())
		{
			if(status.value.intValue() == val.intValue()) return status;
		}
		return null;
	}

	public Integer getValue() {
		return value;
	}
	
	
}
