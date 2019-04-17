package com.github.lihang941.v2ray;

import com.github.lihang941.v2ray.tool.FileTool;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class V2rayServiceApplication {


    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        JsonObject config = getConfig();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(new RestServerVerticle(), deploymentOptions);
        vertx.deployVerticle(new V2rayServerVerticle(), deploymentOptions);
    }

    private static JsonObject getConfig() {
        try (InputStream resourceAsStream = V2rayServiceApplication.class.getClassLoader().getResourceAsStream("service.json")) {
            JsonObject config = new JsonObject(FileTool.readFile(resourceAsStream));
            File file = new File("service.json");
            if (file.exists()) {
                try {
                    JsonObject jsonObject = FileTool.readFileToJson(file.getAbsolutePath());
                    config = config.mergeIn(jsonObject, true);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
