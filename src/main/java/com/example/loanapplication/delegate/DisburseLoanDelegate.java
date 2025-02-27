package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
public class DisburseLoanDelegate implements JavaDelegate {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    // Default no-args constructor for Flowable
    public DisburseLoanDelegate() {
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        log.info("Disbursing loan for application with process instance ID: {}", processInstanceId);
        
        // Retrieve loan application from database
        LoanApplication loanApplication = loanApplicationRepository.findByProcessInstanceId(processInstanceId);
        
        // If not found by process instance ID, try to find by loan application ID from variables
        if (loanApplication == null && execution.hasVariable("loanApplicationId")) {
            Long loanApplicationId = (Long) execution.getVariable("loanApplicationId");
            log.info("Loan application not found by process instance ID, trying with loan application ID: {}", loanApplicationId);
            loanApplication = loanApplicationRepository.findById(loanApplicationId)
                    .orElse(null);
            
            // If found, update the process instance ID
            if (loanApplication != null) {
                loanApplication.setProcessInstanceId(processInstanceId);
                loanApplicationRepository.save(loanApplication);
                log.info("Updated loan application with process instance ID");
            }
        }
        
        if (loanApplication == null) {
            throw new RuntimeException("Loan application not found for process instance ID: " + processInstanceId);
        }
        
        // Get the approved interest rate from the process variables
        BigDecimal interestRate = new BigDecimal(execution.getVariable("interestRate").toString());
        loanApplication.setApprovedInterestRate(interestRate);
        
        // Update loan status to DISBURSED
        loanApplication.setStatus("DISBURSED");
        
        // Save the updated loan application
        loanApplicationRepository.save(loanApplication);
        
        // In a real-world scenario, this would trigger a funds transfer
        // For this example, we'll just log the disbursement
        log.info("Loan disbursed for applicant: {}", loanApplication.getApplicantName());
        log.info("Loan amount: ${}, Interest rate: {}%, Term: {} months", 
                loanApplication.getLoanAmount(), 
                loanApplication.getApprovedInterestRate(),
                loanApplication.getLoanTermMonths());
    }
}
