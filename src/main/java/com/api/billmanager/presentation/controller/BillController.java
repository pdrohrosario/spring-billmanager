package com.api.billmanager.presentation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.api.billmanager.application.service.BillService;
import com.api.billmanager.presentation.dto.interfaces.Insert;
import com.api.billmanager.presentation.dto.interfaces.Update;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;
import com.api.billmanager.presentation.dto.response.PaginatedResponse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/bill/")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PostMapping("create")
    public ResponseEntity<BillResponse> create(@Validated(Insert.class) @RequestBody BillRequest bill) {
        BillResponse newBill = billService.create(bill);
        return new ResponseEntity<BillResponse>(newBill, HttpStatus.CREATED);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<BillResponse> update(@PathVariable Long id,
            @Validated(Update.class) @RequestBody BillRequest bill) {
        BillResponse updatedBill = billService.update(bill);
        return new ResponseEntity<>(updatedBill, HttpStatus.OK);
    }

    @PatchMapping("status/{id}/newBillStatus")
    public ResponseEntity<BillResponse> updateStatus(@PathVariable Long id, @PathVariable String newBillStatus) {
        BillResponse updatedBill = billService.updateStatus(id, newBillStatus);
        return new ResponseEntity<>(updatedBill, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<BillResponse> getById(@PathVariable Long id) {
        BillResponse bill = billService.getById(id);
        return new ResponseEntity<>(bill, HttpStatus.OK);
    }

    @GetMapping("search-bills")
    public ResponseEntity<PaginatedResponse<BillResponse>>  getBillByDueDateAndDescription(
            @RequestParam(name = "dueDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @NotNull(message = "O campo 'dueDate' é obrigatório")
            LocalDate dueDate,

            @RequestParam(name = "description")
            @NotBlank(message = "O campo 'description' é obrigatório e deve ser diferent de vazio")
            String description,
            
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "dueDate")
            String sort) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));

        PaginatedResponse<BillResponse> bills = billService.getBillsByDueDateAndDescription(dueDate, description, pageable);

        return new ResponseEntity<PaginatedResponse<BillResponse>>(bills, HttpStatus.OK);
    }

    @GetMapping("amount-by-period")
    public ResponseEntity<?> getAmountByPeriod(
            @RequestParam(name = "startDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @NotNull(message = "O campo 'startDate' é obrigatório")
            LocalDate startDate,

            @RequestParam(name = "endDate")
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @NotNull(message = "O campo 'endDate' é obrigatório")
            LocalDate endDate) {

        BigDecimal totalAmount = billService.getAmountByPeriod(startDate, endDate);
        return new ResponseEntity<>("Total amount between " + startDate + " and " + endDate + " : " + totalAmount,
                HttpStatus.OK);
    }

    @PostMapping("/csv/import")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            billService.importCsvBill(file);
            return ResponseEntity.ok("Bills imported from file csv!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

}
