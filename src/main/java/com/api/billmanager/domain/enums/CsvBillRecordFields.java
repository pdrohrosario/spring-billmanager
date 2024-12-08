package com.api.billmanager.domain.enums;

import lombok.Getter;

@Getter
public enum CsvBillRecordFields {
    ID("ID"),
    DUE_DATE("Due Date"),
    PAYMENT_DATE("Payment Date"),
    AMOUNT("Amount"),
    DESCRIPTION("Description"),
    USER_EMAIL("User Email");

    private final String description;

    CsvBillRecordFields(String description) {
        this.description = description;
    }

    public static CsvBillRecordFields fromDescription(String description) {
        for (CsvBillRecordFields field : CsvBillRecordFields.values()) {
            if (field.description.equalsIgnoreCase(description)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Description invalid: " + description);
    }
}
