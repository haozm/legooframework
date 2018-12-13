package com.legooframework.model.usergroups.entity;

public class SimpleGroupDto {
	
	private final String id;
	
	private final String label;
	
	private final int size;
	
	private final String type;

	public SimpleGroupDto(String id, String label, int size, String type) {
		super();
		this.id = id;
		this.label = label;
		this.size = size;
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public int getSize() {
		return size;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return "SimpleGroupDto [id=" + id + ", label=" + label + ", size=" + size + ", type=" + type + "]";
	}
	
	
}
