package com.api.billmanager.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.api.billmanager.domain.enums.BillStatus;

import com.api.billmanager.domain.model.Bill;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BillResponse {

    private Long id;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    private UserResponse user;

    public BillResponse convertBillToResponse(Bill bill){
        return BillResponse.builder()
                .id(bill.getId())
                .dueDate(bill.getDueDate())
                .paymentDate(bill.getPaymentDate())
                .amount(bill.getAmount())
                .billStatus(bill.getBillStatus())
                .description(bill.getDescription())
                .user(new UserResponse().convertUserToResponse(bill.getUser()))
                .build();
    }
}
