package com.legooframework.model.wechat.entity;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.legooframework.model.core.jdbc.BatchSetter;

public class WechatLabelPO implements BatchSetter {

	private final int id;

	private final Long accountId;

	private final String name;

	private final String pyfull;

	private final String pyShort;

	private final int isTemporary;

	private int operation = 0;// 0为插入操作 1为更新操作

	public WechatLabelPO(int id, Long accountId, String name, String pyfull, String pyShort, int isTemporary) {
		super();
		this.id = id;
		this.accountId = accountId;
		this.name = name;
		this.pyfull = pyfull;
		this.pyShort = pyShort;
		this.isTemporary = isTemporary;
	}

	public void execUpdateOperation() {
		this.operation = 1;
	}

	public void execInsertOperation() {
		this.operation = 0;
	}

	public boolean isInsertOperation() {
		return this.operation == 0;
	}

	public boolean isUpdateOperation() {
		return this.operation == 1;
	}

	@Override
	public void setValues(PreparedStatement ps) throws SQLException {
		if (isInsertOperation())
			addValues(ps);
		if (isUpdateOperation())
			updateValues(ps);
	}

	private void addValues(PreparedStatement ps) throws SQLException {
		ps.setInt(1, this.id);
		ps.setLong(2, this.accountId);
		ps.setString(3, this.name);
		ps.setString(4, this.pyfull);
		ps.setString(5, this.pyShort);
		ps.setInt(6, this.isTemporary);
	}

	private void updateValues(PreparedStatement ps) throws SQLException {
		ps.setLong(1, this.accountId);
		ps.setString(2, this.name);
		ps.setString(3, this.pyfull);
		ps.setString(4, this.pyShort);
		ps.setInt(5, this.isTemporary);
		ps.setInt(6, this.id);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accountId == null) ? 0 : accountId.hashCode());
		result = prime * result + id;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pyShort == null) ? 0 : pyShort.hashCode());
		result = prime * result + ((pyfull == null) ? 0 : pyfull.hashCode());
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
		WechatLabelPO other = (WechatLabelPO) obj;
		if (accountId == null) {
			if (other.accountId != null)
				return false;
		} else if (!accountId.equals(other.accountId))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pyShort == null) {
			if (other.pyShort != null)
				return false;
		} else if (!pyShort.equals(other.pyShort))
			return false;
		if (pyfull == null) {
			if (other.pyfull != null)
				return false;
		} else if (!pyfull.equals(other.pyfull))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WechatLabelPO [id=" + id + ", accountId=" + accountId + ", name=" + name + ", pyfull=" + pyfull
				+ ", pyShort=" + pyShort + "]";
	}

}
