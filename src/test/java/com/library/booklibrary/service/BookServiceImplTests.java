package com.library.booklibrary.service;

import com.library.booklibrary.assertion.BookAssertions;
import com.library.booklibrary.assertion.BookReservationAssertions;
import com.library.booklibrary.entity.Book;
import com.library.booklibrary.entity.BookReservation;
import com.library.booklibrary.exception.RequestException;
import com.library.booklibrary.exception.ServiceException;
import com.library.booklibrary.exception.error.ApplicationError;
import com.library.booklibrary.mockdata.BookMock;
import com.library.booklibrary.mockdata.BookReservationMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BookServiceImplTests {

    private BookServiceImpl bookService;

    @Mock
    private JsonFileService jsonFileService;

    @BeforeEach
    public void loadBookService() {
        this.bookService = new BookServiceImpl(jsonFileService,
                "book-storage-files",
                "all-books.json",
                "book-reservations.json",
                2,
                1);
    }

    @Test
    public void testCreateBookSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookMock.createMockBook())));

        Book requestBook = BookMock.createMockBook();
        requestBook.setGuid(2L);

        Book returnedBook = bookService.createBook(requestBook);

        BookAssertions.assertEquals(requestBook, returnedBook);
    }

    @Test
    public void testCreateBookServiceException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenThrow(new ServiceException("test exception"));


        Assertions.assertThrows(ServiceException.class, () -> bookService.createBook(BookMock.createMockBook()));
    }

    @Test
    public void testCreateBookBookExistsException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));


        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.createBook(BookMock.createMockBook()));

        Assertions.assertEquals(ApplicationError.BOOK_ALREADY_EXISTS.getErrorName(), requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.BOOK_ALREADY_EXISTS.getHttpStatus(), requestException.getHttpStatus());
    }

    @Test
    public void testTakeBookSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> {
                    ArrayList<Book> newList = new ArrayList<>();

                    Book firstBook = BookMock.createMockBook();
                    Book secondBook = BookMock.createMockBook();
                    secondBook.setGuid(2L);

                    newList.add(firstBook);
                    newList.add(secondBook);

                    return newList;
                });
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookReservationMock.createMockBookReservation())));

        BookReservation requestReservation = new BookReservation(
                2L,
                "Test Dave",
                LocalDate.now()
        );

        BookReservation returnedReservation = bookService.takeBook(2L, requestReservation);

        BookReservationAssertions.assertEquals(requestReservation, returnedReservation);
    }

    @Test
    public void testTakeBookServiceException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenThrow(new ServiceException("test exception"));


        Assertions.assertThrows(ServiceException.class, () -> bookService.takeBook(1L,
                BookReservationMock.createMockBookReservation()));
    }

    @Test
    public void testTakeBookBookDoesntExistException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookReservationMock.createMockBookReservation())));

        BookReservation requestReservation = new BookReservation(
                2L,
                "Test Dave",
                LocalDate.now()
        );

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.takeBook(2L, requestReservation));

        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getErrorName(), requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getHttpStatus(), requestException.getHttpStatus());
    }

    @Test
    public void testTakeBookBookAlreadyTakenException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookReservationMock.createMockBookReservation())));

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.takeBook(1L, BookReservationMock.createMockBookReservation()));

        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_BOOK_ALREADY_TAKEN.getErrorName(),
                requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_BOOK_ALREADY_TAKEN.getHttpStatus(),
                requestException.getHttpStatus());
    }

    @Test
    public void testTakeBookExceedsPeriodException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> new ArrayList<>());

        BookReservation requestReservation = BookReservationMock.createMockBookReservation();
        requestReservation.setTakenUntilDate(LocalDate.now().plusMonths(3));

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.takeBook(1L, requestReservation));

        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD.getErrorName(),
                requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD.getHttpStatus(),
                requestException.getHttpStatus());
    }

    @Test
    public void testTakeBookMaxReservationsException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> {
                    ArrayList<Book> newList = new ArrayList<>();

                    Book firstBook = BookMock.createMockBook();
                    Book secondBook = BookMock.createMockBook();
                    secondBook.setGuid(2L);

                    newList.add(firstBook);
                    newList.add(secondBook);

                    return newList;
                });
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookReservationMock.createMockBookReservation())));

        BookReservation requestReservation = BookReservationMock.createMockBookReservation();
        requestReservation.setBookGuid(2L);

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.takeBook(2L, requestReservation));

        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_MAX_RESERVATIONS_REACHED.getErrorName(),
                requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.RESERVATION_INVALID_MAX_RESERVATIONS_REACHED.getHttpStatus(),
                requestException.getHttpStatus());
    }

    @Test
    public void testGetBookByGuidSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));

        Book requestBook = BookMock.createMockBook();
        Book returnedBook = bookService.getBookByGuid(1L);

        BookAssertions.assertEquals(requestBook, returnedBook);
    }

    @Test
    public void testGetBookServiceException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenThrow(new ServiceException("test exception"));


        Assertions.assertThrows(ServiceException.class, () -> bookService.getBookByGuid(1L));
    }

    @Test
    public void testGetBookByGuidBookDoesntExistException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.getBookByGuid(2L));

        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getErrorName(), requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getHttpStatus(), requestException.getHttpStatus());
    }

    @Test
    public void testListAllBooksByFiltersAllParametersSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> BookMock.createMockBookList());
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> Collections.singletonList(BookReservationMock.createMockBookReservation()));

        Book expectedBook = BookMock.createMockBook();
        List<Book> returnedList = bookService.listAllBooksByFilter(expectedBook.getName(),
                expectedBook.getAuthor(),
                expectedBook.getCategory(),
                expectedBook.getLanguage(),
                expectedBook.getIsbn(),
                true,
                false);

        Assertions.assertNotNull(returnedList);
        Assertions.assertEquals(returnedList.size(), 1);
        BookAssertions.assertEquals(expectedBook, returnedList.get(0));
    }

    @Test
    public void testListAllBooksByFiltersOneParameterSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), eq(Book.class)))
                .thenAnswer(invocation -> BookMock.createMockBookList());
        when(jsonFileService.readFromFileToList(any(), eq(BookReservation.class)))
                .thenAnswer(invocation -> Collections.singletonList(BookReservationMock.createMockBookReservation()));

        Book expectedBook = BookMock.createMockBook();
        List<Book> returnedList = bookService.listAllBooksByFilter(expectedBook.getName(),
                null,
                null,
                null,
                null,
                false,
                false);

        Assertions.assertNotNull(returnedList);
        Assertions.assertEquals(returnedList.size(), 6);
        BookAssertions.assertEquals(expectedBook, returnedList.get(0));
    }

    @Test
    public void testListAllBooksByFiltersServiceException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenThrow(new ServiceException("test exception"));

        Book expectedBook = BookMock.createMockBook();
        Assertions.assertThrows(ServiceException.class, () -> bookService.listAllBooksByFilter(expectedBook.getName(),
                expectedBook.getAuthor(),
                expectedBook.getCategory(),
                expectedBook.getLanguage(),
                expectedBook.getIsbn(),
                false,
                true));
    }

    @Test
    public void testListAllBooksByFiltersCantRequestBothException() throws Exception {
        Book expectedBook = BookMock.createMockBook();

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.listAllBooksByFilter(expectedBook.getName(),
                        expectedBook.getAuthor(),
                        expectedBook.getCategory(),
                        expectedBook.getLanguage(),
                        expectedBook.getIsbn(),
                        true,
                        true));

        Assertions.assertEquals(ApplicationError.CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE.getErrorName(),
                requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE.getHttpStatus(),
                requestException.getHttpStatus());
    }

    @Test
    public void testDeleteBookByGuidSuccess() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> new ArrayList<>(Collections.singletonList(BookMock.createMockBook())));

        bookService.deleteBookByGuid(1L);

        verify(jsonFileService, times(1)).writeToFile(any(), any());
    }

    @Test
    public void testDeleteBookByGuidServiceException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenThrow(new ServiceException("test exception"));

        Assertions.assertThrows(ServiceException.class, () -> bookService.deleteBookByGuid(1L));
    }

    @Test
    public void testDeleteBookByGuidBookDoesntExistException() throws Exception {
        when(jsonFileService.readFromFileToList(any(), any()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));

        RequestException requestException = Assertions.assertThrows(RequestException.class,
                () -> bookService.deleteBookByGuid(2L));

        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getErrorName(), requestException.getErrorName());
        Assertions.assertEquals(ApplicationError.BOOK_DOESNT_EXIST.getHttpStatus(), requestException.getHttpStatus());
    }
}
