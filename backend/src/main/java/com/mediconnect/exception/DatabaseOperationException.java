package com.mediconnect.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when a low-level JDBC or database operation encounters an error.
 * Translates low-level SQLExceptions or Spring DataAccessExceptions into clean application-level exceptions.
 */
public class DatabaseOperationException extends MediConnectException {

    public DatabaseOperationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR");
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_ERROR");
    }
}
