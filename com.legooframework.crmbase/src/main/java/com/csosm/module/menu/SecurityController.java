package com.csosm.module.menu;

import com.csosm.commons.adapter.LoginUserContext;
import com.csosm.commons.mvc.BaseController;
import com.csosm.module.menu.entity.ResourceDto;
import com.csosm.module.menu.entity.ResourcesFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/online")
public class SecurityController extends BaseController {

    @RequestMapping(value = "/userdetail.json")
    public Map<String, Object> loadLoginUserDetail() {
        LoginUserContext user = loadLoginUser(null);
        return wrapperResponse(user.toMap());
    }

    /**
     * 获取可访问的菜单资源
     *
     * @param request HttpServletRequest
     * @return Map
     */
    @RequestMapping(value = "/actived/menu.json")
    public Map<String, Object> loadMenuByLoginUser(HttpServletRequest request) {
        LoginUserContext user = loadLoginUser(null);
        Optional<ResourceDto> resource = getBean(SecAccessService.class, request).loadResByAccount(user);
        return wrapperResponse(resource.orElse(null));
    }

    /**
     * 获取可访问的菜单资源
     *
     * @param request HttpServletRequest
     * @return Map
     */
    @RequestMapping(value = "/all/menu.json")
    public Map<String, Object> loadAllMenu(HttpServletRequest request) {
        LoginUserContext user = loadLoginUser(null);
        Optional<ResourceDto> menus = getBean(ResourcesFactory.class, request).getAllReource(1L);
        return wrapperResponse(menus.orElse(null));
    }

}
