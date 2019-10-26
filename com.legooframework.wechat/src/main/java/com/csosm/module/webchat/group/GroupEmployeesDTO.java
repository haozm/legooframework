package com.csosm.module.webchat.group;

import java.util.List;

import com.google.common.collect.Lists;

public class GroupEmployeesDTO {
	
	private String groupId;
	
	private String groupName;
	
	private List<String> guideIds = Lists.newArrayList();

	public GroupEmployeesDTO(String groupId, String groupName, List<String> guideIds) {
		super();
		this.groupId = groupId;
		this.groupName = groupName;
		this.guideIds = guideIds;
	}

	public GroupEmployeesDTO(String groupId, String groupName) {
		super();
		this.groupId = groupId;
		this.groupName = groupName;
	}
	public String getGroupId() {
		return groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public List<String> getGuideIds() {
		return guideIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((guideIds == null) ? 0 : guideIds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupEmployeesDTO other = (GroupEmployeesDTO) obj;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (guideIds == null) {
			if (other.guideIds != null)
				return false;
		} else if (!guideIds.equals(other.guideIds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GroupGuideDTO [groupId=" + groupId + ", groupName=" + groupName + ", guideIds=" + guideIds + "]";
	}
	
	
}
