package com.github.lihang941.v2ray.resource;

import com.github.lihang941.v2ray.Constant;
import com.github.lihang941.v2ray.bean.User;
import com.github.lihang941.v2ray.service.UserService;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.List;

/**
 * 检查登录和权限
 *
 * @author : lihang941
 * @since : 2019/4/19
 */
public class AdminPermissionFilter implements Handler<RoutingContext> {

    private String[] permissionNames;

    private static UserService userService = UserService.userService;

    public AdminPermissionFilter(String... permissionNames) {
        this.permissionNames = permissionNames;
    }

    @Override
    public void handle(RoutingContext event) {
        String authorization = event.request().getHeader(Constant.HEADER_AUTHORIZATION);
        String[] split;

        if (authorization == null || authorization.trim().equals("") || !authorization.contains(":")
                || (split = authorization.split(":")).length > 2
        ) {
            event.response().setStatusCode(401).end();
            return;
        }

        String email = split[0];
        String password = split[1];
        User user;


        if ((user = userService.get(email)) == null || !user.getPassword().equals(password)) {
            event.response().setStatusCode(401).end();
            return;
        }

        List<String> list = Arrays.asList(user.getPermission());

        for (String permissionName : permissionNames) {
            if (!list.contains(permissionName)) {
                event.response().setStatusCode(401).end();
                return;
            }
        }

        event.next();
    }
}
