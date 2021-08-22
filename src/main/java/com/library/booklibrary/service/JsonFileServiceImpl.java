package com.library.booklibrary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.booklibrary.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class JsonFileServiceImpl implements JsonFileService {

    private final ObjectMapper objectMapper;

    public JsonFileServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeToFile(String filepath, Object object) throws ServiceException {
        try {
            objectMapper.writeValue(new File(filepath), object);
        } catch (IOException e) {
            throw new ServiceException("Exception occurred while writing to " + filepath + " file.\n" + e);
        }
    }

    public <T> List<T> readFromFileToList(String filepath, Class<T> objectType) throws ServiceException {
        try {
            return objectMapper.readValue(new File(filepath), objectMapper.getTypeFactory().constructCollectionType(List.class, objectType));
        } catch (IOException e) {
            throw new ServiceException("Exception occurred while reading from " + filepath + " file.\n" + e);
        }
    }
}
