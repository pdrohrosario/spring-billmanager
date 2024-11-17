package com.api.billmanager.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.api.billmanager.domain.model.Bill;

public interface BillRepositoryInterface extends JpaRepository<Bill, Long> {

    Optional<Bill> findByIdAndUserEmail(Long id, String user);

    Page<Bill> findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(LocalDate dueDate, String description, Pageable pageable);

    @Query("SELECT SUM(b.amount) FROM Bill b WHERE b.dueDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalAmountByPeriod(LocalDate startDate, LocalDate endDate);
}
