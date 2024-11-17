package com.api.billmanager.domain.exception;

public class CsvFailedImportException extends RuntimeException {
    public CsvFailedImportException(String message) {
        super(message);
    }
}
