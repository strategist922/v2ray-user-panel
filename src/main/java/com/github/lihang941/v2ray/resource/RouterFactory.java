package com.github.lihang941.v2ray.resource;

import io.vertx.ext.web.Router;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public interface RouterFactory {
    void onInit(Router router);
}
