package com.github.lihang941.v2ray;

import com.github.lihang941.v2ray.resource.UserResource;
import com.github.lihang941.v2ray.tool.Logger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;


/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class RestServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = new Logger(RestServerVerticle.class.getName());
    private String host;
    private String staticPath;
    private int port;
    private HttpServer server;
    private Router router;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        JsonObject config = config().getJsonObject("http");
        host = config.getString("host", "0.0.0.0");
        port = config.getInteger("port", 9000);
        staticPath = config.getString("staticPath", "static");
        server = vertx.createHttpServer();
        onInitRouter();
    }

    private void onInitRouter() {
        router = Router.router(vertx);
        new UserResource().onInit(router);

        // 跨域
        router.route().handler(CorsHandler.create("*")
                .allowedMethods(new HashSet<>(Arrays.asList(
                        HttpMethod.GET,
                        HttpMethod.POST,
                        HttpMethod.DELETE,
                        HttpMethod.HEAD,
                        HttpMethod.OPTIONS,
                        HttpMethod.PUT,
                        HttpMethod.OPTIONS)))
                .exposedHeaders(new HashSet<>(Arrays.asList("Origin", "X-Requested-With", "Accept", "User-Agent", "Authorization", "Content-Type")))
                .allowedHeaders(new HashSet<>(Arrays.asList("Origin", "X-Requested-With", "Accept", "User-Agent", "Authorization", "Content-Type", "offset", "size", "PlatformType")))
        );

        File staticFilePath = new File(staticPath);


        if (!staticFilePath.exists()) {
            if (staticFilePath.mkdirs()) {
                LOGGER.info("create static folder = " + staticFilePath.getAbsolutePath() + " success");
            }

        }

        if (!staticFilePath.isDirectory()) {
            throw new RuntimeException("请检查 http.static.path 设置");
        }

        router.route().path("/static/*").handler(StaticHandler.create()
                .setAllowRootFileSystemAccess(true)
                .setWebRoot(new File(staticPath).getAbsolutePath()));

    }


    @Override
    public void start() throws Exception {
        super.start();
        server.requestHandler(router).listen(port, host, e -> {
                    if (e.succeeded()) {
                        LOGGER.info("rest server start success listening -> " + host + ":" + port);
                    } else {
                        LOGGER.info("rest server start failure");
                    }
                }
        );
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        server.close(e ->
                LOGGER.info("rest server close")
        );
    }
}
