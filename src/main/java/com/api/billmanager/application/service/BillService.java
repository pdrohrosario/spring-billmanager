package com.api.billmanager.application.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVRecord;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.enums.EnumUtils;
import com.api.billmanager.domain.exception.BillAlreadyPaidException;
import com.api.billmanager.domain.exception.BillNotFoundException;
import com.api.billmanager.domain.exception.CsvFailedImportException;
import com.api.billmanager.domain.exception.CsvFileException;
import com.api.billmanager.domain.exception.PaymentDateException;
import com.api.billmanager.domain.exception.UserNotFoundException;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.persistence.BillRepositoryInterface;
import com.api.billmanager.infrastructure.utils.CsvUtils;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;
import com.api.billmanager.presentation.dto.response.PaginatedResponse;

@Service
public class BillService {

    private final UserService userService;

    private final BillRepositoryInterface repository;

    private final ModelMapper mapper;

    public BillService(UserService userService, BillRepositoryInterface repository, ModelMapper mapper) {
        this.userService = userService;
        this.repository = repository;
        this.mapper = mapper;
    }

    public BillResponse create(BillRequest billRequest) {

        this.validateBillDefault(billRequest);

        Bill newBill = this.mapper.map(billRequest, Bill.class);
        newBill.setBillStatus(BillStatus.PENDING);
        newBill = this.repository.save(newBill);

        return this.mapper.map(newBill, BillResponse.class);
    }

    public BillResponse update(BillRequest billUpdate) {
        Bill existingBill = findById(billUpdate.getId());
        
        this.validateBillAlreadyPaid(existingBill.getBillStatus());

        this.validateBillDefault(billUpdate);

        existingBill.setPaymentDate(billUpdate.getPaymentDate());
        existingBill.setPaymentDate(billUpdate.getDueDate());
        existingBill.setAmount(billUpdate.getAmount());
        existingBill.setBillStatus(billUpdate.getBillStatus());

        existingBill = this.repository.save(existingBill);
        
        return this.mapper.map(existingBill, BillResponse.class);
    }

    public BillResponse updateStatus(Long id, String newBillStatus) {
        Bill existingBill = findById(id);

        this.validateBillAlreadyPaid(existingBill.getBillStatus());

        existingBill.setBillStatus(EnumUtils.parseEnum(BillStatus.class, newBillStatus));

        existingBill = this.repository.save(existingBill);

        return this.mapper.map(existingBill, BillResponse.class);
    }

    public BillResponse getById(Long id) {
        return this.mapper.map(this.findById(id), BillResponse.class);
    }

    public PaginatedResponse<BillResponse> getBillsByDueDateAndDescription(
            LocalDate dueDate,
            String description,
            Pageable pageable) {

        Page<BillResponse> list = this.repository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate,
                description, pageable).map(bill -> mapper.map(bill, BillResponse.class));

        if (list.isEmpty()) {
            throw new BillNotFoundException("Bills with 'dueDate' " + dueDate + " and 'description' " + description
                    + " not found.");
        }

        PaginatedResponse<BillResponse> response = new PaginatedResponse<>();
        response.setContent(list.getContent());
        response.setPageNumber(list.getNumber());
        response.setPageSize(list.getSize());
        response.setTotalElements(list.getTotalElements());
        response.setTotalPages(list.getTotalPages());
        return response;
    }

    public PaginatedResponse<BillResponse> importCsvBill(MultipartFile file) {

        if (!CsvUtils.hasCsvFormat(file)) {
            throw new CsvFileException("The file must be a CSV");
        }

        try {
            Iterable<CSVRecord> billListImport = CsvUtils.csvToBillList(file.getInputStream());
            List<Bill> billsToSave = new ArrayList<>(null);

            for (CSVRecord csvRecord : billListImport) {
                Bill bill = new Bill();
                
                bill.setDueDate(LocalDate.parse(csvRecord.get("Due Date")));
                bill.setPaymentDate(LocalDate.parse(csvRecord.get("Payment Date")));
                bill.setAmount(new BigDecimal(csvRecord.get("Amount")));
                bill.setDescription(csvRecord.get("Description"));
                
                BillStatus billStatus = EnumUtils.parseEnum(BillStatus.class, csvRecord.get("Bill Status").toUpperCase());
                bill.setBillStatus(billStatus);
                
                Long userId = Long.parseLong(csvRecord.get("User ID"));
                User user = userService.findById(userId);
                bill.setUser(user);

                billsToSave.add(bill);
            }

            repository.saveAll(billsToSave);

            PaginatedResponse<BillResponse> response = new PaginatedResponse<>();
            response.setContent(billsToSave.stream().map(bill -> mapper.map(bill, BillResponse.class)).toList());
            response.setPageNumber(1);
            response.setPageSize(billsToSave.size());
            response.setTotalElements(billsToSave.size());
            response.setTotalPages(1);
            return response;
        } catch (IOException e) {
            throw new CsvFailedImportException("Erro import csv file, verify the values and the columns");
        }
    }

    private Bill findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Bill with id " + id
                        + " not exist."));
    }

    private void validateBillDefault(BillRequest billRequest ){

        if(!this.userService.validateUserExist(billRequest.getUser().getEmail())){
            throw new UserNotFoundException("User with email " + billRequest.getUser().getEmail() + " not exist.");
        }
        
        this.validatePaymentDateIsBeforeDueDate(billRequest.getPaymentDate(), billRequest.getDueDate());
    }
    
    private void validatePaymentDateIsBeforeDueDate(LocalDate paymenDate, LocalDate dueDate){
        if(paymenDate.isAfter(dueDate)){
            throw new PaymentDateException("The 'paymentDate' field must have a date before or equal to the 'dueDate' field.");
        }
    }

    private void validateBillAlreadyPaid(BillStatus billStatus){
            if(billStatus.equals(BillStatus.PAID)){
            throw new BillAlreadyPaidException("This bill already paid, you can't modify the status of this bill.");
        }
    }

    public BigDecimal getAmountByPeriod(LocalDate startDate,
            LocalDate endDate) {
        return this.repository.getTotalAmountByPeriod(startDate, endDate);
    }

}
