package com.legooframework.model.families.entity;

public class MemberBO {
	
	private Integer id;
	
	private String name;
	
	private String cardNo;
	
	private String phone;
	
	private String employeeName;

	public MemberBO() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	@Override
	public String toString() {
		return String.format("MemberBO [id=%s, name=%s, cardNo=%s, phone=%s, employeeName=%s]", id, name, cardNo, phone,
				employeeName);
	}
	
	
}
