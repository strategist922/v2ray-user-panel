package com.github.lihang941.v2ray.tool;

import java.text.DateFormat;
import java.util.Date;

/**
 * @author : lihang941
 * @since : 2019/4/17
 */
public class Logger {

    private static DateFormat formatter = DateFormat.getDateTimeInstance();
    private String name;

    public Logger(String name) {
        this.name = name;
    }

    private String format(String type, String msg) {
        return formatter.format(new Date()) + " - " + type + " [" + name + "] - " + msg;
    }


    public void info(String msg) {
        System.out.println(format("INFO", msg));
    }

    public void warn(String msg) {
        System.err.println(format("WARN", msg));
    }

}

