package com.library.booklibrary.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.booklibrary.assertion.BookAssertions;
import com.library.booklibrary.assertion.BookReservationAssertions;
import com.library.booklibrary.entity.Book;
import com.library.booklibrary.entity.BookReservation;
import com.library.booklibrary.exception.RequestException;
import com.library.booklibrary.exception.ServiceException;
import com.library.booklibrary.exception.error.ApplicationError;
import com.library.booklibrary.exception.error.RequestError;
import com.library.booklibrary.mockdata.BookMock;
import com.library.booklibrary.mockdata.BookReservationMock;
import com.library.booklibrary.service.BookService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@AutoConfigureMockMvc
public class BookControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @Test
    public void testCreateBookEndpointSuccess() throws Exception {
        when(bookService.createBook(any()))
                .thenAnswer(invocation -> BookMock.createMockBook());

        Book requestBook = BookMock.createMockBook();

        MvcResult mvcResult = this.mockMvc.perform(post("/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBook)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Book fetchedBook = objectMapper.readValue(response, Book.class);

        BookAssertions.assertEquals(requestBook, fetchedBook);
    }

    @Test
    public void testCreateBookEndpointServiceException() throws Exception {
        when(bookService.createBook(any()))
                .thenThrow(new ServiceException("test exception"));

        Book requestBook = BookMock.createMockBook();

        MvcResult mvcResult = this.mockMvc.perform(post("/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBook)))
                .andExpect(status().isServiceUnavailable())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        RequestError error = objectMapper.readValue(response, RequestError.class);

        Assertions.assertEquals(ApplicationError.SERVICE_UNAVAILABLE.getErrorName(), error.getErrorName());
        Assertions.assertEquals(ApplicationError.SERVICE_UNAVAILABLE.getHttpStatus().toString(), error.getHttpStatus());
    }

    @Test
    public void testCreateBookEndpointBookExistsException() throws Exception {
        when(bookService.createBook(any()))
                .thenThrow(new RequestException(ApplicationError.BOOK_ALREADY_EXISTS));

        Book requestBook = BookMock.createMockBook();

        MvcResult mvcResult = this.mockMvc.perform(post("/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBook)))
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        RequestError error = objectMapper.readValue(response, RequestError.class);

        Assertions.assertEquals(ApplicationError.BOOK_ALREADY_EXISTS.getErrorName(), error.getErrorName());
        Assertions.assertEquals(ApplicationError.BOOK_ALREADY_EXISTS.getHttpStatus().toString(), error.getHttpStatus());
    }

    @Test
    public void testTakeBookEndpointSuccess() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenAnswer(invocation -> BookReservationMock.createMockBookReservation());

        BookReservation requestReservation = BookReservationMock.createMockBookReservation();

        MvcResult mvcResult = this.mockMvc.perform(post("/v1/books/1/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestReservation)))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        BookReservation fetchedReservation = objectMapper.readValue(response, BookReservation.class);

        BookReservationAssertions.assertEquals(requestReservation, fetchedReservation);
    }

    @Test
    public void testTakeBookEndpointServiceException() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenThrow(new ServiceException("Test exception"));

        testTakeBookException(ApplicationError.SERVICE_UNAVAILABLE, status().isServiceUnavailable());
    }

    @Test
    public void testTakeBookEndpointBookDoesntExistException() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenThrow(new RequestException(ApplicationError.BOOK_DOESNT_EXIST));

        testTakeBookException(ApplicationError.BOOK_DOESNT_EXIST, status().isBadRequest());
    }

    @Test
    public void testTakeBookEndpointBookAlreadyTakenException() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenThrow(new RequestException(ApplicationError.RESERVATION_INVALID_BOOK_ALREADY_TAKEN));

        testTakeBookException(ApplicationError.RESERVATION_INVALID_BOOK_ALREADY_TAKEN, status().isBadRequest());
    }

    @Test
    public void testTakeBookEndpointExceedsPeriodException() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenThrow(new RequestException(ApplicationError.RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD));

        testTakeBookException(ApplicationError.RESERVATION_INVALID_EXCEEDS_ALLOWED_PERIOD, status().isBadRequest());
    }

    @Test
    public void testTakeBookEndpointMaxReservationsException() throws Exception {
        when(bookService.takeBook(any(), any()))
                .thenThrow(new RequestException(ApplicationError.RESERVATION_INVALID_MAX_RESERVATIONS_REACHED));

        testTakeBookException(ApplicationError.RESERVATION_INVALID_MAX_RESERVATIONS_REACHED, status().isBadRequest());
    }

    @Test
    public void testGetBookByGuidEndpointSuccess() throws Exception {
        when(bookService.getBookByGuid(any()))
                .thenAnswer(invocation -> BookMock.createMockBook());

        MvcResult mvcResult = this.mockMvc.perform(get("/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        Book fetchedBook = objectMapper.readValue(response, Book.class);
        Book expectedBook = BookMock.createMockBook();

        BookAssertions.assertEquals(expectedBook, fetchedBook);
    }

    @Test
    public void testGetBookByGuidEndpointServiceException() throws Exception {
        when(bookService.getBookByGuid(any()))
                .thenThrow(new ServiceException("Test Exception"));

        testGeneralEndpointException(ApplicationError.SERVICE_UNAVAILABLE,
                status().isServiceUnavailable(),
                get("/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetBookByGuidEndpointFailBookDoesntExist() throws Exception {
        when(bookService.getBookByGuid(any()))
                .thenThrow(new RequestException(ApplicationError.BOOK_DOESNT_EXIST));

        testGeneralEndpointException(ApplicationError.BOOK_DOESNT_EXIST,
                status().isBadRequest(),
                get("/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testListAllBooksByFiltersEndpointAllParametersSuccess() throws Exception {
        when(bookService.listAllBooksByFilter(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));

        Book expectedBook = BookMock.createMockBook();
        String requestUrl = "/v1/books?name=" + expectedBook.getGuid() + "&author=" + expectedBook.getAuthor() +
                "&category=" + expectedBook.getCategory() + "&language=" + expectedBook.getLanguage() +
                "&isbn=" + expectedBook.getIsbn() + "&onlyTaken=true&onlyAvailable=false";
        MvcResult mvcResult = this.mockMvc.perform(get(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        List<Book> fetchedList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));

        Assertions.assertNotNull(fetchedList);
        Assertions.assertEquals(fetchedList.size(), 1);
        BookAssertions.assertEquals(expectedBook, fetchedList.get(0));
    }

    @Test
    public void testListAllBooksByFiltersEndpointOneParameterSuccess() throws Exception {
        when(bookService.listAllBooksByFilter(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenAnswer(invocation -> Collections.singletonList(BookMock.createMockBook()));

        Book expectedBook = BookMock.createMockBook();
        String requestUrl = "/v1/books?name=" + expectedBook.getGuid();

        MvcResult mvcResult = this.mockMvc.perform(get(requestUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        List<Book> fetchedList = objectMapper.readValue(response,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));

        Assertions.assertNotNull(fetchedList);
        Assertions.assertEquals(fetchedList.size(), 1);
        BookAssertions.assertEquals(expectedBook, fetchedList.get(0));
    }

    @Test
    public void testListAllBooksByFiltersEndpointServiceException() throws Exception {
        when(bookService.listAllBooksByFilter(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenThrow(new ServiceException("Test Exception"));

        testGeneralEndpointException(ApplicationError.SERVICE_UNAVAILABLE,
                status().isServiceUnavailable(),
                get("/v1/books?name=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testListAllBooksByFiltersEndpointCantRequestBothException() throws Exception {
        when(bookService.listAllBooksByFilter(any(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                .thenThrow(new RequestException(ApplicationError.CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE));

        testGeneralEndpointException(ApplicationError.CANT_REQUEST_BOTH_TAKEN_AND_AVAILABLE,
                status().isBadRequest(),
                get("/v1/books?name=1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteBookByGuidEndpointSuccess() throws Exception {
        this.mockMvc.perform(delete("/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    public void testDeleteBookByGuidEndpointServiceException() throws Exception {
        Mockito.doThrow(new ServiceException("Test exception"))
                .when(bookService).deleteBookByGuid(any());

        testGeneralEndpointException(ApplicationError.SERVICE_UNAVAILABLE,
                status().isServiceUnavailable(),
                delete("/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testDeleteBookByGuidEndpointBookDoesntExistException() throws Exception {
        Mockito.doThrow(new RequestException(ApplicationError.BOOK_DOESNT_EXIST))
                .when(bookService).deleteBookByGuid(any());

        testGeneralEndpointException(ApplicationError.BOOK_DOESNT_EXIST,
                status().isBadRequest(),
                delete("/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    private void testTakeBookException(ApplicationError expectedError, ResultMatcher status) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(post("/v1/books/1/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BookReservationMock.createMockBookReservation())))
                .andExpect(status)
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        RequestError error = objectMapper.readValue(response, RequestError.class);

        Assertions.assertEquals(expectedError.getErrorName(), error.getErrorName());
        Assertions.assertEquals(expectedError.getHttpStatus().toString(), error.getHttpStatus());
    }

    private void testGeneralEndpointException(ApplicationError expectedError, ResultMatcher status, RequestBuilder request) throws Exception {
        MvcResult mvcResult = this.mockMvc.perform(request)
                .andExpect(status)
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        RequestError error = objectMapper.readValue(response, RequestError.class);

        Assertions.assertEquals(expectedError.getErrorName(), error.getErrorName());
        Assertions.assertEquals(expectedError.getHttpStatus().toString(), error.getHttpStatus());
    }
}
