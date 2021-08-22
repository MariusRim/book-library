package com.library.booklibrary.assertion;

import com.library.booklibrary.entity.Book;
import org.junit.jupiter.api.Assertions;

public class BookAssertions {

    public static void assertEquals(Book book1, Book book2) {
        Assertions.assertEquals(book1.getName(), book2.getName());
        Assertions.assertEquals(book1.getAuthor(), book2.getAuthor());
        Assertions.assertEquals(book1.getCategory(), book2.getCategory());
        Assertions.assertEquals(book1.getLanguage(), book2.getLanguage());
        Assertions.assertEquals(book1.getPublicationDate(), book2.getPublicationDate());
        Assertions.assertEquals(book1.getIsbn(), book2.getIsbn());
        Assertions.assertEquals(book1.getGuid(), book2.getGuid());
    }
}
