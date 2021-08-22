package com.library.booklibrary.exception;

import com.library.booklibrary.exception.error.ApplicationError;
import com.library.booklibrary.exception.error.RequestError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationExceptionHandler.class);

    @ExceptionHandler(value = {RequestException.class})
    public ResponseEntity<RequestError> requestExceptionHandler(RequestException e) {
        logger.error("httpStatus={}, errorName={}, errorMessage={}, detailMessage={}",
                e.getHttpStatus(), e.getErrorName(), e.getErrorMessage(), e.getMessage());

        RequestError requestError = new RequestError();
        requestError.setHttpStatus(e.getHttpStatus().toString());
        requestError.setErrorName(e.getErrorName());
        requestError.setText(e.getErrorMessage());

        return new ResponseEntity<>(requestError, new HttpHeaders(),
                e.getHttpStatus() != null ? e.getHttpStatus() : HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<RequestError> anyExceptionHandler(Exception e) {
        return requestExceptionHandler(new RequestException(ApplicationError.SYSTEM_ERR, e));
    }
}
