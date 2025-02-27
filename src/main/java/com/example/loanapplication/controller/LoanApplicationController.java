package com.example.loanapplication.controller;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan-applications")
@RequiredArgsConstructor
@Slf4j
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    @PostMapping
    public ResponseEntity<LoanApplication> submitLoanApplication(@RequestBody LoanApplication loanApplication) {
        log.info("Received loan application submission for: {}", loanApplication.getApplicantName());
        LoanApplication savedApplication = loanApplicationService.submitLoanApplication(loanApplication);
        return ResponseEntity.ok(savedApplication);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplication> getLoanApplication(@PathVariable Long id) {
        log.info("Retrieving loan application with ID: {}", id);
        LoanApplication loanApplication = loanApplicationService.getLoanApplication(id);
        return ResponseEntity.ok(loanApplication);
    }
    
    @GetMapping
    public ResponseEntity<List<LoanApplication>> getAllLoanApplications() {
        log.info("Retrieving all loan applications");
        List<LoanApplication> applications = loanApplicationService.getAllLoanApplications();
        return ResponseEntity.ok(applications);
    }
    
    @PostMapping("/{id}/borderline-review")
    public ResponseEntity<Void> startBorderlineReview(@PathVariable Long id) {
        log.info("Starting borderline review for loan application ID: {}", id);
        loanApplicationService.handleBorderlineApplication(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/customer-inquiry")
    public ResponseEntity<Void> startCustomerInquiry(@PathVariable Long id) {
        log.info("Starting customer inquiry for loan application ID: {}", id);
        loanApplicationService.handleCustomerInquiry(id);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{id}/loan-modification")
    public ResponseEntity<Void> startLoanModification(@PathVariable Long id) {
        log.info("Starting loan modification for loan application ID: {}", id);
        loanApplicationService.handleLoanModification(id);
        return ResponseEntity.ok().build();
    }
}
