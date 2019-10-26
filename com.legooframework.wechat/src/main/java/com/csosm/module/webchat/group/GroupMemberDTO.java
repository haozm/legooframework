package com.csosm.module.webchat.group;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GroupMemberDTO {

	private final String id;

	private final String label;

	private int size;

	private List<MemberDTO> members = Lists.newArrayList();

	public GroupMemberDTO(String id, String label, int size, List<MemberDTO> members) {
		super();
		this.id = id;
		this.label = label;
		this.size = size;
		this.members = members;
	}

	public GroupMemberDTO(String id, String label, int size) {
		super();
		this.id = id;
		this.label = label;
		this.size = size;
	}
	
	public GroupMemberDTO(String id, String label) {
		super();
		this.id = id;
		this.label = label;
	}
	
	public int getSize() {
		return members.size();
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<MemberDTO> getMembers() {
		return members;
	}

	public void setMembers(List<MemberDTO> members) {
		this.members = members;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		result = prime * result + size;
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
		GroupMemberDTO other = (GroupMemberDTO) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GroupMemberDTO [id=" + id + ", label=" + label + ", size=" + size + ", members=" + members + "]";
	}

	public Map<String, Object> toMap() {
		Map<String, Object> retMap = Maps.newHashMap();
		retMap.put("id", this.getId());
		retMap.put("label", this.getLabel());
		retMap.put("size", this.getSize());
		List<Map<String, Object>> children = Lists.newArrayList();
		for (MemberDTO member : this.getMembers()) {
			children.add(member.toMap());
		}
		retMap.put("children", children);
		return retMap;
	}

}
