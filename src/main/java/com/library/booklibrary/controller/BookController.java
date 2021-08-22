package com.library.booklibrary.controller;

import com.library.booklibrary.entity.Book;
import com.library.booklibrary.entity.BookReservation;
import com.library.booklibrary.exception.RequestException;
import com.library.booklibrary.exception.ServiceException;
import com.library.booklibrary.exception.error.ApplicationError;
import com.library.booklibrary.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/books", produces = {MediaType.APPLICATION_JSON_VALUE})
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Book saveBook(@RequestBody Book newBook) {
        try {
            return bookService.createBook(newBook);
        } catch (ServiceException e) {
            throw new RequestException(ApplicationError.SERVICE_UNAVAILABLE);
        }
    }

    @PostMapping("/{bookGuid}/reserve")
    public BookReservation takeBook(@PathVariable(name = "bookGuid") Long bookId,
                                    @RequestBody BookReservation newReservation) {
        try {
            newReservation.setBookGuid(bookId);
            return bookService.takeBook(bookId, newReservation);
        } catch (ServiceException e) {
            throw new RequestException(ApplicationError.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping("/{bookGuid}")
    public Book getBookByGuid(@PathVariable(name = "bookGuid") Long bookId) {
        try {
            return bookService.getBookByGuid(bookId);
        } catch (ServiceException e) {
            throw new RequestException(ApplicationError.SERVICE_UNAVAILABLE);
        }
    }

    @GetMapping
    public List<Book> listAllBooksByFilter(@RequestParam(required = false) String name,
                                           @RequestParam(required = false) String author,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(required = false) String language,
                                           @RequestParam(required = false) String isbn,
                                           @RequestParam(required = false) boolean isOnlyTaken,
                                           @RequestParam(required = false) boolean isOnlyAvailable) {
        try {
            return bookService.listAllBooksByFilter(name,
                    author,
                    category,
                    language,
                    isbn,
                    isOnlyTaken,
                    isOnlyAvailable);
        } catch (ServiceException e) {
            throw new RequestException(ApplicationError.SERVICE_UNAVAILABLE);
        }
    }

    @DeleteMapping("/{bookGuid}")
    public void deleteBookByGuid(@PathVariable(name = "bookGuid") Long bookId) {
        try {
            bookService.deleteBookByGuid(bookId);
        } catch (ServiceException e) {
            throw new RequestException(ApplicationError.SERVICE_UNAVAILABLE);
        }
    }
}
