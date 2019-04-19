package com.github.lihang941.v2ray;

/**
 * @author : lihang941
 * @since : 2019/4/19
 */
public class ErrorMessageException extends RuntimeException {
    private String message;

    public ErrorMessageException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
