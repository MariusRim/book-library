package com.library.booklibrary.service;

import com.library.booklibrary.entity.Book;
import com.library.booklibrary.entity.BookReservation;
import com.library.booklibrary.exception.RequestException;
import com.library.booklibrary.exception.ServiceException;

import java.util.List;

public interface BookService {

    Book createBook(Book newBook) throws ServiceException, RequestException;

    BookReservation takeBook(Long bookId, BookReservation newReservation) throws ServiceException, RequestException;

    Book getBookByGuid(Long bookId) throws ServiceException, RequestException;

    List<Book> listAllBooksByFilter(String name,
                                    String author,
                                    String category,
                                    String language,
                                    String isbn,
                                    boolean onlyTaken,
                                    boolean onlyAvailable) throws ServiceException, RequestException;

    void deleteBookByGuid(Long bookId) throws ServiceException, RequestException;
}
