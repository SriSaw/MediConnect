package com.mediconnect.exception;

import org.springframework.http.HttpStatus;

/**
 * Base abstract exception demonstrating OOP inheritance and exception handling hierarchy.
 * All domain-specific and database-specific runtime exceptions extend this class.
 */
public abstract class MediConnectException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    protected MediConnectException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }

    protected MediConnectException(String message, Throwable cause, HttpStatus status, String errorCode) {
        super(message, cause);
        this.status = status;
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
