package com.api.billmanager.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.presentation.dto.interfaces.Insert;
import com.api.billmanager.presentation.dto.interfaces.Update;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BillRequest {

    @NotNull(groups = {Update.class}, message = "The 'id' field is required")
    private Long id;

    @NotNull(groups = {Insert.class, Update.class}, message = "The 'due date' field is required")
    @Future(message = "The 'due date' field must contain a future date")
    private LocalDate dueDate;

    @NotNull(groups = {Insert.class, Update.class}, message = "The 'payment date' field is required")
    @Future(message = "The 'payment date' field must contain a future date")
    private LocalDate paymentDate;

    @NotNull(groups = {Insert.class, Update.class}, message = "The 'amount' field is required")
    @DecimalMin(value = "0.1", inclusive = false, message = "The 'amount' field must have a value greater than 0.1")
    private BigDecimal amount;

    @NotBlank(groups = {Insert.class, Update.class}, message = "The 'description' field is required")
    private String description;

    @NotBlank(groups = {Update.class}, message = "The 'billStatus' field is required")
    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @NotNull(groups = {Insert.class}, message = "The 'user' field is required")
    private UserRequest user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BillStatus getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(BillStatus billStatus) {
        this.billStatus = billStatus;
    }

    public UserRequest getUser() {
        return user;
    }

    public void setUser(UserRequest user) {
        this.user = user;
    }

    
    
}
