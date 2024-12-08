package com.api.billmanager.domain.exception;

public class CsvInvalidData extends RuntimeException {
    public CsvInvalidData(String message) {
        super(message);
    }

    public CsvInvalidData(String field, String id, String dataFormat) {
        super("Check the record with ID=" + id +
                ", the field '" + field + "' expects to receive a " + dataFormat + ".");
    }
}
