package com.api.billmanager.infrastructure.utils;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.enums.CsvBillRecordFields;
import com.api.billmanager.domain.enums.EnumUtils;
import com.api.billmanager.domain.exception.CsvInvalidData;
import com.api.billmanager.domain.exception.CsvParseException;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.request.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
public class CsvUtils {

    public static final String TYPE = "text/csv";

    public static boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }
    public static List<BillRequest> csvToBillRequestList(InputStream is) {
        List<BillRequest> successfulRecords = new ArrayList<>();

        try (BufferedReader bReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(
                     bReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {

            Iterable<CSVRecord> csvRecords = csvParser.getRecords();

            StreamSupport.stream(csvRecords.spliterator(), false).forEach(record -> {
                try {
                    BillRequest billRequest = buildBillRequestFromCsvRecord(record);
                    successfulRecords.add(billRequest);
                } catch (Exception e) {
                    log.error("Error on process record: " + record.toString() + " - " + e.getMessage());
                }
            });

        } catch (IOException e) {
            throw new CsvParseException("Failed to parse CSV data due to I/O error: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while processing the CSV: " + e.getMessage(), e);
        }

        return successfulRecords;
    }

    public static BillRequest buildBillRequestFromCsvRecord(CSVRecord billRecord) {
        try {
            CsvUtils.validateCsvRecord(billRecord);
            return BillRequest.builder()
                    .dueDate(LocalDate.parse(billRecord.get(CsvBillRecordFields.DUE_DATE.getDescription())))
                    .paymentDate(LocalDate.parse(billRecord.get(CsvBillRecordFields.PAYMENT_DATE.getDescription())))
                    .amount(new BigDecimal(billRecord.get(CsvBillRecordFields.AMOUNT.getDescription())))
                    .description(billRecord.get(CsvBillRecordFields.DESCRIPTION.getDescription()).isBlank() ? "" : billRecord.get(CsvBillRecordFields.DESCRIPTION.getDescription()))
                    .user(UserRequest.builder().email(billRecord.get(CsvBillRecordFields.USER_EMAIL.getDescription())).build())
                    .build();
        } catch (DateTimeParseException e) {
            throw new CsvParseException("Invalid date format in CSV record: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new CsvParseException("Invalid number format in CSV record: " + e.getMessage(), e);
        } catch (CsvInvalidData e){
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while parsing CSV record: " + e.getMessage(), e);
        }
    }

    public static void validateCsvRecord(CSVRecord billRecord){
        String billRecordId = billRecord.get(CsvBillRecordFields.ID.getDescription());
        if(billRecordId.isBlank()){
            throw new CsvInvalidData("Check the record of line " + (billRecord.getRecordNumber() + 1) +
                    ", the field 'ID' expects to receive a number");
        }

        if(billRecord.get(CsvBillRecordFields.PAYMENT_DATE.getDescription()).isBlank()){
            throw new CsvInvalidData(CsvBillRecordFields.PAYMENT_DATE.getDescription(),billRecordId,"date");
        }

        if(billRecord.get(CsvBillRecordFields.AMOUNT.getDescription()).isBlank()){
            throw new CsvInvalidData(CsvBillRecordFields.AMOUNT.getDescription(),billRecordId, "date");
        }

        if(billRecord.get(CsvBillRecordFields.USER_EMAIL.getDescription()).isBlank()){
            throw new CsvInvalidData(CsvBillRecordFields.USER_EMAIL.getDescription(),billRecordId, "Email");
        }
    }
}