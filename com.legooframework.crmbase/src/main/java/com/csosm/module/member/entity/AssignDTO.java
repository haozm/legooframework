package com.csosm.module.member.entity;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Splitter;

public class AssignDTO {
	
	private Integer employeeId;
	
	private Integer count;
	
	public AssignDTO(Integer employeeId,Integer count) {
		this.employeeId = employeeId;
		this.count = count;
	}
	
	
	public static List<AssignDTO> valueOf(String params){
		List<String> assginStrs = Splitter.on(",").splitToList(params);
		return assginStrs.stream().map(x -> {
			List<String> assgins = Splitter.on(":").splitToList(x);
			return new AssignDTO(Integer.parseInt(assgins.get(0)),Integer.parseInt(assgins.get(1)));
		}).collect(Collectors.toList());
	}


	public Integer getEmployeeId() {
		return employeeId;
	}


	public Integer getCount() {
		return count;
	}


	public void setEmployeeId(Integer employeeId) {
		this.employeeId = employeeId;
	}


	public void setCount(Integer count) {
		this.count = count;
	}


	@Override
	public String toString() {
		return "AssignDTO [employeeId=" + employeeId + ", count=" + count + "]";
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((employeeId == null) ? 0 : employeeId.hashCode());
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
		AssignDTO other = (AssignDTO) obj;
		if (count == null) {
			if (other.count != null)
				return false;
		} else if (!count.equals(other.count))
			return false;
		if (employeeId == null) {
			if (other.employeeId != null)
				return false;
		} else if (!employeeId.equals(other.employeeId))
			return false;
		return true;
	}
	
	
	
}
