package com.api.billmanager.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.presentation.dto.interfaces.Insert;
import com.api.billmanager.presentation.dto.interfaces.Update;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
public class BillRequest {

    @NotNull(groups = {Update.class}, message = "The field 'id' is required")
    private Long id;

    @NotNull(groups = {Insert.class, Update.class}, message = "The field 'dueDate' is required")
    @Future(groups = {Insert.class, Update.class}, message = "The field 'dueDate' must be a date in the future")
    private LocalDate dueDate;

    @NotNull(groups = {Insert.class, Update.class}, message = "The field 'paymentDate' is required")
    @Future(groups = {Insert.class, Update.class}, message = "The field 'paymentDate' must be a date in the future")
    private LocalDate paymentDate;

    @NotNull(groups = {Insert.class, Update.class}, message = "The field 'amount' is required")
    @DecimalMin(groups = {Insert.class, Update.class}, value = "0.1", inclusive = false, message = "The field 'amount' must have a value greater than 0.1")
    private BigDecimal amount;

    @NotBlank(groups = {Insert.class, Update.class}, message = "The field 'description' is required")
    private String description;

    @NotNull(groups = {Update.class}, message = "The field 'billStatus' is required")
    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @NotNull(groups = {Insert.class}, message = "The 'user' field is required")
    private UserRequest user;

    public Bill convertRequestToBill(){
        return Bill.builder()
                .id(this.id)
                .dueDate(this.dueDate)
                .paymentDate(this.paymentDate)
                .amount(this.amount)
                .description(this.description)
                .user(this.user.convertRequestToUser())
                .billStatus(this.billStatus)
                .build();
    }
}
