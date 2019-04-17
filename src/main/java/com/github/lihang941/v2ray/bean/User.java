package com.github.lihang941.v2ray.bean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
@Accessors(chain = true)
@Data
public class User {
    // id
    private String id;
    // 邮箱
    private String email;
    // 流量总量
    private Long totalTraffic;
    // 已用流量
    private Long usedTraffic;
    // 结算方式
    private int settlementMethod;
    // 密码
    private String password;
    // 创建时间
    private Long createTime;
    // 权限
    private String[] permission;
    // 状态
    private int status;
}
