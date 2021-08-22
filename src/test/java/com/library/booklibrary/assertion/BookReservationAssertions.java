package com.library.booklibrary.assertion;

import com.library.booklibrary.entity.BookReservation;
import org.junit.jupiter.api.Assertions;

public class BookReservationAssertions {

    public static void assertEquals(BookReservation reservation1, BookReservation reservation2) {
        Assertions.assertEquals(reservation1.getBookGuid(), reservation2.getBookGuid());
        Assertions.assertEquals(reservation1.getClientName(), reservation2.getClientName());
        Assertions.assertEquals(reservation1.getTakenUntilDate(), reservation2.getTakenUntilDate());
    }
}
