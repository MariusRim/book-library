package com.library.booklibrary.exception.error;

import org.springframework.http.HttpStatus;

public enum ApplicationError {

    SYSTEM_ERR("Internal server error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("Requested service is unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    BOOK_ALREADY_EXISTS("This book already exists", HttpStatus.BAD_REQUEST),
    BOOK_DOESNT_EXIST("Requested book doesn't exist", HttpStatus.BAD_REQUEST),
    RESERVATION_INVALID_BOOK_ALREADY_TAKEN("Reservation is invalid. This book is already taken",
            HttpStatus.BAD_REQUEST),
    RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD("Reservation is invalid. Reservation period exceeds allowed " +
            "boundaries", HttpStatus.BAD_REQUEST),
    RESERVATION_INVALID_MAX_RESERVATIONS_REACHED("Reservation is invalid. This client has already reached " +
            "the maximum number of reservations.", HttpStatus.BAD_REQUEST),
    CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE("It is not possible to request both only taken and only available "
            + "books.", HttpStatus.BAD_REQUEST);

    private final String errorName;
    private final String message;
    private final HttpStatus httpStatus;

    ApplicationError(String message, HttpStatus httpStatus) {
        this.errorName = this.name();
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getErrorName() {
        return errorName;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
