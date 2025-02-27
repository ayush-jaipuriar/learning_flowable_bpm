package com.example.loanapplication.controller;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final LoanApplicationService loanApplicationService;
    
    @GetMapping("/create-sample-loan")
    public ResponseEntity<LoanApplication> createSampleLoan() {
        log.info("Creating sample loan application");
        
        LoanApplication loanApplication = LoanApplication.builder()
                .applicantName("John Doe")
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("123 Main St, City")
                .loanAmount(new BigDecimal("10000"))
                .loanPurpose("Home Improvement")
                .loanTermMonths(24)
                .monthlyIncome(new BigDecimal("5000"))
                .existingDebt(new BigDecimal("2000"))
                .creditScore(750)
                .status("PENDING")
                .build();
                
        LoanApplication savedApplication = loanApplicationService.submitLoanApplication(loanApplication);
        return ResponseEntity.ok(savedApplication);
    }
    
    @GetMapping("/loan-applications")
    public ResponseEntity<?> getAllLoanApplications() {
        log.info("Retrieving all loan applications");
        return ResponseEntity.ok(loanApplicationService.getAllLoanApplications());
    }
}
