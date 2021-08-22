package com.library.booklibrary.mockdata;

import com.library.booklibrary.entity.Book;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookMock {

    public static Book createMockBook() {
        return new Book(
                "Test Book",
                "Test Author",
                "Test Category",
                "Test Language",
                LocalDate.of(2020, 2, 12),
                "1234567890123",
                1L
        );
    }

    public static List<Book> createMockBookList() {
        ArrayList<Book> newList = new ArrayList<>();

        Book book = BookMock.createMockBook();
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(2L);
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(3L);
        book.setName("Not Test Book");
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(4L);
        book.setAuthor("Not Test Author");
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(5L);
        book.setCategory("Not Test Category");
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(6L);
        book.setLanguage("Not Test Language");
        newList.add(book);

        book = BookMock.createMockBook();
        book.setGuid(7L);
        book.setIsbn("123456789999");
        newList.add(book);

        return newList;
    }
}
