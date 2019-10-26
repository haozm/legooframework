package com.legooframework.model.families.entity;

public class MemberFamilyBO {
	
	private MemberBO member;
	
	private FamilyBO family;

	public MemberFamilyBO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MemberBO getMember() {
		return member;
	}

	public void setMember(MemberBO member) {
		this.member = member;
	}

	public FamilyBO getFamily() {
		return family;
	}

	public void setFamily(FamilyBO family) {
		this.family = family;
	}

	@Override
	public String toString() {
		return String.format("MemberFamilyBO [member=%s, family=%s]", member, family);
	}
	
	
	
}
