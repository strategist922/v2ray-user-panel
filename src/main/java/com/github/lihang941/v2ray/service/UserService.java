package com.github.lihang941.v2ray.service;

import com.github.lihang941.v2ray.bean.User;
import com.github.lihang941.v2ray.tool.FileTool;
import com.github.lihang941.v2ray.tool.Logger;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class UserService {
    private final Logger logger = new Logger(UserService.class.getName());
    private String userJsonPath = "user.json";
    private Map<String, User> users = Collections.synchronizedMap(new HashMap<>());

    public static UserService userService = new UserService();

    private UserService() {
        read();
    }

    private void read() {
        try {
            JsonObject jsonObject = FileTool.readFileToJson(userJsonPath);
            for (Map.Entry<String, Object> entry : jsonObject) {
                String key = entry.getKey();
                User user = jsonObject.getJsonObject(key).mapTo(User.class);
                users.put(key, user);
            }
        } catch (Exception e) {
            logger.warn("读取用户数据失败" + e.getMessage());
        }
    }

    private void wirte() {
        try {
            FileTool.wirte(userJsonPath, Json.encode(users));
        } catch (Exception e) {
            logger.warn("写入用户数据失败" + e.getMessage());
        }
    }


    public void add(User user) {
        users.put(user.getEmail(), user);
        wirte();
    }


    public User get(String email) {
        return users.get(email);
    }

    public User delete(String email) {
        return users.remove(email);
    }

    public Map<String, User> getUsers() {
        return new HashMap<>(users);
    }

}
