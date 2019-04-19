package com.github.lihang941.v2ray.resource;

import com.github.lihang941.v2ray.PermissionName;
import com.github.lihang941.v2ray.bean.User;
import com.github.lihang941.v2ray.event.AddUserEvent;
import com.github.lihang941.v2ray.event.DeleteUserEvent;
import com.github.lihang941.v2ray.service.UserService;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class UserResource implements RouterFactory {

    private UserService userService = UserService.userService;

    @Override
    public void onInit(Router router) {

        router.post("/users").method(HttpMethod.POST)
                .handler(new AdminPermissionFilter(PermissionName.admin))
                .handler(event -> {
                    User user = event.getBodyAsJson().mapTo(User.class);
                    if (userService.get(user.getEmail()) != null) {
                        event.response().setStatusCode(400).end("email = " + user.getEmail() + " is exist.");
                        return;
                    }
                    event.vertx().eventBus().send(AddUserEvent.class.getName(), new AddUserEvent()
                                    .setAlterId(user.getAlterId())
                                    .setEmail(user.getEmail())
                                    .setId(user.getId())
                            , res -> {
                                if (res.succeeded()) {
                                    userService.add(user);
                                    event.response().setStatusCode(200).end();
                                } else {
                                    event.response().setStatusCode(400).end(res.cause().getMessage());
                                }
                            });
                });

        router.delete("/users/:email")
                .handler(new AdminPermissionFilter(PermissionName.admin))
                .handler(event -> {
                    String email = event.pathParam("email");
                    if (userService.get(email) == null) {
                        event.response().setStatusCode(400).end("email = " + email + " not exist.");
                        return;
                    }
                    event.vertx().eventBus().send(DeleteUserEvent.class.getName(), new DeleteUserEvent()
                                    .setEmail(email)
                            , res -> {
                                if (res.succeeded()) {
                                    userService.delete(email);
                                    event.response().setStatusCode(200);
                                } else {
                                    event.response().setStatusCode(400).end(res.cause().getMessage());
                                }
                            });
                });

        router.get("/users/:email")
                .handler(new AdminPermissionFilter(PermissionName.admin))
                .handler(event -> {
                    String email = event.pathParam("email");
                    User user = userService.get(email);
                    if (user == null) {
                        event.response().setStatusCode(404).end();
                    } else {
                        event.response().setStatusCode(200).end(Json.encode(user));
                    }
                });

        router.get("/users")
                .handler(new AdminPermissionFilter(PermissionName.admin))
                .handler(event -> {
                    HttpServerResponse response = event.response();
                    response.end(Json.encode(userService.getUsers().values()));
                });
    }
}
