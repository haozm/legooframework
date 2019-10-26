package com.csosm.module.base.entity;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.entity.BaseEntity;
import com.csosm.commons.event.BusEvent;
import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

import java.util.Map;

public class SystemlogEntity extends BaseEntity<Integer> implements BusEvent {

    private final String operation, message, subType, crud;
    private final static String CRUD_C = "C";
    private final static String CRUD_R = "R";
    private final static String CRUD_U = "U";
    private final static String CRUD_D = "D";
    private LoginUserContext user;

    @Override
    public void setLoginUser(LoginUserContext user) {
        this.user = user;
    }

    private SystemlogEntity(Class<?> clazz, String fun, String message, String subType, String crud) {
        super(0, 0, DateTime.now().toDate());
        this.operation = String.format("%s.%s", clazz.getName(), fun);
        this.message = message;
        this.subType = subType;
        this.crud = crud;
    }

    LoginUserContext getUser() {
        return user;
    }

    public static SystemlogEntity create(Class<?> clazz, String fun, String message, String subType) {
        return new SystemlogEntity(
                clazz, fun, message, subType, CRUD_C);
    }

    public static SystemlogEntity update(Class<?> clazz, String fun, String message, String subType) {
        return new SystemlogEntity(clazz, fun, message, subType, CRUD_U);
    }

    public static SystemlogEntity delete(Class<?> clazz, String fun, String message, String subType) {
        return new SystemlogEntity(clazz, fun, message, subType, CRUD_D);
    }

    public static SystemlogEntity read(Class<?> clazz, String fun, String message, String subType) {
        return new SystemlogEntity(clazz, fun, message, subType, CRUD_R);
    }

    @Override
    protected Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("operation", operation);
        map.put("message", message);
        map.put("subType", subType);
        map.put("crud", crud);
        map.put("companyId", user.getCompany().isPresent() ? user.getCompany().get().getId() : -1);
        map.put("createUserId", user.getUserId());
        return map;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("operation", operation)
                .add("message", message)
                .add("subType", subType)
                .add("crud", crud)
                .toString();
    }
}
