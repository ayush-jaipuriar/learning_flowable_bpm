package com.example.loanapplication.delegate;

import com.example.loanapplication.service.LoanApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartBorderlineReviewDelegate implements JavaDelegate {

    @Autowired
    private ApplicationContext applicationContext;
    
    // Lazy getter for LoanApplicationService
    private LoanApplicationService getLoanApplicationService() {
        return applicationContext.getBean(LoanApplicationService.class);
    }

    @Override
    public void execute(DelegateExecution execution) {
        Long loanApplicationId = (Long) execution.getVariable("loanApplicationId");
        log.info("Starting borderline review case for loan application ID: {}", loanApplicationId);
        
        // Use the existing service method to start the CMMN case
        getLoanApplicationService().handleBorderlineApplication(loanApplicationId);
        
        // Set a variable to track that a case has been started
        execution.setVariable("borderlineCaseStarted", true);
    }
} 