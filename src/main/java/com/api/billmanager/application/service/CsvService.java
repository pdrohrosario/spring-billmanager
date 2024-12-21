package com.api.billmanager.application.service;

import com.api.billmanager.domain.enums.CsvBillRecordFields;
import com.api.billmanager.domain.exception.CsvInvalidData;
import com.api.billmanager.domain.exception.CsvParseException;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.request.CsvBillRequest;
import com.api.billmanager.presentation.dto.request.UserRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@AllArgsConstructor
public class CsvService {

    public List<CsvBillRequest> importCsv(MultipartFile file){
        Iterable<CSVRecord> billsFromCsv = readCsv(file);
        return convertRecordsToCsvBillRequestList(billsFromCsv);
    }

    private List<CSVRecord> readCsv(MultipartFile file){
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            CSVParser csvParser = new CSVParser(bReader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
            return csvParser.getRecords();
        } catch (IOException ex){
            throw new RuntimeException("An unexpected error occurred while reading the CSV: " + ex.getMessage(), ex);
        }
    }

    private List<CsvBillRequest> convertRecordsToCsvBillRequestList(Iterable<CSVRecord> billFromCsv) {
        List<CsvBillRequest> csvBillRequests = List.of();
        for(CSVRecord record : billFromCsv){
            try {
                BillRequest billRequest = buildBillRequestFromCsvRecord(record);
                CsvBillRequest csvRequest = new CsvBillRequest(record.getRecordNumber(), billRequest, true);
                csvBillRequests.add(csvRequest);
            } catch (CsvParseException | CsvInvalidData e) {
                CsvBillRequest csvRequest = new CsvBillRequest(record.getRecordNumber(), true);
                csvBillRequests.add(csvRequest);
                log.error("Error on process record: " + record.toString() + " - " + e.getMessage());
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred while processing the CSV: " + e.getMessage(), e);
            }
        }
        return csvBillRequests;
    }

    private BillRequest buildBillRequestFromCsvRecord(CSVRecord billRecord) {
        try {
            validateCsvRecord(billRecord);
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

    private void validateCsvRecord(CSVRecord billRecord){
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
