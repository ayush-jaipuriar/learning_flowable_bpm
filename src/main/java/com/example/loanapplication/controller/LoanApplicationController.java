package com.example.loanapplication.controller;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.service.LoanApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-applications")
@Slf4j
public class LoanApplicationController {

    @Autowired
    private LoanApplicationService loanApplicationService;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    // Lazy getter for RuntimeService to avoid circular dependencies
    private RuntimeService getRuntimeService() {
        return applicationContext.getBean(RuntimeService.class);
    }

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
        LoanApplication loanApplication = loanApplicationService.getLoanApplication(id);
        
        if (loanApplication.getProcessInstanceId() != null) {
            // Send a message to the running process instance
            Map<String, Object> variables = new HashMap<>();
            variables.put("loanApplicationId", id);
            getRuntimeService().messageEventReceived("customerInquiryMessage", loanApplication.getProcessInstanceId(), variables);
            log.info("Sent customer inquiry message to process instance: {}", loanApplication.getProcessInstanceId());
            return ResponseEntity.ok().build();
        } else {
            // Fall back to direct case creation if no process instance exists
            loanApplicationService.handleCustomerInquiry(id);
            return ResponseEntity.ok().build();
        }
    }
    
    @PostMapping("/{id}/loan-modification")
    public ResponseEntity<Void> startLoanModification(@PathVariable Long id) {
        log.info("Starting loan modification for loan application ID: {}", id);
        LoanApplication loanApplication = loanApplicationService.getLoanApplication(id);
        
        if (loanApplication.getProcessInstanceId() != null) {
            // Send a message to the running process instance
            Map<String, Object> variables = new HashMap<>();
            variables.put("loanApplicationId", id);
            getRuntimeService().messageEventReceived("loanModificationMessage", loanApplication.getProcessInstanceId(), variables);
            log.info("Sent loan modification message to process instance: {}", loanApplication.getProcessInstanceId());
            return ResponseEntity.ok().build();
        } else {
            // Fall back to direct case creation if no process instance exists
            loanApplicationService.handleLoanModification(id);
            return ResponseEntity.ok().build();
        }
    }
}
