package com.api.billmanager.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.presentation.dto.request.UserRequest;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class BillResponse {

    private Long id;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    private UserRequest user;

    public Long getId() {
        return id;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public BillStatus getBillStatus() {
        return billStatus;
    }

    public UserRequest getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBillStatus(BillStatus billStatus) {
        this.billStatus = billStatus;
    }

    public void setUser(UserRequest user) {
        this.user = user;
    }
    

    
}
