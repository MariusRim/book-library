package com.library.booklibrary.service;

import com.library.booklibrary.entity.Book;
import com.library.booklibrary.entity.BookReservation;
import com.library.booklibrary.exception.RequestException;
import com.library.booklibrary.exception.ServiceException;
import com.library.booklibrary.exception.error.ApplicationError;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookServiceImpl implements BookService {

    private final JsonFileService jsonFileService;

    private final String fileStoragePath;

    private final String allBooksFilename;

    private final String bookReservationFilename;

    private final int reservationPeriodLimit;

    private final int reservationCountLimit;

    public BookServiceImpl(JsonFileService jsonFileService,
                           @Value("${storage.path}") String fileStoragePath,
                           @Value("${storage.books.filename}") String allBooksFilename,
                           @Value("${storage.reservations.filename}") String bookReservationFilename,
                           @Value("${reservation.period}") int reservationPeriodLimit,
                           @Value("${reservation.count.max}") int reservationCountLimit) {
        this.jsonFileService = jsonFileService;
        this.fileStoragePath = fileStoragePath;
        this.allBooksFilename = allBooksFilename;
        this.bookReservationFilename = bookReservationFilename;
        this.reservationPeriodLimit = reservationPeriodLimit;
        this.reservationCountLimit = reservationCountLimit;
    }

    public Book createBook(Book newBook) throws ServiceException, RequestException {
        List<Book> allBooks = getAllBooks();

        Optional<Book> optionalBook = allBooks.stream()
                .filter(book -> book.getGuid().equals(newBook.getGuid()))
                .findAny();
        if (optionalBook.isPresent()) {
            throw new RequestException(ApplicationError.BOOK_ALREADY_EXISTS);
        }

        allBooks.add(newBook);
        jsonFileService.writeToFile(fileStoragePath + "/" + allBooksFilename, allBooks);

        return newBook;
    }

    public BookReservation takeBook(Long bookId,
                                    BookReservation newReservation) throws ServiceException, RequestException {
        List<Book> allBooks = getAllBooks();
        List<BookReservation> allReservations = getAllReservations();

        validateBookReservation(newReservation, allBooks, allReservations);

        allReservations.add(newReservation);
        jsonFileService.writeToFile(fileStoragePath + "/" + bookReservationFilename, allReservations);

        return newReservation;
    }

    public Book getBookByGuid(Long bookId) throws ServiceException, RequestException {
        List<Book> allBooks = getAllBooks();

        return allBooks.stream()
                .filter(book -> book.getGuid().equals(bookId))
                .findAny()
                .orElseThrow(() -> new RequestException(ApplicationError.BOOK_DOESNT_EXIST));
    }

    public List<Book> listAllBooksByFilter(String name,
                                           String author,
                                           String category,
                                           String language,
                                           String isbn,
                                           boolean onlyTaken,
                                           boolean onlyAvailable) throws ServiceException, RequestException {
        //can't request both only taken and only available books. Such don't exist
        if (onlyTaken && onlyAvailable) {
            throw new RequestException(ApplicationError.CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE);
        }

        List<Book> allBooks = getAllBooks();
        List<BookReservation> allReservations = getAllReservations();

        //if either taken or available books are requested, make a list of all taken book indexes
        List<Long> takenBookIds;
        if (onlyTaken || onlyAvailable) {
            takenBookIds = allReservations.stream()
                    .map(BookReservation::getBookGuid)
                    .collect(Collectors.toList());
        } else {
            takenBookIds = new ArrayList<>();
        }

        //return a list of all books that passed the filter
        return allBooks.stream()
                //add a book to the list only if the field matches a filter or the filter is disabled(null/blank)
                //this filter check is done for every field that can be filtered
                .filter(b -> filterByOneField(b.getName(), name))
                .filter(b -> filterByOneField(b.getAuthor(), author))
                .filter(b -> filterByOneField(b.getCategory(), category))
                .filter(b -> filterByOneField(b.getLanguage(), language))
                .filter(b -> filterByOneField(b.getIsbn(), isbn))
                .filter(b -> !onlyTaken || takenBookIds.contains(b.getGuid()))
                .filter(b -> !onlyAvailable || !takenBookIds.contains(b.getGuid()))
                .collect(Collectors.toList());
    }

    public void deleteBookByGuid(Long bookId) throws ServiceException, RequestException {
        List<Book> allBooks = getAllBooks();

        Book book = allBooks.stream()
                .filter(b -> b.getGuid().equals(bookId))
                .findAny()
                .orElseThrow(() -> new RequestException(ApplicationError.BOOK_DOESNT_EXIST));

        allBooks.remove(book);
        jsonFileService.writeToFile(fileStoragePath + "/" + allBooksFilename, allBooks);
    }

    private List<Book> getAllBooks() throws ServiceException {
        return jsonFileService.readFromFileToList(fileStoragePath + "/" + allBooksFilename, Book.class);
    }

    private List<BookReservation> getAllReservations() throws ServiceException {
        return jsonFileService.readFromFileToList(fileStoragePath + "/" + bookReservationFilename,
                BookReservation.class);
    }

    private void validateBookReservation(BookReservation newReservation,
                                         List<Book> allBooks,
                                         List<BookReservation> allReservations) throws RequestException {
        //check if the requested book exists
        allBooks.stream()
                .filter(book -> book.getGuid().equals(newReservation.getBookGuid()))
                .findAny()
                .orElseThrow(() -> new RequestException(ApplicationError.BOOK_DOESNT_EXIST));

        //check if the book isn't already reserved
        Optional<BookReservation> optionalReservation = allReservations.stream()
                .filter(reservation -> reservation.getBookGuid().equals(newReservation.getBookGuid()))
                .findAny();
        if (optionalReservation.isPresent()) {
            throw new RequestException(ApplicationError.RESERVATION_INVALID_BOOK_ALREADY_TAKEN);
        }

        //check if the reservation doesn't exceed the allowed period
        if (ChronoUnit.MONTHS.between(LocalDate.now(), newReservation.getTakenUntilDate()) >= reservationPeriodLimit) {
            throw new RequestException(ApplicationError.RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD);
        }

        //check if this client doesn't already have the maximum number of reservations
        List<BookReservation> clientReservations = allReservations.stream()
                .filter(reservation -> reservation.getClientName().equals(newReservation.getClientName()))
                .collect(Collectors.toList());
        if (clientReservations.size() >= reservationCountLimit) {
            throw new RequestException(ApplicationError.RESERVATION_INVALID_MAX_RESERVATIONS_REACHED);
        }
    }

    private boolean filterByOneField(String currentValue, String filteredValue) {
        return currentValue.equals(filteredValue) || StringUtils.isBlank(filteredValue);
    }
}
