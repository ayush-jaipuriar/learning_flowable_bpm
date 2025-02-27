package com.example.loanapplication.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String applicantName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String address;
    
    private BigDecimal loanAmount;
    private Integer loanTermMonths;
    private String loanPurpose;
    
    private BigDecimal monthlyIncome;
    private BigDecimal existingDebt;
    
    private Integer creditScore;
    
    private BigDecimal approvedInterestRate;
    private String status; // SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED
    
    private String processInstanceId; // Reference to Flowable process instance
}
