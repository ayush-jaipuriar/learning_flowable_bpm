package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RejectLoanApplicationDelegate implements JavaDelegate {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    
    // Default no-args constructor for Flowable
    public RejectLoanApplicationDelegate() {
    }

    @Override
    public void execute(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        log.info("Rejecting loan application with process instance ID: {}", processInstanceId);
        
        // Retrieve loan application from database
        LoanApplication loanApplication = loanApplicationRepository.findByProcessInstanceId(processInstanceId);
        
        if (loanApplication == null) {
            throw new RuntimeException("Loan application not found for process instance ID: " + processInstanceId);
        }
        
        // Get the rejection reason if available
        String rejectionReason = "Application does not meet eligibility criteria";
        if (execution.hasVariable("reason")) {
            rejectionReason = execution.getVariable("reason").toString();
        }
        
        // Update loan status to REJECTED
        loanApplication.setStatus("REJECTED");
        
        // Save the updated loan application
        loanApplicationRepository.save(loanApplication);
        
        // In a real-world scenario, this would trigger a notification to the applicant
        // For this example, we'll just log the rejection
        log.info("Loan application rejected for applicant: {}", loanApplication.getApplicantName());
        log.info("Rejection reason: {}", rejectionReason);
    }
}
