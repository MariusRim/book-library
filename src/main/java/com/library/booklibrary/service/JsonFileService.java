package com.library.booklibrary.service;

import com.library.booklibrary.exception.ServiceException;

import java.util.List;

public interface JsonFileService {

    void writeToFile(String filepath, Object object) throws ServiceException;

    <T> List<T> readFromFileToList(String filepath, Class<T> objectType) throws ServiceException;
}
