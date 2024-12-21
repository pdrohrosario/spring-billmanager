package com.api.billmanager.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.api.billmanager.presentation.dto.interfaces.Insert;
import com.api.billmanager.presentation.dto.request.CsvBillRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.enums.EnumUtils;
import com.api.billmanager.domain.exception.BillAlreadyPaidException;
import com.api.billmanager.domain.exception.BillNotFoundException;
import com.api.billmanager.domain.exception.CsvFileException;
import com.api.billmanager.domain.exception.PaymentDateException;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.persistence.BillRepositoryInterface;
import com.api.billmanager.infrastructure.utils.CsvUtils;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;
import com.api.billmanager.presentation.dto.response.PaginatedResponse;

@Service
@RequiredArgsConstructor
public class BillService {

    private final UserService userService;

    private final CsvService csvService;

    private final BillRepositoryInterface repository;

    public BillResponse create(@Validated(Insert.class) BillRequest billRequest) {

        this.validatePaymentDateIsBeforeDueDate(billRequest.getPaymentDate(), billRequest.getDueDate());
        User user = userService.findByEmail(billRequest.getUser().getEmail());

        Bill newBill = billRequest.convertRequestToBill();
        newBill.setBillStatus(BillStatus.PENDING);
        newBill.setUser(user);
        newBill = this.repository.save(newBill);

        return new BillResponse().convertBillToResponse(newBill);
    }

    public BillResponse update(BillRequest billUpdate) {
        Bill existingBill = findById(billUpdate.getId());
        
        this.validateBillAlreadyPaid(existingBill.getBillStatus());

        this.validatePaymentDateIsBeforeDueDate(billUpdate.getPaymentDate(), billUpdate.getDueDate());

        existingBill.setDescription(billUpdate.getDescription());
        existingBill.setPaymentDate(billUpdate.getPaymentDate());
        existingBill.setDueDate(billUpdate.getDueDate());
        existingBill.setAmount(billUpdate.getAmount());
        existingBill.setBillStatus(billUpdate.getBillStatus());

        existingBill = this.repository.save(existingBill);
        
        return new BillResponse().convertBillToResponse(existingBill);
    }

    public BillResponse updateStatus(Long id, String newBillStatus) {
        Bill existingBill = findById(id);

        this.validateBillAlreadyPaid(existingBill.getBillStatus());

        existingBill.setBillStatus(EnumUtils.parseEnum(BillStatus.class, newBillStatus));

        existingBill = this.repository.save(existingBill);

        return new BillResponse().convertBillToResponse(existingBill);
    }

    public BillResponse getById(Long id) {
        return new BillResponse().convertBillToResponse(this.findById(id));
    }

    public PaginatedResponse<BillResponse> getBillsByDueDateAndDescription(
            LocalDate dueDate,
            String description,
            Pageable pageable) {

        Page<BillResponse> list = repository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate,
                description, pageable).map(bill -> new BillResponse().convertBillToResponse(bill));

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

        List<CsvBillRequest> billListImport = csvService.importCsv(file);
        List<BillResponse> billSaved = billListImport.stream()
                .filter(CsvBillRequest::isImported)
                .map(b -> create(b.getRequest()))
                .toList();

        PaginatedResponse<BillResponse> response = new PaginatedResponse<>();
        response.setContent(billSaved);
        response.setMessage(billSaved.size() == billListImport.size() ?
                "All csv records have been imported.":
                "Check the log, some csv records have errors and have not been imported.");
        response.setPageNumber(1);
        response.setPageSize(billSaved.size());
        response.setTotalElements(billSaved.size());
        response.setTotalPages(1);
        return response;
    }

    public BigDecimal getAmountByPeriod(LocalDate startDate,
                                        LocalDate endDate) {
        return this.repository.getTotalAmountByPeriod(startDate, endDate);
    }

    private Bill findById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new BillNotFoundException("Bill with id " + id
                        + " not exist."));
    }

    private void validatePaymentDateIsBeforeDueDate(LocalDate paymenDate, LocalDate dueDate){
        if(paymenDate.isAfter(dueDate)){
            throw new PaymentDateException("The field 'paymentDate' must be a date before or equal to the  field 'dueDate'.");
        }
    }

    private void validateBillAlreadyPaid(BillStatus billStatus){
        if(billStatus.equals(BillStatus.PAID)){
            throw new BillAlreadyPaidException("This bill already paid, you can't modify the status of this bill.");
        }
    }

}
