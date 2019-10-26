package com.csosm.module.webchat.group;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.csosm.commons.entity.BaseEntity;
import com.csosm.module.base.entity.StoreEntity;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class WeixinGroupEntity extends BaseEntity<String>{

	private String name;

	private final Integer storeId;

	private final Integer companyId;
	
	private int type;
	
	private Integer guideId;
	
	private int size = 0;
	
	WeixinGroupEntity(String id, String name, Integer storeId, Integer companyId, int type,int size) {
		super(id);
		this.name = name;
		this.storeId = storeId;
		this.companyId = companyId;
		this.type = type;
		this.size = size;
	}
	
	WeixinGroupEntity(String id, String name, Integer storeId, Integer companyId, int type,Integer guideId,int size) {
		super(id);
		this.name = name;
		this.storeId = storeId;
		this.companyId = companyId;
		this.type = type;
		this.guideId = guideId;
		this.size = size;
	}
	
	
	public WeixinGroupEntity(String id, Object createUserId, Date createTime, String name, Integer storeId,
			Integer companyId, int type) {
		super(id, createUserId, createTime);
		this.name = name;
		this.storeId = storeId;
		this.companyId = companyId;
		this.type = type;
	}
	
	
	
	public WeixinGroupEntity(String id, Object createUserId, Date createTime, String name, Integer storeId,
			Integer companyId, int type, int size) {
		super(id, createUserId, createTime);
		this.name = name;
		this.storeId = storeId;
		this.companyId = companyId;
		this.type = type;
		this.size = size;
	}


	public WeixinGroupEntity modifyName(String name) {
		WeixinGroupEntity clone = null;
		try {
			clone = (WeixinGroupEntity) this.clone();
			clone.name = name;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}

	static WeixinGroupEntity createUserDefinedGroup(StoreEntity store,String name) {
		Objects.requireNonNull(store);
		Preconditions.checkArgument(store.getCompanyId().isPresent(),String.format("门店[%s]无公司信息", store.getId()));
		return new WeixinGroupEntity(UUID.randomUUID().toString().replaceAll("-", ""),
				-1, new Date(), name, store.getId(), 
				store.getCompanyId().get(),
				Type.USER_DEFINED_GROUP.type);
	}
	
	static WeixinGroupEntity createEmployeeGroup(StoreEntity store,String name) {
		Objects.requireNonNull(store);
		Preconditions.checkArgument(store.getCompanyId().isPresent(),String.format("门店[%s]无公司信息", store.getId()));
		return new WeixinGroupEntity(UUID.randomUUID().toString().replaceAll("-", ""),
				-1, new Date(), name, store.getId(), 
				store.getCompanyId().get(),
				Type.EMPLOYEE_GROUP.type);
	}
	
	public static WeixinGroupEntity createGuideGroup(StoreEntity store) {
		Objects.requireNonNull(store);
		Preconditions.checkArgument(store.getCompanyId().isPresent(),String.format("门店[%s]无公司信息", store.getId()));
		return new WeixinGroupEntity(UUID.randomUUID().toString().replaceAll("-", ""),
				-1, new Date(), Type.GUIDE_GROUP.name, store.getId(), 
				store.getCompanyId().get(),
				Type.GUIDE_GROUP.type);
	}
	
	static WeixinGroupEntity valueOf(ResultSet rs) {
		try {
		  String id = rs.getString("id");
          String name = rs.getString("groupName");
          Integer storeId = rs.getInt("storeId");
          Integer companyId = rs.getInt("companyId");
          int type = rs.getInt("type");
          int size = rs.getInt("size");
          return new WeixinGroupEntity(id, name, storeId, companyId, type,size);
		}catch(SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("从数据库中查询分组记录还原成对象时发生异常");
		}
	}
	
	
	public boolean isUserDefinedGroup() {
		return Type.of(this.type) == Type.USER_DEFINED_GROUP ? true:false;
	}
	
	public boolean isCommonGroup() {
		return isUserDefinedGroup() || isEmployeeGroup();
	}
	
	public boolean isAllFriendGroup() {
		return (Type.of(this.type) == Type.ALL_FRIEND_GROUP 
				|| Type.of(this.getId()) == Type.ALL_FRIEND_GROUP)?true:false;
	}
	
	public boolean isEmployeeGroup() {
		return (Type.of(this.type) == Type.EMPLOYEE_GROUP 
				|| Type.of(this.getId()) == Type.EMPLOYEE_GROUP)?true:false;
	}
	
	public boolean isNewFriendGroup(){
		return (Type.of(this.type) == Type.NEW_FRIEND_GROUP 
				|| Type.of(this.getId()) == Type.NEW_FRIEND_GROUP)?true:false;
	}
	
	public boolean isLabelGroup() {
		return (Type.of(this.type) == Type.LABEL_GROUP 
				|| Type.of(this.getId()) == Type.LABEL_GROUP)?true:false;
	}
	
	public boolean isEditable() {
		return 1 == Type.of(this.type).getEditable();
	}
	
	
	public boolean isGrantable() {
		return 1 == Type.of(this.type).getGrantable();
	}
	
	public static enum Type{
		
		USER_DEFINED_GROUP(0,"","自定义组",1,1),
		
		ALL_FRIEND_GROUP(2,"0000","所有好友",0,1),
		
		EMPLOYEE_GROUP(1,"","职员组",1,1),
		
		NEW_FRIEND_GROUP(3,"2222","新增好友",0,1),
		
		GUIDE_GROUP(4,"1111","导购组",0,0),
		
		LABEL_GROUP(5,"","标签组",0,0);
		
		private final String id;
		
		private final String name;
		
		private final int type;
		
		private final int editable;
		
		private final int grantable;
		
		private Type(int type,String id,String name,int editable,int grantable){
			this.type = type;
			this.id = id;
			this.name = name;
			this.editable = editable;
			this.grantable = grantable;
		}
		
		public static Type of(int type) {
			for(Type t : values()) {
				if(t.getType() == type) return t;
			}
			return null;
		}
		
		public static Type of(String id) {
			if(id == null) return null;
			for(Type t : values()) {
				if(t.getId().equals(id)) return t;
			}
			return null;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getType() {
			return type;
		}

		public int getEditable() {
			return editable;
		}

		public int getGrantable() {
			return grantable;
		}
		
	}

	public String getName() {
		return name;
	}

	public Integer getStoreId() {
		return storeId;
	}

	public Integer getCompanyId() {
		return companyId;
	}

	public int getType() {
		return type;
	}
	
	public Optional<Integer> getGuideId() {
		if(Type.of(this.type) != Type.EMPLOYEE_GROUP) return Optional.absent();
		if(this.guideId == null) return Optional.absent();
		return null;
	}

	public int getSize() {
		return size;
	}
	
	
	public Map<String,Object> toViewMap(){
		Map<String, Object> retMap = Maps.newHashMap();
        retMap.put("id", this.getId());
        retMap.put("label", this.getName());
        retMap.put("size", this.getSize());
        retMap.put("editable", Type.of(this.type).getEditable());
        retMap.put("type", this.type);
        return retMap;
	}
	
	public Map<String, Object> toMap() {
        Map<String, Object> paramMap = Maps.newHashMap();
        paramMap.put("id", this.getId());
        paramMap.put("storeId", this.getStoreId());
        paramMap.put("companyId", this.getCompanyId());
        paramMap.put("groupName", this.getName());
        paramMap.put("type", this.type);
        paramMap.put("createUserId", this.getCreateUserId());
        return paramMap;
    }



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((companyId == null) ? 0 : companyId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + size;
		result = prime * result + ((storeId == null) ? 0 : storeId.hashCode());
		result = prime * result + type;
		return result;
	}


	public boolean isIdSame(WeixinGroupEntity group) {
		return this.getId().equals(group.getId());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof WeixinGroupEntity))
			return false;
		WeixinGroupEntity other = (WeixinGroupEntity) obj;
		if (companyId == null) {
			if (other.companyId != null)
				return false;
		} else if (!companyId.equals(other.companyId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (size != other.size)
			return false;
		if (storeId == null) {
			if (other.storeId != null)
				return false;
		} else if (!storeId.equals(other.storeId))
			return false;
		if (type != other.type)
			return false;
		return true;
	}



	@Override
	public String toString() {
		return String.format("WeixinGroupEntity [name=%s, storeId=%s, companyId=%s, type=%s, size=%s]",
				name, storeId, companyId, type, size);
	}
	
	
}
