package com.csosm.commons.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * 基础实体建模
 *
 * @author Smart
 */
public abstract class BaseEntity<T extends Serializable> implements Cloneable {

    private T id;

    protected BaseEntity(T id) {
        this.id = id;
    }

    protected BaseEntity(T id, Object createUserId, Date createTime) {
        this.id = id;
        this.createUserId = createUserId;
        this.createTime = createTime;
    }

    protected BaseEntity(T id, Object createUserId, Date createTime, Object modifyUserId, Date modifyTime) {
        this.id = id;
        this.createUserId = createUserId;
        this.createTime = createTime;
        this.modifyUserId = modifyUserId;
        this.modifyTime = modifyTime;
    }

    public T getId() {
        return id;
    }

    protected void setCreateUserId(Object createUserId) {
        this.createUserId = createUserId;
    }

    protected void setModifyUserId(Object modifyUserId) {
        this.modifyUserId = modifyUserId;
    }

    private Object createUserId;
    private Date createTime;

    protected void setId(T id) {
        this.id = id;
    }

 public static int Guid=10000;
    
    public static String generateId() {
		Guid+=1;
		long now = System.currentTimeMillis();  
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy");  
		String time=dateFormat.format(now);
		String info=now+"";
		int ran=0;
		if(Guid>99999) Guid=10000;    	
		ran=Guid;
		return time+info.substring(2, info.length())+ran;  
    }
    
    
    private Object modifyUserId;
    private Date modifyTime;

    protected Map<String, Object> toMap() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("id", getId());
        map.put("createUserId", createUserId);
        map.put("createTime", createTime);
        map.put("modifyUserId", modifyUserId);
        map.put("modifyTime", modifyTime);
        return map;
    }

    public boolean isMyGod(LoginUserContext userContext) {
        Preconditions.checkNotNull(userContext);
        return Objects.equal(this.createUserId, userContext.getUserId());
    }

    protected void init4Create(Object createUserId) {
        Preconditions.checkNotNull(createUserId);
        if (createUserId instanceof Integer) {
            this.createUserId = createUserId;
        } else if (createUserId instanceof LoginUserContext) {
            this.createUserId = ((LoginUserContext) createUserId).getUserId();
        }
        this.createTime = DateTime.now().toDate();
    }

    protected void init4LastModify(Object modifyUserId) {
        Preconditions.checkNotNull(modifyUserId);
        this.modifyUserId = modifyUserId;
        this.modifyTime = DateTime.now().toDate();
    }

    public Object getCreateUserId() {
        return createUserId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Object getModifyUserId() {
        return modifyUserId;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
