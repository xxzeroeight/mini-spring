package com.minispring.context.exception;

public class BeanCreationException extends RuntimeException {
    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
