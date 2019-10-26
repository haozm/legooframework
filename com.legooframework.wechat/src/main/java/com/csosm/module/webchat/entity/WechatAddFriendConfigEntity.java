package com.csosm.module.webchat.entity;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.util.Strings;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.OrganizationEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class WechatAddFriendConfigEntity extends BaseEntity<Integer> {
	// 发送内容
	private String content;
	// 发送个数
	private int sendNum = 15;
	// 发送开始时间
	private String beginTime = "10:00";
	// 发送结束时间
	private String endTime = "22:00";
	// 是否启动
	private int enable = 0;
	// 第几轮
	private int runTimes = 1;
	//门店ID、公司ID
	private Integer storeId,companyId;
	
	private WechatAddFriendConfigEntity(Integer id, String content, int sendNum, String beginTime, String endTime,
			int enable, int runTimes,Integer storeId,Integer companyId) {
		super(id);
		this.content = content;
		this.sendNum = sendNum;
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.enable = enable;
		this.runTimes = runTimes;
		this.storeId = storeId;
		this.companyId = companyId;
	}
	
	public WechatAddFriendConfigEntity() {
		super(0);
	}
	
	public WechatAddFriendConfigEntity(String content,StoreEntity store,OrganizationEntity company) {
		super(0);
		this.content = content;
		this.storeId = store.getId();
		this.companyId = company.getId();
	}
	
	static WechatAddFriendConfigEntity valueOf(ResultSet rs) {
		try {
			Integer id = rs.getInt("id");
			String content = rs.getString("content");
			int sendNum = rs.getInt("sendNum");
			String beginTime = rs.getString("beginTime");
			String endTime = rs.getString("endTime");
			int enable = rs.getInt("enable");
			int runTimes = rs.getInt("runTimes");
			Integer storeId = rs.getInt("storeId");
			Integer companyId = rs.getInt("companyId");
			WechatAddFriendConfigEntity entity = new WechatAddFriendConfigEntity(id, content, sendNum, beginTime, 
					endTime, enable, runTimes,storeId,companyId);
			return entity;
		} catch (SQLException e) {
			throw new RuntimeException("读取ResultSet创建WechatAddFriendConfigEntity发生异常");
		}
	}
	
	public static enum SuitType{
		ENABLE_STORE(1),DISABLE_STORE(0),ALL_STORE(2);
		private final int value;
		
		private SuitType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
		
		public static SuitType valueOf(int value) {
			for(SuitType type : values()) {
				if(type.value == value) return type;
			}
			return null;
		}
		
	}

	public boolean isEnable() {
		return this.enable == 1;
	}

	public WechatAddFriendConfigEntity modify(String content) {
		Preconditions.checkArgument(Strings.isNotEmpty(content));
		WechatAddFriendConfigEntity clone = null;
		try {
			clone = (WechatAddFriendConfigEntity) this.clone();
			clone.content = content;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("克隆修改WechatAddFriendConfigEntity属性发生异常");
		}
		return clone;
	}
	
	public WechatAddFriendConfigEntity modifyToEnable() {
		WechatAddFriendConfigEntity clone = null;
		try {
			clone = (WechatAddFriendConfigEntity) this.clone();
			clone.enable = 1;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("克隆修改WechatAddFriendConfigEntity属性发生异常");
		}
		return clone;
	}
	
	public void enable() {
		this.enable = 1;
	}
	
	public void disable() {
		this.enable = 0;
	}
	
	public WechatAddFriendConfigEntity modifyToDisable() {
		WechatAddFriendConfigEntity clone = null;
		try {
			clone = (WechatAddFriendConfigEntity) this.clone();
			clone.enable = 0;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("克隆修改WechatAddFriendConfigEntity属性发生异常");
		}
		return clone;
	}
	
	public String getContent() {
		return content;
	}

	public int getSendNum() {
		return sendNum;
	}

	public int getEnable() {
		return enable;
	}

	public int getRunTimes() {
		return runTimes;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public Map<String,Object> toVO(){
		Map<String,Object> map = Maps.newHashMap();
		map.put("id", this.getId());
		map.put("content", this.content);
		map.put("sendNum", this.sendNum);
		map.put("beginTime", this.beginTime);
		map.put("endTime", this.endTime);
		map.put("enable", this.enable);
		map.put("runTimes", this.runTimes);
		map.put("storeId", this.storeId);
		map.put("companyId", this.companyId);
		return map;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((beginTime == null) ? 0 : beginTime.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + enable;
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + runTimes;
		result = prime * result + sendNum;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof WechatAddFriendConfigEntity))
			return false;
		WechatAddFriendConfigEntity other = (WechatAddFriendConfigEntity) obj;
		if (beginTime == null) {
			if (other.beginTime != null)
				return false;
		} else if (!beginTime.equals(other.beginTime))
			return false;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (enable != other.enable)
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (runTimes != other.runTimes)
			return false;
		if (sendNum != other.sendNum)
			return false;
		return true;
	}
	
	public boolean existConfig(StoreEntity store) {
		if(store == null) return false;
		if(this.storeId == store.getId()) return true;
		return false;
	}
	@Override
	public String toString() {
		return String.format(
				"WechatAddFriendConfigEntity [content=%s, sendNum=%s, beginTime=%s, endTime=%s, enable=%s, runTimes=%s]",
				content, sendNum, beginTime, endTime, enable, runTimes);
	}
	
}
