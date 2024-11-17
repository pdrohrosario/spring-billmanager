package com.api.billmanager.domain.exception;

public class CsvFileException extends RuntimeException {
    public CsvFileException(String message) {
        super(message);
    }
}