package com.library.booklibrary.mockdata;

import com.library.booklibrary.entity.BookReservation;

import java.time.LocalDate;

public class BookReservationMock {

    public static BookReservation createMockBookReservation() {
        return new BookReservation(
                1L,
                "Test Client",
                LocalDate.now()
        );
    }
}
