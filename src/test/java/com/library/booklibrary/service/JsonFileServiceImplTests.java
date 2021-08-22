package com.library.booklibrary.service;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.library.booklibrary.assertion.BookAssertions;
import com.library.booklibrary.entity.Book;
import com.library.booklibrary.mockdata.BookMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JsonFileServiceImplTests {

    private JsonFileServiceImpl jsonFileService;

    private File testWriteFile, testReadFile;
    private Path testWriteFilePath, testReadFilePath;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    public void createTempFiles() {
        this.jsonFileService = new JsonFileServiceImpl(JsonMapper.builder().findAndAddModules().build());

        testWriteFilePath = tempDirectory.resolve("test-write.json");
        testReadFilePath = tempDirectory.resolve("test-read.json");

        testWriteFile = testWriteFilePath.toFile();
        testReadFile = testReadFilePath.toFile();
    }

    @Test
    public void testWriteToFileSuccess() throws Exception {
        Book testBook = BookMock.createMockBook();

        jsonFileService.writeToFile(testWriteFilePath.toString(), testBook);

        BufferedReader reader = new BufferedReader(new FileReader(testWriteFile));

        String receivedOutput = reader.readLine();
        //This is a book object from BookMock.createMockBook() written as a json string
        String expectedOutput = "{\"name\":\"Test Book\",\"author\":\"Test Author\",\"category\":\"Test Category\"," +
                "\"language\":\"Test Language\",\"publicationDate\":[2020,2,12],\"isbn\":\"1234567890123\",\"guid\":1}";

        reader.close();

        Assertions.assertEquals(receivedOutput, expectedOutput);
    }

    @Test
    public void testReadFromFileToListSuccess() throws Exception {
        BufferedWriter writer = new BufferedWriter(new FileWriter(testReadFile));
        //This is a List<Book> object, which contains a book from BookMock.createMockBook(), written as a json string
        String testInput = "[{\"name\":\"Test Book\",\"author\":\"Test Author\",\"category\":\"Test Category\"," +
                "\"language\":\"Test Language\",\"publicationDate\":[2020,2,12],\"isbn\":\"1234567890123\",\"guid\":1}]";
        writer.write(testInput);
        writer.close();

        List<Book> returnedList = jsonFileService.readFromFileToList(testReadFilePath.toString(), Book.class);
        Book expectedBook = BookMock.createMockBook();

        Assertions.assertNotNull(returnedList);
        Assertions.assertEquals(returnedList.size(), 1);
        BookAssertions.assertEquals(expectedBook, returnedList.get(0));
    }
}
