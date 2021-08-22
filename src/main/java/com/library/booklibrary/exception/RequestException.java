package com.library.booklibrary.exception;

import com.library.booklibrary.exception.error.ApplicationError;
import org.springframework.http.HttpStatus;

public class RequestException extends RuntimeException {

    private final String errorName;

    private final String errorMessage;

    private final HttpStatus httpStatus;

    public RequestException(ApplicationError error) {
        super(error.getMessage());
        this.errorName = error.getErrorName();
        this.errorMessage = error.getMessage();
        this.httpStatus = error.getHttpStatus();
    }

    public RequestException(ApplicationError error, Exception exception) {
        super(error.getMessage() + ". " + exception.getMessage(), exception);
        this.errorName = error.getErrorName();
        this.errorMessage = error.getMessage();
        this.httpStatus = error.getHttpStatus();
    }

    public String getErrorName() {
        return errorName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
