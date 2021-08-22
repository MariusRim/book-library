package com.library.booklibrary.entity;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class BookReservation {

    @NotNull
    @Min(value = 0)
    private Long bookGuid;

    @NotBlank
    private String clientName;

    @NotNull
    @FutureOrPresent
    private LocalDate takenUntilDate;

    public BookReservation(Long bookGuid,
                           String clientName,
                           LocalDate takenUntilDate) {
        this.bookGuid = bookGuid;
        this.clientName = clientName;
        this.takenUntilDate = takenUntilDate;
    }

    public Long getBookGuid() {
        return bookGuid;
    }

    public void setBookGuid(Long bookGuid) {
        this.bookGuid = bookGuid;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public LocalDate getTakenUntilDate() {
        return takenUntilDate;
    }

    public void setTakenUntilDate(LocalDate takenUntilDate) {
        this.takenUntilDate = takenUntilDate;
    }
}
