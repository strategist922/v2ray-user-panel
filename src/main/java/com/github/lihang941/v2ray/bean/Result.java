package com.github.lihang941.v2ray.bean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : lihang941
 * @since : 2019/4/19
 */
@Accessors(chain = true)
@Data
public class Result {
    private boolean success;
    private String content;
    private String errorMessage;
}
