package com.api.billmanager.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.api.billmanager.domain.enums.BillStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Builder
@AllArgsConstructor
@Entity
@Data
@NoArgsConstructor
@Table(name="bills")
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private BigDecimal amount;

    private String description;

    @Enumerated(EnumType.STRING)
    private BillStatus billStatus;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
