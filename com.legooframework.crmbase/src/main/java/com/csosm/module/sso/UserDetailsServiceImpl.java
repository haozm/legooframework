package com.csosm.module.sso;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.server.AbstractBaseServer;
import com.csosm.module.base.BaseModelServer;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl extends AbstractBaseServer implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] login_info = StringUtils.split(username, '@');
        Preconditions.checkState(ArrayUtils.isNotEmpty(login_info) && login_info.length == 2, "登陆账号格式错误，格式为：公司@账户");
        Integer companyId = Integer.valueOf(login_info[0]);
        String userName = login_info[1];
        LoginUserContext user = baseModelServer.loadByUserName(companyId, userName);
        if (user == null) 
        	throw new UsernameNotFoundException(String.format("账号或密码有误....", username));
        if(!user.getEmployee().isEnabled()) 
        	throw new UsernameNotFoundException(String.format("账户已被禁用....", username));
        user.setLoginName(username);
        if (logger.isDebugEnabled())
            logger.debug(String.format("loadUserByUsername(%s) return %s", username, user));
        return user;
    }

    private BaseModelServer baseModelServer;

    public void setBaseModelServer(BaseModelServer baseModelServer) {
        this.baseModelServer = baseModelServer;
    }

}
