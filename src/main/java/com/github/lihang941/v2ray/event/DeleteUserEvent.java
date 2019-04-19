package com.github.lihang941.v2ray.event;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : lihang941
 * @since : 2019/4/19
 */
@Accessors(chain = true)
@Data
public class DeleteUserEvent {
    private String email;
}
