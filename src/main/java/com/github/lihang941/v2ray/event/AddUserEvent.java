package com.github.lihang941.v2ray.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
@Accessors(chain = true)
@Data
public class AddUserEvent {
    private int alterId;
    private String id;
    private String email;
}
