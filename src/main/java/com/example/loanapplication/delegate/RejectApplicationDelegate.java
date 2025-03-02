package com.example.loanapplication.delegate;

import com.example.loanapplication.model.LoanApplication;
import com.example.loanapplication.repository.LoanApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Delegate to handle loan application rejection.
 */
@Component
@Slf4j
public class RejectApplicationDelegate implements JavaDelegate {

    @Autowired
    private ApplicationContext applicationContext;
    
    // Lazy getter for LoanApplicationRepository
    private LoanApplicationRepository getLoanApplicationRepository() {
        return applicationContext.getBean(LoanApplicationRepository.class);
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long loanApplicationId = (Long) execution.getVariable("loanApplicationId");
        String reason = (String) execution.getVariable("reason");
        
        log.info("Rejecting loan application with ID: {}, reason: {}", loanApplicationId, reason);
        
        // Update the loan application status in the database
        getLoanApplicationRepository().findById(loanApplicationId).ifPresent(application -> {
            application.setStatus("REJECTED");
            getLoanApplicationRepository().save(application);
            log.info("Updated loan application status to REJECTED for ID: {}", loanApplicationId);
        });
        
        // Here you would typically send a notification to the applicant
        log.info("Notification would be sent to applicant about rejection");
    }
} 