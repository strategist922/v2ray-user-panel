package com.github.lihang941.v2ray.resource;

import com.github.lihang941.v2ray.bean.User;
import com.github.lihang941.v2ray.service.UserService;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
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
        router.post("/users").method(HttpMethod.POST).handler(event -> {
            User user = event.getBodyAsJson().mapTo(User.class);
            userService.add(user);
            event.response().setStatusCode(200).end();
        });

        router.delete("/users/:email").handler(event -> {
            String email = event.pathParam("email");
            event.response().setStatusCode(200).end(String.valueOf(userService.get(email) != null));
        });

        router.get("/users/:email").handler(event -> {
            String email = event.pathParam("email");
            User user = userService.get(email);
            if (user == null) {
                event.response().setStatusCode(404).end();
            } else {
                event.response().setStatusCode(200).end(Json.encode(user));
            }
        });

        router.get("/users").handler(event -> {
            HttpServerResponse response = event.response();
            response.end(Json.encode(userService.getUsers().values()));
        });
    }
}
