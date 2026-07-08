package com.taskflow.exception;

import org.springframework.http.HttpStatus;

public enum BusinessError {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "user.not.found", "User not found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "user.already.exists", "User already exists"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "project.not.found", "Project not found"),
    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "task.not.found", "Task not found"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "bad.request", "Bad request"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "validation.error", "Validation error"),
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "internal.server.error",
            "Internal server error"
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    BusinessError(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
