package com.csosm.commons.event;

import com.csosm.commons.adapter.LoginUserContext;

/**
 * 标识接口 不做任何 接口实现
 */
public interface BusEvent {

    void setLoginUser(LoginUserContext user);
}
