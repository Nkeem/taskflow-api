package com.taskflow.exception;

public class BaseException extends RuntimeException {

    private final BusinessError error;

    public BaseException(BusinessError error) {
        super(error.getMessage());
        this.error = error;
    }

    public BaseException(BusinessError error, String message) {
        super(message);
        this.error = error;
    }

    public BusinessError getError() {
        return error;
    }
}
